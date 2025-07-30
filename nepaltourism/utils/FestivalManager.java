package com.example.nepaltourism.utils;

import java.time.LocalDate;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages festival information and calculates discounts.
 * Based on common Nepali festivals.
 */
public class FestivalManager {

    // Define some major Nepali festivals and their typical date ranges (month-day)
    // Note: Actual dates vary yearly based on lunar calendar. This is a simplified approach.
    private static final Map<String, LocalDate[]> FESTIVAL_PERIODS = new HashMap<>();

    static {
        // Dashain (Approximate range, usually spans Ashwin/Kartik)
        FESTIVAL_PERIODS.put("Dashain", new LocalDate[]{
                LocalDate.of(2025, Month.OCTOBER, 1),
                LocalDate.of(2025, Month.OCTOBER, 15)
        });

        // Tihar (Approximate range, usually spans Kartik)
        FESTIVAL_PERIODS.put("Tihar", new LocalDate[]{
                LocalDate.of(2025, Month.OCTOBER, 25),
                LocalDate.of(2025, Month.NOVEMBER, 5)
        });

        // Holi (Approximate range, usually Falgun)
        FESTIVAL_PERIODS.put("Holi", new LocalDate[]{
                LocalDate.of(2025, Month.MARCH, 10),
                LocalDate.of(2025, Month.MARCH, 20)
        });
    }

    private static final double DEFAULT_FESTIVAL_DISCOUNT = 0.10; // 10% default discount

    /**
     * Checks if a given date falls within any major festival period.
     * @param date The date to check.
     * @return The name of the festival if found, otherwise null.
     */
    public static String getFestivalForDate(LocalDate date) {
        if (date == null) return null;

        for (Map.Entry<String, LocalDate[]> entry : FESTIVAL_PERIODS.entrySet()) {
            LocalDate[] period = entry.getValue();
            if (!period[0].isAfter(date) && !period[1].isBefore(date)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Calculates the discount percentage for a booking date.
     * @param bookingDate The date of the booking/tour.
     * @return The discount percentage (e.g., 0.10 for 10%).
     */
    public static double getFestivalDiscount(LocalDate bookingDate) {
        String festival = getFestivalForDate(bookingDate);
        if (festival != null) {
            // Could implement different discounts for different festivals
            // For now, use a default discount
            return DEFAULT_FESTIVAL_DISCOUNT;
        }
        return 0.0;
    }

    /**
     * Gets a descriptive message for the festival discount.
     * @param bookingDate The date of the booking/tour.
     * @return A message describing the discount.
     */
    public static String getFestivalDiscountMessage(LocalDate bookingDate) {
        String festival = getFestivalForDate(bookingDate);
        if (festival != null) {
            return String.format("%.0f%% discount for %s festival", DEFAULT_FESTIVAL_DISCOUNT * 100, festival);
        }
        return "";
    }
}