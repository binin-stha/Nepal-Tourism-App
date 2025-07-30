package com.example.nepaltourism.controllers;

import com.example.nepaltourism.Main;
import com.example.nepaltourism.models.*;
import com.example.nepaltourism.utils.LanguageManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Controller for admin-dashboard.fxml.
 * Handles all admin functionalities including managing users, attractions, bookings, and analytics.
 */
public class AdminDashboardController {

    private static final Logger logger = Logger.getLogger(AdminDashboardController.class.getName());

    // --- Top Navigation ---
    @FXML private Label welcomeLabel;
    @FXML private Button logoutButton;

    // --- Dashboard Tab (KPIs) ---
    @FXML private Label totalTouristsLabel;
    @FXML private Label activeGuidesLabel;
    @FXML private Label totalBookingsLabel;
    @FXML private Label totalRevenueLabel;

    // --- Dashboard Tab (Charts) ---
    @FXML private javafx.scene.chart.LineChart<String, Number> revenueChart;
    @FXML private javafx.scene.chart.CategoryAxis revenueChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis revenueChartYAxis;
    @FXML private javafx.scene.chart.BarChart<String, Number> attractionsChart;
    @FXML private javafx.scene.chart.CategoryAxis attractionsChartXAxis;
    @FXML private javafx.scene.chart.NumberAxis attractionsChartYAxis;
    @FXML private javafx.scene.chart.PieChart regionalChart;

    // --- Tourists Tab ---
    @FXML private Button addTouristButton;
    @FXML private TableView<Tourist> touristsTable;
    @FXML private TableColumn<Tourist, String> touristIdColumn;
    @FXML private TableColumn<Tourist, String> touristNameColumn;
    @FXML private TableColumn<Tourist, String> touristEmailColumn;
    @FXML private TableColumn<Tourist, String> touristPhoneColumn;
    @FXML private TableColumn<Tourist, Double> touristSpentColumn;
    @FXML private TableColumn<Tourist, Integer> touristTripsColumn;
    @FXML private TableColumn<Tourist, Tourist> touristActionsColumn;

    // --- Guides Tab ---
    @FXML private Button addGuideButton;
    @FXML private TableView<Guide> guidesTable;
    @FXML private TableColumn<Guide, String> guideIdColumn;
    @FXML private TableColumn<Guide, String> guideNameColumn;
    @FXML private TableColumn<Guide, String> guideEmailColumn;
    @FXML private TableColumn<Guide, String> guidePhoneColumn;
    @FXML private TableColumn<Guide, String> guideAreaColumn;
    @FXML private TableColumn<Guide, Double> guideRatingColumn;
    @FXML private TableColumn<Guide, String> guideStatusColumn;
    @FXML private TableColumn<Guide, Guide> guideActionsColumn;

    // --- Attractions Tab ---
    @FXML private Button addAttractionButton;
    @FXML private TableView<Attraction> attractionsTable;
    @FXML private TableColumn<Attraction, String> attractionIdColumn;
    @FXML private TableColumn<Attraction, String> attractionNameColumn;
    @FXML private TableColumn<Attraction, String> attractionRegionColumn;
    @FXML private TableColumn<Attraction, String> attractionCategoryColumn;
    @FXML private TableColumn<Attraction, String> attractionDifficultyColumn;
    @FXML private TableColumn<Attraction, Integer> attractionDurationColumn;
    @FXML private TableColumn<Attraction, Double> attractionPriceColumn;
    @FXML private TableColumn<Attraction, Double> attractionRatingColumn;
    @FXML private TableColumn<Attraction, Boolean> attractionStatusColumn;
    @FXML private TableColumn<Attraction, Attraction> attractionActionsColumn;

    // --- Bookings Tab ---
    @FXML private ComboBox<String> bookingStatusFilter;
    @FXML private Button exportBookingsButton;
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> bookingIdColumn;
    @FXML private TableColumn<Booking, String> bookingTouristColumn;
    @FXML private TableColumn<Booking, String> bookingGuideColumn;
    @FXML private TableColumn<Booking, String> bookingAttractionColumn;
    @FXML private TableColumn<Booking, LocalDate> bookingDateColumn;
    @FXML private TableColumn<Booking, String> bookingStatusColumn;
    @FXML private TableColumn<Booking, Double> bookingPriceColumn;
    @FXML private TableColumn<Booking, Booking> bookingActionsColumn;

    // --- Data ---
    private Main mainApp;
    private Admin loggedInUser;
    private ObservableList<Tourist> touristObservableList;
    private ObservableList<Guide> guideObservableList;
    private ObservableList<Attraction> attractionObservableList;
    private ObservableList<Booking> bookingObservableList;
    private FilteredList<Booking> filteredBookingList;

    @FXML
    private void initialize() {
        setupTopNavigation();
        setupTouristsTab();
        setupGuidesTab();
        setupAttractionsTab();
        setupBookingsTab();
    }

    private void setupTopNavigation() {
        logoutButton.setOnAction(event -> handleLogout());
    }

    // ------------------------- TOURISTS TAB -------------------------
    private void setupTouristsTab() {
        touristIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        touristNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        touristEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        touristPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));

        touristSpentColumn.setCellValueFactory(cellData -> {
            String touristId = cellData.getValue().getId();
            double totalSpent = mainApp.getBookings().stream()
                    .filter(b -> b.getTouristId().equals(touristId))
                    .mapToDouble(Booking::getTotalPrice)
                    .sum();
            return new javafx.beans.property.SimpleDoubleProperty(totalSpent).asObject();
        });

        touristTripsColumn.setCellValueFactory(cellData -> {
            String touristId = cellData.getValue().getId();
            long totalTrips = mainApp.getBookings().stream()
                    .filter(b -> b.getTouristId().equals(touristId))
                    .count();
            return new javafx.beans.property.SimpleIntegerProperty((int) totalTrips).asObject();
        });

        touristActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button(LanguageManager.getString("button.edit"));
            private final Button deleteButton = new Button(LanguageManager.getString("button.delete"));

            {
                editButton.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));
                deleteButton.getStyleClass().add("button-danger");
            }

            @Override
            protected void updateItem(Tourist item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : new javafx.scene.layout.HBox(5, editButton, deleteButton));
            }
        });

        touristObservableList = FXCollections.observableArrayList();
        touristsTable.setItems(touristObservableList);
        addTouristButton.setOnAction(event -> handleAddUser("Tourist"));
    }

    // ------------------------- GUIDES TAB -------------------------
    private void setupGuidesTab() {
        guideIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        guideNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        guideEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        guidePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        guideAreaColumn.setCellValueFactory(new PropertyValueFactory<>("tourArea"));
        guideRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        guideStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().isAvailable() ? "Available" : "Unavailable"
        ));

        guideActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button(LanguageManager.getString("button.edit"));
            private final Button deleteButton = new Button(LanguageManager.getString("button.delete"));

            {
                editButton.setOnAction(event -> handleEditUser(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteUser(getTableView().getItems().get(getIndex())));
                deleteButton.getStyleClass().add("button-danger");
            }

            @Override
            protected void updateItem(Guide item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : new javafx.scene.layout.HBox(5, editButton, deleteButton));
            }
        });

        guideObservableList = FXCollections.observableArrayList();
        guidesTable.setItems(guideObservableList);
        addGuideButton.setOnAction(event -> handleAddUser("Guide"));
    }

    // ------------------------- ATTRACTIONS TAB -------------------------
    private void setupAttractionsTab() {
        attractionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        attractionNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        attractionRegionColumn.setCellValueFactory(new PropertyValueFactory<>("region"));
        attractionCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        attractionDifficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
        attractionDurationColumn.setCellValueFactory(new PropertyValueFactory<>("durationDays"));
        attractionPriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceUSD"));
        attractionRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));

        attractionStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isActive()));
        attractionStatusColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : (item ? "Active" : "Inactive"));
            }
        });

        attractionActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button(LanguageManager.getString("button.edit"));
            private final Button deleteButton = new Button(LanguageManager.getString("button.delete"));

            {
                editButton.setOnAction(event -> handleEditAttraction(getTableView().getItems().get(getIndex())));
                deleteButton.setOnAction(event -> handleDeleteAttraction(getTableView().getItems().get(getIndex())));
                deleteButton.getStyleClass().add("button-danger");
            }

            @Override
            protected void updateItem(Attraction item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : new javafx.scene.layout.HBox(5, editButton, deleteButton));
            }
        });

        attractionObservableList = FXCollections.observableArrayList();
        attractionsTable.setItems(attractionObservableList);
        addAttractionButton.setOnAction(event -> handleAddAttraction());
    }

    // ------------------------- BOOKINGS TAB -------------------------
    private void setupBookingsTab() {
        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        bookingTouristColumn.setCellValueFactory(cellData -> {
            String touristId = cellData.getValue().getTouristId();
            Optional<User> touristOpt = mainApp.getUsers().stream()
                    .filter(u -> u instanceof Tourist && u.getId().equals(touristId))
                    .findFirst();
            return new javafx.beans.property.SimpleStringProperty(
                    touristOpt.map(User::getName).orElse("Unknown Tourist")
            );
        });

        bookingGuideColumn.setCellValueFactory(cellData -> {
            String guideId = cellData.getValue().getGuideId();
            if (guideId == null || guideId.isEmpty()) {
                return new javafx.beans.property.SimpleStringProperty("Not Assigned");
            }
            Optional<User> guideOpt = mainApp.getUsers().stream()
                    .filter(u -> u instanceof Guide && u.getId().equals(guideId))
                    .findFirst();
            return new javafx.beans.property.SimpleStringProperty(
                    guideOpt.map(User::getName).orElse("Unknown Guide")
            );
        });

        bookingAttractionColumn.setCellValueFactory(cellData -> {
            String attractionId = cellData.getValue().getAttractionId();
            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
                    .filter(a -> a.getId().equals(attractionId)).findFirst();
            return new javafx.beans.property.SimpleStringProperty(
                    attractionOpt.map(Attraction::getName).orElse("Unknown Attraction")
            );
        });

        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("tourDate"));
        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        bookingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));

        bookingActionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button viewButton = new Button(LanguageManager.getString("button.view"));
            private final Button updateStatusButton = new Button(LanguageManager.getString("button.update"));

            {
                viewButton.setOnAction(event -> {
                    Booking booking = getTableView().getItems().get(getIndex());
                    showAlert(Alert.AlertType.INFORMATION, "Booking Details",
                            "Booking ID: " + booking.getId() + "\n" +
                                    "Tourist: " + booking.getTouristId() + "\n" +
                                    "Guide: " + (booking.getGuideId() != null ? booking.getGuideId() : "N/A") + "\n" +
                                    "Attraction: " + booking.getAttractionId() + "\n" +
                                    "Date: " + booking.getTourDate() + "\n" +
                                    "People: " + booking.getNumberOfPeople() + "\n" +
                                    "Requests: " + booking.getSpecialRequests() + "\n" +
                                    "Status: " + booking.getStatus() + "\n" +
                                    "Total Price: $" + String.format("%.2f", booking.getTotalPrice()) + "\n" +
                                    "Discount: " + booking.getDiscountApplied());
                });

                updateStatusButton.setOnAction(event -> handleUpdateBookingStatus(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Booking item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty || item == null ? null : new javafx.scene.layout.HBox(5, viewButton, updateStatusButton));
            }
        });

        bookingObservableList = FXCollections.observableArrayList();
        filteredBookingList = new FilteredList<>(bookingObservableList, p -> true);
        bookingsTable.setItems(filteredBookingList);

        bookingStatusFilter.getItems().addAll("All", "Pending", "Confirmed", "Completed", "Cancelled");
        bookingStatusFilter.setValue("All");
        bookingStatusFilter.setOnAction(event -> filterBookings());

        exportBookingsButton.setOnAction(event -> handleExportBookings());
    }

    // ------------------------- SETUP METHODS -------------------------
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setLoggedInUser(Admin user) {
        this.loggedInUser = user;
        welcomeLabel.setText(LanguageManager.getString("admin.dashboard.welcome").replace("{0}", user.getName()));
        setupDashboard();
    }

    public void setupDashboard() {
        if (mainApp == null || loggedInUser == null) {
            logger.warning("Main app or logged-in user not set before calling setupDashboard()");
            return;
        }
        loadTourists();
        loadGuides();
        loadAttractions();
        loadBookings();
        loadDashboardData();
    }

    // ------------------------- DATA LOADING -------------------------
//    private void loadTourists() {
//        List<Tourist> tourists = mainApp.getUsers().stream()
//                .filter(u -> u instanceof Tourist)
//                .map(u -> (Tourist) u)
//                .collect(Collectors.toList());
//        logger.info("Loading Tourists: Found " + tourists.size()); // Add this log
//        for(Tourist t : tourists) {
//            logger.info("Loading Tourist: " + t); // Log details
//        }
//        touristObservableList.setAll(tourists);
//        // Consider adding touristsTable.refresh(); if UI isn't updating
//    }
    // Inside AdminDashboardController.java
    private void loadTourists() {
        // --- ADD LOGGING TO DEBUG ---
        logger.info("AdminDashboardController: Starting to load tourists...");
        if (mainApp == null) {
            logger.severe("AdminDashboardController: mainApp is NULL in loadTourists!");
            return; // Critical error
        }
        if (mainApp.getUsers() == null) {
            logger.warning("AdminDashboardController: mainApp.getUsers() returned NULL");
            touristObservableList.clear();
            return;
        }
        // --- END ADD LOGGING ---

        List<Tourist> tourists = mainApp.getUsers().stream()
                .filter(u -> u instanceof Tourist)
                .map(u -> (Tourist) u)
                .collect(Collectors.toList());

        // --- ADD MORE LOGGING ---
        logger.info("AdminDashboardController: Found " + tourists.size() + " tourists in mainApp data.");
        for (Tourist t : tourists) {
            logger.info("AdminDashboardController: Loading Tourist -> ID: '" + t.getId() +
                    "', Name: '" + t.getName() +
                    "', Email: '" + t.getEmail() +
                    "', Phone: '" + t.getPhone() + "'");
            // Add this check to see if any fields are unexpectedly null
            if (t.getName() == null || t.getEmail() == null) {
                logger.warning("AdminDashboardController: Tourist " + t.getId() + " has NULL name or email!");
            }
        }
        // --- END ADD MORE LOGGING ---

        touristObservableList.setAll(tourists); // This should trigger a UI update
        // Optional, sometimes needed if UI doesn't refresh automatically
        // touristsTable.refresh();
        logger.info("AdminDashboardController: Tourist list updated in TableView.");
    }

    public void loadGuides() {
        List<Guide> guides = mainApp.getUsers().stream()
                .filter(u -> u instanceof Guide)
                .map(u -> (Guide) u)
                .collect(Collectors.toList());

        logger.info("Loading Guides: " + guides.size());
        for (Guide g : guides) {
            logger.info("Guide: ID=" + g.getId() + ", Name=" + g.getName() + ", Email=" + g.getEmail() +
                    ", TourArea=" + g.getTourArea() + ", Rating=" + g.getRating());
        }
        guideObservableList.setAll(guides);
        guidesTable.refresh();
    }

    public void loadAttractions() {
        List<Attraction> attractions = mainApp.getAttractions();
        logger.info("Loading Attractions: " + attractions.size());
        for (Attraction a : attractions) {
            logger.info("Attraction: ID=" + a.getId() + ", Name=" + a.getName() + ", Region=" + a.getRegion() +
                    ", Category=" + a.getCategory() + ", Active=" + a.isActive());
        }
        attractionObservableList.setAll(attractions);
        attractionsTable.refresh();
    }

    private void loadBookings() {
        bookingObservableList.setAll(mainApp.getBookings());
    }

    // ------------------------- DASHBOARD DATA -------------------------
    private void loadDashboardData() {
        long totalTourists = mainApp.getUsers().stream().filter(u -> u instanceof Tourist).count();
        long activeGuides = mainApp.getUsers().stream().filter(u -> u instanceof Guide && ((Guide) u).isAvailable()).count();
        long totalBookings = mainApp.getBookings().size();
        double totalRevenue = mainApp.getBookings().stream().mapToDouble(Booking::getTotalPrice).sum();

        totalTouristsLabel.setText(String.valueOf(totalTourists));
        activeGuidesLabel.setText(String.valueOf(activeGuides));
        totalBookingsLabel.setText(String.valueOf(totalBookings));
        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));

        updateRevenueChart();
        updateAttractionsChart();
        updateRegionalChart();
    }

    // ------------------------- CHARTS -------------------------
    private void updateRevenueChart() {
        revenueChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Monthly Revenue");

        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        for (int i = 5; i >= 0; i--) {
            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
            monthlyRevenue.put(monthKey, 0.0);
        }

        for (Booking booking : mainApp.getBookings()) {
            if (booking.getTourDate() != null) {
                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
                monthlyRevenue.merge(monthKey, booking.getTotalPrice(), Double::sum);
            }
        }

        for (Map.Entry<String, Double> entry : monthlyRevenue.entrySet()) {
            series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        revenueChart.getData().add(series);
    }

    private void updateAttractionsChart() {
        attractionsChart.getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Bookings");

        Map<String, Long> attractionBookings = mainApp.getBookings().stream()
                .collect(Collectors.groupingBy(Booking::getAttractionId, Collectors.counting()));

        attractionBookings.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> {
                    String attractionId = entry.getKey();
                    Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
                            .filter(a -> a.getId().equals(attractionId)).findFirst();
                    String name = attractionOpt.map(Attraction::getName).orElse("Unknown (" + attractionId + ")");
                    series.getData().add(new javafx.scene.chart.XYChart.Data<>(name, entry.getValue()));
                });

        attractionsChart.getData().add(series);
    }

    private void updateRegionalChart() {
        regionalChart.getData().clear();
        Map<String, Long> regionBookings = new HashMap<>();

        for (Booking booking : mainApp.getBookings()) {
            String attractionId = booking.getAttractionId();
            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
                    .filter(a -> a.getId().equals(attractionId)).findFirst();
            String region = attractionOpt.map(Attraction::getRegion).orElse("Unknown Region");
            regionBookings.merge(region, 1L, Long::sum);
        }

        for (Map.Entry<String, Long> entry : regionBookings.entrySet()) {
            regionalChart.getData().add(new javafx.scene.chart.PieChart.Data(entry.getKey(), entry.getValue()));
        }
    }

    // ------------------------- FILTER -------------------------
    private void filterBookings() {
        String selectedStatus = bookingStatusFilter.getValue();
        if ("All".equals(selectedStatus) || selectedStatus == null) {
            filteredBookingList.setPredicate(booking -> true);
        } else {
            filteredBookingList.setPredicate(booking -> selectedStatus.equals(booking.getStatus()));
        }
    }

    // ------------------------- EVENT HANDLERS -------------------------
    @FXML
    private void handleLogout() {
        try {
            mainApp.showLoginScreen();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to load login screen", e);
            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not return to login screen.");
        }
    }

    private void handleAddUser(String userType) {
        openUserFormDialog(null, userType);
    }

    private void handleEditUser(User user) {
        openUserFormDialog(user, user.getUserType());
    }

    private void openUserFormDialog(User user, String userType) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-form-dialog.fxml"));
            Parent root = loader.load();

            UserFormDialogController controller = loader.getController();
            controller.setMainApp(mainApp);
            controller.setUser(user);
            controller.setUserType(userType);

            Stage dialogStage = new Stage();
            String titleKey = (user == null) ? "user.form.dialog.title.add" : "user.form.dialog.title.edit";
            dialogStage.setTitle(LanguageManager.getString(titleKey));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            if ("Tourist".equals(userType)) {
                loadTourists();
            } else if ("Guide".equals(userType)) {
                loadGuides();
            }
            loadDashboardData();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load user form dialog", e);
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open user form dialog.");
        }
    }

    @FXML
    private void handleDeleteUser(User user) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete user " + user.getName() + " (" + user.getId() + ")?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            mainApp.getUsers().remove(user);
            mainApp.saveAllData();
            if (user instanceof Tourist) {
                loadTourists();
            } else if (user instanceof Guide) {
                loadGuides();
            }
            loadDashboardData();
            showAlert(Alert.AlertType.INFORMATION, "User Deleted", "User " + user.getName() + " has been deleted.");
        }
    }

    @FXML
    private void handleAddAttraction() {
        openAttractionFormDialog(null);
    }

    @FXML
    private void handleEditAttraction(Attraction attraction) {
        openAttractionFormDialog(attraction);
    }

    private void openAttractionFormDialog(Attraction attraction) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/attraction-form-dialog.fxml"));
            Parent root = loader.load();

            AttractionFormDialogController controller = loader.getController();
            controller.setMainApp(mainApp);
            controller.setAttraction(attraction);

            Stage dialogStage = new Stage();
            String titleKey = (attraction == null) ? "attraction.form.dialog.title.add" : "attraction.form.dialog.title.edit";
            dialogStage.setTitle(LanguageManager.getString(titleKey));
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(mainApp.getPrimaryStage());
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogStage.setScene(scene);
            dialogStage.setResizable(false);
            dialogStage.showAndWait();

            loadAttractions();
            loadDashboardData();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load attraction form dialog", e);
            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open attraction form dialog.");
        }
    }

    private void handleDeleteAttraction(Attraction attraction) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Are you sure you want to delete attraction " + attraction.getName() + " (" + attraction.getId() + ")?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            mainApp.getAttractions().remove(attraction);
            mainApp.saveAllData();
            loadAttractions();
            loadDashboardData();
            showAlert(Alert.AlertType.INFORMATION, "Attraction Deleted", "Attraction " + attraction.getName() + " has been deleted.");
        }
    }

    private void handleUpdateBookingStatus(Booking booking) {
        ChoiceDialog<String> dialog = new ChoiceDialog<>(booking.getStatus(), "Pending", "Confirmed", "Completed", "Cancelled");
        dialog.setTitle("Update Booking Status");
        dialog.setHeaderText("Change status for booking " + booking.getId());
        dialog.setContentText("Select new status:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newStatus = result.get();
            if (!newStatus.equals(booking.getStatus())) {
                booking.setStatus(newStatus);
                mainApp.saveAllData();
                loadBookings();
                loadDashboardData();
                showAlert(Alert.AlertType.INFORMATION, "Status Updated", "Booking status updated to " + newStatus);
            }
        }
    }

    private void handleExportBookings() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Bookings");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("bookings_export.csv");

        Stage stage = (Stage) exportBookingsButton.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Booking ID,Tourist ID,Guide ID,Attraction ID,Tour Date,Number of People,Special Requests,Status,Total Price,Discount Applied\n");

                for (Booking booking : filteredBookingList) {
                    writer.append(escapeCSV(booking.getId())).append(",")
                            .append(escapeCSV(booking.getTouristId())).append(",")
                            .append(escapeCSV(booking.getGuideId() != null ? booking.getGuideId() : "")).append(",")
                            .append(escapeCSV(booking.getAttractionId())).append(",")
                            .append(booking.getTourDate() != null ? booking.getTourDate().toString() : "").append(",")
                            .append(String.valueOf(booking.getNumberOfPeople())).append(",")
                            .append(escapeCSV(booking.getSpecialRequests())).append(",")
                            .append(escapeCSV(booking.getStatus())).append(",")
                            .append(String.format("%.2f", booking.getTotalPrice())).append(",")
                            .append(escapeCSV(booking.getDiscountApplied())).append("\n");
                }
                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Bookings exported to " + file.getAbsolutePath());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to export bookings", e);
                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not export bookings to file.");
            }
        }
    }

    private String escapeCSV(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    // ------------------------- UTIL -------------------------
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}


//  **********************************************************************************************

//package com.example.nepaltourism.controllers;
//
//import com.example.nepaltourism.Main;
//import com.example.nepaltourism.models.*;
//import com.example.nepaltourism.utils.LanguageManager;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.collections.transformation.FilteredList;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.Scene;
//import javafx.scene.control.*;
//import javafx.scene.control.cell.PropertyValueFactory;
//import javafx.stage.FileChooser;
//import javafx.stage.Modality;
//import javafx.stage.Stage;
//
//import java.io.File;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.logging.Logger;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//
///**
// * Controller class for the admin-dashboard.fxml view.
// */
//public class AdminDashboardController {
//
//    private static final Logger logger = Logger.getLogger(AdminDashboardController.class.getName());
//
//    // --- Top Navigation ---
//    @FXML
//    private Label welcomeLabel;
//    @FXML
//    private Button logoutButton;
//
//    // --- Dashboard Tab (KPIs) ---
//    @FXML
//    private Label totalTouristsLabel;
//    @FXML
//    private Label activeGuidesLabel;
//    @FXML
//    private Label totalBookingsLabel;
//    @FXML
//    private Label totalRevenueLabel;
//
//    // --- Dashboard Tab (Charts) ---
//    @FXML
//    private javafx.scene.chart.LineChart<String, Number> revenueChart;
//    @FXML
//    private javafx.scene.chart.CategoryAxis revenueChartXAxis;
//    @FXML
//    private javafx.scene.chart.NumberAxis revenueChartYAxis;
//    @FXML
//    private javafx.scene.chart.BarChart<String, Number> attractionsChart;
//    @FXML
//    private javafx.scene.chart.CategoryAxis attractionsChartXAxis;
//    @FXML
//    private javafx.scene.chart.NumberAxis attractionsChartYAxis;
//    @FXML
//    private javafx.scene.chart.PieChart regionalChart;
//
//    // --- Tourists Tab ---
//    @FXML
//    private Button addTouristButton;
//    @FXML
//    private TableView<Tourist> touristsTable;
//    @FXML
//    private TableColumn<Tourist, String> touristIdColumn;
//    @FXML
//    private TableColumn<Tourist, String> touristNameColumn;
//    @FXML
//    private TableColumn<Tourist, String> touristEmailColumn;
//    @FXML
//    private TableColumn<Tourist, String> touristPhoneColumn;
//    @FXML
//    private TableColumn<Tourist, Double> touristSpentColumn;
//    @FXML
//    private TableColumn<Tourist, Integer> touristTripsColumn;
//    @FXML
//    private TableColumn<Tourist, Tourist> touristActionsColumn; // For buttons
//
//    // --- Guides Tab ---
//    @FXML
//    private Button addGuideButton;
//    @FXML
//    private TableView<Guide> guidesTable;
//    @FXML
//    private TableColumn<Guide, String> guideIdColumn;
//    @FXML
//    private TableColumn<Guide, String> guideNameColumn;
//    @FXML
//    private TableColumn<Guide, String> guideEmailColumn;
//    @FXML
//    private TableColumn<Guide, String> guidePhoneColumn;
//    @FXML
//    private TableColumn<Guide, String> guideAreaColumn;
//    @FXML
//    private TableColumn<Guide, Double> guideRatingColumn;
//    @FXML
//    private TableColumn<Guide, String> guideStatusColumn;
//    @FXML
//    private TableColumn<Guide, Guide> guideActionsColumn; // For buttons
//
//    // --- Attractions Tab ---
//    @FXML
//    private Button addAttractionButton;
//    @FXML
//    private TableView<Attraction> attractionsTable;
//    @FXML
//    private TableColumn<Attraction, String> attractionIdColumn;
//    @FXML
//    private TableColumn<Attraction, String> attractionNameColumn;
//    @FXML
//    private TableColumn<Attraction, String> attractionRegionColumn;
//    @FXML
//    private TableColumn<Attraction, String> attractionCategoryColumn;
//    @FXML
//    private TableColumn<Attraction, String> attractionDifficultyColumn;
//    @FXML
//    private TableColumn<Attraction, Integer> attractionDurationColumn;
//    @FXML
//    private TableColumn<Attraction, Double> attractionPriceColumn;
//    @FXML
//    private TableColumn<Attraction, Double> attractionRatingColumn;
//    @FXML
//    private TableColumn<Attraction, Boolean> attractionStatusColumn;
//    @FXML
//    private TableColumn<Attraction, Attraction> attractionActionsColumn; // For buttons
//
//    // --- Bookings Tab ---
//    @FXML
//    private ComboBox<String> bookingStatusFilter;
//    @FXML
//    private Button exportBookingsButton;
//    @FXML
//    private TableView<Booking> bookingsTable;
//    @FXML
//    private TableColumn<Booking, String> bookingIdColumn;
//    @FXML
//    private TableColumn<Booking, String> bookingTouristColumn;
//    @FXML
//    private TableColumn<Booking, String> bookingGuideColumn;
//    @FXML
//    private TableColumn<Booking, String> bookingAttractionColumn;
//    @FXML
//    private TableColumn<Booking, LocalDate> bookingDateColumn;
//    @FXML
//    private TableColumn<Booking, String> bookingStatusColumn;
//    @FXML
//    private TableColumn<Booking, Double> bookingPriceColumn;
//    @FXML
//    private TableColumn<Booking, Booking> bookingActionsColumn; // For buttons
//
//    // --- Data ---
//    private Main mainApp;
//    private Admin loggedInUser;
//    private ObservableList<Tourist> touristObservableList;
//    private ObservableList<Guide> guideObservableList;
//    private ObservableList<Attraction> attractionObservableList;
//    private ObservableList<Booking> bookingObservableList;
//    private FilteredList<Booking> filteredBookingList;
//
//    /**
//     * Initializes the controller class. This method is automatically called
//     * after the fxml file has been loaded.
//     */
//    @FXML
//    private void initialize() {
//        setupTopNavigation();
//        setupDashboardTab();
//        setupTouristsTab();
//        setupGuidesTab();
//        setupAttractionsTab();
//        setupBookingsTab();
//    }
//
//    private void setupTopNavigation() {
//        logoutButton.setOnAction(event -> handleLogout());
//    }
//
//    private void setupDashboardTab() {
//        // KPIs and charts will be updated when user is set and data loads
//    }
//
//    private void setupTouristsTab() {
//        // Set up table columns
//        touristIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        touristNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
//        touristEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
//        touristPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
//        touristSpentColumn.setCellValueFactory(cellData -> {
//            String touristId = cellData.getValue().getId();
//            double totalSpent = mainApp.getBookings().stream()
//                    .filter(b -> b.getTouristId().equals(touristId))
//                    .mapToDouble(Booking::getTotalPrice)
//                    .sum();
//            return new javafx.beans.property.SimpleDoubleProperty(totalSpent).asObject();
//        });
//        touristTripsColumn.setCellValueFactory(cellData -> {
//            String touristId = cellData.getValue().getId();
//            long totalTrips = mainApp.getBookings().stream()
//                    .filter(b -> b.getTouristId().equals(touristId))
//                    .count();
//            return new javafx.beans.property.SimpleIntegerProperty((int)totalTrips).asObject();
//        });
//
//        // Actions column for tourists (e.g., Edit, Delete)
//        touristActionsColumn.setCellFactory(param -> new TableCell<>() {
//            private final Button editButton = new Button(LanguageManager.getString("button.edit"));
//            private final Button deleteButton = new Button(LanguageManager.getString("button.delete"));
//
//            {
//                editButton.setOnAction(event -> {
//                    Tourist tourist = getTableView().getItems().get(getIndex());
//                    handleEditUser(tourist);
//                });
//                deleteButton.setOnAction(event -> {
//                    Tourist tourist = getTableView().getItems().get(getIndex());
//                    handleDeleteUser(tourist);
//                });
//                deleteButton.getStyleClass().add("button-danger"); // Add style class for red button
//            }
//
//            @Override
//            protected void updateItem(Tourist item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setGraphic(null);
//                } else {
//                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editButton, deleteButton);
//                    setGraphic(buttons);
//                }
//            }
//        });
//
//        // Initialize tourist list
//        touristObservableList = FXCollections.observableArrayList();
//        touristsTable.setItems(touristObservableList);
//
//        // Add Tourist button
//        addTouristButton.setOnAction(event -> handleAddUser("Tourist"));
//    }
//
//    private void setupGuidesTab() {
//        // Set up table columns
//        guideIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        guideNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
//        guideEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
//        guidePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
//        guideAreaColumn.setCellValueFactory(new PropertyValueFactory<>("tourArea"));
//        guideRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
//        guideStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(
//                cellData.getValue().isAvailable() ? "Available" : "Unavailable"
//        ));
//
//        // Actions column for guides (e.g., Edit, Delete)
//        guideActionsColumn.setCellFactory(param -> new TableCell<>() {
//            private final Button editButton = new Button(LanguageManager.getString("button.edit"));
//            private final Button deleteButton = new Button(LanguageManager.getString("button.delete"));
//
//            {
//                editButton.setOnAction(event -> {
//                    Guide guide = getTableView().getItems().get(getIndex());
//                    handleEditUser(guide);
//                });
//                deleteButton.setOnAction(event -> {
//                    Guide guide = getTableView().getItems().get(getIndex());
//                    handleDeleteUser(guide);
//                });
//                deleteButton.getStyleClass().add("button-danger");
//            }
//
//            @Override
//            protected void updateItem(Guide item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setGraphic(null);
//                } else {
//                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editButton, deleteButton);
//                    setGraphic(buttons);
//                }
//            }
//        });
//
//        // Initialize guide list
//        guideObservableList = FXCollections.observableArrayList();
//        guidesTable.setItems(guideObservableList);
//
//        // Add Guide button
//        addGuideButton.setOnAction(event -> handleAddUser("Guide"));
//    }
//
//    private void setupAttractionsTab() {
//        // Set up table columns
//        attractionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        attractionNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
//        attractionRegionColumn.setCellValueFactory(new PropertyValueFactory<>("region"));
//        attractionCategoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
//        attractionDifficultyColumn.setCellValueFactory(new PropertyValueFactory<>("difficulty"));
//        attractionDurationColumn.setCellValueFactory(new PropertyValueFactory<>("durationDays"));
//        attractionPriceColumn.setCellValueFactory(new PropertyValueFactory<>("priceUSD"));
//        attractionRatingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
//        attractionStatusColumn.setCellValueFactory(cellData -> new javafx.beans.property.SimpleBooleanProperty(cellData.getValue().isActive()));
//
//        // Custom cell factory for status to show text
//        attractionStatusColumn.setCellFactory(tc -> new TableCell<>() {
//            @Override
//            protected void updateItem(Boolean item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setText(null);
//                } else {
//                    setText(item ? "Active" : "Inactive");
//                }
//            }
//        });
//
//        // Actions column for attractions (e.g., Edit, Delete)
//        attractionActionsColumn.setCellFactory(param -> new TableCell<>() {
//            private final Button editButton = new Button(LanguageManager.getString("button.edit"));
//            private final Button deleteButton = new Button(LanguageManager.getString("button.delete"));
//
//            {
//                editButton.setOnAction(event -> {
//                    Attraction attraction = getTableView().getItems().get(getIndex());
//                    handleEditAttraction(attraction);
//                });
//                deleteButton.setOnAction(event -> {
//                    Attraction attraction = getTableView().getItems().get(getIndex());
//                    handleDeleteAttraction(attraction);
//                });
//                deleteButton.getStyleClass().add("button-danger");
//            }
//
//            @Override
//            protected void updateItem(Attraction item, boolean empty) {
//                super.updateItem(item, empty);
//                if (empty || item == null) {
//                    setGraphic(null);
//                } else {
//                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, editButton, deleteButton);
//                    setGraphic(buttons);
//                }
//            }
//        });
//
//        // Initialize attraction list
//        attractionObservableList = FXCollections.observableArrayList();
//        attractionsTable.setItems(attractionObservableList);
//
//        // Add Attraction button
//        addAttractionButton.setOnAction(event -> handleAddAttraction());
//    }
//
//    private void setupBookingsTab() {
//        // Set up table columns
//        bookingIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
//        bookingTouristColumn.setCellValueFactory(cellData -> {
//            String touristId = cellData.getValue().getTouristId();
//            Optional<User> touristOpt = mainApp.getUsers().stream()
//                    .filter(u -> u instanceof Tourist && u.getId().equals(touristId))
//                    .findFirst();
//            return new javafx.beans.property.SimpleStringProperty(
//                    touristOpt.map(User::getName).orElse("Unknown Tourist")
//            );
//        });
//        bookingGuideColumn.setCellValueFactory(cellData -> {
//            String guideId = cellData.getValue().getGuideId();
//            if (guideId == null || guideId.isEmpty()) {
//                return new javafx.beans.property.SimpleStringProperty("Not Assigned");
//            }
//            Optional<User> guideOpt = mainApp.getUsers().stream()
//                    .filter(u -> u instanceof Guide && u.getId().equals(guideId))
//                    .findFirst();
//            return new javafx.beans.property.SimpleStringProperty(
//                    guideOpt.map(User::getName).orElse("Unknown Guide")
//            );
//        });
//        bookingAttractionColumn.setCellValueFactory(cellData -> {
//            String attractionId = cellData.getValue().getAttractionId();
//            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
//                    .filter(a -> a.getId().equals(attractionId)).findFirst();
//            return new javafx.beans.property.SimpleStringProperty(
//                    attractionOpt.map(Attraction::getName).orElse("Unknown Attraction")
//            );
//        });
//        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("tourDate"));
//        bookingStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
//        bookingPriceColumn.setCellValueFactory(new PropertyValueFactory<>("totalPrice"));
//
//        // Actions column for bookings (e.g., View Details, Update Status)
//        bookingActionsColumn.setCellFactory(param -> new TableCell<>() {
//            private final Button viewButton = new Button(LanguageManager.getString("button.view"));
//            private final Button updateStatusButton = new Button(LanguageManager.getString("button.update"));
//
//            {
//                viewButton.setOnAction(event -> {
//                    Booking booking = getTableView().getItems().get(getIndex());
//                    // Could open a detailed view dialog, for now just show an alert
//                    showAlert(Alert.AlertType.INFORMATION, "Booking Details",
//                            "Booking ID: " + booking.getId() + "\n" +
//                                    "Tourist ID: " + booking.getTouristId() + "\n" +
//                                    "Guide ID: " + (booking.getGuideId() != null ? booking.getGuideId() : "N/A") + "\n" +
//                                    "Attraction ID: " + booking.getAttractionId() + "\n" +
//                                    "Date: " + booking.getTourDate() + "\n" +
//                                    "People: " + booking.getNumberOfPeople() + "\n" +
//                                    "Requests: " + booking.getSpecialRequests() + "\n" +
//                                    "Status: " + booking.getStatus() + "\n" +
//                                    "Total Price: $" + String.format("%.2f", booking.getTotalPrice()) + "\n" +
//                                    "Discount: " + booking.getDiscountApplied()
//                    );
//                });
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
//                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(5, viewButton, updateStatusButton);
//                    setGraphic(buttons);
//                }
//            }
//        });
//
//        // Initialize booking list with filter
//        bookingObservableList = FXCollections.observableArrayList();
//        filteredBookingList = new FilteredList<>(bookingObservableList, p -> true);
//        bookingsTable.setItems(filteredBookingList);
//
//        // Setup status filter
//        bookingStatusFilter.getItems().addAll("All", "Pending", "Confirmed", "Completed", "Cancelled");
//        bookingStatusFilter.setValue("All");
//        bookingStatusFilter.setOnAction(event -> filterBookings());
//
//        // Export button
//        exportBookingsButton.setOnAction(event -> handleExportBookings());
//
//        // Initially load bookings
//        // loadBookings() will be called after setting the user
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
//     * Sets the logged-in admin user.
//     * @param user The logged-in Admin.
//     */
//    public void setLoggedInUser(Admin user) {
//        this.loggedInUser = user;
//        welcomeLabel.setText(LanguageManager.getString("admin.dashboard.welcome").replace("{0}", user.getName()));
//
//        // Load data for all tabs
//        loadTourists();
//        loadGuides();
//        loadAttractions();
//        loadBookings();
//        loadDashboardData();
//    }
//
//    // --- Data Loading Methods ---
//    private void loadTourists() {
//        // Filter users to get only tourists
//        touristObservableList.setAll(
//                mainApp.getUsers().stream()
//                        .filter(u -> u instanceof Tourist)
//                        .map(u -> (Tourist) u)
//                        .collect(Collectors.toList())
//        );
//    }
//
//    private void loadGuides() {
//        guideObservableList.setAll(
//                mainApp.getUsers().stream()
//                        .filter(u -> u instanceof Guide)
//                        .map(u -> (Guide) u)
//                        .collect(Collectors.toList())
//        );
//    }
//
//    private void loadAttractions() {
//        attractionObservableList.setAll(mainApp.getAttractions());
//    }
//
//    private void loadBookings() {
//        bookingObservableList.setAll(mainApp.getBookings());
//    }
//
//    private void loadDashboardData() {
//        // KPIs
//        long totalTourists = mainApp.getUsers().stream().filter(u -> u instanceof Tourist).count();
//        long activeGuides = mainApp.getUsers().stream().filter(u -> u instanceof Guide && ((Guide) u).isAvailable()).count();
//        long totalBookings = mainApp.getBookings().size();
//        double totalRevenue = mainApp.getBookings().stream().mapToDouble(Booking::getTotalPrice).sum();
//
//        totalTouristsLabel.setText(String.valueOf(totalTourists));
//        activeGuidesLabel.setText(String.valueOf(activeGuides));
//        totalBookingsLabel.setText(String.valueOf(totalBookings));
//        totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));
//
//        // Charts
//        updateRevenueChart();
//        updateAttractionsChart();
//        updateRegionalChart();
//    }
//
//    // --- Chart Updates ---
//    private void updateRevenueChart() {
//        revenueChart.getData().clear();
//        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
//        series.setName("Monthly Revenue");
//
//        // Dummy data for months (last 6)
//        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
//        for (int i = 5; i >= 0; i--) {
//            String monthKey = LocalDate.now().minusMonths(i).format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//            monthlyRevenue.put(monthKey, 0.0);
//        }
//        // Populate with actual data
//        for (Booking booking : mainApp.getBookings()) {
//            if (booking.getTourDate() != null) { // && "Completed".equalsIgnoreCase(booking.getStatus()) is optional
//                String monthKey = booking.getTourDate().format(DateTimeFormatter.ofPattern("MMM yy", LanguageManager.getCurrentLocale()));
//                monthlyRevenue.merge(monthKey, booking.getTotalPrice(), Double::sum);
//            }
//        }
//        for (Map.Entry<String, Double> entry : monthlyRevenue.entrySet()) {
//            series.getData().add(new javafx.scene.chart.XYChart.Data<>(entry.getKey(), entry.getValue()));
//        }
//        revenueChart.getData().add(series);
//    }
//
//    private void updateAttractionsChart() {
//        attractionsChart.getData().clear();
//        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
//        series.setName("Bookings");
//
//        // Count bookings per attraction
//        Map<String, Long> attractionBookings = mainApp.getBookings().stream()
//                .collect(Collectors.groupingBy(Booking::getAttractionId, Collectors.counting()));
//
//        // Get top 10 attractions by booking count
//        attractionBookings.entrySet().stream()
//                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
//                .limit(10)
//                .forEach(entry -> {
//                    String attractionId = entry.getKey();
//                    Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
//                            .filter(a -> a.getId().equals(attractionId)).findFirst();
//                    String name = attractionOpt.map(Attraction::getName).orElse("Unknown (" + attractionId + ")");
//                    series.getData().add(new javafx.scene.chart.XYChart.Data<>(name, entry.getValue()));
//                });
//
//        attractionsChart.getData().add(series);
//    }
//
//    private void updateRegionalChart() {
//        regionalChart.getData().clear();
//
//        // Count bookings per region
//        Map<String, Long> regionBookings = new HashMap<>();
//        for (Booking booking : mainApp.getBookings()) {
//            String attractionId = booking.getAttractionId();
//            Optional<Attraction> attractionOpt = mainApp.getAttractions().stream()
//                    .filter(a -> a.getId().equals(attractionId)).findFirst();
//            String region = attractionOpt.map(Attraction::getRegion).orElse("Unknown Region");
//            regionBookings.merge(region, 1L, Long::sum);
//        }
//
//        // Add data to pie chart
//        for (Map.Entry<String, Long> entry : regionBookings.entrySet()) {
//            javafx.scene.chart.PieChart.Data slice = new javafx.scene.chart.PieChart.Data(entry.getKey(), entry.getValue());
//            regionalChart.getData().add(slice);
//        }
//    }
//
//    // --- Filtering ---
//    private void filterBookings() {
//        String selectedStatus = bookingStatusFilter.getValue();
//        if ("All".equals(selectedStatus) || selectedStatus == null) {
//            filteredBookingList.setPredicate(booking -> true);
//        } else {
//            filteredBookingList.setPredicate(booking -> selectedStatus.equals(booking.getStatus()));
//        }
//    }
//
//    // --- Event Handlers ---
//    @FXML
//    private void handleLogout() {
//        try {
//            mainApp.showLoginScreen();
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Failed to load login screen", e);
//            showAlert(Alert.AlertType.ERROR, "Logout Error", "Could not return to login screen.");
//        }
//    }
//
//    private void handleAddUser(String userType) {
//        openUserFormDialog(null, userType); // null for new user
//    }
//
//    private void handleEditUser(User user) {
//        openUserFormDialog(user, user.getUserType());
//    }
//
//    private void openUserFormDialog(User user, String userType) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user-form-dialog.fxml"));
//            Parent root = loader.load();
//
//            UserFormDialogController controller = loader.getController();
//            controller.setMainApp(mainApp);
//            controller.setUser(user); // null for Add, User object for Edit
//            controller.setUserType(userType);
//
//            Stage dialogStage = new Stage();
//            String titleKey = (user == null) ? "user.form.dialog.title.add" : "user.form.dialog.title.edit";
//            dialogStage.setTitle(LanguageManager.getString(titleKey));
//            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(mainApp.getPrimaryStage());
//            Scene scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
//            dialogStage.setScene(scene);
//            dialogStage.setResizable(false);
//            dialogStage.showAndWait(); // Wait for dialog to close
//
//            // Refresh relevant tables after dialog closes
//            if ("Tourist".equals(userType)) {
//                loadTourists();
//            } else if ("Guide".equals(userType)) {
//                loadGuides();
//            }
//            // Reload dashboard data as user count might have changed
//            loadDashboardData();
//
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Failed to load user form dialog", e);
//            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open user form dialog.");
//        }
//    }
//
//    @FXML
//    private void handleDeleteUser(User user) {
//        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
//        confirmAlert.setTitle("Confirm Delete");
//        confirmAlert.setHeaderText(null);
//        confirmAlert.setContentText("Are you sure you want to delete user " + user.getName() + " (" + user.getId() + ")?");
//
//        Optional<ButtonType> result = confirmAlert.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            mainApp.getUsers().remove(user);
//            mainApp.saveAllData();
//            if (user instanceof Tourist) {
//                loadTourists();
//            } else if (user instanceof Guide) {
//                loadGuides();
//            }
//            loadDashboardData(); // Update counts
//            showAlert(Alert.AlertType.INFORMATION, "User Deleted", "User " + user.getName() + " has been deleted.");
//        }
//    }
//
//    @FXML
//    private void handleAddAttraction() {
//        openAttractionFormDialog(null); // null for new attraction
//    }
//
//    @FXML
//    private void handleEditAttraction(Attraction attraction) {
//        openAttractionFormDialog(attraction);
//    }
//
//    private void openAttractionFormDialog(Attraction attraction) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/attraction-form-dialog.fxml"));
//            Parent root = loader.load();
//
//            AttractionFormDialogController controller = loader.getController();
//            controller.setMainApp(mainApp);
//            controller.setAttraction(attraction); // null for Add, Attraction object for Edit
//
//            Stage dialogStage = new Stage();
//            String titleKey = (attraction == null) ? "attraction.form.dialog.title.add" : "attraction.form.dialog.title.edit";
//            dialogStage.setTitle(LanguageManager.getString(titleKey));
//            dialogStage.initModality(Modality.WINDOW_MODAL);
//            dialogStage.initOwner(mainApp.getPrimaryStage());
//            Scene scene = new Scene(root);
//            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
//            dialogStage.setScene(scene);
//            dialogStage.setResizable(false);
//            dialogStage.showAndWait(); // Wait for dialog to close
//
//            // Refresh attractions table after dialog closes
//            loadAttractions();
//            loadDashboardData(); // Update charts if needed
//
//        } catch (IOException e) {
//            logger.log(Level.SEVERE, "Failed to load attraction form dialog", e);
//            showAlert(Alert.AlertType.ERROR, "Dialog Error", "Could not open attraction form dialog.");
//        }
//    }
//
//    private void handleDeleteAttraction(Attraction attraction) {
//        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
//        confirmAlert.setTitle("Confirm Delete");
//        confirmAlert.setHeaderText(null);
//        confirmAlert.setContentText("Are you sure you want to delete attraction " + attraction.getName() + " (" + attraction.getId() + ")?");
//
//        Optional<ButtonType> result = confirmAlert.showAndWait();
//        if (result.isPresent() && result.get() == ButtonType.OK) {
//            mainApp.getAttractions().remove(attraction);
//            mainApp.saveAllData();
//            loadAttractions();
//            loadDashboardData(); // Update charts
//            showAlert(Alert.AlertType.INFORMATION, "Attraction Deleted", "Attraction " + attraction.getName() + " has been deleted.");
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
//                loadDashboardData(); // Refresh KPIs/charts if needed
//                showAlert(Alert.AlertType.INFORMATION, "Status Updated", "Booking status updated to " + newStatus);
//            }
//        }
//    }
//
//    private void handleExportBookings() {
//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Export Bookings");
//        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
//        fileChooser.setInitialFileName("bookings_export.csv");
//
//        Stage stage = (Stage) exportBookingsButton.getScene().getWindow();
//        File file = fileChooser.showSaveDialog(stage);
//
//        if (file != null) {
//            try (FileWriter writer = new FileWriter(file)) {
//                // Write CSV header
//                writer.append("Booking ID,Tourist ID,Guide ID,Attraction ID,Tour Date,Number of People,Special Requests,Status,Total Price,Discount Applied\n");
//
//                // Write booking data
//                for (Booking booking : filteredBookingList) { // Export filtered list
//                    writer.append(escapeCSV(booking.getId())).append(",")
//                            .append(escapeCSV(booking.getTouristId())).append(",")
//                            .append(escapeCSV(booking.getGuideId() != null ? booking.getGuideId() : "")).append(",")
//                            .append(escapeCSV(booking.getAttractionId())).append(",")
//                            .append(booking.getTourDate() != null ? booking.getTourDate().toString() : "").append(",")
//                            .append(String.valueOf(booking.getNumberOfPeople())).append(",")
//                            .append(escapeCSV(booking.getSpecialRequests())).append(",")
//                            .append(escapeCSV(booking.getStatus())).append(",")
//                            .append(String.format("%.2f", booking.getTotalPrice())).append(",")
//                            .append(escapeCSV(booking.getDiscountApplied())).append("\n");
//                }
//                showAlert(Alert.AlertType.INFORMATION, "Export Successful", "Bookings exported to " + file.getAbsolutePath());
//            } catch (IOException e) {
//                logger.log(Level.SEVERE, "Failed to export bookings", e);
//                showAlert(Alert.AlertType.ERROR, "Export Failed", "Could not export bookings to file.");
//            }
//        }
//    }
//
//    // Simple CSV escaping helper
//    private String escapeCSV(String field) {
//        if (field == null) return "";
//        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
//            return "\"" + field.replace("\"", "\"\"") + "\"";
//        }
//        return field;
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
//
//    public void setupDashboard() {
//        if (mainApp == null || loggedInUser == null) {
//            logger.warning("Main app or logged in user not set before calling setupDashboard()");
//            return;
//        }
//        loadAttractions();
//        loadBookings();
//        loadGuides();
////        loadAnalytics();
//    }
//}