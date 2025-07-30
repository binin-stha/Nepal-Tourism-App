package com.example.nepaltourism.controllers;

import com.example.nepaltourism.Main;
import com.example.nepaltourism.models.Admin;
import com.example.nepaltourism.models.Guide;
import com.example.nepaltourism.models.Tourist;
import com.example.nepaltourism.models.User;
import com.example.nepaltourism.utils.LanguageManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for login.fxml view.
 */
public class LoginController {

    private static final Logger logger = Logger.getLogger(LoginController.class.getName());

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> userTypeCombo;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink signupLink;

    @FXML
    private Label errorLabel;

    private Main mainApp;

    @FXML
    private void initialize() {
        userTypeCombo.getItems().addAll(
                LanguageManager.getString("login.userType.tourist"),
                LanguageManager.getString("login.userType.guide"),
                LanguageManager.getString("login.userType.admin")
        );
        userTypeCombo.setPromptText(LanguageManager.getString("login.userType.prompt"));

        loginButton.setOnAction(event -> handleLogin());
        signupLink.setOnAction(event -> handleSignupLink());
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() {
        errorLabel.setText("");

        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String selectedUserType = userTypeCombo.getValue();

        // ✅ Basic field check
        if (email.isEmpty() || password.isEmpty() || selectedUserType == null || selectedUserType.isEmpty()) {
            errorLabel.setText(LanguageManager.getString( "Please fill all required fields."));
            return;
        }

        String expectedTouristType = LanguageManager.getString("login.userType.tourist");
        String expectedGuideType = LanguageManager.getString("login.userType.guide");
        String expectedAdminType = LanguageManager.getString("login.userType.admin");

        User authenticatedUser = null;

        // ✅ Authenticate user
        for (User user : mainApp.getUsers()) {
            if (user.getEmail().equalsIgnoreCase(email) && user.getPassword().equals(password)) {
                if ((user instanceof Tourist && expectedTouristType.equals(selectedUserType)) ||
                        (user instanceof Guide && expectedGuideType.equals(selectedUserType)) ||
                        (user instanceof Admin && expectedAdminType.equals(selectedUserType))) {
                    authenticatedUser = user;
                    break;
                }
            }
        }

        if (authenticatedUser != null) {
            logger.info("User logged in: " + authenticatedUser.getName() + " (" + authenticatedUser.getUserType() + ")");
            try {
                showDashboard(authenticatedUser);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to load dashboard for user: " + authenticatedUser.getId(), e);
                errorLabel.setText(LanguageManager.getString("An unexpected error occurred."));
            }
        } else {
            errorLabel.setText(LanguageManager.getString("Invalid email, password, or user type."));
        }
    }

    @FXML
    private void handleSignupLink() {
        try {
            mainApp.showSignupScreen();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load signup screen", e);
            errorLabel.setText("Error: Could not open signup page.");
        }
    }

    private void showDashboard(User user) throws IOException {
        String fxmlFile = null;
        FXMLLoader loader;

        if (user instanceof Tourist) {
            fxmlFile = "/fxml/tourist-dashboard.fxml";
        } else if (user instanceof Guide) {
            fxmlFile = "/fxml/guide-dashboard.fxml";
        } else if (user instanceof Admin) {
            fxmlFile = "/fxml/admin-dashboard.fxml";
        }

        if (fxmlFile != null) {
            loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();

            if (controller instanceof TouristDashboardController) {
                TouristDashboardController c = (TouristDashboardController) controller;
                c.setMainApp(mainApp);
                c.setLoggedInUser((Tourist) user);
                c.setupDashboard();

            } else if (controller instanceof GuideDashboardController) {
                GuideDashboardController c = (GuideDashboardController) controller;
                c.setMainApp(mainApp);
                c.setLoggedInUser((Guide) user);
                c.setupDashboard();

            } else if (controller instanceof AdminDashboardController) {
                AdminDashboardController c = (AdminDashboardController) controller;
                c.setMainApp(mainApp);
                c.setLoggedInUser((Admin) user);
                c.setupDashboard();
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            mainApp.getPrimaryStage().setScene(scene);
            mainApp.getPrimaryStage().centerOnScreen();
        }
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
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import java.io.IOException;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
//public class LoginController {
//
//    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
//
//    @FXML
//    private TextField emailField;
//
//    @FXML
//    private PasswordField passwordField;
//
//    @FXML
//    private ComboBox<String> userTypeCombo;
//
//    @FXML
//    private Button loginButton;
//
//    @FXML
//    private Hyperlink signupLink;
//
//    @FXML
//    private Label errorLabel;
//
//    private Main mainApp;
//
//    @FXML
//    private void initialize() {
//        userTypeCombo.getItems().addAll(
//                LanguageManager.getString("login.userType.tourist"),
//                LanguageManager.getString("login.userType.guide"),
//                LanguageManager.getString("login.userType.admin")
//        );
//        userTypeCombo.setPromptText(LanguageManager.getString("login.userType.prompt"));
//
//        loginButton.setOnAction(event -> handleLogin());
//        signupLink.setOnAction(event -> handleSignupLink());
//    }
//
//    public void setMainApp(Main mainApp) {
//        this.mainApp = mainApp;
//    }
//
//    @FXML
//    private void handleLogin() {
//        errorLabel.setText("");
//
//        String email = emailField.getText().trim();
//        String password = passwordField.getText();
//        String selectedUserType = userTypeCombo.getValue();
//
//        if (email.isEmpty() || password.isEmpty() || selectedUserType == null || selectedUserType.isEmpty()) {
//            errorLabel.setText(LanguageManager.getString("login.error.fields"));
//            return;
//        }
//
//        String expectedTouristType = LanguageManager.getString("login.userType.tourist");
//        String expectedGuideType = LanguageManager.getString("login.userType.guide");
//        String expectedAdminType = LanguageManager.getString("login.userType.admin");
//
//        User authenticatedUser = null;
//
//        for (User user : mainApp.getUsers()) {
//            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
//                if ((user instanceof Tourist && expectedTouristType.equals(selectedUserType)) ||
//                        (user instanceof Guide && expectedGuideType.equals(selectedUserType)) ||
//                        (user instanceof Admin && expectedAdminType.equals(selectedUserType))) {
//
//                    authenticatedUser = user;
//                    break;
//                }
//            }
//        }
//
//        if (authenticatedUser != null) {
//            logger.info("User logged in: " + authenticatedUser.getName() + " (" + authenticatedUser.getUserType() + ")");
//            try {
//                showDashboard(authenticatedUser);
//            } catch (IOException e) {
//                logger.log(Level.SEVERE, "Failed to load dashboard for user: " + authenticatedUser.getId(), e);
//                errorLabel.setText(LanguageManager.getString("error.general"));
//            }
//        } else {
//            errorLabel.setText(LanguageManager.getString("login.error.invalid"));
//        }
//    }
//
//    @FXML
//    private void handleSignupLink() {
//        try {
//            mainApp.showSignupScreen();
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Failed to load signup screen", e);
//            errorLabel.setText("Error: Could not open signup page. See log for details.");
//        }
//    }
//
//    private void showDashboard(User user) throws IOException {
//        String fxmlFile = null;
//        FXMLLoader loader;
//
//        if (user instanceof Tourist) {
//            fxmlFile = "/fxml/tourist-dashboard.fxml";
//        } else if (user instanceof Guide) {
//            fxmlFile = "/fxml/guide-dashboard.fxml";
//        } else if (user instanceof Admin) {
//            fxmlFile = "/fxml/admin-dashboard.fxml";
//        }
//
//        if (fxmlFile != null) {
//            loader = new FXMLLoader(getClass().getResource(fxmlFile));
//            Parent root = loader.load();
//
//            Object controller = loader.getController();
//
//            // ✅ Inject dependencies into the controller
//            if (controller instanceof TouristDashboardController) {
//                TouristDashboardController c = (TouristDashboardController) controller;
//                c.setMainApp(mainApp);
//                c.setLoggedInUser((Tourist) user);
//                c.setupDashboard();
//
//            } else if (controller instanceof GuideDashboardController) {
//                GuideDashboardController c = (GuideDashboardController) controller;
//                c.setMainApp(mainApp);
//                c.setLoggedInUser((Guide) user);
//                c.setupDashboard();
//
//            } else if (controller instanceof AdminDashboardController) {
//                AdminDashboardController c = (AdminDashboardController) controller;
//                c.setMainApp(mainApp);
//                c.setLoggedInUser((Admin) user);
//                c.setupDashboard(); // Make sure this method exists
//            }
//
//            // ✅ Load scene
//            Scene scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
//            mainApp.getPrimaryStage().setScene(scene);
//            mainApp.getPrimaryStage().centerOnScreen();
//        }
//    }
//
//
////    private void showDashboard(User user) throws IOException {
////        String fxmlFile = null;
////        if (user instanceof Tourist) {
////            fxmlFile = "/fxml/tourist-dashboard.fxml";
////        } else if (user instanceof Guide) {
////            fxmlFile = "/fxml/guide-dashboard.fxml";
////        } else if (user instanceof Admin) {
////            fxmlFile = "/fxml/admin-dashboard.fxml";
////        }
////
////        if (fxmlFile != null) {
////            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
////            Parent root = loader.load();
////
////            Object controller = loader.getController();
////            if (controller instanceof TouristDashboardController) {
////                TouristDashboardController c = (TouristDashboardController) controller;
////                c.setMainApp(mainApp);
////                c.setLoggedInUser((Tourist) user);
////                c.setupDashboard();  // Load data now that mainApp and user are set
////            }
////            // Add similar setup for GuideDashboardController and AdminDashboardController if needed
////
////            Scene scene = new Scene(root);
////            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
////
////            mainApp.getPrimaryStage().setScene(scene);
////            mainApp.getPrimaryStage().centerOnScreen();
////        }
////    }
//}
//
////package com.example.nepaltourism.controllers;
////
////import com.example.nepaltourism.Main;
////import com.example.nepaltourism.models.Admin;
////import com.example.nepaltourism.models.Guide;
////import com.example.nepaltourism.models.Tourist;
////import com.example.nepaltourism.models.User;
////import com.example.nepaltourism.utils.LanguageManager;
////import javafx.fxml.FXML;
////import javafx.fxml.FXMLLoader;
////import javafx.scene.Parent;
////import javafx.scene.Scene;
////import javafx.scene.control.*;
////import javafx.stage.Stage;
////
////import java.io.IOException;
////import java.util.logging.Logger;
////import java.util.logging.Level;
////
/////**
//// * Controller class for the login.fxml view.
//// */
////public class LoginController {
////
////    private static final Logger logger = Logger.getLogger(LoginController.class.getName());
////
////    @FXML
////    private TextField emailField;
////
////    @FXML
////    private PasswordField passwordField;
////
////    @FXML
////    private ComboBox<String> userTypeCombo;
////
////    @FXML
////    private Button loginButton;
////
////    @FXML
////    private Hyperlink signupLink;
////
////    @FXML
////    private Label errorLabel;
////
////    private Main mainApp;
////
////    /**
////     * Initializes the controller class. This method is automatically called
////     * after the fxml file has been loaded.
////     */
////    @FXML
////    private void initialize() {
////        // Populate the user type combo box
////        userTypeCombo.getItems().addAll(
////                LanguageManager.getString("login.userType.tourist"),
////                LanguageManager.getString("login.userType.guide"),
////                LanguageManager.getString("login.userType.admin")
////        );
////        userTypeCombo.setPromptText(LanguageManager.getString("login.userType.prompt"));
////
////        // Set up event handlers
////        loginButton.setOnAction(event -> handleLogin());
////        signupLink.setOnAction(event -> handleSignupLink());
////    }
////
////    /**
////     * Sets the main application reference.
////     * @param mainApp The main application instance.
////     */
////    public void setMainApp(Main mainApp) {
////        this.mainApp = mainApp;
////    }
////
////    /**
////     * Handles the login button action.
////     */
////    @FXML
////    private void handleLogin() {
////        errorLabel.setText(""); // Clear previous errors
////
////        String email = emailField.getText().trim();
////        String password = passwordField.getText(); // Get raw password
////        String selectedUserType = userTypeCombo.getValue();
////
////        // Basic validation
////        if (email.isEmpty() || password.isEmpty() || selectedUserType == null || selectedUserType.isEmpty()) {
////            errorLabel.setText(LanguageManager.getString("login.error.fields"));
////            return;
////        }
////
////        // Determine expected user type string for comparison
////        String expectedTouristType = LanguageManager.getString("login.userType.tourist");
////        String expectedGuideType = LanguageManager.getString("login.userType.guide");
////        String expectedAdminType = LanguageManager.getString("login.userType.admin");
////
////        User authenticatedUser = null;
////
////        // Iterate through users to find a match
////        for (User user : mainApp.getUsers()) {
////            // Check email and password (Note: In a real app, password should be hashed and compared securely)
////            if (user.getEmail().equals(email) && user.getPassword().equals(password)) {
////                // Check user type
////                if ((user instanceof Tourist && expectedTouristType.equals(selectedUserType)) ||
////                        (user instanceof Guide && expectedGuideType.equals(selectedUserType)) ||
////                        (user instanceof Admin && expectedAdminType.equals(selectedUserType))) {
////
////                    authenticatedUser = user;
////                    break; // Found the correct user
////                }
////            }
////        }
////
////        if (authenticatedUser != null) {
////            logger.info("User logged in: " + authenticatedUser.getName() + " (" + authenticatedUser.getUserType() + ")");
////            // Navigate to the appropriate dashboard
////            try {
////                showDashboard(authenticatedUser);
////            } catch (IOException e) {
////                logger.log(Level.SEVERE, "Failed to load dashboard for user: " + authenticatedUser.getId(), e);
////                errorLabel.setText(LanguageManager.getString("error.general"));
////            }
////        } else {
////            // Login failed
////            errorLabel.setText(LanguageManager.getString("login.error.invalid"));
////        }
////    }
////
////    /**
////     * Handles the signup link action.
////     */
////    @FXML
////    private void handleSignupLink() {
////        try {
////            mainApp.showSignupScreen();
////        } catch (IOException e) {
////            logger.log(Level.SEVERE, "Failed to load signup screen", e);
////            // In a real app, you might show an alert dialog here
////            errorLabel.setText("Error: Could not open signup page. See log for details.");
////        }
////    }
////
////    /**
////     * Shows the appropriate dashboard based on the user type.
////     * @param user The authenticated user.
////     * @throws IOException if the FXML file cannot be loaded.
////     */
////    private void showDashboard(User user) throws IOException {
////        String fxmlFile = null;
////        if (user instanceof Tourist) {
////            fxmlFile = "/fxml/tourist-dashboard.fxml";
////        } else if (user instanceof Guide) {
////            fxmlFile = "/fxml/guide-dashboard.fxml";
////        } else if (user instanceof Admin) {
////            fxmlFile = "/fxml/admin-dashboard.fxml";
////        }
////
////        if (fxmlFile != null) {
////            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
////            Parent root = loader.load();
////
////            // Pass the main app and user to the dashboard controller
////            // We need to cast to the specific controller type to access its method
////            Object controller = loader.getController();
////            if (controller instanceof TouristDashboardController) {
////                ((TouristDashboardController) controller).setMainApp(mainApp);
////                ((TouristDashboardController) controller).setLoggedInUser((Tourist) user);
////            } else if (controller instanceof GuideDashboardController) {
////                ((GuideDashboardController) controller).setMainApp(mainApp);
////                ((GuideDashboardController) controller).setLoggedInUser((Guide) user);
////            } else if (controller instanceof AdminDashboardController) {
////                ((AdminDashboardController) controller).setMainApp(mainApp);
////                ((AdminDashboardController) controller).setLoggedInUser((Admin) user);
////            }
////
////            Scene scene = new Scene(root);
////            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
////
////            mainApp.getPrimaryStage().setScene(scene);
////            mainApp.getPrimaryStage().centerOnScreen();
////        }
////    }
////}