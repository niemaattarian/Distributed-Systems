package ie.gmit.ds;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class UserRESTController {

    private final Validator validator;

    public UserRESTController(Validator validator) {
        this.validator = validator;
    }

    @GET
    @Path("/users")
    public Response getUser() {
        return Response.ok(UserDB.getUser()).build();
    }

    @GET
    @Path("/users/{id}")
    public Response getUserById(@PathParam("id") Integer id) {
        User user = UserDB.getUser(id);
        if (user != null)
            return Response.ok(user).build();
        else
            return Response.status(Status.NOT_FOUND).build();
    }

    @POST
    @Path("/users")
    public Response createUser(User user) throws URISyntaxException {
        // validation
        //  Set<ConstraintViolation<User>> violations = validator.validate(user);
        User u = UserDB.getUser(user.getId());

        if (u == null) {
            UserDB.createUser(new User(user.getId(), user.getUserName(), user.getEmail(), user.getPassword()));
            return Response.status(Status.CREATED).build();
//            if (violations.size() > 0) {
//                ArrayList<String> validationMessages = new ArrayList<String>();
//                for (ConstraintViolation<User> violation : violations) {
//                    validationMessages.add(violation.getPropertyPath().toString() + ": " + violation.getMessage());
//                }
//                return Response.status(Status.BAD_REQUEST).entity(validationMessages).build();
//            } else {
//                UserDB.createUser(new User(user.getId(), user.getUserName(), user.getEmail(), user.getPassword()));
//                return Response.status(Status.CREATED).build();
//            }
        } else {
            return Response.status(Status.CONFLICT).build();
        }
    }

    @PUT
    @Path("/users/{id}")
    public Response updateUserById(@PathParam("id") Integer id, User user) {
        // validation
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        User u = UserDB.getUser(id);

        if (violations.size() > 0) {
            ArrayList<String> validationMessages = new ArrayList<String>();
            for (ConstraintViolation<User> violation : violations) {
                validationMessages.add(violation.getPropertyPath().toString() + ": " + violation.getMessage());
            }
            return Response.status(Status.BAD_REQUEST).entity(validationMessages).build();
        }

        if (u != null) {
            // Remove the
            UserDB.removeUser(id);

            if (user.getPassword() == null) {
                UserDB.updateUser(user.getId(), new User(user.getId(), user.getUserName(), user.getEmail(), user.getPassword(), u.getHashedPassword(), u.getSalt()));
            } else {
                UserDB.updateUser(user.getId(), new User(user.getId(), user.getUserName(), user.getEmail(), user.getPassword()));
            }
            return Response.ok(UserDB.getUser(user.getId())).build();
        } else
            return Response.status(Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/users/{id}")
    public Response removeUserById(@PathParam("id") Integer id) {
        User user = UserDB.getUser(id);
        if (user != null) {
            UserDB.removeUser(id);
            return Response.status(Status.NO_CONTENT).build();
        } else
            return Response.status(Status.NOT_FOUND).build();
    }

    @POST
    @Path("/login")
    public Response login(Login login) throws URISyntaxException {
        // Creating instance of the client
        PasswordServiceClient client = new PasswordServiceClient("localhost", 50551);

        // validation
        Set<ConstraintViolation<Login>> violations = validator.validate(login);
        // Want to check if the user name matches
        User u = UserDB.getUserName(login.getUserName());

        // Checking for errors
        if (violations.size() > 0) {
            ArrayList<String> validationMessages = new ArrayList<String>();
            // builds the list of errors
            for (ConstraintViolation<Login> violation : violations) {
                validationMessages.add(violation.getPropertyPath().toString() + ": " + violation.getMessage());
            }
            return Response.status(Status.BAD_REQUEST).entity(validationMessages).build();
        }
        // If there's no errors
        else {
            // If the user is not null, test to see if the password is valid
            if (u != null) {
                // is the password being passed at login, when its decoded from Hex to String, match the password assigned to the user
                boolean isValidUser = client.validate(login.getPassword(), client.decodeHexString(u.getHashedPassword()), client.decodeHexString(u.getSalt()));
                // If it is valid
                if (isValidUser) {
                    // return an ok response
                    return Response.status(Status.OK).build();
                }
            }
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }
}