package com.example.nepaltourism.models;

import java.time.LocalDate;
import java.util.Objects;

public class Booking {
    private String id; // Unique booking ID
    private String touristId; // ID of the Tourist
    private String guideId; // ID of the assigned Guide (can be null initially)
    private String attractionId; // ID of the Attraction
    private LocalDate tourDate; // Scheduled date of the tour
    private int numberOfPeople;
    private String specialRequests; // Optional requests
    private String status; // e.g., "Pending", "Confirmed", "Completed", "Cancelled"
    private double totalPrice; // Final price after discounts
    private String discountApplied; // Description of discount (e.g., "Dashain 10%")

    public Booking() {
        // Default constructor
    }

    public Booking(String id, String touristId, String guideId, String attractionId, LocalDate tourDate,
                   int numberOfPeople, String specialRequests, String status, double totalPrice, String discountApplied) {
        this.id = id;
        this.touristId = touristId;
        this.guideId = guideId;
        this.attractionId = attractionId;
        this.tourDate = tourDate;
        this.numberOfPeople = numberOfPeople;
        this.specialRequests = specialRequests;
        this.status = status;
        this.totalPrice = totalPrice;
        this.discountApplied = discountApplied;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTouristId() {
        return touristId;
    }

    public void setTouristId(String touristId) {
        this.touristId = touristId;
    }

    public String getGuideId() {
        return guideId;
    }

    public void setGuideId(String guideId) {
        this.guideId = guideId;
    }

    public String getAttractionId() {
        return attractionId;
    }

    public void setAttractionId(String attractionId) {
        this.attractionId = attractionId;
    }

    public LocalDate getTourDate() {
        return tourDate;
    }

    public void setTourDate(LocalDate tourDate) {
        this.tourDate = tourDate;
    }

    public int getNumberOfPeople() {
        return numberOfPeople;
    }

    public void setNumberOfPeople(int numberOfPeople) {
        this.numberOfPeople = numberOfPeople;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getDiscountApplied() {
        return discountApplied;
    }

    public void setDiscountApplied(String discountApplied) {
        this.discountApplied = discountApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Booking booking = (Booking) o;
        return Objects.equals(id, booking.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id='" + id + '\'' +
                ", touristId='" + touristId + '\'' +
                ", guideId='" + guideId + '\'' +
                ", attractionId='" + attractionId + '\'' +
                ", tourDate=" + tourDate +
                ", numberOfPeople=" + numberOfPeople +
                ", specialRequests='" + specialRequests + '\'' +
                ", status='" + status + '\'' +
                ", totalPrice=" + totalPrice +
                ", discountApplied='" + discountApplied + '\'' +
                '}';
    }
}