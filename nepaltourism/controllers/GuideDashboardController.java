package com.example.nepaltourism.controllers;

import com.example.nepaltourism.Main;
import com.example.nepaltourism.models.*;
import com.example.nepaltourism.utils.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class GuideDashboardController {

    private static final Logger logger = Logger.getLogger(GuideDashboardController.class.getName());

    // --- Top Navigation ---
    @FXML private Label welcomeLabel;
    @FXML private CheckBox availabilityCheckBox;
    @FXML private Button logoutButton;
    @FXML private Button languageSwitchButton;
    @FXML private HBox statusAlert;
    @FXML private Label statusAlertLabel;

    // --- Dashboard KPIs ---
    @FXML private Label activeBookingsLabel;
    @FXML private Label totalEarningsLabel;
    @FXML private Label averageRatingLabel;
    @FXML private Label totalToursLabel;
    @FXML private Label monthlyEarningsLabel;
    @FXML private Label completedToursLabel;
    @FXML private Label repeatCustomersLabel;
    @FXML private Label responseTimeLabel;

    // --- Charts ---
    @FXML private javafx.scene.chart.LineChart<String, Number> earningsChart;
    @FXML private javafx.scene.chart.CategoryAxis earningsChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis earningsChartYAxis;
    @FXML private javafx.scene.chart.BarChart<String, Number> tourChart;
    @FXML private javafx.scene.chart.CategoryAxis tourChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis tourChartYAxis;
    @FXML private javafx.scene.chart.PieChart ratingsChart;
    @FXML private VBox insightsBox;

    // --- My Bookings Tab ---
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> touristColumn;
    @FXML private TableColumn<Booking, String> attractionColumn;
    @FXML private TableColumn<Booking, LocalDate> dateColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, Booking> bookingActionsColumn;

    // --- Emergency Reports Tab ---
    @FXML private TableView<EmergencyReport> emergencyTable;
    @FXML private TableColumn<EmergencyReport, String> reportIdColumn;
    @FXML private TableColumn<EmergencyReport, String> reportTouristColumn;
    @FXML private TableColumn<EmergencyReport, String> locationColumn;
    @FXML private TableColumn<EmergencyReport, String> issueColumn;
    @FXML private TableColumn<EmergencyReport, String> priorityColumn;
    @FXML private TableColumn<EmergencyReport, String> reportStatusColumn;
    @FXML private TableColumn<EmergencyReport, EmergencyReport> reportActionsColumn;

    // --- Profile Tab ---
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField tourAreaField;
    @FXML private TextField experienceField;
    @FXML private TextField languagesField;
    @FXML private Button resetProfileButton;
    @FXML private Button updateProfileButton;

    private Main mainApp;
    private Guide loggedInUser;
    private ObservableList<Booking> bookingObservableList;
    private ObservableList<EmergencyReport> emergencyObservableList;

    @FXML
    private void initialize() {
        setupTopNavigation();
        setupBookingsTab();
        setupEmergencyReportsTab();
        setupProfileTab();
    }

    private void setupTopNavigation() {
        logoutButton.setOnAction(event -> handleLogout());
        availabilityCheckBox.setOnAction(event -> handleAvailabilityChange());
        if (languageSwitchButton != null) {
            languageSwitchButton.setOnAction(event -> handleLanguageSwitch());
        }
    }

    private void setupBookingsTab() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        touristColumn.setCellValueFactory(cellData -> {
            String touristId = cellData.getValue().getTouristId();
            Optional<User> touristOpt = mainApp.getUsers().stream()
                    .filter(u -> u instanceof Tourist && u.getId().equals(touristId))
                    .findFirst();
            return new javafx.beans.property.SimpleStringProperty(
                    touristOpt.map(User::getName).orElse("Unknown Tourist")
            );
        });
        attractionColumn.setCellValueFactory(cellData -> {
            String attractionId = cellData.getValue().getAttractionId();
            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
                    .filter(a -> a.getId().equals(attractionId)).findFirst();
            return new javafx.beans.property.SimpleStringProperty(
                    attractionOpt.map(Attraction::getName).orElse("Unknown Attraction")
            );
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("tourDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        bookingActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button updateStatusButton = new Button(LanguageManager.getString("button.update"));
            {
                updateStatusButton.setOnAction(event -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    handleUpdateBookingStatus(booking);
                });
            }
            @Override
            protected void updateItem(Booking item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : updateStatusButton);
            }
        });

        bookingObservableList = FXCollections.observableArrayList();
        bookingsTable.setItems(bookingObservableList);
    }

    private void setupEmergencyReportsTab() {
        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        reportTouristColumn.setCellValueFactory(cellData -> {
            String reporterId = cellData.getValue().getReporterId();
            if (!"Tourist".equals(cellData.getValue().getReporterType())) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
            Optional<User> touristOpt = mainApp.getUsers().stream()
                    .filter(u -> u instanceof Tourist && u.getId().equals(reporterId))
                    .findFirst();
            return new javafx.beans.property.SimpleStringProperty(
                    touristOpt.map(User::getName).orElse("Unknown Tourist")
            );
        });
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
        issueColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyType"));
        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        reportStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        reportActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button ackButton = new Button("Ack");
            private final Button resolveButton = new Button("Resolve");
            {
                ackButton.setOnAction(event -> handleAcknowledgeReport(getTableView().getItems().get(getIndex())));
                resolveButton.setOnAction(event -> handleResolveReport(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(EmergencyReport item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else if ("Reported".equals(item.getStatus())) {
                    setGraphic(ackButton);
                } else if ("Acknowledged".equals(item.getStatus())) {
                    setGraphic(resolveButton);
                } else {
                    setGraphic(null);
                }
            }
        });

        emergencyObservableList = FXCollections.observableArrayList();
        emergencyTable.setItems(emergencyObservableList);
    }

    private void setupProfileTab() {
        resetProfileButton.setOnAction(event -> loadProfileData());
        updateProfileButton.setOnAction(event -> handleUpdateProfile());
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setLoggedInUser(Guide user) {
        this.loggedInUser = user;
        welcomeLabel.setText(LanguageManager.getString("guide.dashboard.welcome").replace("{0}", user.getName()));
        loadProfileData();
        loadBookings();
        loadEmergencyReports();
        loadDashboardData();
    }

    private void loadProfileData() {
        if (loggedInUser != null) {
            nameField.setText(loggedInUser.getName());
            emailField.setText(loggedInUser.getEmail());
            phoneField.setText(loggedInUser.getPhone());
            tourAreaField.setText(loggedInUser.getTourArea());
            experienceField.setText(String.valueOf(loggedInUser.getExperience()));
            languagesField.setText(loggedInUser.getLanguages());
            availabilityCheckBox.setSelected(loggedInUser.isAvailable());
            updateStatusAlert();
        }
    }

    private void loadBookings() {
        if (loggedInUser != null) {
            bookingObservableList.setAll(mainApp.getBookings().stream()
                    .filter(b -> loggedInUser.getId().equals(b.getGuideId()))
                    .collect(Collectors.toList()));
        }
    }

    private void loadEmergencyReports() {
        emergencyObservableList.setAll(mainApp.getEmergencyReports());
    }

    private void loadDashboardData() {
        if (loggedInUser == null) return;
        List<Booking> guideBookings = mainApp.getBookings().stream()
                .filter(b -> loggedInUser.getId().equals(b.getGuideId()))
                .collect(Collectors.toList());
        long activeBookings = guideBookings.stream()
                .filter(b -> "Confirmed".equalsIgnoreCase(b.getStatus()) || "Pending".equalsIgnoreCase(b.getStatus()))
                .count();
        double totalEarnings = guideBookings.stream().mapToDouble(Booking::getTotalPrice).sum();
        double avgRating = loggedInUser.getRating();
        long totalTours = guideBookings.size();
        double monthlyEarnings = totalEarnings * 0.2;
        long completedTours = guideBookings.stream().filter(b -> "Completed".equalsIgnoreCase(b.getStatus())).count();
        long repeatCustomers = guideBookings.stream()
                .collect(Collectors.groupingBy(Booking::getTouristId, Collectors.counting()))
                .values().stream().filter(count -> count > 1).count();
        String responseTime = "2h";

        activeBookingsLabel.setText(String.valueOf(activeBookings));
        totalEarningsLabel.setText(String.format("$%.2f", totalEarnings));
        averageRatingLabel.setText(String.format("%.1f", avgRating));
        totalToursLabel.setText(String.valueOf(totalTours));
        monthlyEarningsLabel.setText(String.format("$%.2f", monthlyEarnings));
        completedToursLabel.setText(String.valueOf(completedTours));
        repeatCustomersLabel.setText(String.valueOf(repeatCustomers));
        responseTimeLabel.setText(responseTime);

        updateEarningsChart(guideBookings);
        updateTourChart(guideBookings);
        updateRatingsChart(avgRating);
        updateInsights(activeBookings, totalEarnings, avgRating);
    }

    private void updateEarningsChart(List<Booking> bookings) {
        earningsChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName(LanguageManager.getString("chart.earnings"));

        Map<String, Double> monthlyEarnings = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
            monthlyEarnings.put(monthKey, 0.0);
        }
        for (Booking booking : bookings) {
            if (booking.getTourDate() != null && "Completed".equalsIgnoreCase(booking.getStatus())) {
                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
                monthlyEarnings.merge(monthKey, booking.getTotalPrice(), Double::sum);
            }
        }
        for (Map.Entry<String, Double> entry : monthlyEarnings.entrySet()) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        earningsChart.getData().add(series);
    }

    private void updateTourChart(List<Booking> bookings) {
        tourChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName(LanguageManager.getString("chart.tours"));

        Map<String, Integer> monthlyTours = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
            monthlyTours.put(monthKey, 0);
        }
        for (Booking booking : bookings) {
            if (booking.getTourDate() != null && "Completed".equalsIgnoreCase(booking.getStatus())) {
                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
                monthlyTours.merge(monthKey, 1, Integer::sum);
            }
        }
        for (Map.Entry<String, Integer> entry : monthlyTours.entrySet()) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        tourChart.getData().add(series);
    }

    private void updateRatingsChart(double avgRating) {
        ratingsChart.getData().clear();
        double remaining = 5.0 - avgRating;
        if (avgRating > 0) {
            javafx.scene.chart.PieChart.Data avgData = new javafx.scene.chart.PieChart.Data(
                    LanguageManager.getString("chart.average_rating") + " (" + String.format("%.1f", avgRating) + ")", avgRating);
            javafx.scene.chart.PieChart.Data remainData = new javafx.scene.chart.PieChart.Data(
                    LanguageManager.getString("chart.remaining") + " (5.0)", remaining);
            ratingsChart.getData().addAll(avgData, remainData);
        }
    }

    private void updateInsights(long activeBookings, double totalEarnings, double avgRating) {
        insightsBox.getChildren().clear();
        if (activeBookings > 5) {
            Label insight1 = new Label("📅 Busy Guide: You have more than 5 active bookings.");
            insight1.getStyleClass().add("insight-label");
            insightsBox.getChildren().add(insight1);
        }
        if (totalEarnings > 2000) {
            Label insight2 = new Label("💰 High Earner: Your total earnings are above $2000.");
            insight2.getStyleClass().add("insight-label");
            insightsBox.getChildren().add(insight2);
        }
        if (avgRating < 3.5) {
            Label insight3 = new Label("⚠️ Rating Alert: Your average rating is below 3.5. Focus on service quality.");
            insight3.getStyleClass().add("insight-label");
            insightsBox.getChildren().add(insight3);
        }
    }

    @FXML
    private void handleLogout() {
        try {
            if (loggedInUser != null) {
                mainApp.saveAllData();
            }
            mainApp.showLoginScreen();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load login screen", e);
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not return to login screen.");
        }
    }

    @FXML
    private void handleAvailabilityChange() {
        if (loggedInUser != null) {
            boolean newAvailability = availabilityCheckBox.isSelected();
            loggedInUser.setAvailable(newAvailability);
            updateStatusAlert();
            logger.info("Guide " + loggedInUser.getName() + " availability set to " + newAvailability);
        }
    }

    private void updateStatusAlert() {
        if (loggedInUser != null) {
            if (loggedInUser.isAvailable()) {
                statusAlertLabel.setText(LanguageManager.getString("guide.dashboard.status.available"));
                statusAlert.getStyleClass().removeIf(s -> s.equals("alert-error"));
                if (!statusAlert.getStyleClass().contains("alert-warning")) {
                    statusAlert.getStyleClass().add("alert-warning");
                }
            } else {
                statusAlertLabel.setText(LanguageManager.getString("guide.dashboard.status.unavailable"));
                statusAlert.getStyleClass().removeIf(s -> s.equals("alert-warning"));
                if (!statusAlert.getStyleClass().contains("alert-error")) {
                    statusAlert.getStyleClass().add("alert-error");
                }
            }
        }
    }

    private void handleUpdateBookingStatus(Booking booking) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(booking.getStatus(), "Pending", "Confirmed", "Completed", "Cancelled");
        dialog.setTitle(LanguageManager.getString("dialog.update_booking_status_title"));
        dialog.setHeaderText(LanguageManager.getString("dialog.update_booking_status_header") + " " + booking.getId());
        dialog.setContentText(LanguageManager.getString("dialog.update_booking_status_content"));

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newStatus = result.get();
            if (!newStatus.equals(booking.getStatus())) {
                booking.setStatus(newStatus);
                mainApp.saveAllData();
                loadBookings();
                loadDashboardData();
                showAlert(Alert.AlertType.INFORMATION, LanguageManager.getString("alert.status_updated_title"),
                        LanguageManager.getString("alert.status_updated_message") + " " + newStatus);
            }
        }
    }

    private void handleAcknowledgeReport(EmergencyReport report) {
        if ("Reported".equals(report.getStatus())) {
            report.setStatus("Acknowledged");
            mainApp.saveAllData();
            loadEmergencyReports();
            showAlert(Alert.AlertType.INFORMATION, LanguageManager.getString("alert.report_acknowledged_title"),
                    LanguageManager.getString("alert.report_acknowledged_message") + " " + report.getId());
        }
    }

    private void handleResolveReport(EmergencyReport report) {
        if ("Acknowledged".equals(report.getStatus())) {
            report.setStatus("Resolved");
            mainApp.saveAllData();
            loadEmergencyReports();
            showAlert(Alert.AlertType.INFORMATION, LanguageManager.getString("alert.report_resolved_title"),
                    LanguageManager.getString("alert.report_resolved_message") + " " + report.getId());
        }
    }

    private void handleUpdateProfile() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String tourArea = tourAreaField.getText().trim();

        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || tourArea.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, LanguageManager.getString("alert.validation_error_title"),
                    LanguageManager.getString("alert.validation_error_message"));
            return;
        }

        loggedInUser.setName(name);
        loggedInUser.setEmail(email);
        loggedInUser.setPhone(phone);
        loggedInUser.setTourArea(tourArea);

        try {
            int experience = Integer.parseInt(experienceField.getText().trim());
            loggedInUser.setExperience(experience);
        } catch (NumberFormatException e) {
            logger.info("Invalid experience number entered, keeping old value.");
        }

        loggedInUser.setLanguages(languagesField.getText().trim());

        mainApp.saveAllData();
        showAlert(Alert.AlertType.INFORMATION, LanguageManager.getString("alert.profile_updated_title"),
                LanguageManager.getString("alert.profile_updated_message"));
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        // Optionally add icon: stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));

        alert.showAndWait();
    }

    @FXML
    private void handleLanguageSwitch() {
        refreshUILanguage();
    }

    public void setupDashboard() {
        if (mainApp == null || loggedInUser == null) {
            System.out.println("MainApp or LoggedInUser is not set");
            return;
        }
        loadBookings(); // Add guide-specific booking list logic
        loadEmergencyReports(); // If the guide dashboard shows emergencies
        // If you have guide-specific analytics
    }
    private void refreshUILanguage() {
        if (loggedInUser != null) {
            welcomeLabel.setText(LanguageManager.getString("guide.dashboard.welcome").replace("{0}", loggedInUser.getName()));
        }
        resetProfileButton.setText(LanguageManager.getString("button.reset"));
        updateProfileButton.setText(LanguageManager.getString("button.update"));

        // You may want to refresh all other UI texts here as well.
        // For example, column headers, buttons, labels etc.
    }
}

//package com.example.nepaltourism.controllers;
//
//import com.example.nepaltourism.Main;
//import com.example.nepaltourism.models.*;
//import com.example.nepaltourism.utils.LanguageManager;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.layout.HBox;
//import javafx.scene.layout.VBox;
//import javafx.stage.Stage;
//
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//
///**
// * Controller class for the guide-dashboard.fxml view.
// */
//public class GuideDashboardController {
//
//    private static final Logger logger = Logger.getLogger(GuideDashboardController.class.getName());
//
//    // --- Top Navigation ---
//    @FXML
//    private Label welcomeLabel;
//    @FXML
//    private CheckBox availabilityCheckBox;
//    @FXML
//    private Button logoutButton;
//    @FXML
//    private HBox statusAlert;
//    @FXML
//    private Label statusAlertLabel;
//
//    // --- Dashboard Tab (KPIs) ---
//    @FXML
//    private Label activeBookingsLabel;
//    @FXML
//    private Label totalEarningsLabel;
//    @FXML
//    private Label averageRatingLabel;
//    @FXML
//    private Label totalToursLabel;
//    @FXML
//    private Label monthlyEarningsLabel;
//    @FXML
//    private Label completedToursLabel;
//    @FXML
//    private Label repeatCustomersLabel;
//    @FXML
//    private Label responseTimeLabel;
//
//    // --- Dashboard Tab (Charts) ---
//    @FXML
//    private javafx.scene.chart.LineChart<String, Number> earningsChart;
//    @FXML
//    private javafx.scene.chart.CategoryAxis earningsChartXAxis;
//    @FXML
//    private javafx.scene.chart.NumberAxis earningsChartYAxis;
//    @FXML
//    private javafx.scene.chart.BarChart<String, Number> tourChart;
//    @FXML
//    private javafx.scene.chart.CategoryAxis tourChartXAxis;
//    @FXML
//    private javafx.scene.chart.NumberAxis tourChartYAxis;
//    @FXML
//    private javafx.scene.chart.PieChart ratingsChart;
//    @FXML
//    private VBox insightsBox;
//
//    // --- My Bookings Tab ---
//    @FXML
//    private TableView<Booking> bookingsTable;
//    @FXML
//    private TableColumn<Booking, String> bookingIdColumn;
//    @FXML
//    private TableColumn<Booking, String> touristColumn;
//    @FXML
//    private TableColumn<Booking, String> attractionColumn;
//    @FXML
//    private TableColumn<Booking, LocalDate> dateColumn;
//    @FXML
//    private TableColumn<Booking, String> statusColumn;
//    @FXML
//    private TableColumn<Booking, Booking> bookingActionsColumn; // For buttons
//
//    // --- Emergency Reports Tab ---
//    @FXML
//    private TableView<EmergencyReport> emergencyTable;
//    @FXML
//    private TableColumn<EmergencyReport, String> reportIdColumn;
//    @FXML
//    private TableColumn<EmergencyReport, String> reportTouristColumn;
//    @FXML
//    private TableColumn<EmergencyReport, String> locationColumn;
//    @FXML
//    private TableColumn<EmergencyReport, String> issueColumn;
//    @FXML
//    private TableColumn<EmergencyReport, String> priorityColumn;
//    @FXML
//    private TableColumn<EmergencyReport, String> reportStatusColumn;
//    @FXML
//    private TableColumn<EmergencyReport, EmergencyReport> reportActionsColumn; // For buttons
//
//    // --- Profile Tab ---
//    @FXML
//    private TextField nameField;
//    @FXML
//    private TextField emailField;
//    @FXML
//    private TextField phoneField;
//    @FXML
//    private TextField tourAreaField;
//    @FXML
//    private TextField experienceField;
//    @FXML
//    private TextField languagesField;
//    @FXML
//    private Button resetProfileButton;
//    @FXML
//    private Button updateProfileButton;
//
//    // --- Data ---
//    private Main mainApp;
//    private Guide loggedInUser;
//    private ObservableList<Booking> bookingObservableList;
//    private ObservableList<EmergencyReport> emergencyObservableList;
//
//    /**
//     * Initializes the controller class. This method is automatically called
//     * after the fxml file has been loaded.
//     */
//    @FXML
//    private void initialize() {
//        setupTopNavigation();
//        setupDashboardTab();
//        setupBookingsTab();
//        setupEmergencyReportsTab();
//        setupProfileTab();
//    }
//
//    private void setupTopNavigation() {
//        logoutButton.setOnAction(event -> handleLogout());
//        availabilityCheckBox.setOnAction(event -> handleAvailabilityChange());
//    }
//
//    private void setupDashboardTab() {
//        // KPIs and charts will be updated when user is set
//    }
//
//    private void setupBookingsTab() {
//        // Set up table columns
//        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        touristColumn.setCellValueFactory(cellData -> {
//            String touristId = cellData.getValue().getTouristId();
//            Optional<User> touristOpt = mainApp.getUsers().stream()
//                    .filter(u -> u instanceof Tourist && u.getId().equals(touristId))
//                    .findFirst();
//            return new javafx.beans.property.SimpleStringProperty(
//                    touristOpt.map(User::getName).orElse("Unknown Tourist")
//            );
//        });
//        attractionColumn.setCellValueFactory(cellData -> {
//            String attractionId = cellData.getValue().getAttractionId();
//            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
//                    .filter(a -> a.getId().equals(attractionId)).findFirst();
//            return new javafx.beans.property.SimpleStringProperty(
//                    attractionOpt.map(Attraction::getName).orElse("Unknown Attraction")
//            );
//        });
//        dateColumn.setCellValueFactory(ne PropertyValueFactory<>("tourDate"));
//        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
//
//        // Actions column for bookings (e.g., Update Status)
//        bookingActionsColumn.setCellFactory(param -> new TableCell<>() {
//            private final Button updwateStatusButton = new Button(LanguageManager.getString("button.update"));
//
//            {
//                updateStatusButton.setOnAction(event -> {
//                    Booking booking = getTableView().getItems().get(getIndex());
//                    handleUpdateBookingStatus(booking);
//                });
//            }
//
//            @Override
//            protected void updateItem(Booking item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setGraphic(null);
//                } else {
//                    setGraphic(updateStatusButton);
//                }
//            }
//        });
//
//        // Initialize booking list
//        bookingObservableList = FXCollections.observableArrayList();
//        bookingsTable.setItems(bookingObservableList);
//    }
//
//    private void setupEmergencyReportsTab() {
//        // Set up table columns
//        reportIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        reportTouristColumn.setCellValueFactory(cellData -> {
//            String reporterId = cellData.getValue().getReporterId();
//            if (!"Tourist".equals(cellData.getValue().getReporterType())) {
//                return new javafx.beans.property.SimpleStringProperty("N/A");
//            }
//            Optional<User> touristOpt = mainApp.getUsers().stream()
//                    .filter(u -> u instanceof Tourist && u.getId().equals(reporterId))
//                    .findFirst();
//            return new javafx.beans.property.SimpleStringProperty(
//                    touristOpt.map(User::getName).orElse("Unknown Tourist")
//            );
//        });
//        locationColumn.setCellValueFactory(new PropertyValueFactory<>("location"));
//        issueColumn.setCellValueFactory(new PropertyValueFactory<>("emergencyType"));
//        priorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
//        reportStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
//
//        // Actions column for reports (e.g., Acknowledge, Resolve)
//        reportActionsColumn.setCellFactory(param -> new TableCell<>() {
//            private final Button ackButton = new Button("Ack");
//            private final Button resolveButton = new Button("Resolve");
//
//            {
//                ackButton.setOnAction(event -> {
//                    EmergencyReport report = getTableView().getItems().get(getIndex());
//                    handleAcknowledgeReport(report);
//                });
//                resolveButton.setOnAction(event -> {
//                    EmergencyReport report = getTableView().getItems().get(getIndex());
//                    handleResolveReport(report);
//                });
//            }
//
//            @Override
//            protected void updateItem(EmergencyReport item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setGraphic(null);
//                } else {
//                    if ("Reported".equals(item.getStatus())) {
//                        setGraphic(ackButton);
//                    } else if ("Acknowledged".equals(item.getStatus())) {
//                        setGraphic(resolveButton);
//                    } else {
//                        setGraphic(null); // Resolved
//                    }
//                }
//            }
//        });
//
//        // Initialize emergency report list
//        emergencyObservableList = FXCollections.observableArrayList();
//        emergencyTable.setItems(emergencyObservableList);
//    }
//
//    private void setupProfileTab() {
//        resetProfileButton.setOnAction(event -> loadProfileData());
//        updateProfileButton.setOnAction(event -> handleUpdateProfile());
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
//     * Sets the logged-in guide user.
//     * @param user The logged-in Guide.
//     */
//    public void setLoggedInUser(Guide user) {
//        this.loggedInUser = user;
//        welcomeLabel.setText(LanguageManager.getString("guide.dashboard.welcome").replace("{0}", user.getName()));
//
//        // Load data for all tabs
//        loadProfileData();
//        loadBookings();
//        loadEmergencyReports();
//        loadDashboardData();
//    }
//
//    // --- Data Loading Methods ---
//    private void loadProfileData() {
//        if (loggedInUser != null) {
//            nameField.setText(loggedInUser.getName());
//            emailField.setText(loggedInUser.getEmail());
//            phoneField.setText(loggedInUser.getPhone());
//            tourAreaField.setText(loggedInUser.getTourArea());
//            experienceField.setText(String.valueOf(loggedInUser.getExperience()));
//            languagesField.setText(loggedInUser.getLanguages());
//            availabilityCheckBox.setSelected(loggedInUser.isAvailable());
//
//            updateStatusAlert();
//        }
//    }
//
//    private void loadBookings() {
//        if (loggedInUser != null) {
//            bookingObservableList.setAll(
//                    mainApp.getBookings().stream()
//                            .filter(b -> loggedInUser.getId().equals(b.getGuideId()))
//                            .collect(Collectors.toList())
//            );
//        }
//    }
//
//    private void loadEmergencyReports() {
//        // Load reports related to this guide's tourists or general reports
//        // For simplicity, load all reports for now. In a real app, you might filter by area or assigned tourists.
//        emergencyObservableList.setAll(mainApp.getEmergencyReports());
//    }
//
//    private void loadDashboardData() {
//        if (loggedInUser == null) return;
//
//        List<Booking> guideBookings = mainApp.getBookings().stream()
//                .filter(b -> loggedInUser.getId().equals(b.getGuideId()))
//                .collect(Collectors.toList());
//
//        // KPIs
//        long activeBookings = guideBookings.stream()
//                .filter(b -> "Confirmed".equalsIgnoreCase(b.getStatus()) || "Pending".equalsIgnoreCase(b.getStatus()))
//                .count();
//        double totalEarnings = guideBookings.stream().mapToDouble(Booking::getTotalPrice).sum();
//        // Simplified rating calculation (average of all bookings where guide was rated, if such data existed)
//        // For now, use guide's own rating
//        double avgRating = loggedInUser.getRating();
//        long totalTours = guideBookings.size();
//        // Dummy data for other KPIs
//        double monthlyEarnings = totalEarnings * 0.2; // Simplified
//        long completedTours = guideBookings.stream().filter(b -> "Completed".equalsIgnoreCase(b.getStatus())).count();
//        long repeatCustomers = guideBookings.stream()
//                .collect(Collectors.groupingBy(Booking::getTouristId, Collectors.counting()))
//                .values().stream().filter(count -> count > 1).count();
//        String responseTime = "2h"; // Dummy
//
//        activeBookingsLabel.setText(String.valueOf(activeBookings));
//        totalEarningsLabel.setText(String.format("$%.2f", totalEarnings));
//        averageRatingLabel.setText(String.format("%.1f", avgRating));
//        totalToursLabel.setText(String.valueOf(totalTours));
//        monthlyEarningsLabel.setText(String.format("$%.2f", monthlyEarnings));
//        completedToursLabel.setText(String.valueOf(completedTours));
//        repeatCustomersLabel.setText(String.valueOf(repeatCustomers));
//        responseTimeLabel.setText(responseTime);
//
//        // Charts (simplified)
//        updateEarningsChart(guideBookings);
//        updateTourChart(guideBookings);
//        updateRatingsChart(avgRating);
//        updateInsights(activeBookings, totalEarnings, avgRating);
//    }
//
//    // --- Chart Updates (Simplified) ---
//    private void updateEarningsChart(List<Booking> bookings) {
//        earningsChart.getData().clear();
//        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
//        series.setName("Earnings");
//
//        // Dummy data for months
//        Map<String, Double> monthlyEarnings = new LinkedHashMap<>();
//        for (int i = 5; i >= 0; i--) {
//            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//            monthlyEarnings.put(monthKey, 0.0);
//        }
//        // Populate with actual data (simplified sum by month of tour date)
//        for (Booking booking : bookings) {
//            if (booking.getTourDate() != null && "Completed".equalsIgnoreCase(booking.getStatus())) {
//                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//                monthlyEarnings.merge(monthKey, booking.getTotalPrice(), Double::sum);
//            }
//        }
//        for (Map.Entry<String, Double> entry : monthlyEarnings.entrySet()) {
//            series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
//        }
//        earningsChart.getData().add(series);
//    }
//
//    private void updateTourChart(List<Booking> bookings) {
//        tourChart.getData().clear();
//        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
//        series.setName("Tours");
//
//        Map<String, Integer> monthlyTours = new LinkedHashMap<>();
//        for (int i = 5; i >= 0; i--) {
//            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//            monthlyTours.put(monthKey, 0);
//        }
//        for (Booking booking : bookings) {
//            if (booking.getTourDate() != null && "Completed".equalsIgnoreCase(booking.getStatus())) {
//                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//                monthlyTours.merge(monthKey, 1, Integer::sum);
//            }
//        }
//        for (Map.Entry<String, Integer> entry : monthlyTours.entrySet()) {
//            series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
//        }
//        tourChart.getData().add(series);
//    }
//
//    private void updateRatingsChart(double avgRating) {
//        ratingsChart.getData().clear();
//        // Simplified: Show average rating vs. potential max (5.0)
//        double remaining = 5.0 - avgRating;
//        if (avgRating > 0) {
//            javafx.scene.chart.PieChart.Data avgData = new javafx.scene.chart.PieChart.Data("Average Rating (" + String.format("%.1f", avgRating) + ")", avgRating);
//            javafx.scene.chart.PieChart.Data remainData = new javafx.scene.chart.PieChart.Data("Remaining (5.0)", remaining);
//            ratingsChart.getData().addAll(avgData, remainData);
//        }
//    }
//
//    private void updateInsights(long activeBookings, double totalEarnings, double avgRating) {
//        insightsBox.getChildren().clear();
//        if (activeBookings > 5) {
//            Label insight1 = new Label("📅 Busy Guide: You have more than 5 active bookings.");
//            insight1.getStyleClass().add("insight-label");
//            insightsBox.getChildren().add(insight1);
//        }
//        if (totalEarnings > 2000) {
//            Label insight2 = new Label("💰 High Earner: Your total earnings are above $2000.");
//            insight2.getStyleClass().add("insight-label");
//            insightsBox.getChildren().add(insight2);
//        }
//        if (avgRating < 3.5) {
//            Label insight3 = new Label("⚠️ Rating Alert: Your average rating is below 3.5. Focus on service quality.");
//            insight3.getStyleClass().add("insight-label");
//            insightsBox.getChildren().add(insight3);
//        }
//        // Add more insights...
//    }
//
//    // --- Event Handlers ---
//    @FXML
//    private void handleLogout() {
//        try {
//            // Save the updated guide status before logging out
//            if (loggedInUser != null) {
//                mainApp.saveAllData(); // This saves the entire user list, including the updated guide
//            }
//            mainApp.showLoginScreen();
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Failed to load login screen", e);
//            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not return to login screen.");
//        }
//    }
//
//    @FXML
//    private void handleAvailabilityChange() {
//        if (loggedInUser != null) {
//            boolean newAvailability = availabilityCheckBox.isSelected();
//            loggedInUser.setAvailable(newAvailability);
//            updateStatusAlert();
//            // Note: The change is in memory. It will be saved when the app closes or on explicit save.
//            // For real-time persistence, you'd call mainApp.saveAllData() here, but that might be too frequent.
//            logger.info("Guide " + loggedInUser.getName() + " availability set to " + newAvailability);
//        }
//    }
//
//    private void updateStatusAlert() {
//        if (loggedInUser != null) {
//            if (loggedInUser.isAvailable()) {
//                statusAlertLabel.setText(LanguageManager.getString("guide.dashboard.status.available"));
//                statusAlert.getStyleClass().removeIf(s -> s.equals("alert-error"));
//                if (!statusAlert.getStyleClass().contains("alert-warning")) {
//                    statusAlert.getStyleClass().add("alert-warning"); // Or a specific "available" style
//                }
//            } else {
//                statusAlertLabel.setText(LanguageManager.getString("guide.dashboard.status.unavailable"));
//                statusAlert.getStyleClass().removeIf(s -> s.equals("alert-warning"));
//                if (!statusAlert.getStyleClass().contains("alert-error")) {
//                    statusAlert.getStyleClass().add("alert-error");
//                }
//            }
//        }
//    }
//
//    private void handleUpdateBookingStatus(Booking booking) {
//        // Simple dialog to update status
//        ChoiceDialog<String> dialog = new ChoiceDialog<>(booking.getStatus(), "Pending", "Confirmed", "Completed", "Cancelled");
//        dialog.setTitle("Update Booking Status");
//        dialog.setHeaderText("Change status for booking " + booking.getId());
//        dialog.setContentText("Select new status:");
//
//        Optional<String> result = dialog.showAndWait();
//        if (result.isPresent()) {
//            String newStatus = result.get();
//            if (!newStatus.equals(booking.getStatus())) {
//                booking.setStatus(newStatus);
//                mainApp.saveAllData(); // Save changes
//                loadBookings(); // Refresh table
//                loadDashboardData(); // Refresh KPIs/charts
//                showAlert(Alert.AlertType.INFORMATION, "Status Updated", "Booking status updated to " + newStatus);
//            }
//        }
//    }
//
//    private void handleAcknowledgeReport(EmergencyReport report) {
//        if ("Reported".equals(report.getStatus())) {
//            report.setStatus("Acknowledged");
//            mainApp.saveAllData();
//            loadEmergencyReports(); // Refresh table
//            showAlert(Alert.AlertType.INFORMATION, "Report Acknowledged", "Emergency report " + report.getId() + " has been acknowledged.");
//        }
//    }
//
//    private void handleResolveReport(EmergencyReport report) {
//        if ("Acknowledged".equals(report.getStatus())) {
//            report.setStatus("Resolved");
//            mainApp.saveAllData();
//            loadEmergencyReports(); // Refresh table
//            showAlert(Alert.AlertType.INFORMATION, "Report Resolved", "Emergency report " + report.getId() + " has been marked as resolved.");
//        }
//    }
//
//    private void handleUpdateProfile() {
//        // Basic validation
//        String name = nameField.getText().trim();
//        String email = emailField.getText().trim();
//        String phone = phoneField.getText().trim();
//        String tourArea = tourAreaField.getText().trim();
//
//        if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || tourArea.isEmpty()) {
//            showAlert(Alert.AlertType.WARNING, "Validation Error", "Name, Email, Phone, and Tour Area are required.");
//            return;
//        }
//
//        // Update user object
//        loggedInUser.setName(name);
//        loggedInUser.setEmail(email);
//        loggedInUser.setPhone(phone);
//        loggedInUser.setTourArea(tourArea);
//
//        try {
//            int experience = Integer.parseInt(experienceField.getText().trim());
//            loggedInUser.setExperience(experience);
//        } catch (NumberFormatException e) {
//            // Log or handle, but don't prevent update
//            logger.info("Invalid experience number entered, keeping old value.");
//        }
//
//        loggedInUser.setLanguages(languagesField.getText().trim());
//
//        // Save all data
//        mainApp.saveAllData();
//        showAlert(Alert.AlertType.INFORMATION, "Profile Updated", "Your profile has been successfully updated.");
//    }
//
//    // --- Utility ---
//    private void showAlert(Alert.AlertType alertType, String title, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//
//        // Get the Stage of the alert to set the icon (optional)
//        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
//        // stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
//
//        alert.showAndWait();
//    }
//
//    public void setupDashboard() {
//        if (mainApp == null || loggedInUser == null) {
//            logger.warning("Main app or logged in user not set before calling setupDashboard()");
//            return;
//        }
//        loadAttractions();
//        loadBookings();
//        loadGuides();
//        loadAnalytics();
//    }


//    ---------------------------------------------------------------------------------

//    private void loadAnalytics() {
//        List<Booking> userBookings = mainApp.getBookings().stream()
//                .filter(b -> b.getTouristId().equals(loggedInUser.getId()))
//                .collect(Collectors.toList());
//
//        double totalSpent = userBookings.stream().mapToDouble(Booking::getTotalPrice).sum();
//        long totalTrips = userBookings.size();
//        long completedTrips = userBookings.stream().filter(b -> "Completed".equalsIgnoreCase(b.getStatus())).count();
//        double avgTripCost = totalTrips > 0 ? totalSpent / totalTrips : 0.0;
//
//        totalSpentLabel.setText(String.format("$%.2f", totalSpent));
//        totalTripsLabel.setText(String.valueOf(totalTrips));
//        avgTripCostLabel.setText(String.format("$%.2f", avgTripCost));
//        completedTripsLabel.setText(String.valueOf(completedTrips));
//
//        // Spending Chart
//        spendingChart.getData().clear();
//        javafx.scene.chart.XYChart.Series<String, Number> spendingSeries = new javafx.scene.chart.XYChart.Series<>();
//        spendingSeries.setName("Spending");
//
//        Map<String, Double> monthlySpending = new LinkedHashMap<>();
//        for (int i = 5; i >= 0; i--) {
//            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//            monthlySpending.put(monthKey, 0.0);
//        }
//        for (Booking booking : userBookings) {
//            if (booking.getTourDate() != null) {
//                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//                monthlySpending.merge(monthKey, booking.getTotalPrice(), Double::sum);
//            }
//        }
//        for (Map.Entry<String, Double> entry : monthlySpending.entrySet()) {
//            spendingSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
//        }
//        spendingChart.getData().add(spendingSeries);
//
//        // Category Chart
//        categoryChart.getData().clear();
//        javafx.scene.chart.XYChart.Series<String, Number> categorySeries = new javafx.scene.chart.XYChart.Series<>();
//        categorySeries.setName("Spending by Category");
//
//        Map<String, Double> categorySpending = new HashMap<>();
//        for (Booking booking : userBookings) {
//            String attractionId = booking.getAttractionId();
//            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
//                    .filter(a -> a.getId().equals(attractionId)).findFirst();
//            String category = attractionOpt.map(Attraction::getCategory).orElse("Unknown");
//            categorySpending.merge(category, booking.getTotalPrice(), Double::sum);
//        }
//        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
//            categorySeries.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
//        }
//        categoryChart.getData().add(categorySeries);
//
//        // Insights
//        insightsBox.getChildren().clear();
//        if (totalTrips > 5) {
//            Label insight1 = new Label("🌟 Frequent Traveler: You've booked more than 5 trips!");
//            insight1.getStyleClass().add("insight-label");
//            insightsBox.getChildren().add(insight1);
//        }
//        if (avgTripCost > 500) {
//            Label insight2 = new Label("💸 High Spender: Your average trip cost is above $500.");
//            insight2.getStyleClass().add("insight-label");
//            insightsBox.getChildren().add(insight2);
//        }
//    }

