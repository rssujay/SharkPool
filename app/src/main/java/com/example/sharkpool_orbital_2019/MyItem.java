package com.example.sharkpool_orbital_2019;

import java.util.UUID;

// This object is to populate the lending list of the current user
public class MyItem {
    private String itemName;
    private String itemType;
    private String uuid;

    public MyItem(){
    }

    public void initialize(String Name, String Type){
        this.itemName = Name;
        this.itemType = Type;
        this.uuid = java.util.UUID.randomUUID().toString();
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
}
