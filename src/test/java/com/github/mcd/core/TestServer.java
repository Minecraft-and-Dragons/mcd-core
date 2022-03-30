package com.github.mcd.core;

import net.minestom.server.MinecraftServer;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.optifine.OptifineSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(TestServer.class);

    public static void main(String[] args) {
        MinecraftServer server = MinecraftServer.init();
        OptifineSupport.enable();
        MojangAuth.init();

        String ip = System.getenv("minestom.address");
        int port = Integer.parseInt(System.getenv("minestom.port"));
        LOGGER.info("Creating server with IP {}:{}", ip, port);
        server.start(ip, port);
    }
}
