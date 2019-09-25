package org.imdc.zwavedriver.zwave;

import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Format;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Size;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ByteUtilities {
    public static final int NUMBER_OF_NODE_BYTES = 29;

    public static int readInt16MSB(ByteArrayInputStream in) {
        return readIntMSB(in, Size.BIT16);
    }

    public static int readIntMSB(ByteArrayInputStream in, Size size) {
        return readIntMSB(in, size, Format.SIGNED_INTEGER);
    }

    public static double readIntMSBPrecision(ByteArrayInputStream in, Size size, int precision) {
        return readIntMSBPrecision(in, size, Format.SIGNED_INTEGER, precision);
    }

    public static int readIntMSB(ByteArrayInputStream in, Size size, Format format) {
        int value = in.read();
        for (int i = 1; i < size.value; i++) {
            value <<= 8;
            value |= in.read();
        }
        return value;
    }

    public static double readIntMSBPrecision(ByteArrayInputStream in, Size size, Format format, int precision) {
        int value = readIntMSB(in, size, format);
        return ((double) value) / Math.pow(10, precision);
    }

    public static String readString(ByteArrayInputStream in, int stringLength) {
        String name;
        byte[] nameData = new byte[stringLength];
        for (int i = 0; i < stringLength; i++) {
            nameData[i] = (byte) in.read();
        }
        return new String(nameData, StandardCharsets.UTF_8);
    }

    public static void writeInt16MSB(ByteArrayOutputStream result, int value) {
        writeIntMSB(result, Size.BIT16, value);
    }

    public static void writeIntMSB(ByteArrayOutputStream result, Size size, int value) {
        for (int i = (size.value - 1); i >= 0; i--) {
            result.write(value >> (i * 8) & 0xFF);
        }
    }

    public static void writeDouble(ByteArrayOutputStream result, Size size, int precision, double value) {
        int rawValue = Double.valueOf(value * Math.pow(10, precision)).intValue();
        writeIntMSB(result, size, rawValue);
    }

    public static byte[] encryptAES128OFB(byte[] key, byte[] iv, byte[] bytesToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/OFB/NOPADDING");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec ivparameterspec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivparameterspec);
            return cipher.doFinal(bytesToEncrypt);
        } catch (Exception ex) {
        }

        return new byte[0];
    }

    public static byte[] encryptAES128ECB(byte[] key, byte[] bytesToEncrypt) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return cipher.doFinal(bytesToEncrypt);
        } catch (Exception ex) {
        }

        return new byte[0];
    }

    public static byte[] decryptAES128OFB(byte[] key, byte[] iv, byte[] encryptedBytes) {
        try {
            Cipher cipher = Cipher.getInstance("AES/OFB/NOPADDING");
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");
            IvParameterSpec ivparameterspec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivparameterspec);
            return cipher.doFinal(encryptedBytes);
        } catch (Exception ex) {
        }

        return new byte[0];
    }

    public static byte[] combine(byte[] a, byte[] b) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(a);
            baos.write(b);
            return baos.toByteArray();
        } catch (Exception ex) {
        }

        return new byte[0];
    }

    public static int find(byte[] array, byte value) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == value) {
                return i;
            }
        }
        return array.length;
    }

    public static byte[] buffer(byte[] data, int size) {
        byte[] ret = new byte[size];
        for (int i = 0; i < data.length; i++) {
            ret[i] = data[i];
        }
        for (int i = 0; i < (size - data.length); i++) {
            ret[data.length + i] = 0;
        }
        return ret;
    }

    public static byte[] copy(byte[] input, int start, int end) {
        return Arrays.copyOfRange(input, start, end);
    }

    public static byte[] generateAuthData(byte[] key, byte[] iv, byte commandCode, int sendingNode, int receivingNode, byte[] commandData) {
        byte[] auth = ByteUtilities.copy(ByteUtilities.encryptAES128ECB(key, iv), 0, 16);
        byte[] buffer = new byte[commandData.length + 4];
        buffer[0] = commandCode;
        buffer[1] = (byte) sendingNode;
        buffer[2] = (byte) receivingNode;
        buffer[3] = (byte) commandData.length;
        for (int i = 0; i < commandData.length; i++) {
            buffer[4 + i] = commandData[i];
        }

        int block = 0;
        byte[] pack = new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i < buffer.length; i++) {
            pack[block] = buffer[i];
            block++;
            if (block == 16) {
                for (int j = 0; j < 16; j++) {
                    auth[j] = (byte) (pack[j] ^ auth[j]);
                    pack[j] = 0;
                }

                block = 0;
                auth = ByteUtilities.copy(ByteUtilities.encryptAES128ECB(key, auth), 0, 16);
            }
        }

        if (block > 0) {
            for (int j = 0; j < 16; j++) {
                auth[j] = (byte) (pack[j] ^ auth[j]);
            }

            auth = ByteUtilities.copy(ByteUtilities.encryptAES128ECB(key, auth), 0, 16);
        }

        return auth;
    }

    public static byte[] toByteArray(List<Byte> inList) {
        byte[] bytes = new byte[inList.size()];
        for (int i = 0; i < inList.size(); i++) {
            bytes[i] = inList.get(i);
        }

        return bytes;
    }

    public static String asString(List<Integer> messages) {
        return asString(messages, ",");
    }

    public static String asString(List<Integer> messages, String separator) {
        String data = new String();
        String sep = "";
        for (int b : messages) {
            data += String.format("%s%d", sep, b);
            sep = separator;
        }
        return data;
    }

    public static List<Byte> getNodesFromBitString(ByteArrayInputStream inputStream) {
        byte nodeId = 1;
        List<Byte> nodes = new ArrayList();
        for (int nodeByteCounter = 0; nodeByteCounter < NUMBER_OF_NODE_BYTES; nodeByteCounter++) {
            byte nodeByte = (byte) inputStream.read();
            for (int bit = 0; bit < 8; bit++) {
                if ((nodeByte & 0x01) != 0) {
                    nodes.add(nodeId);
                }
                nodeByte >>= 1;
                nodeId++;
            }
        }
        return nodes;
    }
}
