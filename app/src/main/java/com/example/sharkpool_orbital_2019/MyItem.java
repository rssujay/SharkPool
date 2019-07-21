package com.example.sharkpool_orbital_2019;

import com.google.firebase.auth.FirebaseAuth;

// This object is to populate the lending list of the current user
public class MyItem {
    private String itemName;
    private String itemType;
    private String uuid; //User's unique auth ID
    private String lenditemID; //Unique ID for this lendList item

    public MyItem(){
    }

    public void initialize(String Name, String Type){
        this.itemName = Name;
        this.itemType = Type;
        this.lenditemID = java.util.UUID.randomUUID().toString();
        this.uuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public String getItemType() {
        return itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public String getLenditemID() {
        return lenditemID;
    }

    public String getUUID() {
        return uuid;
    }
}
