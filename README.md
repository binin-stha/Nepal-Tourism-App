# Nepal-Tourism-App
Will be updating full functional application soon!

Project Strucutre:
NepalTourismApp/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── nepaltourism/
│       │               ├── Main.java
│       │               ├── CSVDataManager.java
│       │               ├── utils/
│       │               │   ├── FestivalManager.java
│       │               │   ├── SafetyAlertManager.java
│       │               │   └── LanguageManager.java
│       │               ├── models/
│       │               │   ├── User.java
│       │               │   ├── Tourist.java
│       │               │   ├── Guide.java
│       │               │   ├── Admin.java
│       │               │   ├── Attraction.java
│       │               │   ├── Booking.java
│       │               │   └── EmergencyReport.java
│       │               └── controllers/
│       │                   ├── LoginController.java
│       │                   ├── SignupController.java
│       │                   ├── TouristDashboardController.java
│       │                   ├── GuideDashboardController.java
│       │                   ├── AdminDashboardController.java
│       │                   ├── BookingDialogController.java
│       │                   ├── EmergencyDialogController.java
│       │                   ├── AttractionFormController.java
│       │                   └── UserFormController.java
│       └── resources/
│           ├── fxml/
│           │   ├── login.fxml
│           │   ├── signup.fxml
│           │   ├── tourist-dashboard.fxml
│           │   ├── guide-dashboard.fxml
│           │   ├── admin-dashboard.fxml
│           │   ├── booking-dialog.fxml
│           │   ├── emergency-dialog.fxml
│           │   ├── attraction-form.fxml
│           │   └── user-form.fxml
│           ├── css/
│           │   └── styles.css
│           └── lang/
│               ├── messages_en.properties
│               └── messages_np.properties
└── data/
    ├── users.csv
    ├── attractions.csv
    ├── bookings.csv
    └── emergency_reports.csv
