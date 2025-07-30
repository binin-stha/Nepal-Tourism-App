package com.example.nepaltourism.models;

import java.time.LocalDateTime;
import java.util.Objects;

public class EmergencyReport {
    private String id; // Unique report ID
    private String reporterId; // ID of the User (Tourist or Guide) reporting
    private String reporterType; // "Tourist" or "Guide"
    private String location; // Current location of the reporter
    private String emergencyType; // Type of emergency (e.g., "Medical", "Lost", "Weather")
    private String priority; // Priority level (e.g., "Low", "Medium", "High", "Critical")
    private String description; // Detailed description of the situation
    private String contactNumber; // Contact number for the reporter
    private LocalDateTime timestamp; // When the report was submitted
    private String status; // Status of the report (e.g., "Reported", "Acknowledged", "Resolved")

    public EmergencyReport() {
        // Default constructor
        this.timestamp = LocalDateTime.now(); // Set timestamp on creation
        this.status = "Reported"; // Default status
    }

    public EmergencyReport(String id, String reporterId, String reporterType, String location, String emergencyType,
                           String priority, String description, String contactNumber) {
        this.id = id;
        this.reporterId = reporterId;
        this.reporterType = reporterType;
        this.location = location;
        this.emergencyType = emergencyType;
        this.priority = priority;
        this.description = description;
        this.contactNumber = contactNumber;
        this.timestamp = LocalDateTime.now(); // Set timestamp on creation
        this.status = "Reported"; // Default status
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterType() {
        return reporterType;
    }

    public void setReporterType(String reporterType) {
        this.reporterType = reporterType;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEmergencyType() {
        return emergencyType;
    }

    public void setEmergencyType(String emergencyType) {
        this.emergencyType = emergencyType;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmergencyReport that = (EmergencyReport) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "EmergencyReport{" +
                "id='" + id + '\'' +
                ", reporterId='" + reporterId + '\'' +
                ", reporterType='" + reporterType + '\'' +
                ", location='" + location + '\'' +
                ", emergencyType='" + emergencyType + '\'' +
                ", priority='" + priority + '\'' +
                ", description='" + description + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", timestamp=" + timestamp +
                ", status='" + status + '\'' +
                '}';
    }
}