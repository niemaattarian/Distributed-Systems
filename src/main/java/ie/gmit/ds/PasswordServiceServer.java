package ie.gmit.ds;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import java.io.IOException;
import java.util.logging.Logger;

public class PasswordServiceServer {
    private Server grpcServer;
    private static final Logger logger = Logger.getLogger(PasswordServiceServer.class.getName());
    private static final int PORT = 50551;

    /** Starting the Server */
    private void start() throws IOException {
        grpcServer = ServerBuilder.forPort(PORT)
                .addService(new PasswordServiceImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + PORT);
    }// Start

    /** Stopping the Server */
    private void stop() {
        if (grpcServer != null) {
            grpcServer.shutdown();
        }
    } // Stop

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (grpcServer != null) {
            grpcServer.awaitTermination();
        }
    } // blockUntilShutdown

    /**
     * Main method runs the server.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final PasswordServiceServer inventoryServer = new PasswordServiceServer();
        inventoryServer.start();
        inventoryServer.blockUntilShutdown();
    }
} // PasswordServiceServer