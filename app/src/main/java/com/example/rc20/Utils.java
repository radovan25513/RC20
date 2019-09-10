package com.example.rc20;

public class Utils
{
    public static boolean tryParseInt(String value)
    {
        try
        {
            Integer.parseInt(value);
            return true;
        }
        catch (NumberFormatException e)
        {
            return false;
        }
    }

    public static boolean byteArrayOnlyZeros(final byte[] array)
    {
        int hits = 0;
        for (byte b : array)
        {
            if (b != 0)
            {
                hits++;
            }
        }
        return (hits == 0);
    }

    public static byte[] ParseMessageFromSettings(String s)
    {
        s = s.replaceAll("\\s+", "");
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static boolean checkAckResponse(byte[] message)
    {
        if (message[8] == 0x11 && message[9] == 0x02 && message[10] == 0x0b && message[11] == 0x02)
            return true;

        return false;
    }

    public static boolean checkAckResponse(byte[] message, byte incrementValue)
    {
        if (message[8] == 0x11 && message[9] == 0x02 && message[10] == 0x0b && message[11] == incrementValue)
            return true;

        return false;
    }

}
