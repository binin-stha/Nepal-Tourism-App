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
 * Controller class for the signup.fxml view.
 */
public class SignupController {

    private static final Logger logger = Logger.getLogger(SignupController.class.getName());

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private ComboBox<String> userTypeCombo;

    @FXML
    private TextField tourAreaField; // For Guide

    @FXML
    private VBox tourAreaBox; // Container for Tour Area field

    @FXML
    private Button signupButton;

    @FXML
    private Hyperlink loginLink;

    @FXML
    private Label errorLabel;

    private Main mainApp;

    @FXML
    private void initialize() {
        // Populate the user type combo box
        userTypeCombo.getItems().addAll(
                LanguageManager.getString("login.userType.tourist"),
                LanguageManager.getString("login.userType.guide"),
                LanguageManager.getString("login.userType.admin")
        );
        userTypeCombo.setPromptText(LanguageManager.getString("login.userType.prompt"));

        // Initially hide the tour area field
        tourAreaBox.setVisible(false);
        tourAreaBox.setManaged(false);

        // Show/hide tour area based on user type
        userTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            String guideType = LanguageManager.getString("login.userType.guide");
            if (guideType.equals(newVal)) {
                tourAreaBox.setVisible(true);
                tourAreaBox.setManaged(true);
            } else {
                tourAreaBox.setVisible(false);
                tourAreaBox.setManaged(false);
                tourAreaField.clear();
            }
        });

        signupButton.setOnAction(event -> handleSignup());
        loginLink.setOnAction(event -> handleLoginLink());
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleSignup() {
        errorLabel.setText(""); // Clear previous errors

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();
        String selectedUserType = userTypeCombo.getValue();
        String tourArea = tourAreaField.getText().trim();

        // Basic empty field check
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty() ||
                selectedUserType == null || selectedUserType.isEmpty()) {
            errorLabel.setText(LanguageManager.getString("signup.error.fields"));
            return;
        }

        // Email validation
        if (!isValidEmail(email)) {
            errorLabel.setText(LanguageManager.getString("signup.error.invalid.email"));
            return;
        }

        // Password validation
        if (!isValidPassword(password)) {
            errorLabel.setText(LanguageManager.getString("signup.error.weak.password"));
            return;
        }

        // Password match check
        if (!password.equals(confirmPassword)) {
            errorLabel.setText(LanguageManager.getString("signup.error.password.mismatch"));
            return;
        }

        // Check if email exists
        boolean emailExists = mainApp.getUsers().stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
        if (emailExists) {
            errorLabel.setText(LanguageManager.getString("signup.error.email.exists"));
            return;
        }

        // Create user
        User newUser = null;
        String userId = generateUserId(selectedUserType);

        String expectedGuideType = LanguageManager.getString("login.userType.guide");
        if (expectedGuideType.equals(selectedUserType)) {
            if (tourArea.isEmpty()) {
                errorLabel.setText(LanguageManager.getString("error.field.required"));
                return;
            }
            newUser = new Guide(userId, name, email, phone, password, tourArea, 0, "");
        } else if (LanguageManager.getString("login.userType.tourist").equals(selectedUserType)) {
            newUser = new Tourist(userId, name, email, phone, password, "");
        } else if (LanguageManager.getString("login.userType.admin").equals(selectedUserType)) {
            newUser = new Admin(userId, name, email, phone, password);
        }

        if (newUser != null) {
            mainApp.getUsers().add(newUser);
            mainApp.saveAllData();
            logger.info("New user signed up: " + newUser.getName() + " (" + newUser.getUserType() + ")");
            showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "Welcome, " + name + "! Your account has been created.");
            handleLoginLink();
        } else {
            errorLabel.setText(LanguageManager.getString("error.general"));
        }
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    private boolean isValidPassword(String password) {
        // Minimum 8 characters, at least one uppercase, one lowercase, one number, one special character
        return password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    private String generateUserId(String userType) {
        String prefix = "USR";
        if (LanguageManager.getString("login.userType.tourist").equals(userType)) {
            prefix = "TR";
        } else if (LanguageManager.getString("login.userType.guide").equals(userType)) {
            prefix = "GD";
        } else if (LanguageManager.getString("login.userType.admin").equals(userType)) {
            prefix = "ADM";
        }
        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return prefix + uuidPart;
    }

    @FXML
    private void handleLoginLink() {
        try {
            mainApp.showLoginScreen();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load login screen", e);
            showAlert(Alert.AlertType.ERROR, "Error", "Could not navigate to login screen.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert.showAndWait();
    }
}

//package com.example.nepaltourism.controllers;
//
//import com.example.nepaltourism.Main;
//import com.example.nepaltourism.models.Admin;
//import com.example.nepaltourism.models.Guide;
//import com.example.nepaltourism.models.Tourist;
//import com.example.nepaltourism.models.User;
//import com.example.nepaltourism.utils.LanguageManager;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//import java.util.UUID;
//import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//
///**
// * Controller class for the signup.fxml view.
// */
//public class SignupController {
//
//    private static final Logger logger = Logger.getLogger(SignupController.class.getName());
//
//    @FXML
//    private TextField nameField;
//
//    @FXML
//    private TextField emailField;
//
//    @FXML
//    private TextField phoneField;
//
//    @FXML
//    private PasswordField passwordField;
//
//    @FXML
//    private PasswordField confirmPasswordField;
//
//    @FXML
//    private ComboBox<String> userTypeCombo;
//
//    @FXML
//    private TextField tourAreaField; // For Guide
//
//    @FXML
//    private VBox tourAreaBox; // Container for Tour Area field
//
//    @FXML
//    private Button signupButton;
//
//    @FXML
//    private Hyperlink loginLink;
//
//    @FXML
//    private Label errorLabel;
//
//    private Main mainApp;
//
//    /**
//     * Initializes the controller class. This method is automatically called
//     * after the fxml file has been loaded.
//     */
//    @FXML
//    private void initialize() {
//        // Populate the user type combo box
//        userTypeCombo.getItems().addAll(
//                LanguageManager.getString("login.userType.tourist"),
//                LanguageManager.getString("login.userType.guide"),
//                LanguageManager.getString("login.userType.admin")
//        );
//        userTypeCombo.setPromptText(LanguageManager.getString("login.userType.prompt"));
//
//        // Initially hide the tour area field
//        tourAreaBox.setVisible(false);
//        tourAreaBox.setManaged(false); // Ensures it doesn't take up space
//
//        // Add listener to user type combo to show/hide tour area field
//        userTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
//            String guideType = LanguageManager.getString("login.userType.guide");
//            if (guideType.equals(newValue)) {
//                tourAreaBox.setVisible(true);
//                tourAreaBox.setManaged(true);
//            } else {
//                tourAreaBox.setVisible(false);
//                tourAreaBox.setManaged(false);
//                tourAreaField.clear(); // Clear field when hidden
//            }
//        });
//
//        // Set up event handlers
//        signupButton.setOnAction(event -> handleSignup());
//        loginLink.setOnAction(event -> handleLoginLink());
//    }
//
//    /**
//     * Sets the main application reference.
//     * @param mainApp The main application instance.
//     */
//    public void setMainApp(Main mainApp) {
//        this.mainApp = mainApp;
//    }
//
//    /**
//     * Handles the signup button action.
//     */
//    @FXML
//    private void handleSignup() {
//        errorLabel.setText(""); // Clear previous errors
//
//        String name = nameField.getText().trim();
//        String email = emailField.getText().trim();
//        String phone = phoneField.getText().trim();
//        String password = passwordField.getText();
//        String confirmPassword = confirmPasswordField.getText();
//        String selectedUserType = userTypeCombo.getValue();
//        String tourArea = tourAreaField.getText().trim();
//
//        // Basic validation
//        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || selectedUserType == null || selectedUserType.isEmpty()) {
//            errorLabel.setText(LanguageManager.getString("signup.error.fields"));
//            return;
//        }
//
//        if (!password.equals(confirmPassword)) {
//            errorLabel.setText(LanguageManager.getString("signup.error.password.mismatch"));
//            return;
//        }
//
//        // Check if email already exists
//        boolean emailExists = mainApp.getUsers().stream()
//                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
//        if (emailExists) {
//            errorLabel.setText(LanguageManager.getString("signup.error.email.exists"));
//            return;
//        }
//
//        // Determine user type and create user object
//        User newUser = null;
//        String userId = generateUserId(selectedUserType); // Generate unique ID
//
//        String expectedGuideType = LanguageManager.getString("login.userType.guide");
//
//        if (expectedGuideType.equals(selectedUserType)) {
//            if (tourArea.isEmpty()) {
//                errorLabel.setText(LanguageManager.getString("error.field.required")); // Or a specific message for tour area
//                return;
//            }
//            newUser = new Guide(userId, name, email, phone, password, tourArea, 0, ""); // Default experience 0, languages empty
//        } else if (LanguageManager.getString("login.userType.tourist").equals(selectedUserType)) {
//            newUser = new Tourist(userId, name, email, phone, password, ""); // Default emergency contact empty
//        } else if (LanguageManager.getString("login.userType.admin").equals(selectedUserType)) {
//            newUser = new Admin(userId, name, email, phone, password);
//        }
//
//        if (newUser != null) {
//            mainApp.getUsers().add(newUser);
//            // Save data immediately after successful signup
//            mainApp.saveAllData();
//            logger.info("New user signed up: " + newUser.getName() + " (" + newUser.getUserType() + ")");
//            showAlert(Alert.AlertType.INFORMATION, "Signup Successful", "Welcome, " + name + "! Your account has been created.");
//            // Navigate back to login
//            handleLoginLink();
//        } else {
//            errorLabel.setText(LanguageManager.getString("error.general"));
//        }
//    }
//
//    /**
//     * Generates a unique user ID based on user type.
//     * Simple implementation using UUID and prefix.
//     * @param userType The type of user (Tourist, Guide, Admin).
//     * @return A unique ID string.
//     */
//    private String generateUserId(String userType) {
//        String prefix = "USR"; // Default prefix
//        if (LanguageManager.getString("login.userType.tourist").equals(userType)) {
//            prefix = "TR";
//        } else if (LanguageManager.getString("login.userType.guide").equals(userType)) {
//            prefix = "GD";
//        } else if (LanguageManager.getString("login.userType.admin").equals(userType)) {
//            prefix = "ADM";
//        }
//        // Generate a simple unique ID (in a real app, you might use a database sequence)
//        // Using last 6 chars of UUID for simplicity
//        String uuidPart = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
//        return prefix + uuidPart;
//    }
//
//
//    /**
//     * Handles the login link action.
//     */
//    @FXML
//    private void handleLoginLink() {
//        try {
//            mainApp.showLoginScreen();
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Failed to load login screen", e);
//            showAlert(Alert.AlertType.ERROR, "Error", "Could not navigate to login screen.");
//        }
//    }
//
//    /**
//     * Shows a simple alert dialog.
//     * @param alertType The type of alert (INFO, ERROR, etc.).
//     * @param title The title of the alert.
//     * @param message The message to display.
//     */
//    private void showAlert(Alert.AlertType alertType, String title, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(null); // No header
//        alert.setContentText(message);
//
//        // Get the Stage of the alert to set the icon (optional)
//        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
//        // stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png"))); // Add icon if you have one
//
//        alert.showAndWait();
//    }
//}