package com.example.nepaltourism.models;

public class Guide extends User {
    private String tourArea; // Region/Area the guide specializes in
    private int experience; // Years of experience (optional field)
    private String languages; // Comma-separated list (e.g., "English, Nepali")
    private double rating; // Average rating
    private boolean available; // Availability status for bookings

    public Guide() {
        super();
        this.available = true; // Default to available
    }

    public Guide(String id, String name, String email, String phone, String password,
                 String tourArea, int experience, String languages) {
        super(id, name, email, phone, password);
        this.tourArea = tourArea;
        this.experience = experience;
        this.languages = languages;
        this.rating = 0.0; // Default rating
        this.available = true; // Default to available
    }

    // Getters and Setters
    public String getTourArea() {
        return tourArea;
    }

    public void setTourArea(String tourArea) {
        this.tourArea = tourArea;
    }

    public int getExperience() {
        return experience;
    }

    public void setExperience(int experience) {
        this.experience = experience;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String getUserType() {
        return "Guide";
    }

    @Override
    public String toString() {
        return "Guide{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", tourArea='" + tourArea + '\'' +
                ", experience=" + experience +
                ", languages='" + languages + '\'' +
                ", rating=" + rating +
                ", available=" + available +
                '}';
    }
}