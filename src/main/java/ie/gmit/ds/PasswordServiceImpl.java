package ie.gmit.ds;

import com.google.protobuf.BoolValue;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;

import java.util.logging.Logger;

public class PasswordServiceImpl extends PasswordServiceGrpc.PasswordServiceImplBase{
    private static final Logger logger =
            Logger.getLogger(PasswordServiceImpl.class.getName());

    public PasswordServiceImpl() {
    }

    @Override
    public void hash(HashRequest request, StreamObserver<HashResponse> responseObserver) {
        //Variables
        char[] password = request.getPassword().toCharArray();
        byte[] salt = Passwords.getNextSalt();
        // Stores hashed password made from password and salt:
        byte[] hashedPassword = Passwords.hash(password, salt);

        //Creating a response
        HashResponse response = HashResponse.newBuilder().setUserId(request.getUserId())
                .setHashedPassword(ByteString.copyFrom(hashedPassword))
                .setSalt(ByteString.copyFrom(salt))
                .build();
        //Send the response back to client
        responseObserver.onNext(response);
        // Completing the request
        responseObserver.onCompleted();
    }

    @Override
    public void validate(ValidateRequest request, StreamObserver<BoolValue> responseObserver) {

    }
}