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

    // Adapted from https://www.baeldung.com/java-byte-arrays-hex-strings
    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    private String encodeHexString(byte[] byteArray) {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            hexStringBuffer.append(byteToHex(byteArray[i]));
        }
        return hexStringBuffer.toString();
    }

    public String[] hash(int userId, String password){
        logger.info("Hash Request Detail \nUser ID: " + userId + "\nPassword: " + password);
        //Create a request to send to PasswordServiceImpl hash method
        HashRequest request =  HashRequest.newBuilder().setUserId(userId)
                .setPassword(password).build();
        try{
            // Passing hashed password as a byte array and encoding it as a hex string
            HashResponse hashResponse =  syncPasswordService.hash(request);
            String hashPassword = encodeHexString(hashResponse.getHashedPassword().toByteArray());
            String salt = encodeHexString(hashResponse.getSalt().toByteArray());
            return new String[]{hashPassword, salt};

        }catch (StatusRuntimeException ex){
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
        }
        return null;
    }

    private byte hexToByte(String hexString) {
        int firstDigit = toDigit(hexString.charAt(0));
        int secondDigit = toDigit(hexString.charAt(1));
        return (byte) ((firstDigit << 4) + secondDigit);
    }

    private int toDigit(char hexChar) {
        int digit = Character.digit(hexChar, 16);
        if(digit == -1) {
            throw new IllegalArgumentException(
                    "Invalid Hexadecimal Character: "+ hexChar);
        }
        return digit;
    }

    public byte[] decodeHexString(String hexString) {
        if (hexString.length() % 2 == 1) {
            throw new IllegalArgumentException(
                    "Invalid hexadecimal String supplied.");
        }

        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = hexToByte(hexString.substring(i, i + 2));
        }
        return bytes;
    }

    public boolean validate(String password, byte[] hashedPassword, byte[] salt) {
        ValidateRequest request = ValidateRequest.newBuilder()
                .setPassword(password)
                .setHashedPassword(ByteString.copyFrom(hashedPassword))
                .setSalt(ByteString.copyFrom(salt)).build();

        BoolValue response;
        try{
            response  = syncPasswordService.validate(request);
            logger.info("Response from server: \n" + response.getValue());
            return response.getValue();
        } catch (StatusRuntimeException ex){
            logger.log(Level.WARNING, "RPC failed: {0}", ex.getStatus());
            return false;
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

        }finally {
            // Don't stop process, keep alive to receive async response
            Thread.currentThread().join();
        } // try/finally
    } // main
} // PasswordServiceClient