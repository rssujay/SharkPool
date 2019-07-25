package com.example.sharkpool_orbital_2019;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;
import java.util.UUID;

public class BorrowRequest {
    //Item properties
    private String itemName;
    private String itemType;

    //Borrower properties
    private String borrowerUID;
    private String borrowerName;
    private String comments;
    private boolean recommendations;
    private int borrowCodeOne;
    private int borrowCodeTwo;

    //Lender properties
    private String lenderUID;
    private String lenderName;
    private int lendCodeOne;
    private int lendCodeTwo;

    //Final, joint attributes
    private String requestID;
    private int creditValue;
    private boolean dispute;
    private String status;

    @ServerTimestamp
    public Date createdDate;

    public Date startDate;
    public Date returnDate;

    /*
    Open: open request, visible for all
    Closed: lender has indicated interest, request made invisible to public
    Lent/Borrowed: transaction is confirmed by both parties, credits are exchanged
    Completed: request is complete (item is returned), added to AppUser.history

    Additional flag: disputed (Y/N)
     */

    public BorrowRequest(){} //no-argument constructor for firestore

    public void startBorrowRequest(String borrowerUID, String borrowerName, String itemName, String itemType, int creditValue,
                                   String comments, boolean recommendations){
        //Item
        this.itemName = itemName;
        this.itemType = itemType;

        //Borrower
        this.borrowerUID = borrowerUID;
        this.borrowerName = borrowerName;
        this.comments = comments;
        this.recommendations = recommendations;
        this.borrowCodeOne = (int) (Math.random()*8998 + 1001); //To generate a 4 digit number
        this.borrowCodeTwo = (int) (Math.random()*8998 + 1001);

        //Lender
        this.lenderUID = "";
        this.lenderName = "";
        this.lendCodeOne = (int) (Math.random()*8998 + 1001);
        this.lendCodeTwo = (int) (Math.random()*8998 + 1001);

        //Joint
        this.requestID = UUID.randomUUID().toString();
        this.creditValue = creditValue;
        this.dispute = false;
        this.status = "Open";

        this.startDate = new Date(0);
        this.returnDate = new Date(0);
    }

    public void populate(String requestID, String borrowerUID, String borrowerName, String lenderUID, String lenderName,
                         String status, String comments, Date createdDate, Date startDate, Date returnDate,
                         int borrowCodeOne, int borrowCodeTwo, int lendCodeOne, int lendCodeTwo,String itemName,
                         String itemType, boolean recommendations, int creditValue, boolean dispute
                         ){
        //Item
        this.itemName = itemName;
        this.itemType = itemType;

        //Borrower
        this.borrowerUID = borrowerUID;
        this.borrowerName = borrowerName;
        this.comments = comments;
        this.recommendations = recommendations;
        this.borrowCodeOne = borrowCodeOne;
        this.borrowCodeTwo = borrowCodeTwo;

        //Lender
        this.lenderUID = lenderUID;
        this.lenderName = lenderName;
        this.lendCodeOne = lendCodeOne;
        this.lendCodeTwo = lendCodeTwo;

        //Joint
        this.requestID = requestID;
        this.creditValue = creditValue;
        this.dispute = dispute;
        this.status = status;

        this.createdDate = createdDate;
        this.startDate = startDate;
        this.returnDate = returnDate;
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

    public boolean isDispute() {
        return dispute;
    }

    public String getRequestID() {
        return requestID;
    }

    public int getBorrowCodeOne() {
        return borrowCodeOne;
    }

    public int getBorrowCodeTwo() {
        return borrowCodeTwo;
    }

    public int getLendCodeOne() {
        return lendCodeOne;
    }

    public int getLendCodeTwo() {
        return lendCodeTwo;
    }

    public String getComments() {
        return comments;
    }

    public boolean isRecommendations() {
        return recommendations;
    }

    public String getLenderUID() {
        return lenderUID;
    }

    public String getLenderName() {
        return lenderName;
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

    public void setLenderName(String lenderName) {
        this.lenderName = lenderName;
    }

    public void setLenderUID(String lenderUID) {
        this.lenderUID = lenderUID;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}


