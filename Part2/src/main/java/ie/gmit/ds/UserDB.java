package ie.gmit.ds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserDB {

    public static HashMap<Integer, User> users = new HashMap<>();
    static{
        users.put(1, new User(1, "User", "Email", "Password"));
        users.put(2, new User(2, "Niema", "niema@gmail.com", "Wow123"));
        users.put(3, new User(3, "Hello", "hello@google.com", "hi321"));
        users.put(4, new User(4, "AnyUser", "user@email.com", "userpassword"));
    }

    public static List<User> getUser(){
        return new ArrayList<User>(users.values());
    }

    public static User getUser(Integer id){
        return users.get(id);
    }

    public static User getUserName(String userName){
        // Cycles through the user id values
        for(User u : users.values()){
            if(u.getUserName().equals(userName)){
                return u;
            }
        }
        return null;
    }

    public static void updateUser(Integer id, User user){
        users.put(id, user);
    }

    public static void removeUser(Integer id){
        users.remove(id);
    }

    public static void createUser(User user) {
        users.put(user.getId(), user);
    }
}