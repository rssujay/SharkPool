package com.example.sharkpool_orbital_2019;

import android.util.Pair;

import java.util.ArrayList;

public class AppUser{
    private String displayName;
    private String emailAddress;
    private int credits;
    private ArrayList<String> currentRequests;
    private ArrayList<String> transactionLog;
    private ArrayList<Pair<String,String>> lendingList;
    private boolean tocAgreed;
    private boolean initLend;

    public AppUser(){}

    public void initialize(String displayName, String emailAddress, int credits, Boolean tocAgreed){
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.credits = credits;
        this.tocAgreed = tocAgreed;
        this.initLend = false;
        this.currentRequests = new ArrayList<>();
        this.transactionLog = new ArrayList<>();
        this.lendingList = new ArrayList<>();
    }

    public String getDisplayName(){
        return this.displayName;
    }

    public ArrayList<Pair<String, String>> getLendingList() {
        return lendingList;
    }

    public ArrayList<String> getCurrentRequests() {
        return currentRequests;
    }

    public ArrayList<String> getTransactionLog() {
        return transactionLog;
    }

    public boolean isInitLend() {
        return initLend;
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

    public boolean updateCredits(int extraCredits){ //+ve for lend, -ve for borrow
        if (this.credits + extraCredits < 0){
            return false; //indicates unsuccessful
        }
        this.credits += extraCredits;
        return true;
    }
}
