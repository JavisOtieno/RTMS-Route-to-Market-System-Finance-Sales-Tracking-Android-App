package com.scg.sf.util;
import android.text.InputFilter;
import android.text.Spanned;

public class LongInputFilter implements InputFilter {
    private static final long MAX_LONG = Long.MAX_VALUE;

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String newValue = dest.toString() + source.toString();
            long input = Long.parseLong(newValue);
            if (input <= MAX_LONG) {
                return null; // Accept the input
            }
        } catch (NumberFormatException e) {
            // Input is not a valid long, reject it
        }
        return ""; // Reject the input
    }
}
