package com.example.bai2;

public class Ticket {
    public String id;
    public String userId;
    public String showtimeId;
    public String seatNumber;
    public String bookingTime;
    public double totalPrice;

    public Ticket() {}

    public Ticket(String id, String userId, String showtimeId, String seatNumber, String bookingTime, double totalPrice) {
        this.id = id;
        this.userId = userId;
        this.showtimeId = showtimeId;
        this.seatNumber = seatNumber;
        this.bookingTime = bookingTime;
        this.totalPrice = totalPrice;
    }
}