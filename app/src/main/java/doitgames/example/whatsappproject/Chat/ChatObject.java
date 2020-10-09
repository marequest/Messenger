package doitgames.example.whatsappproject.Chat;

import java.io.Serializable;
import java.util.ArrayList;

import doitgames.example.whatsappproject.User.UserObject;

public class ChatObject implements Serializable {

    private String chatId;
    private String name;

    private ArrayList<UserObject> userObjectArrayList = new ArrayList<>();

    public ChatObject(String chatId) {
        this.chatId = chatId;
    }

    public ChatObject(String chatId, String name) {
        this.chatId = chatId;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getChatId() {
        return chatId;
    }

    public ArrayList<UserObject> getUserObjectArrayList() {
        return userObjectArrayList;
    }

    public void addUserToArrayList(UserObject userObject){
        userObjectArrayList.add(userObject);
    }

}
