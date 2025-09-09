package com.developer.attd.Model;

import com.google.firebase.Timestamp;

public class AttendanceModel {
    private String userId;
    private String date;
    private Timestamp checkIn;
    private Timestamp checkOut;
    private String status;
    private String checkoutComments;

    public AttendanceModel() {}

    public AttendanceModel(String userId, String date, Timestamp checkIn, Timestamp checkOut,
                           String status, String checkoutComments) {
        this.userId = userId;
        this.date = date;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.status = status;
        this.checkoutComments = checkoutComments;
    }

    // Getters
    public String getUserId() { return userId; }
    public String getDate() { return date; }
    public Timestamp getCheckIn() { return checkIn; }
    public Timestamp getCheckOut() { return checkOut; }
    public String getStatus() { return status; }
    public String getCheckoutComments() { return checkoutComments; }
}
