# Nepal Tourism Management System

A comprehensive desktop application for managing tourism operations in Nepal, built with Java, JavaFX, and Maven. The system provides role-based dashboards for admins, tourists, and guides, supporting the full lifecycle of tour booking and management.

## Features

- **Role-Based Access:** Secure login and dashboards for Admin, Tourist, and Guide roles.
- **Admin Dashboard:** View, manage, and analyze tourist, booking, and attraction details.
- **Tourist Booking:** Book tours with guide selection, date picking, people count, dynamic price calculation, and special requests.
- **Attraction Management:** Add, update, and remove tourist attractions with region and details.
- **Booking Management:** Real-time booking status, festival discount application, and safety information display.
- **Guide Assignment:** Assign guides to tours based on availability and expertise.
- **Responsive UI:** Modern, user-friendly interface using JavaFX FXML and CSS.
- **Data Handling:** Uses `ObservableList` for dynamic UI updates; supports integration with database or API for persistent storage.
- **Validation & Alerts:** Input validation, error handling, and user feedback via dialogs and alerts.
- **Maven Build:** Modular project structure with dependency management.

## Technologies Used

- Java 17+
- JavaFX (FXML, CSS)
- Maven
- MVC Architecture

## Project Structure
src/ └── main/ ├── java/ │ └── com/example/nepaltourism/... ├── resources/ │ └── fxml/ │ └── *.fxml │ └── css/ │ └── *.css out/ └── artifacts/ └── Paryatan_Nepal/ └── Paryatan_Nepal.jar pom.xml README.md

## Getting Started

### Prerequisites

- Java 17 or higher installed ([Download here](https://adoptium.net/))
- Maven installed ([Download here](https://maven.apache.org/download.cgi))

### Clone the Repository:
git clone https://github.com/your-username/nepal-tourism-management.git
cd nepal-tourism-management

**Build the Project**
To build the project and generate the JAR file:
mvn clean package

The JAR file will be generated in the out/artifacts/Paryatan_Nepal/ directory as Paryatan_Nepal.jar.

## **Running the Application**
Using the JAR File
Open a terminal or command prompt.
Navigate to the project directory.

Run the following command: 
cd out/artifacts/Paryatan_Nepal
java -jar Paryatan_Nepal.jar

Note:
Make sure you have JavaFX SDK set up if your Java distribution does not include JavaFX.
If you encounter JavaFX errors, you may need to add JavaFX modules to your command:

java --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls,javafx.fxml -jar Paryatan_Nepal.jar


## **Contributing**
Contributions are welcome! Please open issues or submit pull requests for improvements.

## **License**
This project is licensed under the MIT License.

## **Developed by:**
Binin Dhon Shrestha
