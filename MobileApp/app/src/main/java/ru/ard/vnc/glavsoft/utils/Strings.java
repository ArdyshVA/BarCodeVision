package ru.ard.vnc.glavsoft.utils;

public class Strings {
    public static String toString(byte[] byteArray) {
        StringBuilder sb = new StringBuilder("[");
        boolean notFirst = false;
        for (byte b : byteArray) {
            if (notFirst) {
                sb.append(", ");
            } else {
                notFirst = true;
            }
            sb.append((int) b);
        }
        return sb.append("]").toString();
    }

    public static boolean isTrimmedEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }
}
