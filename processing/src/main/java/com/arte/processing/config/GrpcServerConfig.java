package com.arte.processing.config;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Configuration
@Slf4j
public class GrpcServerConfig {
    @Value("${grpc.server.port:50052}")
    private int grpcPort;

    // will add the service

    private Server server;

    @PostConstruct
    public void startGrpcServer() {
        try {
            server = ServerBuilder.forPort(grpcPort)
                    .build()
                    .start();

            log.info("gRPC server started on port: {}", grpcPort);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down the gRPC server....");
                if (server != null) {
                    server.shutdown();
                }
            }));
        } catch (IOException e) {
            log.error("Failed to start gRPC server on port {}", grpcPort, e);
            throw new RuntimeException("Failed to start gRPC server", e);
        }
    }
}
