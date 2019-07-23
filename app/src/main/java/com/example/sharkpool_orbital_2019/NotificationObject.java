package com.example.sharkpool_orbital_2019;

import com.sendbird.android.shadow.com.google.gson.JsonElement;

public class NotificationObject {

    public String otherID;
    public String brID;
    public String notifTitle;
    public String notifBody;

    public NotificationObject() {} //no argument for Firestore

    //one function for Firestore notifications, one for SendBird notifications

    public void initialize(JsonElement payload){ //SendBird

        this.otherID = payload.getAsJsonObject().get("sender").getAsJsonObject().get("id").toString().substring(1,29);
        this.brID = "";

        String temp = payload.getAsJsonObject().get("sender").getAsJsonObject().get("name").toString();
        temp = temp.substring(1,temp.length()-1);
        this.notifTitle = "New message from " + temp;

        String temp2 = payload.getAsJsonObject().get("message").toString();
        this.notifBody = temp2.substring(1,temp2.length()-1);

    }

    public void initialize(){ //Firestore
        return;
    }
}
