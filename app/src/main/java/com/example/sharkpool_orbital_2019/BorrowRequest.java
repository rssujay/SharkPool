package com.example.sharkpool_orbital_2019;

public class BorrowRequest {
    private String borrowerUID;
    private String lenderUID;
    private int creditValue;
    private String itemType;
    private String status;
    /*
    Open: open request, visible for all
    OnHold: lender has indicated interest, request made invisible to public
    Confirmed: transaction is confirmed by both parties, credits are exchanged
    Complete: request is complete (item is returned), added to AppUser.history
     */

    public BorrowRequest(){} //no-argument constructor for firestore

    public void initialize(String borrowerUID, String itemType){
        this.borrowerUID = borrowerUID;
        this.itemType = itemType;
        this.lenderUID = "None";
        this.status = "Open";
    }

    public String getBorrowerUID() {
        return this.borrowerUID;
    }

    public String getLenderUID() {
        return this.lenderUID;
    }

    public void setLenderUID(String lenderUID) {
        this.lenderUID = lenderUID;
    }

    public String getItemType() {
        return this.itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreditValue() {
        return this.creditValue;
    }

    public void setCreditValue() { //hard-coded setting of credit values
        String type = this.getItemType();
        int creditValue = 10;
        switch (type){
            case "Pencil":
                creditValue = 10;
                break;
            case "Eraser":
                creditValue = 10;
                break;
            case "Pen":
                creditValue = 20;
                break;
            case "Ruler":
                creditValue = 20;
                break;
            case "Scientific Calculator":
                creditValue = 100;
                break;
            case "GC":
                creditValue = 500;
        }
        this.creditValue = creditValue;
    }
}
