package com.example.navpe.Models;

public class GetSuggestion {
    String user_name, image;
    public GetSuggestion(String user_name, String image) {
        this.user_name = user_name;
        this.image = image;
    }
    public GetSuggestion(){}

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
