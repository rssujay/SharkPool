package com.example.sharkpool_orbital_2019;

public class AppUser{
    private String displayName;
    private String emailAddress;
    private int credits;
    //private ArrayList<int> transactionLog;

    public AppUser(){}

    public void initialize(String displayName, String emailAddress, int credits){
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.credits = credits;
    }

    public String getDisplayName(){
        return this.displayName;
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

    public boolean updateCredits(int extraCredits){
        if (this.credits + extraCredits < 0){
            return false; //indicates unsuccessful
        }
        this.credits += extraCredits;
        return true;
    }
}
