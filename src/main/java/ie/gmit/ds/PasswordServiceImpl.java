package ie.gmit.ds;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class PasswordServiceImpl extends PasswordServiceGrpc.PasswordServiceImplBase {
    private static final Logger logger =
            Logger.getLogger(PasswordServiceImpl.class.getName());

    public PasswordServiceImpl() {
    } // PasswordServiceImpl

    /**
    * Hash method requires 'userId' and 'password'.
    *
    * The HashRequest takes in the 'userId' and 'password' from the client in turn
    * giving a HashResponse of the 'userId', 'hashedPassword', and 'salt' that is
    * implemented from Passwords class.
    *
    * A hashed password is then made from the password with the salt added to it.
    *
    * */
    @Override
    public void hash(HashRequest request, StreamObserver<HashResponse> responseObserver) {
        // Variables
        char[] password = request.getPassword().toCharArray();
        byte[] salt = Passwords.getNextSalt();
        // Stores hashed password made from password and salt:
        byte[] hashedPassword = Passwords.hash(password, salt);

        // Creating a response
        HashResponse response = HashResponse.newBuilder().setUserId(request.getUserId())
                .setHashedPassword(ByteString.copyFrom(hashedPassword))
                .setSalt(ByteString.copyFrom(salt))
                .build();
        // Send the response back to the client
        responseObserver.onNext(response);
        // Completing the request
        responseObserver.onCompleted();
        System.out.println(request);
    } // Hash

    /**
     * Validate method requires 'password', 'salt' and 'hashedPassword'.
     *
     * The ValidateRequest takes in the password, salt and hashedPassword (which contains a password and a salt)
     * and gives back a value of true or false which determines whether the password is validated.
     *
     * */
    @Override
    public void validate(ValidateRequest request, StreamObserver<BoolValue> responseObserver) {
        BoolValue response = BoolValue.of(Passwords.isExpectedPassword(
                request.getPassword().toCharArray(),
                request.getSalt().toByteArray(),
                request.getHashedPassword().toByteArray())
        );
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    } // Validate
} // PasswordServiceImpl