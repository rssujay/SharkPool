package com.example.sharkpool_orbital_2019;

public class AppUser{
    private String displayName;
    private String emailAddress;
    private int credits;
    private boolean tocAgreed;
    private String notificationToken;
    private int foregroundNotifications;

    public AppUser(){}

    //Minimal version - used in BR Creation and Main Menu (default value before database retrieval)
    public void initialize(String displayName, String emailAddress, int credits){
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.credits = credits;
        this.foregroundNotifications = 0;
    }

    //For creation of new user only
    public void createUser(String displayName, String emailAddress){
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.credits = 100;
        this.tocAgreed = false;
        this.foregroundNotifications = 0;
    }

    // This is used in main menu for initialization upon successful database retrieval
    public void initialize(String displayName, String emailAddress, int credits, Boolean tocAgreed, int foregroundNotifications){
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.credits = credits;
        this.tocAgreed = tocAgreed;
        this.foregroundNotifications = foregroundNotifications;
    }

    // This is used in main menu
    public void setNotificationToken(String notificationToken){
        this.notificationToken = notificationToken;
    }

    public String getDisplayName(){
        return this.displayName;
    }

    public boolean isTocAgreed() {
        return tocAgreed;
    }

    public String getEmailAddress(){
        return this.emailAddress;
    }

    public int getCredits() {
        return this.credits;
    }

    public String getNotificationToken() {
        return notificationToken;
    }

    public int getForegroundNotifications() {
        return foregroundNotifications;
    }

    public void setForegroundNotifications(int foregroundNotifications) {
        this.foregroundNotifications = foregroundNotifications;
    }
}


