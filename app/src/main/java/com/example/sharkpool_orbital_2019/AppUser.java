package com.example.sharkpool_orbital_2019;

import android.util.Pair;

import java.util.ArrayList;

public class AppUser{
    private String displayName;
    private String emailAddress;
    private int credits;
    private ArrayList<String> requests;
    private ArrayList<Pair<String,String>> lendingList;
    private boolean tocAgreed;
    private boolean initLend;

    public AppUser(){}

    //Minimal version
    public void initialize(String displayName, String emailAddress, int credits){
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.credits = credits;
    }

    //For creation of new user only
    public void createUser(String displayName, String emailAddress){
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.credits = 100;
        this.tocAgreed = false;
    }

    // This is used in main menu, to verify TOC agreement
    public void initialize(String displayName, String emailAddress, int credits, Boolean tocAgreed){
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.credits = credits;
        this.tocAgreed = tocAgreed;
    }

    public String getDisplayName(){
        return this.displayName;
    }

    public ArrayList<Pair<String, String>> getLendingList() {
        return lendingList;
    }

    public boolean isTocAgreed() {
        return tocAgreed;
    }

    public void setDisplayName(String newName){
        this.displayName = newName;
    }

    public String getEmailAddress(){
        return this.emailAddress;
    }

    public int getCredits() {
        return this.credits;
    }


    public boolean testUpdateCredits(int extraCredits){ //+ve for lend, -ve for borrow
        if (this.credits + extraCredits < 0){
            return false; //indicates unsuccessful
        }
        return true;
    }
}
