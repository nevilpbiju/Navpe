package com.example.navpe.Models;

public class GetMessage {

    public static final int SENT_MESSAGE = 0;
    public static final int RECEIVE_MESSAGE = 1;

    private int viewType;
    String sendMessage, timeStamp, uid;
    String receiveMessage;
    String phoneNo;
    public String getReceiveMessage() {
        return receiveMessage;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public void setReceiveMessage(String receiveMessage) {
        this.receiveMessage = receiveMessage;
    }
    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSendMessage() {
        return sendMessage;
    }

    public void setSendMessage(String sendMessage) {
        this.sendMessage = sendMessage;
    }

    public GetMessage(String sendMessage, String timeStamp, String uid, int viewType) {
        this.viewType = viewType;
        if(viewType == SENT_MESSAGE) {
            this.sendMessage = sendMessage;
        }else{
            this.receiveMessage = sendMessage;
        }
        this.timeStamp = timeStamp;
        this.uid = uid;
    }
    public GetMessage(String sendMessage, String timeStamp, String uid, int viewType,String phoneNo) {
        this.viewType = viewType;
        if(viewType == SENT_MESSAGE) {
            this.sendMessage = sendMessage;
        }else{
            this.receiveMessage = sendMessage;
        }
        this.timeStamp = timeStamp;
        this.uid = uid;
        this.phoneNo = phoneNo;
    }
    public GetMessage(String sendMessage, String timeStamp, String uid,String phoneNo) {
        this.sendMessage = sendMessage;
        this.timeStamp = timeStamp;
        this.uid = uid;
        this.phoneNo = phoneNo;
    }
}
