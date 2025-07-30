package com.example.nepaltourism.controllers;

import com.example.nepaltourism.Main;
import com.example.nepaltourism.models.EmergencyReport;
import com.example.nepaltourism.models.User;
import com.example.nepaltourism.utils.LanguageManager;
import com.example.nepaltourism.utils.SafetyAlertManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller class for the emergency-dialog.fxml view.
 */
public class EmergencyDialogController {

    private static final Logger logger = Logger.getLogger(EmergencyDialogController.class.getName());

    @FXML
    private TextField locationField;
    @FXML
    private ComboBox<String> emergencyTypeCombo;
    @FXML
    private ComboBox<String> priorityCombo;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField contactField;
    @FXML
    private Button cancelButton;
    @FXML
    private Button submitButton;

    private Main mainApp;
    private User reporter; // Can be Tourist or Guide

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Populate combo boxes
        emergencyTypeCombo.getItems().addAll("Medical Emergency", "Lost/Navigation", "Weather/Environmental", "Accident", "Security Threat", "Other");
        emergencyTypeCombo.setPromptText("Select Emergency Type");

        priorityCombo.getItems().addAll("Low", "Medium", "High", "Critical");
        priorityCombo.setPromptText("Select Priority");

        // Set default contact field value if user has one
        // This will be set when the reporter is assigned

        // Set up button actions
        cancelButton.setOnAction(event -> handleClose());
        submitButton.setOnAction(event -> handleSubmit());
    }

    /**
     * Sets the main application reference.
     * @param mainApp The main application instance.
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Sets the user reporting the emergency.
     * Also pre-fills some fields based on user data.
     * @param user The User (Tourist or Guide) reporting the emergency.
     */
    public void setReporter(User user) {
        this.reporter = user;
        if (user != null) {
            // Pre-fill contact field if available
            if (user instanceof com.example.nepaltourism.models.Tourist) {
                String emergencyContact = ((com.example.nepaltourism.models.Tourist) user).getEmergencyContact();
                if (emergencyContact != null && !emergencyContact.isEmpty()) {
                    contactField.setText(emergencyContact);
                } else {
                    contactField.setText(user.getPhone()); // Fallback to user's phone
                }
            } else {
                // For Guide or Admin, use their phone number
                contactField.setText(user.getPhone());
            }

            // Pre-fill location if it's a known field (e.g., from a booking or profile)
            // For now, we leave it blank for the user to fill in.
        }
    }

    /**
     * Handles the submit button action.
     */
    @FXML
    private void handleSubmit() {
        // Validate inputs
        if (reporter == null) {
            showAlert(Alert.AlertType.ERROR, "Submission Error", "Reporter information is missing.");
            return;
        }

        String location = locationField.getText().trim();
        String emergencyType = emergencyTypeCombo.getValue();
        String priority = priorityCombo.getValue();
        String description = descriptionArea.getText().trim();
        String contact = contactField.getText().trim();

        if (location.isEmpty() || emergencyType == null || emergencyType.isEmpty() ||
                priority == null || priority.isEmpty() || description.isEmpty() || contact.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Please fill in all fields.");
            return;
        }

        // Create emergency report object
        String reportId = "ER" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        String reporterId = reporter.getId();
        String reporterType = reporter.getUserType();

        EmergencyReport newReport = new EmergencyReport(
                reportId,
                reporterId,
                reporterType,
                location,
                emergencyType,
                priority,
                description,
                contact
        );
        // Timestamp and initial status are set in the EmergencyReport constructor

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Emergency Report");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(
                "Are you sure you want to submit this emergency report?\n\n" +
                        "Location: " + location + "\n" +
                        "Type: " + emergencyType + "\n" +
                        "Priority: " + priority + "\n" +
                        "Contact: " + contact + "\n\n" +
                        "Our team will respond as soon as possible."
        );

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Add report to main app's list
                mainApp.getEmergencyReports().add(newReport);
                // Save all data
                mainApp.saveAllData();
                logger.info("New emergency report submitted: " + reportId + " by " + reporterId);

                showAlert(Alert.AlertType.INFORMATION, "Report Submitted", "Your emergency report has been submitted successfully! Report ID: " + reportId + "\n\nOur team is notified and will respond promptly.");
                handleClose(); // Close the dialog
            }
        });
    }

    /**
     * Handles the cancel button action.
     */
    @FXML
    private void handleClose() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Shows a simple alert dialog.
     * @param alertType The type of alert (INFO, ERROR, etc.).
     * @param title The title of the alert.
     * @param message The message to display.
     */
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}