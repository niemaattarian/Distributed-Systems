package ie.gmit.ds;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PasswordServiceClient {
    private static final Logger logger =
            Logger.getLogger(PasswordServiceClient.class.getName());
    private final ManagedChannel channel;
    private final PasswordServiceGrpc.PasswordServiceBlockingStub syncPasswordService;

    public PasswordServiceClient(String host, int port) {
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext()
                .build();
        syncPasswordService = PasswordServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void hash(int userId, String password){
        logger.info("Hash request detail:\nUser ID: " + userId + "\nPassword: " + password);
        //Create a request to send to PasswordServiceImpl hash method
        HashRequest request =  HashRequest.newBuilder().setUserId(userId)
                .setPassword(password).build();

        //Create a response to read response from server and log it
        HashResponse responce;

        try{
            responce = syncPasswordService.hash(request);
        }catch (StatusRuntimeException ex){
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
            return;
        }

        logger.info("Response from server: " + responce.getUserId() + responce.getHashedPassword()
                + responce.getSalt());
    }



    public static void main(String[] args) throws InterruptedException {
        PasswordServiceClient client = new PasswordServiceClient("localhost", 50551);

        try {
            client.hash(1234, "niema");

        }finally {
            // Don't stop process, keep alive to receive async response
            Thread.currentThread().join();
        }

    }
}
