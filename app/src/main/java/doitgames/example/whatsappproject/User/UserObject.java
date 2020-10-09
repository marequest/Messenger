package doitgames.example.whatsappproject.User;

import java.io.Serializable;

public class UserObject implements Serializable {

    private String name, phone, uid, notificationKey;
    private Boolean selected = false;

    public UserObject(String uid){
        this.uid = uid;
    }

    public UserObject(String uid, String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.uid = uid;
    }

    public UserObject(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.uid = "";
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getNotificationKey() {
        return notificationKey;
    }

    public void setNotificationKey(String notificationKey) {
        this.notificationKey = notificationKey;
    }

    public Boolean getSelected() {
        return selected;
    }

    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
}
