package com.example.nepaltourism.utils;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Manages language switching for the application.
 * Uses Java ResourceBundle for localization.
 */
public class LanguageManager {
    private static final String BUNDLE_NAME = "lang/messages";
    private static Locale currentLocale = Locale.ENGLISH; // Default to English
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);

    /**
     * Sets the application language.
     * @param locale The locale to switch to (e.g., Locale.ENGLISH, new Locale("np")).
     */
    public static void setLocale(Locale locale) {
        currentLocale = locale;
        resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, currentLocale);
    }

    /**
     * Gets the current locale.
     * @return The current Locale.
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }

    /**
     * Gets the localized string for a given key.
     * @param key The key for the string in the properties file.
     * @return The localized string, or the key itself if not found.
     */
    public static String getString(String key) {
        try {
            return resourceBundle.getString(key);
        } catch (Exception e) {
            // Return the key if the string is not found
            return key;
        }
    }

    /**
     * Gets the display name for the current language.
     * Useful for UI elements like language switch buttons.
     * @return The display name (e.g., "English", "नेपाली").
     */
    public static String getCurrentLanguageDisplayName() {
        if (Locale.ENGLISH.equals(currentLocale)) {
            return "English";
        } else if (new Locale("np").equals(currentLocale)) {
            return "नेपाली";
        }
        return currentLocale.getDisplayLanguage(currentLocale);
    }

    /**
     * Gets the opposite language display name for switching.
     * @return The display name of the other language.
     */
    public static String getSwitchLanguageDisplayName() {
        if (Locale.ENGLISH.equals(currentLocale)) {
            return "नेपाली";
        } else if (new Locale("np").equals(currentLocale)) {
            return "English";
        }
        // Default fallback
        return Locale.ENGLISH.equals(currentLocale) ? "नेपाली" : "English";
    }
}