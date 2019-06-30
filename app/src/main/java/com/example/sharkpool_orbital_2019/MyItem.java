package com.example.sharkpool_orbital_2019;

import java.util.UUID;

// This object is to populate the lending list of the current user
public class MyItem {
    private String itemName;
    private String itemType;
    private String uuid;
    private String token;

    public MyItem(){
    }

    public void initialize(String Name, String Type, String token){
        this.itemName = Name;
        this.itemType = Type;
        this.uuid = java.util.UUID.randomUUID().toString();
        this.token = token;
    }

    public String getItemType() {
        return itemType;
    }

    public String getItemName() {
        return itemName;
    }

    public String getUUID() {
        return uuid;
    }

    public String getToken() {
        return token;
    }
}
