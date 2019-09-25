package org.imdc.zwavedriver.zwave;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class Hex {

    public static byte stringToByte(String s) {
        return stringToByteArray(s)[0];
    }

    public static int stringToInt(String s) {
        return new Long(Long.parseLong(s, 16)).intValue();
    }

    public static byte[] stringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] hexToByteArray(String s) {
        return hexToByteArray(s, 16);
    }

    public static byte[] hexToByteArray(String s, int arrayLength) {
        try {
            if (s != null && s.length() > 0) {
                s = s.replace(",", "");
                s = s.replace(" ", "");
                s = s.replace("0x", "");
                return Hex.stringToByteArray(s);
            } else {
                return new byte[arrayLength];
            }
        } catch (Exception ex) {
            return new byte[arrayLength];
        }
    }

    public static String asString(byte value) {
        return String.format("%02X", value);
    }

    public static String asString(int value) {
        return String.format("%04X", value);
    }

    public static String asString(byte[] message) {
        return asString(message, ",");
    }

    public static String asString(byte[] message, String separator) {
        String data = new String();
        String sep = "";
        if(message != null) {
            for (byte b : message) {
                data += String.format("%s%02X", sep, b);
                sep = separator;
            }
        }
        return data;
    }

    public static String asString(List<?> messages) {
        return asString(messages, ",");
    }

    public static String asString(List<?> messages, String separator) {
        String data = new String();
        String sep = "";
        if(messages != null) {
            for (Object b : messages) {
                if (b instanceof Byte) {
                    data += String.format("%s%02X", sep, b);
                } else {
                    data += String.format("%s%s", sep, b.toString());
                }
                sep = separator;
            }
        }
        return data;
    }

    public static List<Integer> getMaskInts(ByteArrayInputStream in, int length) {
        return getMaskInts(in, length, 0);
    }

    public static List<Integer> getMaskInts(ByteArrayInputStream in, int length, int offset) {
        List<Integer> ret = new ArrayList();
        for (int i = 0; i < length; i++) {
            int mask = in.read();
            for (int b = 0; b < 8; b++) {
                if (((mask >> b) & 0x01) != 0) {
                    ret.add((i * 8) + b + offset);
                }
            }
        }
        return ret;
    }
}
