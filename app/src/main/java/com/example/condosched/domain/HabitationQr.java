package com.example.condosched.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HabitationQr {
    private static final Pattern FORMAT = Pattern.compile("condosched://unit/([1-9][0-9]*)");

    private HabitationQr() {
    }

    public static long parse(String content) {
        Matcher match = FORMAT.matcher(content == null ? "" : content.trim());
        if (!match.matches()) throw new IllegalArgumentException("QR inválido");
        try {
            return Long.parseLong(match.group(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("QR inválido", e);
        }
    }
}
