package com.example.nepaltourism.controllers;

import com.example.nepaltourism.Main;
import com.example.nepaltourism.models.Attraction;
import com.example.nepaltourism.utils.LanguageManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Controller class for the attraction-form-dialog.fxml view.
 */
public class AttractionFormDialogController {

    private static final Logger logger = Logger.getLogger(AttractionFormDialogController.class.getName());

    @FXML
    private Label dialogTitleLabel;
    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> regionCombo;
    @FXML
    private ComboBox<String> categoryCombo;
    @FXML
    private ComboBox<String> difficultyCombo;
    @FXML
    private Spinner<Integer> durationSpinner;
    @FXML
    private TextField priceField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private CheckBox activeCheckBox;
    @FXML
    private Label errorLabel;
    @FXML
    private Button cancelButton;
    @FXML
    private Button saveButton;

    private Main mainApp;
    private Attraction attraction; // null for Add, Attraction object for Edit
    private boolean isEditMode = false;

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        // Populate combo boxes with common values
        // Regions - These could be loaded from a config or data file in a more complex app
        regionCombo.getItems().addAll(
                "Kathmandu Valley", "Pokhara", "Everest Region", "Annapurna Region",
                "Langtang Region", "Mustang", "Manaslu", "Ilam", "Chitwan", "Lumbini"
        );
        regionCombo.setPromptText(LanguageManager.getString("attraction.form.region"));

        // Categories
        categoryCombo.getItems().addAll("Trek", "Heritage", "Adventure", "Cultural", "Wildlife", "Religious");
        categoryCombo.setPromptText(LanguageManager.getString("attraction.form.category"));

        // Difficulties
        difficultyCombo.getItems().addAll("Easy", "Moderate", "Hard", "Expert");
        difficultyCombo.setPromptText(LanguageManager.getString("attraction.form.difficulty"));

        // Set up the duration spinner (1 to 365 days)
        SpinnerValueFactory<Integer> spinnerValueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 365, 1);
        durationSpinner.setValueFactory(spinnerValueFactory);

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
     * Sets the attraction to be edited.
     * If null, the dialog is in "Add" mode.
     * Also initializes the form fields.
     * @param attraction The Attraction object to edit, or null for a new one.
     */
    public void setAttraction(Attraction attraction) {
        this.attraction = attraction;
        this.isEditMode = (attraction != null);

        if (isEditMode) {
            dialogTitleLabel.setText(LanguageManager.getString("attraction.form.dialog.title.edit"));
            populateFieldsFromAttraction();
        } else {
            dialogTitleLabel.setText(LanguageManager.getString("attraction.form.dialog.title.add"));
            // Fields are already empty by default
        }
    }

    /**
     * Populates the form fields with data from the attraction being edited.
     */
    private void populateFieldsFromAttraction() {
        if (attraction != null) {
            nameField.setText(attraction.getName());
            regionCombo.setValue(attraction.getRegion());
            categoryCombo.setValue(attraction.getCategory());
            difficultyCombo.setValue(attraction.getDifficulty());
            durationSpinner.getValueFactory().setValue(attraction.getDurationDays());
            priceField.setText(String.valueOf(attraction.getPriceUSD()));
            descriptionArea.setText(attraction.getDescription());
            activeCheckBox.setSelected(attraction.isActive());
        }
    }

    /**
     * Handles the save button action.
     */
    @FXML
    private void handleSave() {
        errorLabel.setText(""); // Clear previous errors

        // Validate inputs
        String name = nameField.getText().trim();
        String region = regionCombo.getValue();
        String category = categoryCombo.getValue();
        String difficulty = difficultyCombo.getValue();
        int durationDays = durationSpinner.getValue();
        String priceText = priceField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (name.isEmpty() || region == null || region.isEmpty() ||
                category == null || category.isEmpty() || difficulty == null || difficulty.isEmpty() ||
                description.isEmpty()) {
            errorLabel.setText(LanguageManager.getString("error.field.required"));
            return;
        }

        double priceUSD;
        try {
            priceUSD = Double.parseDouble(priceText);
            if (priceUSD < 0) {
                throw new NumberFormatException("Negative price");
            }
        } catch (NumberFormatException e) {
            errorLabel.setText(LanguageManager.getString("error.invalid.number") + " " + LanguageManager.getString("attraction.form.price"));
            return;
        }

        // If in edit mode and attraction is null, something is wrong
        if (isEditMode && this.attraction == null) {
            errorLabel.setText(LanguageManager.getString("error.general"));
            logger.log(Level.SEVERE, "Edit mode is true but attraction object is null.");
            return;
        }

        // Create or update the attraction object
        if (isEditMode) {
            // Update existing attraction
            attraction.setName(name);
            attraction.setRegion(region);
            attraction.setCategory(category);
            attraction.setDifficulty(difficulty);
            attraction.setDurationDays(durationDays);
            attraction.setPriceUSD(priceUSD);
            attraction.setDescription(description);
            attraction.setActive(activeCheckBox.isSelected());
            // ID, rating, altitude remain unchanged
            logger.info("Attraction updated: " + attraction.getId());
        } else {
            // Create new attraction
            String newId = "AT" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
            // Default altitude for new attractions, could be made editable in a more advanced form
            int defaultAltitude = 0;
            if (category != null && (category.contains("Trek") || category.contains("Adventure"))) {
                defaultAltitude = 2000; // Default for treks/adventures
            }
            Attraction newAttraction = new Attraction(
                    newId, name, region, category, difficulty,
                    durationDays, priceUSD, description, activeCheckBox.isSelected(), defaultAltitude
            );
            mainApp.getAttractions().add(newAttraction);
            logger.info("New attraction created: " + newId);
        }

        // Save all data
        mainApp.saveAllData();

        showAlert(Alert.AlertType.INFORMATION, "Success",
                isEditMode ? "Attraction updated successfully." : "New attraction added successfully.");
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