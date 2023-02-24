package com.javanet.conf;

import java.util.List;

/**
 * serverinfo
 */
public class ServerInfo {
    private String clientName;
    private List<ServerWorker> serverWorkers;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<ServerWorker> getServerWorkers() {
        return serverWorkers;
    }

    public void setServerWorkers(List<ServerWorker> serverWorkers) {
        this.serverWorkers = serverWorkers;
    }
}
