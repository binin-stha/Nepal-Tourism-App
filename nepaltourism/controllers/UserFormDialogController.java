package com.example.nepaltourism.controllers;

import com.example.nepaltourism.Main;
import com.example.nepaltourism.models.Admin;
import com.example.nepaltourism.models.Guide;
import com.example.nepaltourism.models.Tourist;
import com.example.nepaltourism.models.User;
import com.example.nepaltourism.utils.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller class for the user-form-dialog.fxml view.
 */
public class UserFormDialogController {

    private static final Logger logger = Logger.getLogger(UserFormDialogController.class.getName());

    @FXML
    private Label dialogTitleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private VBox passwordBox;
    @FXML
    private PasswordField passwordField;
    @FXML
    private VBox confirmPasswordBox;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<String> userTypeCombo;
    @FXML
    private VBox tourAreaBox;
    @FXML
    private TextField tourAreaField;
    @FXML
    private Label errorLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private Main mainApp;
    private User user; // null for Add, User object for Edit
    private String userType; // The type of user being added/edited ("Tourist", "Guide", "Admin")
    private boolean isEditMode = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Populate user type combo box
        userTypeCombo.getItems().addAll(
                LanguageManager.getString("login.userType.tourist"),
                LanguageManager.getString("login.userType.guide"),
                LanguageManager.getString("login.userType.admin")
        );
        userTypeCombo.setPromptText(LanguageManager.getString("user.form.type"));

        // Add listener to user type combo to show/hide tour area field
        userTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String guideType = LanguageManager.getString("login.userType.guide");
            if (guideType.equals(newValue)) {
                tourAreaBox.setVisible(true);
                tourAreaBox.setManaged(true);
            } else {
                tourAreaBox.setVisible(false);
                tourAreaBox.setManaged(false);
                tourAreaField.clear(); // Clear field when hidden
            }
        });

        // Set up button actions
        cancelButton.setOnAction(event -> handleClose());
        saveButton.setOnAction(event -> handleSave());
    }

    /**
     * Sets the main application reference.
     * @param mainApp The main application instance.
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Sets the user to be edited.
     * If null, the dialog is in "Add" mode.
     * Also initializes the form fields.
     * @param user The User object to edit, or null for a new one.
     */
    public void setUser(User user) {
        this.user = user;
        this.isEditMode = (user != null);

        if (isEditMode) {
            dialogTitleLabel.setText(LanguageManager.getString("user.form.dialog.title.edit"));
            populateFieldsFromUser();
            // In edit mode, password fields are often hidden or made optional
            passwordBox.setVisible(false);
            passwordBox.setManaged(false);
            confirmPasswordBox.setVisible(false);
            confirmPasswordBox.setManaged(false);
        } else {
            dialogTitleLabel.setText(LanguageManager.getString("user.form.dialog.title.add"));
            // Fields are already empty by default
            // Password fields should be visible for new users
            passwordBox.setVisible(true);
            passwordBox.setManaged(true);
            confirmPasswordBox.setVisible(true);
            confirmPasswordBox.setManaged(true);
        }
    }

    /**
     * Sets the type of user being managed.
     * This is used when adding a new user to pre-select the type.
     * @param userType The user type string ("Tourist", "Guide", "Admin").
     */
    public void setUserType(String userType) {
        this.userType = userType;
        if (!isEditMode && userType != null) {
            // Pre-select the user type in the combo box
            userTypeCombo.setValue(userType);
            // Trigger the listener to show/hide tour area box
            userTypeCombo.getSelectionModel().select(userType);
        }
        // If editing, the type is fixed by the user object itself, so we don't change the combo
    }


    /**
     * Populates the form fields with data from the user being edited.
     */
    private void populateFieldsFromUser() {
        if (user != null) {
            nameField.setText(user.getName());
            emailField.setText(user.getEmail());
            phoneField.setText(user.getPhone());

            // Set user type in combo box
            String userTypeString = "";
            if (user instanceof Tourist) {
                userTypeString = LanguageManager.getString("login.userType.tourist");
            } else if (user instanceof Guide) {
                userTypeString = LanguageManager.getString("login.userType.guide");
                tourAreaField.setText(((Guide) user).getTourArea());
            } else if (user instanceof Admin) {
                userTypeString = LanguageManager.getString("login.userType.admin");
            }
            userTypeCombo.setValue(userTypeString);
            // Trigger listener to show/hide tour area
            userTypeCombo.getSelectionModel().select(userTypeString);

            // Password fields are hidden in edit mode, so no need to populate them
        }
    }

    /**
     * Generates a unique user ID based on user type.
     * Simple implementation using UUID and prefix.
     * @param userType The type of user (Tourist, Guide, Admin).
     * @return A unique ID string.
     */
    private String generateUserId(String userType) {
        String prefix = "USR"; // Default prefix
        if (LanguageManager.getString("login.userType.tourist").equals(userType)) {
            prefix = "TR";
        } else if (LanguageManager.getString("login.userType.guide").equals(userType)) {
            prefix = "GD";
        } else if (LanguageManager.getString("login.userType.admin").equals(userType)) {
            prefix = "ADM";
        }
        // Generate a simple unique ID (in a real app, you might use a database sequence)
        // Using last 6 chars of UUID for simplicity
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + uuidPart;
    }

    /**
     * Handles the save button action.
     */
    @FXML
    private void handleSave() {
        errorLabel.setText(""); // Clear previous errors

        // Validate inputs
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String selectedUserType = userTypeCombo.getValue();
        String tourArea = tourAreaField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || selectedUserType == null || selectedUserType.isEmpty()) {
            errorLabel.setText(LanguageManager.getString("error.field.required"));
            return;
        }

        // Validate email format (simple check)
        if (!email.contains("@") || !email.contains(".")) {
            errorLabel.setText(LanguageManager.getString("error.invalid.email"));
            return;
        }

        // Validate phone number (simple check for digits and length)
        if (!phone.matches("\\d{10,15}")) { // Assumes 10-15 digit phone numbers
            errorLabel.setText(LanguageManager.getString("error.invalid.phone"));
            return;
        }

        // Password validation (only for Add mode)
        String password = null;
        if (!isEditMode) {
            password = passwordField.getText();
            String confirmPassword = confirmPasswordField.getText();
            if (password.isEmpty() || confirmPassword.isEmpty()) {
                errorLabel.setText(LanguageManager.getString("error.field.required"));
                return;
            }
            if (!password.equals(confirmPassword)) {
                errorLabel.setText(LanguageManager.getString("signup.error.password.mismatch"));
                return;
            }
        } else {
            // In edit mode, if password fields were visible and used, get the new password
            // For simplicity here, we assume password is not changed in edit mode.
            // A real app might have a separate "Change Password" feature.
            if (user != null) {
                password = user.getPassword(); // Keep existing password
            }
        }

        // Specific validation for Guide
        String expectedGuideType = LanguageManager.getString("login.userType.guide");
        if (expectedGuideType.equals(selectedUserType)) {
            if (tourArea.isEmpty()) {
                errorLabel.setText(LanguageManager.getString("error.field.required") + " " + LanguageManager.getString("user.form.tourArea"));
                return;
            }
        }

        // Check for duplicate email (important for Add mode)
        if (!isEditMode) {
            boolean emailExists = mainApp.getUsers().stream()
                    .anyMatch(u -> u.getEmail().equalsIgnoreCase(email) && !u.getId().equals(user != null ? user.getId() : ""));
            if (emailExists) {
                errorLabel.setText(LanguageManager.getString("signup.error.email.exists"));
                return;
            }
        } else {
            // In edit mode, check if email is changed and if the new email already exists for another user
            if (user != null && !user.getEmail().equalsIgnoreCase(email)) {
                boolean emailExists = mainApp.getUsers().stream()
                        .anyMatch(u -> u.getEmail().equalsIgnoreCase(email) && !u.getId().equals(user.getId()));
                if (emailExists) {
                    errorLabel.setText(LanguageManager.getString("signup.error.email.exists"));
                    return;
                }
            }
        }


        // If in edit mode and user is null, something is wrong
        if (isEditMode && this.user == null) {
            errorLabel.setText(LanguageManager.getString("error.general"));
            logger.log(Level.SEVERE, "Edit mode is true but user object is null.");
            return;
        }

        // Create or update the user object
        if (isEditMode) {
            // Update existing user
            user.setName(name);
            user.setEmail(email);
            user.setPhone(phone);
            // Password is handled above (kept the same or potentially updated if field was visible)

            if (user instanceof Guide && expectedGuideType.equals(selectedUserType)) {
                ((Guide) user).setTourArea(tourArea);
                // Other Guide-specific fields (experience, languages) are managed in Guide's profile tab
            }
            // Tourist and Admin don't have additional fields managed here in this general form

            logger.info("User updated: " + user.getId());
        } else {
            // Create new user
            String newId = generateUserId(selectedUserType);

            User newUser = null;
            if (expectedGuideType.equals(selectedUserType)) {
                newUser = new Guide(newId, name, email, phone, password, tourArea, 0, "");
            } else if (LanguageManager.getString("login.userType.tourist").equals(selectedUserType)) {
                newUser = new Tourist(newId, name, email, phone, password, ""); // Default emergency contact
            } else if (LanguageManager.getString("login.userType.admin").equals(selectedUserType)) {
                newUser = new Admin(newId, name, email, phone, password);
            }

            if (newUser != null) {
                mainApp.getUsers().add(newUser);
                logger.info("New user created: " + newId + " (" + newUser.getUserType() + ")");
            } else {
                errorLabel.setText(LanguageManager.getString("error.general"));
                return; // Don't close dialog or save if user creation failed
            }
        }

        // Save all data
        mainApp.saveAllData();

        showAlert(Alert.AlertType.INFORMATION, "Success",
                isEditMode ? "User updated successfully." : "New user added successfully.");
        handleClose(); // Close the dialog
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