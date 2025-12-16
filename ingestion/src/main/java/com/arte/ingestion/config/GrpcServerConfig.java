package com.arte.ingestion.config;

import com.arte.ingestion.grpc.IngestionGrpcServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class GrpcServerConfig {

    @Value("${grpc.server.port:50052}")
    private int grpcPort;

    private Server server;
    private final IngestionGrpcServiceImpl ingestionGrpcService;

    public GrpcServerConfig(IngestionGrpcServiceImpl ingestionGrpcService) {
        this.ingestionGrpcService = ingestionGrpcService;
    }

    @PostConstruct
    public void startGrpcServer() {
        try {
            server = ServerBuilder.forPort(grpcPort)
                    .addService(ingestionGrpcService)
                    .build()
                    .start();
            
            log.info("gRPC server started on port: {}", grpcPort);
            
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down gRPC server...");
                if (server != null) {
                    server.shutdown();
                }
            }));
        } catch (IOException e) {
            log.error("Failed to start gRPC server on port {}", grpcPort, e);
            throw new RuntimeException("Failed to start gRPC server", e);
        }
    }

    @PreDestroy
    public void stopGrpcServer() {
        if (server != null) {
            log.info("Stopping gRPC server...");
            server.shutdown();
        }
    }
}
