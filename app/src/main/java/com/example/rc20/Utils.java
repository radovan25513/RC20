package com.example.rc20;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

public class Utils
{

    // Check whether this app has android write settings permission.
    public static boolean hasWriteSettingsPermission(Context context)
    {
        boolean ret = true;
        // Get the result from below code.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            ret = Settings.System.canWrite(context);
        }
        return ret;
    }

    // Start can modify system settings panel to let user change the write settings permission.
    public static void changeWriteSettingsPermission(Context context)
    {
        Intent intent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
        {
            intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    // it can not take effect in android emulator.
    public static void changeScreenBrightness(Context context, int screenBrightnessValue)
    {
        // Change the screen brightness change mode to manual.
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
        // Apply the screen brightness value to the system, this will change the value in Settings ---> Display ---> Brightness level.
        // It will also change the screen brightness for the device.
        Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightnessValue);

        /*
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = screenBrightnessValue / 255f;
        window.setAttributes(layoutParams);
        */
    }

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
