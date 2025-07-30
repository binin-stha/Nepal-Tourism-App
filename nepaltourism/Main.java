package com.example.nepaltourism;

import com.example.nepaltourism.controllers.AdminDashboardController;
import com.example.nepaltourism.controllers.GuideDashboardController;
import com.example.nepaltourism.controllers.TouristDashboardController;
import com.example.nepaltourism.models.*;
import com.example.nepaltourism.utils.LanguageManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Main entry point for the Nepal Tourism Application.
 * Initializes the application, loads initial data, and starts the login screen.
 */
public class Main extends Application {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private CSVDataManager dataManager;
    private Stage primaryStage;

    // In-memory data stores (could be moved to a dedicated service later)
    private List<User> users;
    private List<Attraction> attractions;
    private List<Booking> bookings;
    private List<EmergencyReport> emergencyReports;
//    private List<Tourist> tourists;

    @Override
    public void init() throws Exception {
        super.init();
        logger.info("Initializing Nepal Tourism Application...");

        // Initialize data manager
        dataManager = new CSVDataManager();

        // Load initial data
        loadData();
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Paryatan Nepal: A Tourism Management System");

        // Show the login screen initially
        showLoginScreen();
        this.primaryStage.show();
    }

    /**
     * Loads data from CSV files into memory.
     */
    private void loadData() {
        try {
            users = dataManager.loadUsers();
            logger.info("Loaded " + users.size() + " users.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load users", e);
            users = new java.util.ArrayList<>(); // Initialize with empty list on failure
        }

        try {
            attractions = dataManager.loadAttractions();
            logger.info("Loaded " + attractions.size() + " attractions.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load attractions", e);
            attractions = new java.util.ArrayList<>();
        }

        try {
            bookings = dataManager.loadBookings();
            logger.info("Loaded " + bookings.size() + " bookings.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load bookings", e);
            bookings = new java.util.ArrayList<>();
        }

        try {
            emergencyReports = dataManager.loadEmergencyReports();
            logger.info("Loaded " + emergencyReports.size() + " emergency reports.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load emergency reports", e);
            emergencyReports = new java.util.ArrayList<>();
        }

        // Create sample data if files are empty (for first run)
        createSampleDataIfEmpty();
    }

    /**
     * Saves all data back to CSV files.
     */
    public void saveAllData() {
        dataManager.saveUsers(users);
        dataManager.saveAttractions(attractions);
        dataManager.saveBookings(bookings);
        dataManager.saveEmergencyReports(emergencyReports);
        logger.info("All data saved to CSV files.");
    }

    /**
     * Creates sample data if the loaded lists are empty.
     * This is useful for the first run of the application.
     */
    private void createSampleDataIfEmpty() {
        if (users.isEmpty()) {
            // Add a sample admin user (password is "admin123" - in real app, this should be hashed)
            User admin = new Admin("ADM001", "Admin User", "admin@example.com", "9800000000", "admin123");
            users.add(admin);
            logger.info("Created sample admin user.");
        }
        // Sample data for other entities can be added here if needed
    }

    /**
     * Displays the login screen.
     * @throws IOException if the FXML file cannot be loaded.
     */
    public void showLoginScreen() throws IOException {
        // Load the FXML with ResourceBundle for localization
        ResourceBundle bundle = ResourceBundle.getBundle("lang.messages", Locale.getDefault());

        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/login.fxml"),
                bundle
        );

        Parent root = loader.load();

        // Get the controller and pass the main app instance
        com.example.nepaltourism.controllers.LoginController controller = loader.getController();
        controller.setMainApp(this);

        Scene scene = new Scene(root);
        // Apply CSS styling
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.centerOnScreen(); // Center the window
        primaryStage.setTitle(bundle.getString("login.welcome.title")); // Optional: Set title from bundle
        primaryStage.show();
    }
    /**
     * Displays the signup screen.
     * @throws IOException if the FXML file cannot be loaded.
     */
    public void showSignupScreen() throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("lang.messages", LanguageManager.getCurrentLocale());

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"), bundle);
        Parent root = loader.load();

        com.example.nepaltourism.controllers.SignupController controller = loader.getController();
        controller.setMainApp(this);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }


//    public void showSignupScreen() throws IOException {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/signup.fxml"), bundle);
//        Parent root = loader.load();
//
//        com.example.nepaltourism.controllers.SignupController controller = loader.getController();
//        controller.setMainApp(this);
//
//        Scene scene = new Scene(root);
//        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
//
//        primaryStage.setScene(scene);
//        primaryStage.centerOnScreen();
//    }



    // Getters for data lists (for controllers to access)
    public List<User> getUsers() {
        return users;
    }

    public List<Attraction> getAttractions() {
        return attractions;
    }

    public List<Booking> getBookings() {
        return bookings;
    }

    public List<EmergencyReport> getEmergencyReports() {
        return emergencyReports;
    }

//    public List<Tourist> getTourists() {
//        return tourists;
//    }

    public CSVDataManager getDataManager() {
        return dataManager;
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }
    public void showDashboard(User user) throws IOException {
        String fxmlFile = null;

        if (user instanceof Tourist) {
            fxmlFile = "/fxml/tourist-dashboard.fxml";
        } else if (user instanceof Guide) {
            fxmlFile = "/fxml/guide-dashboard.fxml";
        } else if (user instanceof Admin) {
            fxmlFile = "/fxml/admin-dashboard.fxml";
        }

        if (fxmlFile != null) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof TouristDashboardController) {
                TouristDashboardController c = (TouristDashboardController) controller;
                c.setMainApp(this);
                c.setLoggedInUser((Tourist) user);
                c.setupDashboard();
            } else if (controller instanceof GuideDashboardController) {
                GuideDashboardController c = (GuideDashboardController) controller;
                c.setMainApp(this);
                c.setLoggedInUser((Guide) user);
                c.setupDashboard();
            } else if (controller instanceof AdminDashboardController) {
                AdminDashboardController c = (AdminDashboardController) controller;
                c.setMainApp(this);
                c.setLoggedInUser((Admin) user);
                c.setupDashboard();
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
        }
    }
    public static void main(String[] args) {
        launch(args);
    }

}