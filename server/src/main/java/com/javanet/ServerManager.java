package com.javanet;

import com.javanet.cmd.listener.ErrorLogListener;
import com.javanet.cmd.message.DataConnectionCmd;
import com.javanet.conf.ServerWorker;
import io.netty.channel.Channel;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * server manager
 * Executor promise
 */
public class ServerManager {

    private static Logger log = LoggerFactory.getLogger(ServerManager.class);

    private static EventExecutor managerEventLoop = new DefaultEventExecutor();

    //this promise wait connect
    private static Map<DataConnectionCmd, Promise<Channel>> waitConnections = new HashMap<>();

    /*
     * After the client successfully logs in, traverse the mapping table, start all services, and bind to the lifecycle of the managerChannel
     * Enable and disable servers using a single eventLoop to ensure sequential execution
     * */
    public static Promise<Server> startServers(ServerWorker sw, Channel managerChannel) {
        Promise<Server> promise = managerEventLoop.newPromise();
        execute(() -> {
            if (managerChannel.isActive()) {
                Server server = new Server(sw, managerChannel);
                server.start(promise);
                managerChannel.closeFuture().addListener(future -> {
                    log.error("managerChannel breaked, close Server:" + sw.getServerPort(), future.cause());
                    execute(server::stop);
                });
            } else {
                promise.setFailure(new RuntimeException("The server has not been created yet, but it has been disconnected"));
            }
        });
        return promise;
    }

    /*
     * Received a client connection and added a link for the specified serverId service
     * */
    public static void addConnection(DataConnectionCmd cmd, Channel channel) {
        execute(() -> {
            Promise<Channel> promise = waitConnections.remove(cmd);
            if (promise == null) {
                log.info("ServerManager.addConnection cannt find Promise，maybe promise timeout cancel Channel:{}", channel);
                channel.close();
                return;
            }
            promise.setSuccess(channel);
        });
    }

    public static Promise<Channel> getConnection(Server server) {
        Promise<Channel> promise = managerEventLoop.newPromise();
        ServerWorker sw = server.getServerWorker();
        DataConnectionCmd message = new DataConnectionCmd(sw.getServerPort(), sw.getLocalHost(), sw.getLocalPort(), System.nanoTime());
        server.getManagerChannel().writeAndFlush(message)
                .addListener(ErrorLogListener.INSTANCE)
                .addListener(future -> {
                    if (future.isSuccess()) {
                        execute(() -> {
                            waitConnections.put(message, promise);
                            //Set scheduled tasks, and set promise to failure if timeout occurs
                            schedule(() -> {
                                waitConnections.remove(message);
                                //The result has not been set to a value yet, indicating that it has not been successful
                                if (promise.isCancellable()) {
                                    promise.setFailure(new TimeoutException("Promise Unable to obtain connection due to timeout"));
                                }
                            }, Config.timeOut, TimeUnit.SECONDS);
                        });
                    } else {
                        promise.setFailure(future.cause());
                    }
                });
        return promise;
    }

    private static void execute(Runnable runnable) {
        if (managerEventLoop.inEventLoop()) {
            runnable.run();
        } else {
            managerEventLoop.execute(runnable);
        }
    }

    private static ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return managerEventLoop.schedule(command, delay, unit);
    }


}