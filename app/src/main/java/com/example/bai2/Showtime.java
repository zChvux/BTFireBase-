package com.example.bai2;

public class Showtime {
    public String id;
    public String movieId;
    public String theaterId;
    public String time; // e.g., "2023-10-27 19:30"
    public double price;

    public Showtime() {}

    public Showtime(String id, String movieId, String theaterId, String time, double price) {
        this.id = id;
        this.movieId = movieId;
        this.theaterId = theaterId;
        this.time = time;
        this.price = price;
    }
}