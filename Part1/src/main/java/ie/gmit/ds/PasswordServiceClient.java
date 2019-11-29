package ie.gmit.ds;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
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
    } // PasswordServiceClient

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } // shutdown

    public void hash(int userId, String password){
        logger.info("Hash Request Detail \nUser ID: " + userId + "\nPassword: " + password);
        //Create a request to send to PasswordServiceImpl hash method
        HashRequest request =  HashRequest.newBuilder().setUserId(userId)
                .setPassword(password).build();

        //Create a response to read the response from server and log it
        HashResponse response;
        try{
            response = syncPasswordService.hash(request);
        }catch (StatusRuntimeException ex){
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
            return;
        }
        logger.info("Response from server: " + response);
    }
    public void validate(String password, byte[] hashedPassword, byte[] salt) {
        ValidateRequest request = ValidateRequest.newBuilder()
                .setPassword(password)
                .setHashedPassword(ByteString.copyFrom(hashedPassword))
                .setSalt(ByteString.copyFrom(salt)).build();

        BoolValue response;
        try{
            response  = syncPasswordService.validate(request);
            logger.info("Response from server: \n" + response.getValue());
            System.out.println(request);
        } catch (StatusRuntimeException ex){
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
        } // try/catch
    } // validate

    /** Main method which runs the Client */
    public static void main(String[] args) throws InterruptedException {
        PasswordServiceClient client = new PasswordServiceClient("localhost", 50551);

        try {
            /** Testing the Hash method */
            client.hash(55, "niema");
            client.hash(12, "Hello");
            client.hash(10, "Liverpool");
            client.hash(176, "lol");

            /** Testing the Validate method
             *
             * validates requires a password, hashed passwords which is 'passwords' and 'salt' together and a salt
             *
             */
            byte[] salt = Passwords.getNextSalt();
            client.validate("niema", Passwords.hash("niema".toCharArray(), salt), salt);

        }finally {
            // Don't stop process, keep alive to receive async response
            Thread.currentThread().join();
        } // try/finally
    } // main
} // PasswordServiceClient
