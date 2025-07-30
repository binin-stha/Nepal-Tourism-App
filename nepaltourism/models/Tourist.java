package com.example.nepaltourism.models;

public class Tourist extends User {
    private String nationality; // Could be added if needed
    private String emergencyContact; // For safety feature

    public Tourist() {
        super();
    }

    public Tourist(String id, String name, String email, String phone, String password, String emergencyContact) {
        super(id, name, email, phone, password);
        this.emergencyContact = emergencyContact;
    }

    // Getters and Setters
    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    @Override
    public String getUserType() {
        return "Tourist";
    }

    @Override
    public String toString() {
        return "Tourist{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", emergencyContact='" + emergencyContact + '\'' +
                '}';
    }
}