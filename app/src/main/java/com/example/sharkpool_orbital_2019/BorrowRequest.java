package com.example.sharkpool_orbital_2019;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class BorrowRequest {
    //Borrower properties
    private String borrowerUID;
    private String borrowerName;
    private String itemName;
    private String itemType;
    private int borrowerCredits;
    private String comments;
    private boolean recommendations;
    private boolean borrowerLock;

    //Lender properties
    private String lenderUID;
    private String lenderName;
    private int lenderCredit;
    private boolean lenderLock;

    //Final, joint attributes
    private int creditValue;

    @ServerTimestamp
    public Date createdDate;

    public Date startDate;
    public Date returnDate;


    private String status;
    /*
    Open: open request, visible for all
    OnHold: lender has indicated interest, request made invisible to public
    Confirmed: transaction is confirmed by both parties, credits are exchanged
    Complete: request is complete (item is returned), added to AppUser.history
     */

    public BorrowRequest(){} //no-argument constructor for firestore

    public void startBorrowRequest(String borrowerUID, String borrowerName, String itemName, String itemType, int borrowerCredits, String comments, boolean recommendations){
        this.borrowerUID = borrowerUID;
        this.borrowerName = borrowerName;
        this.itemName = itemName;
        this.itemType = itemType;
        this.borrowerCredits = borrowerCredits;
        this.comments = comments;
        this.recommendations = recommendations;
        this.lenderUID = "";
        this.lenderName = "";
        this.status = "Open";
        this.borrowerLock = true;
        this.lenderLock = false;
        this.startDate = new Date(0);
        this.returnDate = new Date(0);
    }

    public String getBorrowerUID() {
        return borrowerUID;
    }

    public String getBorrowerName() {
        return borrowerName;
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

    public String getLenderName() {
        return lenderName;
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

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getReturnDate() {
        return returnDate;
    }

    public Date getStartDate() {
        return startDate;
    }
}


