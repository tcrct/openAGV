package com.robot.utils;

import java.io.UnsupportedEncodingException;

public class EncodingUtils {
    public static String getString(byte[] data, int offset, int length, String charset) {
        if (data == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        } else if (charset != null && charset.length() != 0) {
            try {
                return new String(data, offset, length, charset);
            } catch (UnsupportedEncodingException var5) {
                return new String(data, offset, length);
            }
        } else {
            throw new IllegalArgumentException("charset may not be null or empty");
        }
    }

    public static String getString(byte[] data, String charset) {
        if (data == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        } else {
            return getString(data, 0, data.length, charset);
        }
    }

    public static byte[] getBytes(String data, String charset) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        } else if (charset != null && charset.length() != 0) {
            try {
                return data.getBytes(charset);
            } catch (UnsupportedEncodingException var3) {
                return data.getBytes();
            }
        } else {
            throw new IllegalArgumentException("charset may not be null or empty");
        }
    }

    public static byte[] getAsciiBytes(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        } else {
            try {
                return data.getBytes("US-ASCII");
            } catch (UnsupportedEncodingException var2) {
                throw new Error("HttpClient requires ASCII support");
            }
        }
    }

    public static String getAsciiString(byte[] data, int offset, int length) {
        if (data == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        } else {
            try {
                return new String(data, offset, length, "US-ASCII");
            } catch (UnsupportedEncodingException var4) {
                throw new Error("HttpClient requires ASCII support");
            }
        }
    }

    public static String getAsciiString(byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("Parameter may not be null");
        } else {
            return getAsciiString(data, 0, data.length);
        }
    }

    private EncodingUtils() {
    }

}
