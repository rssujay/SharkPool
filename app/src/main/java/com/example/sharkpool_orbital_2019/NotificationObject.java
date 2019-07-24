package com.example.sharkpool_orbital_2019;

import com.google.firebase.firestore.ServerTimestamp;
import com.sendbird.android.shadow.com.google.gson.JsonElement;

import java.util.Date;

public class NotificationObject {
    private String notificationUUID;
    private String otherID;
    private String brID;
    private String notifTitle;
    private String notifBody;

    @ServerTimestamp
    private Date receivedDate;

    public NotificationObject() {}

    // Initialization for SendBird notifications
    public void initialize(JsonElement payload){ //SendBird
        this.notificationUUID = java.util.UUID.randomUUID().toString();

        this.otherID = payload.getAsJsonObject().get("sender").getAsJsonObject().get("id").toString().substring(1,29);
        this.brID = "";

        String temp = payload.getAsJsonObject().get("sender").getAsJsonObject().get("name").toString();
        temp = temp.substring(1,temp.length()-1);
        this.notifTitle = "New message from " + temp;

        String temp2 = payload.getAsJsonObject().get("message").toString();
        this.notifBody = temp2.substring(1,temp2.length()-1);
    }

    // Initialization for FCM notifications
    public void initialize(){
        this.notificationUUID = java.util.UUID.randomUUID().toString();
        this.notifTitle = "tempTitle";
        this.notifBody ="tempBody";
        this.otherID = "123456";
        this.brID = "";
    }

    public String getNotifBody() {
        return notifBody;
    }

    public String getNotifTitle() {
        return notifTitle;
    }

    public String getBrID() {
        return brID;
    }

    public String getOtherID() {
        return otherID;
    }

    public String getNotificationUUID() {
        return notificationUUID;
    }

    public Date getReceivedDate() {
        return receivedDate;
    }
}



