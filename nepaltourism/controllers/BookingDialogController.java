package com.example.nepaltourism.controllers;

import com.example.nepaltourism.Main;
import com.example.nepaltourism.models.Attraction;
import com.example.nepaltourism.models.Booking;
import com.example.nepaltourism.models.Guide;
import com.example.nepaltourism.models.Tourist;
import com.example.nepaltourism.utils.FestivalManager;
import com.example.nepaltourism.utils.LanguageManager;
import com.example.nepaltourism.utils.SafetyAlertManager;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Controller class for the booking-dialog.fxml view.
 */
public class BookingDialogController {

    private static final Logger logger = Logger.getLogger(BookingDialogController.class.getName());

    @FXML
    private Label attractionNameLabel;
    @FXML
    private Label attractionDetailsLabel;
    @FXML
    private ComboBox<Guide> guideComboBox;
    @FXML
    private DatePicker tourDatePicker;
    @FXML
    private Spinner<Integer> peopleSpinner;
    @FXML
    private Label totalPriceLabel;
    @FXML
    private TextArea specialRequestsArea;
    @FXML
    private VBox discountBox;
    @FXML
    private Label discountLabel;
    @FXML
    private VBox safetyBox;
    @FXML
    private Label safetyLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private Button bookButton;

    private Main mainApp;
    private Tourist tourist;
    private Attraction attraction;
    private double basePricePerPerson = 0.0;
    private String appliedDiscountInfo = "";

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Set up the number of people spinner
        SpinnerValueFactory<Integer> spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
        peopleSpinner.setValueFactory(spinnerValueFactory);

        // Add listeners to recalculate price
        peopleSpinner.valueProperty().addListener((obs, oldVal, newVal) -> updateTotalPrice());
        tourDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateTotalPrice();
            updateDiscountAndSafety();
        });

        // Set up button actions
        cancelButton.setOnAction(event -> handleClose());
        bookButton.setOnAction(event -> handleBook());
    }

    /**
     * Sets the main application reference.
     * @param mainApp The main application instance.
     */
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Sets the tourist making the booking.
     * @param tourist The Tourist object.
     */
    public void setTourist(Tourist tourist) {
        this.tourist = tourist;
    }

    /**
     * Sets the attraction being booked.
     * Also initializes the dialog UI based on the attraction.
     * @param attraction The Attraction object.
     */
    public void setAttraction(Attraction attraction) {
        this.attraction = attraction;
        if (attraction != null) {
            basePricePerPerson = attraction.getPriceUSD();
            attractionNameLabel.setText(LanguageManager.getString("booking.dialog.title").replace("{0}", attraction.getName()));
            attractionDetailsLabel.setText(attraction.getDescription());

            // Populate guide combo box with guides available for this attraction's area
            // Simplified: Get all available guides. A more complex filter could match tourArea.
            guideComboBox.setItems(
                    FXCollections.observableArrayList(
                            mainApp.getUsers().stream()
                                    .filter(u -> u instanceof Guide && ((Guide) u).isAvailable())
                                    .map(u -> (Guide) u)
                                    .collect(Collectors.toList())
                    )
            );
            guideComboBox.setPromptText("Select a Guide");

            // Set default date to tomorrow
            tourDatePicker.setValue(LocalDate.now().plusDays(1));

            // Initial price calculation
            updateTotalPrice();
            updateDiscountAndSafety();
        }
    }

    /**
     * Updates the total price based on number of people and base price.
     */
    private void updateTotalPrice() {
        int numberOfPeople = peopleSpinner.getValue();
        LocalDate bookingDate = tourDatePicker.getValue();

        double subtotal = basePricePerPerson * numberOfPeople;

        // Apply festival discount if applicable
        double discount = 0.0;
        appliedDiscountInfo = "";
        if (bookingDate != null) {
            discount = FestivalManager.getFestivalDiscount(bookingDate);
            if (discount > 0) {
                appliedDiscountInfo = FestivalManager.getFestivalDiscountMessage(bookingDate);
            }
        }

        double total = subtotal * (1 - discount);
        totalPriceLabel.setText(String.format("$%.2f", total));
    }

    /**
     * Updates the discount and safety information sections.
     */
    private void updateDiscountAndSafety() {
        LocalDate bookingDate = tourDatePicker.getValue();

        // Update Festival Discount
        String discountMessage = "";
        if (bookingDate != null) {
            discountMessage = FestivalManager.getFestivalDiscountMessage(bookingDate);
        }
        if (!discountMessage.isEmpty()) {
            discountLabel.setText(discountMessage);
            discountBox.setVisible(true);
            discountBox.setManaged(true);
        } else {
            discountBox.setVisible(false);
            discountBox.setManaged(false);
        }

        // Update Safety Alert
        String safetyMessage = "";
        if (attraction != null && bookingDate != null) {
            // Check for high altitude alert
            safetyMessage += SafetyAlertManager.getHighAltitudeAlert(attraction);

            // Check for monsoon restriction
            String monsoonMessage = SafetyAlertManager.getMonsoonRestrictionMessage(attraction, bookingDate);
            if (!monsoonMessage.isEmpty()) {
                if (!safetyMessage.isEmpty()) safetyMessage += "\n";
                safetyMessage += monsoonMessage;
                // Disable booking button if restricted
                bookButton.setDisable(true);
            } else {
                bookButton.setDisable(false);
            }
        }

        // Add default safety message if none specific
        if (safetyMessage.isEmpty()) {
            safetyMessage = LanguageManager.getString("booking.dialog.safety.default");
        }

        safetyLabel.setText(safetyMessage);
        // Always show safety box for context
        safetyBox.setVisible(true);
        safetyBox.setManaged(true);
    }

    /**
     * Handles the book button action.
     */
    @FXML
    private void handleBook() {
        // Validate inputs
        if (tourist == null || attraction == null) {
            showAlert(Alert.AlertType.ERROR, "Booking Error", "Tourist or Attraction information is missing.");
            return;
        }

        LocalDate tourDate = tourDatePicker.getValue();
        if (tourDate == null) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", LanguageManager.getString("error.invalid.date"));
            return;
        }

        if (tourDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Tour date cannot be in the past.");
            return;
        }

        Guide selectedGuide = guideComboBox.getValue();
        String guideId = selectedGuide != null ? selectedGuide.getId() : null;

        int numberOfPeople = peopleSpinner.getValue();
        String specialRequests = specialRequestsArea.getText().trim();

        // Get final price and discount info (recalculate to be sure)
        double totalPrice = 0.0;
        try {
            totalPrice = Double.parseDouble(totalPriceLabel.getText().replace("$", ""));
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "Failed to parse total price label", e);
            showAlert(Alert.AlertType.ERROR, "Booking Error", "Failed to calculate total price.");
            return;
        }

        // Create booking object
        String bookingId = "BK" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
        Booking newBooking = new Booking(
                bookingId,
                tourist.getId(),
                guideId,
                attraction.getId(),
                tourDate,
                numberOfPeople,
                specialRequests,
                "Pending", // Default status
                totalPrice,
                appliedDiscountInfo
        );

        // Confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Booking");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText(
                "Are you sure you want to book this tour?\n\n" +
                        "Attraction: " + attraction.getName() + "\n" +
                        "Date: " + tourDate + "\n" +
                        "People: " + numberOfPeople + "\n" +
                        "Guide: " + (selectedGuide != null ? selectedGuide.getName() : "None Selected") + "\n" +
                        "Total Price: $" + String.format("%.2f", totalPrice) + "\n" +
                        (!appliedDiscountInfo.isEmpty() ? "Discount: " + appliedDiscountInfo + "\n" : "")
        );

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Add booking to main app's list
            mainApp.getBookings().add(newBooking);
            // Save all data
            mainApp.saveAllData();
            logger.info("New booking created: " + bookingId + " for tourist " + tourist.getId());

            showAlert(Alert.AlertType.INFORMATION, "Booking Successful", "Your booking has been placed successfully! Booking ID: " + bookingId);
            handleClose(); // Close the dialog
        }
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
