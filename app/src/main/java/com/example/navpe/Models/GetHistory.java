package com.example.navpe.Models;

public class GetHistory {
    String amount;
    String image;
    String name;
    String receiverPhone;
    String time;
    String transaction;//paid or received
    String typeOfTxn;

    public String getReceiverPhone() {
        return receiverPhone;
    }

    public void setReceiverPhone(String receiverPhone) {
        this.receiverPhone = receiverPhone;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
    public GetHistory() {
    }

    public GetHistory(String amount, String image, String name,String receiverPhone ,String time, String transaction, String typeOfTxn) {
        this.image = image;
        this.name = name;
        this.time = time;
        this.receiverPhone = receiverPhone;
        this.transaction = transaction;
        this.amount = amount;
        this.typeOfTxn = typeOfTxn;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getTypeOfTxn() {
        return typeOfTxn;
    }

    public void setTypeOfTxn(String typeOfTxn) {
        this.typeOfTxn = typeOfTxn;
    }

//    @Override
//    public boolean equals(Object other) {
//        if (!(other instanceof GetHistory)) {
//            return false;
//        }
//        GetHistory that = (GetHistory) other;
////        return this.getFood_name().equals(that.getFood_name()) && this.getPrice().equals(that.getPrice());
//    }

}
