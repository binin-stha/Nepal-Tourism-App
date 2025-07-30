package com.example.nepaltourism;

import com.example.nepaltourism.models.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Manages loading and saving data to/from CSV files.
 * Implements a simple DAO (Data Access Object) pattern.
 */
public class CSVDataManager {
    private static final Logger logger = Logger.getLogger(CSVDataManager.class.getName());
    private static final String DATA_DIR = "data";
    private static final String USERS_FILE = DATA_DIR + File.separator + "users.csv";
    private static final String ATTRACTIONS_FILE = DATA_DIR + File.separator + "attractions.csv";
    private static final String BOOKINGS_FILE = DATA_DIR + File.separator + "bookings.csv";
    private static final String EMERGENCY_REPORTS_FILE = DATA_DIR + File.separator + "emergency_reports.csv";

    private static final String USER_HEADER = "id,name,email,phone,passwordHash,userType,tourArea,experience,languages,emergencyContact";
    private static final String ATTRACTION_HEADER = "id,name,region,category,difficulty,durationDays,priceUSD,description,active,rating,altitudeMeters";
    private static final String BOOKING_HEADER = "id,touristId,guideId,attractionId,tourDate,numberOfPeople,specialRequests,status,totalPrice,discountApplied";
    private static final String EMERGENCY_REPORT_HEADER = "id,reporterId,reporterType,location,emergencyType,priority,description,contactNumber,timestamp,status";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public CSVDataManager() {
        initializeDataDirectory();
    }

    private void initializeDataDirectory() {
        try {
            Path path = Paths.get(DATA_DIR);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                logger.info("Created data directory: " + DATA_DIR);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to create data directory", e);
        }
    }

    // --- User Data Management ---
    public List<User> loadUsers() {
        List<User> users = new ArrayList<>();
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            logger.info("Users file not found, returning empty list.");
            return users;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Skip header
            if (line == null || !line.equals(USER_HEADER)) {
                logger.warning("Users file header mismatch or empty file.");
                // Optionally write header if file is empty or corrupt
                if (line == null) {
                    saveUsers(new ArrayList<>()); // This will write the header
                }
                return users;
            }

            while ((line = br.readLine()) != null) {
                String[] parts = parseCSVLine(line);
                if (parts.length < 6) continue; // Skip malformed lines

                String id = parts[0];
                String name = parts[1];
                String email = parts[2];
                String phone = parts[3];
                String passwordHash = parts[4];
                String userType = parts[5];

                User user = null;
                switch (userType) {
                    case "Tourist":
                        String emergencyContact = parts.length > 9 ? parts[9] : "";
                        user = new Tourist(id, name, email, phone, passwordHash, emergencyContact);
                        break;
                    case "Guide":
                        String tourArea = parts.length > 6 ? parts[6] : "";
                        int experience = 0;
                        try {
                            experience = Integer.parseInt(parts.length > 7 ? parts[7] : "0");
                        } catch (NumberFormatException ignored) {
                        }
                        String languages = parts.length > 8 ? parts[8] : "";
                        user = new Guide(id, name, email, phone, passwordHash, tourArea, experience, languages);
                        break;
                    case "Admin":
                        user = new Admin(id, name, email, phone, passwordHash);
                        break;
                }
                if (user != null) {
                    users.add(user);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading users from CSV", e);
        }
        return users;
    }

    public void saveUsers(List<User> users) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(USERS_FILE))) {
            pw.println(USER_HEADER);
            for (User user : users) {
                StringBuilder sb = new StringBuilder();
                sb.append(escapeCSV(user.getId())).append(",")
                        .append(escapeCSV(user.getName())).append(",")
                        .append(escapeCSV(user.getEmail())).append(",")
                        .append(escapeCSV(user.getPhone())).append(",")
                        .append(escapeCSV(user.getPassword())).append(",") // Assumes password is already hashed
                        .append(escapeCSV(user.getUserType())).append(",");

                if (user instanceof Guide) {
                    Guide guide = (Guide) user;
                    sb.append(escapeCSV(guide.getTourArea())).append(",")
                            .append(guide.getExperience()).append(",")
                            .append(escapeCSV(guide.getLanguages())).append(",");
                } else {
                    sb.append(",,,").append(","); // Empty fields for Tourist/Admin for tourArea, experience, languages
                }

                if (user instanceof Tourist) {
                    sb.append(escapeCSV(((Tourist) user).getEmergencyContact()));
                } else {
                    sb.append(""); // Empty field for Guide/Admin
                }
                pw.println(sb.toString());
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving users to CSV", e);
        }
    }

    // --- Attraction Data Management ---
    public List<Attraction> loadAttractions() {
        List<Attraction> attractions = new ArrayList<>();
        File file = new File(ATTRACTIONS_FILE);
        if (!file.exists()) {
            logger.info("Attractions file not found, returning empty list.");
            return attractions;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Skip header
            if (line == null || !line.equals(ATTRACTION_HEADER)) {
                logger.warning("Attractions file header mismatch or empty file.");
                if (line == null) {
                    saveAttractions(new ArrayList<>());
                }
                return attractions;
            }

            while ((line = br.readLine()) != null) {
                String[] parts = parseCSVLine(line);
                if (parts.length < 11) continue;

                Attraction attraction = new Attraction();
                attraction.setId(parts[0]);
                attraction.setName(parts[1]);
                attraction.setRegion(parts[2]);
                attraction.setCategory(parts[3]);
                attraction.setDifficulty(parts[4]);
                try {
                    attraction.setDurationDays(Integer.parseInt(parts[5]));
                } catch (NumberFormatException e) { /* log or handle */ }
                try {
                    attraction.setPriceUSD(Double.parseDouble(parts[6]));
                } catch (NumberFormatException e) { /* log or handle */ }
                attraction.setDescription(parts[7]);
                attraction.setActive("true".equalsIgnoreCase(parts[8]));
                try {
                    attraction.setRating(Double.parseDouble(parts[9]));
                } catch (NumberFormatException e) { /* log or handle */ }
                try {
                    attraction.setAltitudeMeters(Integer.parseInt(parts[10]));
                } catch (NumberFormatException e) { /* log or handle */ }

                attractions.add(attraction);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading attractions from CSV", e);
        }
        return attractions;
    }

    public void saveAttractions(List<Attraction> attractions) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ATTRACTIONS_FILE))) {
            pw.println(ATTRACTION_HEADER);
            for (Attraction attraction : attractions) {
                pw.printf("%s,%s,%s,%s,%s,%d,%.2f,%s,%s,%.2f,%d%n",
                        escapeCSV(attraction.getId()),
                        escapeCSV(attraction.getName()),
                        escapeCSV(attraction.getRegion()),
                        escapeCSV(attraction.getCategory()),
                        escapeCSV(attraction.getDifficulty()),
                        attraction.getDurationDays(),
                        attraction.getPriceUSD(),
                        escapeCSV(attraction.getDescription()),
                        attraction.isActive(),
                        attraction.getRating(),
                        attraction.getAltitudeMeters()
                );
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving attractions to CSV", e);
        }
    }

    // --- Booking Data Management ---
    public List<Booking> loadBookings() {
        List<Booking> bookings = new ArrayList<>();
        File file = new File(BOOKINGS_FILE);
        if (!file.exists()) {
            logger.info("Bookings file not found, returning empty list.");
            return bookings;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Skip header
            if (line == null || !line.equals(BOOKING_HEADER)) {
                logger.warning("Bookings file header mismatch or empty file.");
                if (line == null) {
                    saveBookings(new ArrayList<>());
                }
                return bookings;
            }

            while ((line = br.readLine()) != null) {
                String[] parts = parseCSVLine(line);
                if (parts.length < 10) continue;

                Booking booking = new Booking();
                booking.setId(parts[0]);
                booking.setTouristId(parts[1]);
                booking.setGuideId(parts[2].isEmpty() ? null : parts[2]); // Handle null guide ID
                booking.setAttractionId(parts[3]);
                try {
                    booking.setTourDate(LocalDate.parse(parts[4], DATE_FORMATTER));
                } catch (Exception e) { /* log or handle */ }
                try {
                    booking.setNumberOfPeople(Integer.parseInt(parts[5]));
                } catch (NumberFormatException e) { /* log or handle */ }
                booking.setSpecialRequests(parts[6]);
                booking.setStatus(parts[7]);
                try {
                    booking.setTotalPrice(Double.parseDouble(parts[8]));
                } catch (NumberFormatException e) { /* log or handle */ }
                booking.setDiscountApplied(parts[9]);

                bookings.add(booking);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading bookings from CSV", e);
        }
        return bookings;
    }

    public void saveBookings(List<Booking> bookings) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(BOOKINGS_FILE))) {
            pw.println(BOOKING_HEADER);
            for (Booking booking : bookings) {
                pw.printf("%s,%s,%s,%s,%s,%d,%s,%s,%.2f,%s%n",
                        escapeCSV(booking.getId()),
                        escapeCSV(booking.getTouristId()),
                        escapeCSV(booking.getGuideId() != null ? booking.getGuideId() : ""),
                        escapeCSV(booking.getAttractionId()),
                        booking.getTourDate() != null ? booking.getTourDate().format(DATE_FORMATTER) : "",
                        booking.getNumberOfPeople(),
                        escapeCSV(booking.getSpecialRequests()),
                        escapeCSV(booking.getStatus()),
                        booking.getTotalPrice(),
                        escapeCSV(booking.getDiscountApplied())
                );
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving bookings to CSV", e);
        }
    }

    // --- Emergency Report Data Management ---
    public List<EmergencyReport> loadEmergencyReports() {
        List<EmergencyReport> reports = new ArrayList<>();
        File file = new File(EMERGENCY_REPORTS_FILE);
        if (!file.exists()) {
            logger.info("Emergency reports file not found, returning empty list.");
            return reports;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line = br.readLine(); // Skip header
            if (line == null || !line.equals(EMERGENCY_REPORT_HEADER)) {
                logger.warning("Emergency reports file header mismatch or empty file.");
                if (line == null) {
                    saveEmergencyReports(new ArrayList<>());
                }
                return reports;
            }

            while ((line = br.readLine()) != null) {
                String[] parts = parseCSVLine(line);
                if (parts.length < 10) continue;

                EmergencyReport report = new EmergencyReport();
                report.setId(parts[0]);
                report.setReporterId(parts[1]);
                report.setReporterType(parts[2]);
                report.setLocation(parts[3]);
                report.setEmergencyType(parts[4]);
                report.setPriority(parts[5]);
                report.setDescription(parts[6]);
                report.setContactNumber(parts[7]);
                try {
                    report.setTimestamp(LocalDateTime.parse(parts[8], DATETIME_FORMATTER));
                } catch (Exception e) { /* log or handle */ }
                report.setStatus(parts[9]);

                reports.add(report);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading emergency reports from CSV", e);
        }
        return reports;
    }

    public void saveEmergencyReports(List<EmergencyReport> reports) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(EMERGENCY_REPORTS_FILE))) {
            pw.println(EMERGENCY_REPORT_HEADER);
            for (EmergencyReport report : reports) {
                pw.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s%n",
                        escapeCSV(report.getId()),
                        escapeCSV(report.getReporterId()),
                        escapeCSV(report.getReporterType()),
                        escapeCSV(report.getLocation()),
                        escapeCSV(report.getEmergencyType()),
                        escapeCSV(report.getPriority()),
                        escapeCSV(report.getDescription()),
                        escapeCSV(report.getContactNumber()),
                        report.getTimestamp() != null ? report.getTimestamp().format(DATETIME_FORMATTER) : "",
                        escapeCSV(report.getStatus())
                );
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving emergency reports to CSV", e);
        }
    }

    // --- Helper Methods ---
    private String escapeCSV(String field) {
        if (field == null) return "";
        // Escape double quotes and wrap in quotes if contains comma, quote, or newline
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    // Simple CSV parser that handles quoted fields
    private String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder currentField = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                // Check for escaped quote
                if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    currentField.append('"');
                    i++; // Skip next quote
                } else {
                    inQuotes = !inQuotes; // Toggle quote state
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString());
                currentField = new StringBuilder();
            } else {
                currentField.append(c);
            }
        }
        fields.add(currentField.toString()); // Add last field
        return fields.toArray(new String[0]);
    }
}