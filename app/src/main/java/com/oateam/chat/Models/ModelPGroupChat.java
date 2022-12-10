package com.oateam.chat.Models;

public class ModelPGroupChat {
    String message,sender,time,type,date;

    public  ModelPGroupChat(){}

    public ModelPGroupChat(String message, String sender, String time, String type, String date) {
        this.message = message;
        this.sender = sender;
        this.time = time;
        this.type = type;
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
