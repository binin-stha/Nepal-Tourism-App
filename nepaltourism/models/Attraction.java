package com.example.nepaltourism.models;

public class Attraction {
    private String id;
    private String name;
    private String region; // e.g., "Kathmandu", "Pokhara", "Everest Region"
    private String category; // e.g., "Trek", "Heritage", "Adventure"
    private String difficulty; // e.g., "Easy", "Moderate", "Hard", "Expert"
    private int durationDays; // Estimated duration in days
    private double priceUSD; // Price in USD
    private String description;
    private boolean active; // Whether the attraction is currently bookable
    private double rating; // Average rating
    private int altitudeMeters; // For safety alerts (e.g., > 3000m)

    public Attraction() {
        // Default constructor
    }

    public Attraction(String id, String name, String region, String category, String difficulty,
                      int durationDays, double priceUSD, String description, boolean active, int altitudeMeters) {
        this.id = id;
        this.name = name;
        this.region = region;
        this.category = category;
        this.difficulty = difficulty;
        this.durationDays = durationDays;
        this.priceUSD = priceUSD;
        this.description = description;
        this.active = active;
        this.rating = 0.0; // Default rating
        this.altitudeMeters = altitudeMeters;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public int getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(int durationDays) {
        this.durationDays = durationDays;
    }

    public double getPriceUSD() {
        return priceUSD;
    }

    public void setPriceUSD(double priceUSD) {
        this.priceUSD = priceUSD;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getAltitudeMeters() {
        return altitudeMeters;
    }

    public void setAltitudeMeters(int altitudeMeters) {
        this.altitudeMeters = altitudeMeters;
    }

    @Override
    public String toString() {
        return "Attraction{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", region='" + region + '\'' +
                ", category='" + category + '\'' +
                ", difficulty='" + difficulty + '\'' +
                ", durationDays=" + durationDays +
                ", priceUSD=" + priceUSD +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", rating=" + rating +
                ", altitudeMeters=" + altitudeMeters +
                '}';
    }
}