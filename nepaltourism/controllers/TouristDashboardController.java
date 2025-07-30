package com.example.nepaltourism.controllers;

import com.example.nepaltourism.Main;
import com.example.nepaltourism.models.*;
import com.example.nepaltourism.utils.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class TouristDashboardController {

    private static final Logger logger = Logger.getLogger(TouristDashboardController.class.getName());

    @FXML private Label welcomeLabel;
    @FXML private Button languageButton;
    @FXML private Button emergencyButton;
    @FXML private Button logoutButton;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> regionFilter;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private ListView<Attraction> attractionsList;

    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> attractionColumn;
    @FXML private TableColumn<Booking, String> guideColumn;
    @FXML private TableColumn<Booking, LocalDate> dateColumn;
    @FXML private TableColumn<Booking, String> statusColumn;
    @FXML private TableColumn<Booking, Double> priceColumn;

    @FXML private ListView<Guide> guidesList;

    @FXML private Label totalSpentLabel;
    @FXML private Label totalTripsLabel;
    @FXML private Label avgTripCostLabel;
    @FXML private Label completedTripsLabel;
    @FXML private javafx.scene.chart.LineChart<String, Number> spendingChart;
    @FXML private javafx.scene.chart.CategoryAxis spendingChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis spendingChartYAxis;
    @FXML private javafx.scene.chart.BarChart<String, Number> categoryChart;
    @FXML private javafx.scene.chart.CategoryAxis categoryChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis categoryChartYAxis;
    @FXML private VBox insightsBox;

    private Main mainApp;
    private Tourist loggedInUser;
    private ObservableList<Attraction> attractionObservableList;
    private ObservableList<Booking> bookingObservableList;
    private ObservableList<Guide> guideObservableList;

    @FXML
    private void initialize() {
        setupTopNavigation();
        setupExploreTabUI();
        setupBookingsTabUI();
        setupGuidesTabUI();
        setupAnalyticsTabUI();
    }

    private void setupTopNavigation() {
        logoutButton.setOnAction(event -> handleLogout());
        emergencyButton.setOnAction(event -> handleEmergencyReport());
        languageButton.setOnAction(event -> handleLanguageSwitch());
    }

    private void setupExploreTabUI() {
        attractionObservableList = FXCollections.observableArrayList();
        attractionsList.setItems(attractionObservableList);

        attractionsList.setCellFactory(param -> new ListCell<Attraction>() {
            @Override
            protected void updateItem(Attraction item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " (" + item.getRegion() + ", " + item.getCategory() + ")");
                    setOnMouseClicked(event -> {
                        if (event.getClickCount() == 2) {
                            handleBookAttraction(item);
                        }
                    });
                }
            }
        });

        regionFilter.getItems().add("All Regions");
        categoryFilter.getItems().add("All Categories");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterAttractions());
        regionFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterAttractions());
        categoryFilter.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> filterAttractions());
    }

    private void setupBookingsTabUI() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        attractionColumn.setCellValueFactory(cellData -> {
            String attractionId = cellData.getValue().getAttractionId();
            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
                    .filter(a -> a.getId().equals(attractionId)).findFirst();
            return new javafx.beans.property.SimpleStringProperty(attractionOpt.map(Attraction::getName).orElse("Unknown"));
        });
        guideColumn.setCellValueFactory(cellData -> {
            String guideId = cellData.getValue().getGuideId();
            if (guideId == null || guideId.isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty("Not Assigned");
            }
            Optional<Guide> guideOpt = mainApp.getUsers().stream()
                    .filter(u -> u instanceof Guide && u.getId().equals(guideId))
                    .map(u -> (Guide) u)
                    .findFirst();
            return new javafx.beans.property.SimpleStringProperty(guideOpt.map(Guide::getName).orElse("Unknown Guide"));
        });
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("tourDate"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        bookingObservableList = FXCollections.observableArrayList();
        bookingsTable.setItems(bookingObservableList);
    }

    private void setupGuidesTabUI() {
        guideObservableList = FXCollections.observableArrayList();
        guidesList.setItems(guideObservableList);

        guidesList.setCellFactory(param -> new ListCell<Guide>() {
            @Override
            protected void updateItem(Guide item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getName() + " - " + item.getTourArea() + " (Rating: " + String.format("%.1f", item.getRating()) + ")");
                }
            }
        });
    }

    private void setupAnalyticsTabUI() {
        // UI-only setup if needed; actual analytics data loaded later
    }

    // Called after mainApp and loggedInUser are set
    public void setupDashboard() {
        if (mainApp == null || loggedInUser == null) {
            logger.warning("Main app or logged in user not set before calling setupDashboard()");
            return;
        }
        loadAttractions();
        loadBookings();
        loadGuides();
        loadAnalytics();
    }

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setLoggedInUser(Tourist user) {
        this.loggedInUser = user;
        welcomeLabel.setText(LanguageManager.getString("tourist.dashboard.welcome").replace("{0}", user.getName()));
        languageButton.setText(LanguageManager.getSwitchLanguageDisplayName());
    }

    private void loadAttractions() {
        attractionObservableList.setAll(
                mainApp.getAttractions().stream()
                        .filter(Attraction::isActive)
                        .collect(Collectors.toList())
        );

        Set<String> regions = mainApp.getAttractions().stream()
                .map(Attraction::getRegion)
                .filter(r -> r != null && !r.isEmpty())
                .collect(Collectors.toSet());

        regionFilter.getItems().setAll("All Regions");
        regionFilter.getItems().addAll(regions);

        Set<String> categories = mainApp.getAttractions().stream()
                .map(Attraction::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .collect(Collectors.toSet());

        categoryFilter.getItems().setAll("All Categories");
        categoryFilter.getItems().addAll(categories);

        filterAttractions();
    }

    private void loadBookings() {
        bookingObservableList.setAll(
                mainApp.getBookings().stream()
                        .filter(b -> b.getTouristId().equals(loggedInUser.getId()))
                        .collect(Collectors.toList())
        );
    }

    private void loadGuides() {
        guideObservableList.setAll(
                mainApp.getUsers().stream()
                        .filter(u -> u instanceof Guide && ((Guide) u).isAvailable())
                        .map(u -> (Guide) u)
                        .collect(Collectors.toList())
        );
    }

    private void loadAnalytics() {
        List<Booking> userBookings = mainApp.getBookings().stream()
                .filter(b -> b.getTouristId().equals(loggedInUser.getId()))
                .collect(Collectors.toList());

        double totalSpent = userBookings.stream().mapToDouble(Booking::getTotalPrice).sum();
        long totalTrips = userBookings.size();
        long completedTrips = userBookings.stream().filter(b -> "Completed".equalsIgnoreCase(b.getStatus())).count();
        double avgTripCost = totalTrips > 0 ? totalSpent / totalTrips : 0.0;

        totalSpentLabel.setText(String.format("$%.2f", totalSpent));
        totalTripsLabel.setText(String.valueOf(totalTrips));
        avgTripCostLabel.setText(String.format("$%.2f", avgTripCost));
        completedTripsLabel.setText(String.valueOf(completedTrips));

        // Spending Chart
        spendingChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> spendingSeries = new javafx.scene.chart.XYChart.Series<>();
        spendingSeries.setName("Spending");

        Map<String, Double> monthlySpending = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
            monthlySpending.put(monthKey, 0.0);
        }
        for (Booking booking : userBookings) {
            if (booking.getTourDate() != null) {
                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
                monthlySpending.merge(monthKey, booking.getTotalPrice(), Double::sum);
            }
        }
        for (Map.Entry<String, Double> entry : monthlySpending.entrySet()) {
            spendingSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        spendingChart.getData().add(spendingSeries);

        // Category Chart
        categoryChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> categorySeries = new javafx.scene.chart.XYChart.Series<>();
        categorySeries.setName("Spending by Category");

        Map<String, Double> categorySpending = new HashMap<>();
        for (Booking booking : userBookings) {
            String attractionId = booking.getAttractionId();
            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
                    .filter(a -> a.getId().equals(attractionId)).findFirst();
            String category = attractionOpt.map(Attraction::getCategory).orElse("Unknown");
            categorySpending.merge(category, booking.getTotalPrice(), Double::sum);
        }
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            categorySeries.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        categoryChart.getData().add(categorySeries);

        // Insights
        insightsBox.getChildren().clear();
        if (totalTrips > 5) {
            Label insight1 = new Label("🌟 Frequent Traveler: You've booked more than 5 trips!");
            insight1.getStyleClass().add("insight-label");
            insightsBox.getChildren().add(insight1);
        }
        if (avgTripCost > 500) {
            Label insight2 = new Label("💸 High Spender: Your average trip cost is above $500.");
            insight2.getStyleClass().add("insight-label");
            insightsBox.getChildren().add(insight2);
        }
    }

    private void filterAttractions() {
        String searchText = searchField.getText().toLowerCase();
        String selectedRegion = regionFilter.getValue();
        String selectedCategory = categoryFilter.getValue();

        ObservableList<Attraction> filteredList = FXCollections.observableArrayList();

        for (Attraction attraction : mainApp.getAttractions()) {
            if (!attraction.isActive()) continue;

            boolean matchesSearch = searchText.isEmpty() ||
                    attraction.getName().toLowerCase().contains(searchText) ||
                    attraction.getDescription().toLowerCase().contains(searchText);

            boolean matchesRegion = "All Regions".equals(selectedRegion) || selectedRegion == null ||
                    (attraction.getRegion() != null && attraction.getRegion().equals(selectedRegion));

            boolean matchesCategory = "All Categories".equals(selectedCategory) || selectedCategory == null ||
                    (attraction.getCategory() != null && attraction.getCategory().equals(selectedCategory));

            if (matchesSearch && matchesRegion && matchesCategory) {
                filteredList.add(attraction);
            }
        }
        attractionObservableList.setAll(filteredList);
    }

    @FXML
    private void handleLogout() {
        try {
            mainApp.showLoginScreen();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load login screen", e);
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not return to login screen.");
        }
    }

    @FXML
    private void handleEmergencyReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/emergency-dialog.fxml"));
            Parent root = loader.load();

            EmergencyDialogController controller = loader.getController();
            controller.setMainApp(mainApp);
            controller.setReporter(loggedInUser);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(LanguageManager.getString("emergency.dialog.title"));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load emergency dialog", e);
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open emergency report dialog.");
        }
    }

    @FXML
    private void handleLanguageSwitch() {
        if (Locale.ENGLISH.equals(LanguageManager.getCurrentLocale())) {
            LanguageManager.setLocale(new Locale("np"));
        } else {
            LanguageManager.setLocale(Locale.ENGLISH);
        }
        try {
            mainApp.showDashboard(loggedInUser); // reload with updated language
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to reload dashboard after language switch", e);
        }
    }

//    @FXML
//    private void handleLanguageSwitch() {
//        if (Locale.ENGLISH.equals(LanguageManager.getCurrentLocale())) {
//            LanguageManager.setLocale(new Locale("np"));
//        } else {
//            LanguageManager.setLocale(Locale.ENGLISH);
//        }
//
//        welcomeLabel.setText(LanguageManager.getString("tourist.dashboard.welcome").replace("{0}", loggedInUser.getName()));
//        languageButton.setText(LanguageManager.getSwitchLanguageDisplayName());
//
//        showAlert(Alert.AlertType.INFORMATION, "Language Switched", "Language has been switched to " + LanguageManager.getCurrentLanguageDisplayName());
//
//        // You may want to reload the entire scene or use binding for a full update
//    }

    private void handleBookAttraction(Attraction attraction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking-dialog.fxml"));
            Parent root = loader.load();

            BookingDialogController controller = loader.getController();
            controller.setMainApp(mainApp);
            controller.setTourist(loggedInUser);
            controller.setAttraction(attraction);

            Stage dialogStage = new Stage();
            dialogStage.setTitle(LanguageManager.getString("booking.dialog.title").replace("{0}", attraction.getName()));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            loadBookings();
            loadAnalytics();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load booking dialog for attraction: " + attraction.getId(), e);
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open booking dialog.");
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

//package com.example.nepaltourism.controllers;
//
//import com.example.nepaltourism.Main;
//import com.example.nepaltourism.models.*;
//import com.example.nepaltourism.utils.FestivalManager;
//import com.example.nepaltourism.utils.LanguageManager;
//import com.example.nepaltourism.utils.SafetyAlertManager;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.scene.layout.VBox;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//
///**
// * Controller class for the tourist-dashboard.fxml view.
// */
//public class TouristDashboardController {
//
//    private static final Logger logger = Logger.getLogger(TouristDashboardController.class.getName());
//
//    // --- Top Navigation ---
//    @FXML
//    private Label welcomeLabel;
//    @FXML
//    private Button languageButton;
//    @FXML
//    private Button emergencyButton;
//    @FXML
//    private Button logoutButton;
//
//    // --- Explore Attractions Tab ---
//    @FXML
//    private TextField searchField;
//    @FXML
//    private ComboBox<String> regionFilter;
//    @FXML
//    private ComboBox<String> categoryFilter;
//    @FXML
//    private ListView<Attraction> attractionsList;
//
//    // --- My Bookings Tab ---
//    @FXML
//    private TableView<Booking> bookingsTable;
//    @FXML
//    private TableColumn<Booking, String> bookingIdColumn;
//    @FXML
//    private TableColumn<Booking, String> attractionColumn;
//    @FXML
//    private TableColumn<Booking, String> guideColumn;
//    @FXML
//    private TableColumn<Booking, LocalDate> dateColumn;
//    @FXML
//    private TableColumn<Booking, String> statusColumn;
//    @FXML
//    private TableColumn<Booking, Double> priceColumn;
//
//    // --- Available Guides Tab ---
//    @FXML
//    private ListView<Guide> guidesList;
//
//    // --- My Analytics Tab ---
//    @FXML
//    private Label totalSpentLabel;
//    @FXML
//    private Label totalTripsLabel;
//    @FXML
//    private Label avgTripCostLabel;
//    @FXML
//    private Label completedTripsLabel;
//    @FXML
//    private javafx.scene.chart.LineChart<String, Number> spendingChart;
//    @FXML
//    private javafx.scene.chart.CategoryAxis spendingChartXAxis;
//    @FXML
//    private javafx.scene.chart.NumberAxis spendingChartYAxis;
//    @FXML
//    private javafx.scene.chart.BarChart<String, Number> categoryChart;
//    @FXML
//    private javafx.scene.chart.CategoryAxis categoryChartXAxis;
//    @FXML
//    private javafx.scene.chart.NumberAxis categoryChartYAxis;
//    @FXML
//    private VBox insightsBox;
//
//    // --- Data ---
//    private Main mainApp;
//    private Tourist loggedInUser;
//    private ObservableList<Attraction> attractionObservableList;
//    private ObservableList<Booking> bookingObservableList;
//    private ObservableList<Guide> guideObservableList;
//
//    /**
//     * Initializes the controller class. This method is automatically called
//     * after the fxml file has been loaded.
//     */
//    @FXML
//    private void initialize() {
//        setupTopNavigation();
//        setupExploreTab();
//        setupBookingsTab();
//        setupGuidesTab();
//        setupAnalyticsTab();
//    }
//
//    private void setupTopNavigation() {
//        logoutButton.setOnAction(event -> handleLogout());
//        emergencyButton.setOnAction(event -> handleEmergencyReport());
//        languageButton.setOnAction(event -> handleLanguageSwitch());
//    }
//
//    private void setupExploreTab() {
//        // Initialize attraction list
//        attractionObservableList = FXCollections.observableArrayList();
//        attractionsList.setItems(attractionObservableList);
//
//        // Set cell factory for attraction list (simple display)
//        attractionsList.setCellFactory(param -> new ListCell<Attraction>() {
//            @Override
//            protected void updateItem(Attraction item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.getName() + " (" + item.getRegion() + ", " + item.getCategory() + ")");
//                    setOnMouseClicked(event -> {
//                        if (event.getClickCount() == 2) { // Double-click to book
//                            handleBookAttraction(item);
//                        }
//                    });
//                }
//            }
//        });
//
//        // Setup filters
//        regionFilter.getItems().add("All Regions");
//        categoryFilter.getItems().add("All Categories");
//
//        // Add listeners for search and filters
//        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterAttractions());
//        regionFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> filterAttractions());
//        categoryFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> filterAttractions());
//
//        // Initially load attractions
//        loadAttractions();
//    }
//
//    private void setupBookingsTab() {
//        // Set up table columns
//        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        attractionColumn.setCellValueFactory(cellData -> {
//            String attractionId = cellData.getValue().getAttractionId();
//            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
//                    .filter(a -> a.getId().equals(attractionId)).findFirst();
//            return new javafx.beans.property.SimpleStringProperty(
//                    attractionOpt.map(Attraction::getName).orElse("Unknown")
//            );
//        });
//        guideColumn.setCellValueFactory(cellData -> {
//            String guideId = cellData.getValue().getGuideId();
//            if (guideId == null || guideId.isEmpty()) {
//                return new javafx.beans.property.SimpleStringProperty("Not Assigned");
//            }
//            Optional<Guide> guideOpt = mainApp.getUsers().stream()
//                    .filter(u -> u instanceof Guide && u.getId().equals(guideId))
//                    .map(u -> (Guide) u)
//                    .findFirst();
//            return new javafx.beans.property.SimpleStringProperty(
//                    guideOpt.map(Guide::getName).orElse("Unknown Guide")
//            );
//        });
//        dateColumn.setCellValueFactory(new PropertyValueFactory<>("tourDate"));
//        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
//        priceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
//
//        // Initialize booking list
//        bookingObservableList = FXCollections.observableArrayList();
//        bookingsTable.setItems(bookingObservableList);
//
//        // Initially load bookings
//        // loadBookings() will be called after setting the user
//    }
//
//    private void setupGuidesTab() {
//        guideObservableList = FXCollections.observableArrayList();
//        guidesList.setItems(guideObservableList);
//
//        // Set cell factory for guide list
//        guidesList.setCellFactory(param -> new ListCell<Guide>() {
//            @Override
//            protected void updateItem(Guide item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item.getName() + " - " + item.getTourArea() + " (Rating: " + String.format("%.1f", item.getRating()) + ")");
//                }
//            }
//        });
//
//        // Initially load guides
//        loadGuides();
//    }
//
//    private void setupAnalyticsTab() {
//        // Chart setup is done dynamically when data is loaded
//        // Initially load analytics
//        // loadAnalytics() will be called after setting the user
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
//     * Sets the logged-in tourist user.
//     * @param user The logged-in Tourist.
//     */
//    public void setLoggedInUser(Tourist user) {
//        this.loggedInUser = user;
//        welcomeLabel.setText(LanguageManager.getString("tourist.dashboard.welcome").replace("{0}", user.getName()));
//        languageButton.setText(LanguageManager.getSwitchLanguageDisplayName());
//        loadBookings(); // Load user-specific bookings
//        loadAnalytics(); // Load user-specific analytics
//    }
//
//    // --- Data Loading Methods ---
//    private void loadAttractions() {
//        attractionObservableList.setAll(
//                mainApp.getAttractions().stream()
//                        .filter(Attraction::isActive) // Only show active attractions
//                        .collect(Collectors.toList())
//        );
//
//        // Populate filters
//        Set<String> regions = mainApp.getAttractions().stream()
//                .map(Attraction::getRegion)
//                .filter(r -> r != null && !r.isEmpty())
//                .collect(Collectors.toSet());
//        regionFilter.getItems().setAll("All Regions");
//        regionFilter.getItems().addAll(regions);
//
//        Set<String> categories = mainApp.getAttractions().stream()
//                .map(Attraction::getCategory)
//                .filter(c -> c != null && !c.isEmpty())
//                .collect(Collectors.toSet());
//        categoryFilter.getItems().setAll("All Categories");
//        categoryFilter.getItems().addAll(categories);
//
//        // Apply initial filter
//        filterAttractions();
//    }
//
//    private void loadBookings() {
//        if (loggedInUser != null) {
//            bookingObservableList.setAll(
//                    mainApp.getBookings().stream()
//                            .filter(b -> b.getTouristId().equals(loggedInUser.getId()))
//                            .collect(Collectors.toList())
//            );
//        }
//    }
//
//    private void loadGuides() {
//        guideObservableList.setAll(
//                mainApp.getUsers().stream()
//                        .filter(u -> u instanceof Guide && ((Guide) u).isAvailable())
//                        .map(u -> (Guide) u)
//                        .collect(Collectors.toList())
//        );
//    }
//
//    private void loadAnalytics() {
//        if (loggedInUser == null) return;
//
//        List<Booking> userBookings = mainApp.getBookings().stream()
//                .filter(b -> b.getTouristId().equals(loggedInUser.getId()))
//                .collect(Collectors.toList());
//
//        // KPIs
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
//        // Spending Chart (Last 6 months is a simplification)
//        spendingChart.getData().clear();
//        javafx.scene.chart.XYChart.Series<String, Number> spendingSeries = new javafx.scene.chart.XYChart.Series<>();
//        spendingSeries.setName("Spending");
//
//        // This is a simplified example, ideally you'd group by actual months
//        Map<String, Double> monthlySpending = new LinkedHashMap<>();
//        // Populate with dummy data or real data logic
//        for (int i = 5; i >= 0; i--) {
//            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//            monthlySpending.put(monthKey, 0.0);
//        }
//        for (Booking booking : userBookings) {
//            if (booking.getTourDate() != null) {
//                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//                // Add to existing or create new entry
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
//        // Insights (Simple example)
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
//        // Add more insights based on data...
//    }
//
//    // --- Filtering ---
//    private void filterAttractions() {
//        String searchText = searchField.getText().toLowerCase();
//        String selectedRegion = regionFilter.getValue();
//        String selectedCategory = categoryFilter.getValue();
//
//        ObservableList<Attraction> filteredList = FXCollections.observableArrayList();
//
//        for (Attraction attraction : mainApp.getAttractions()) {
//            if (!attraction.isActive()) continue; // Skip inactive
//
//            boolean matchesSearch = searchText.isEmpty() ||
//                    attraction.getName().toLowerCase().contains(searchText) ||
//                    attraction.getDescription().toLowerCase().contains(searchText);
//
//            boolean matchesRegion = "All Regions".equals(selectedRegion) || selectedRegion == null ||
//                    (attraction.getRegion() != null && attraction.getRegion().equals(selectedRegion));
//
//            boolean matchesCategory = "All Categories".equals(selectedCategory) || selectedCategory == null ||
//                    (attraction.getCategory() != null && attraction.getCategory().equals(selectedCategory));
//
//            if (matchesSearch && matchesRegion && matchesCategory) {
//                filteredList.add(attraction);
//            }
//        }
//        attractionObservableList.setAll(filteredList);
//    }
//
//    // --- Event Handlers ---
//    @FXML
//    private void handleLogout() {
//        try {
//            mainApp.showLoginScreen();
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Failed to load login screen", e);
//            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not return to login screen.");
//        }
//    }
//
//    @FXML
//    private void handleEmergencyReport() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/emergency-dialog.fxml"));
//            Parent root = loader.load();
//
//            EmergencyDialogController controller = loader.getController();
//            controller.setMainApp(mainApp);
//            controller.setReporter(loggedInUser); // Pass the logged-in tourist
//
//            Stage dialogStage = new Stage();
//            dialogStage.setTitle(LanguageManager.getString("emergency.dialog.title"));
//            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(mainApp.getPrimaryStage());
//            Scene scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
//            dialogStage.setScene(scene);
//            dialogStage.setResizable(false); // Often good for dialogs
//            dialogStage.showAndWait(); // Wait for dialog to close
//
//            // Optionally refresh data if report was submitted
//            // For now, we don't need to refresh anything on the tourist dashboard specifically
//
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Failed to load emergency dialog", e);
//            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open emergency report dialog.");
//        }
//    }
//
//    @FXML
//    private void handleLanguageSwitch() {
//        // Toggle language
//        if (Locale.ENGLISH.equals(LanguageManager.getCurrentLocale())) {
//            LanguageManager.setLocale(new Locale("np"));
//        } else {
//            LanguageManager.setLocale(Locale.ENGLISH);
//        }
//        // Refresh UI labels (simplified - in a full app, you might reload the FXML or bind properties)
//        welcomeLabel.setText(LanguageManager.getString("tourist.dashboard.welcome").replace("{0}", loggedInUser.getName()));
//        languageButton.setText(LanguageManager.getSwitchLanguageDisplayName());
//        // A more robust way would involve rebinding all text properties or reloading the scene.
//        // For now, we'll just update the key ones.
//        showAlert(Alert.AlertType.INFORMATION, "Language Switched", "Language has been switched to " + LanguageManager.getCurrentLanguageDisplayName());
//        // In a real app, you'd likely want to reload the entire UI or use property binding extensively.
//    }
//
//    private void handleBookAttraction(Attraction attraction) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/booking-dialog.fxml"));
//            Parent root = loader.load();
//
//            BookingDialogController controller = loader.getController();
//            controller.setMainApp(mainApp);
//            controller.setTourist(loggedInUser);
//            controller.setAttraction(attraction);
//
//            Stage dialogStage = new Stage();
//            dialogStage.setTitle(LanguageManager.getString("booking.dialog.title").replace("{0}", attraction.getName()));
//            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(mainApp.getPrimaryStage());
//            Scene scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
//            dialogStage.setScene(scene);
//            dialogStage.setResizable(false);
//            dialogStage.showAndWait(); // Wait for dialog to close
//
//            // Refresh bookings table after dialog closes (in case a booking was made)
//            loadBookings();
//            loadAnalytics(); // Update analytics if a new booking was made
//
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Failed to load booking dialog for attraction: " + attraction.getId(), e);
//            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open booking dialog.");
//        }
//    }
//
//    // --- Utility ---
//    private void showAlert(Alert.AlertType alertType, String title, String message) {
//        Alert alert = new Alert(alertType);
//        alert.setTitle(title);
//        alert.setHeaderText(null);
//        alert.setContentText(message);
//        alert.showAndWait();
//    }
//}
//
