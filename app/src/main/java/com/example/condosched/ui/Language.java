package com.example.condosched.ui;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

public class Language {
    public static final String[] TAGS  = {"", "en", "pt"};
    public static final String[] NAMES = {"System default", "English", "Português"};

    public static void setLanguage(String tag) {
        AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(tag));
    }

    public static int getCurrentIndex() {
        LocaleListCompat list = AppCompatDelegate.getApplicationLocales();
        if (list.isEmpty() || list.get(0) == null) return 0;
        String lang = list.get(0).getLanguage();
        for (int i = 1; i < TAGS.length; i++) {
            if (TAGS[i].equals(lang)) return i;
        }
        return 0;
    }
}
