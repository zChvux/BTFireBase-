package com.example.bai2;

import com.google.firebase.firestore.PropertyName;

public class Movie {
    public String id;
    public String title;
    public String genre;
    public String description;
    
    @PropertyName("imageUrl")
    public String imageUrl;
    
    public int duration;

    public Movie() {} // Bắt buộc cho Firebase

    public Movie(String id, String title, String genre, String description, String imageUrl, int duration) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.description = description;
        this.imageUrl = imageUrl;
        this.duration = duration;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() { return imageUrl; }

    @PropertyName("imageUrl")
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}