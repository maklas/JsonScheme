package ru.maklas.jscheme;

import java.util.Locale;

class Utils {

    public static String rightPad(final String str, final int size, final char padChar) {
        if (str == null) {
            return null;
        }
        final int pads = size - str.length();
        if (pads <= 0) {
            return str; // returns original String when possible
        }
        return str.concat(repeat(padChar, pads));
    }

    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    public static String df(double d) {
        return String.format(Locale.ENGLISH, "%.01f", d);
    }

    public static String limit (String s, int len, String suffix) {
        if (s.length() < len) return s;
        return s.substring(0, len) + suffix;
    }
}
