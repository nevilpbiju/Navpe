package com.example.navpe.Models;

public class GetAccount {
    String bankName;
    String bankImage;
    String accountNo;
    String IFSC;

    public GetAccount(String bankName, String bankImage, String accountNo) {
        this.bankName = bankName;
        this.bankImage = bankImage;
        this.accountNo = accountNo;
    }

    public String getIFSC() {
        return IFSC;
    }

    public void setIFSC(String IFSC) {
        this.IFSC = IFSC;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getBankImage() {
        return bankImage;
    }

    public void setBankImage(String bankImage) {
        this.bankImage = bankImage;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public GetAccount(){
    }
}