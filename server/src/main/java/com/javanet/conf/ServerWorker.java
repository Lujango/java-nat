package com.javanet.conf;


/**
 * worker
 */
public class ServerWorker {
    private Integer serverPort;
    private String localHost;
    private Integer localPort;

    public ServerWorker(Integer serverPort, String localHost, Integer localPort) {
        this.serverPort = serverPort;
        this.localHost = localHost;
        this.localPort = localPort;
    }

    public Integer getServerPort() {
        return serverPort;
    }

    public void setServerPort(Integer serverPort) {
        this.serverPort = serverPort;
    }

    public String getLocalHost() {
        return localHost;
    }

    public void setLocalHost(String localHost) {
        this.localHost = localHost;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }
}
