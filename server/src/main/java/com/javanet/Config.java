package com.javanet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.javanet.conf.ServerInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * config class
 */
public class Config {
    private static Logger log = LoggerFactory.getLogger(Config.class);
    public static int bindPort;
    public static String token;
    //time out
    public static int timeOut = 5;

    private static List<ServerInfo> serverInfos = new ArrayList<>();

    public static void init(String configPath) throws IOException {
        log.info("read config");
        ObjectMapper mapper = new ObjectMapper();
        InputStream input = getResource(configPath);
        JsonNode config = mapper.readTree(input);
        bindPort = config.at("/server/bindHost").intValue();
        token = config.at("/server/token").textValue();

        for (JsonNode node : config.get("clients")) {
            ServerInfo serverInfo = mapper.treeToValue(node, ServerInfo.class);
            serverInfos.add(serverInfo);
        }
    }

    private static InputStream getResource(String resourceName) {
        if (resourceName == null) {
            return null;
        }
        InputStream input = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);
        if (input != null) {
            return input;
        }
        try {
            return new FileInputStream(resourceName);
        } catch (FileNotFoundException e) {
            return null;
        }
    }
}
