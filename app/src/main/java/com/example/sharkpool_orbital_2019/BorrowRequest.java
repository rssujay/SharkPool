package com.example.sharkpool_orbital_2019;

public class BorrowRequest {
    //Borrower properties
    private String borrowerUID;
    private String itemName;
    private String itemType;
    private int borrowerCredits;
    private String comments;
    private boolean recommendations;
    private boolean borrowerLock;

    //Lender properties
    private String lenderUID;
    private int lenderCredit;
    private boolean lenderLock;

    //Final, joint attributes
    private int creditValue;


    private String status;
    /*
    Open: open request, visible for all
    OnHold: lender has indicated interest, request made invisible to public
    Confirmed: transaction is confirmed by both parties, credits are exchanged
    Complete: request is complete (item is returned), added to AppUser.history
     */

    public BorrowRequest(){} //no-argument constructor for firestore

    public void startBorrowRequest(String borrowerUID, String itemName, String itemType, int borrowerCredits, String comments, boolean recommendations){
        this.borrowerUID = borrowerUID;
        this.itemName = itemName;
        this.itemType = itemType;
        this.borrowerCredits = borrowerCredits;
        this.comments = comments;
        this.recommendations = recommendations;
        this.lenderUID = "None";
        this.status = "Open";
        this.borrowerLock = true;
        this.lenderLock = false;
    }

    public String getBorrowerUID() {
        return borrowerUID;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemType() {
        return itemType;
    }

    public int getBorrowerCredits() {
        return borrowerCredits;
    }

    public String getComments() {
        return comments;
    }

    public boolean Recommendations(){
        return recommendations;
    }

    public boolean isBorrowerLock() {
        return borrowerLock;
    }

    public String getLenderUID() {
        return lenderUID;
    }

    public int getLenderCredit() {
        return lenderCredit;
    }

    public boolean isLenderLock() {
        return lenderLock;
    }

    public int getCreditValue() {
        return creditValue;
    }

    public String getStatus() {
        return status;
    }
}
