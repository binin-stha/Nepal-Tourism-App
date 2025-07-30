package com.example.nepaltourism.utils;

import com.example.nepaltourism.models.Attraction;

import java.time.LocalDate;
import java.time.Month;

/**
 * Manages safety alerts based on attraction altitude and seasonal conditions.
 */
public class SafetyAlertManager {

    private static final int HIGH_ALTITUDE_THRESHOLD = 3000; // Meters
    private static final Month[] MONSOON_MONTHS = {Month.JUNE, Month.JULY, Month.AUGUST};

    /**
     * Checks if an attraction requires a high-altitude safety alert.
     * @param attraction The attraction to check.
     * @return True if the attraction is at high altitude, false otherwise.
     */
    public static boolean isHighAltitude(Attraction attraction) {
        return attraction != null && attraction.getAltitudeMeters() >= HIGH_ALTITUDE_THRESHOLD;
    }

    /**
     * Gets the safety alert message for high-altitude attractions.
     * @param attraction The attraction to check.
     * @return A safety message if high altitude, otherwise an empty string.
     */
    public static String getHighAltitudeAlert(Attraction attraction) {
        if (isHighAltitude(attraction)) {
            return "⚠️ High Altitude Alert: This attraction is located above 3,000 meters. Please ensure you are acclimatized and have proper gear.";
        }
        return "";
    }

    /**
     * Checks if booking is restricted due to monsoon season for high-altitude treks.
     * @param attraction The attraction being booked.
     * @param bookingDate The proposed booking date.
     * @return True if booking should be restricted, false otherwise.
     */
    public static boolean isMonsoonRestricted(Attraction attraction, LocalDate bookingDate) {
        if (attraction == null || bookingDate == null) {
            return false;
        }
        // Only restrict high-altitude attractions during monsoon
        if (isHighAltitude(attraction)) {
            Month bookingMonth = bookingDate.getMonth();
            for (Month monsoonMonth : MONSOON_MONTHS) {
                if (bookingMonth == monsoonMonth) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the monsoon restriction message.
     * @param attraction The attraction being booked.
     * @param bookingDate The proposed booking date.
     * @return A restriction message if applicable, otherwise an empty string.
     */
    public static String getMonsoonRestrictionMessage(Attraction attraction, LocalDate bookingDate) {
        if (isMonsoonRestricted(attraction, bookingDate)) {
            return "❌ Booking Restricted: High-altitude treks are not recommended during the monsoon season (June-August) due to increased risk of landslides and flooding.";
        }
        return "";
    }

    /**
     * Gets general safety instructions.
     * @return A list of general safety tips.
     */
    public static String[] getGeneralSafetyInstructions() {
        return new String[]{
                "• Stay hydrated and carry sufficient water.",
                "• Wear appropriate clothing and footwear.",
                "• Inform someone about your travel plans.",
                "• Carry a basic first aid kit.",
                "• Check weather conditions before heading out.",
                "• Respect local customs and environment.",
                "• Keep emergency contact numbers handy."
        };
    }
}