package com.example.rc20;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TcpSingleton.IOnActionOnTcp
{
    private static final int REQUEST_PERMISSION_PHONE_STATE = 1;
    private TcpSingleton sing;
    private static MainActivity instance;
    public static int port;
    public static String address;
    public static int rc_id;
    public static boolean reconnect;
    SharedPreferences sharedPref;
    private Menu _menu;
    Button button;
    public static boolean enterHideMenu = false;
    String imeiFromDevice;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CheckPermissionAndStartIntent();

        instance = this;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        button = findViewById(R.id.singleButton);

        sing = TcpSingleton.getInstance(GetAddress(), GetPort(), this, this, this);

        if (sing.GetRunning())
        {
            button.setEnabled(true);
        }
        else
        {
            button.setEnabled(false);
        }


        button.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses button
                int id = rc_id;
                char[] message = {0x80, 0x02, 0x00, 0x0B, 0x14, 0x02, 0x02, (char) GetRcId(), 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};


                if (sing != null && sing.GetRunning())
                {
                    sing.sendMsg(message);
                }
                else
                {
                    ShowToasMessage("Connect to server first.");
                }
            }
        });

        ReadPreferenceValues();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.commonmenus, menu);
        MenuItem item = menu.findItem(R.id.menu_oneButton);
        item.setEnabled(false);

        _menu = menu;

        if (sing.GetRunning())
        {
            item = menu.findItem(R.id.menu_connection);
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_settings)
        {
            showInputDialog();
        }
        else if (id == R.id.menu_oneButton)
        {
            //startActivity(new Intent(this, MainActivity.class));
        }
        else if (id == R.id.menu_fourButtons)
        {
            startActivity(new Intent(this, MultipleButtonsActivity.class));

        }
        else if (id == R.id.menu_connection)
        {
            if (CheckImei())
            {
                if (sing == null || !sing.GetRunning())
                {
                    Connect();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));
                    button.setEnabled(true);
                }
                else
                {
                    Disconnect();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_disconnected));
                    button.setEnabled(false);
                }
            }
            else
            {
                ShowToasMessage("Bad IMEI configuration. Please contact CEIT customer support.");
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private void Connect()
    {
        TcpSingleton.Connect(GetAddress(), GetPort());
    }

    private boolean CheckImei()
    {
        String imeiFromSettings = Objects.requireNonNull(sharedPref.getString("edit_text_imei", "00000000000000"));

        imeiFromDevice = getDeviceId(this);

        if (imeiFromSettings.equals(imeiFromDevice))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private void CheckPermissionAndStartIntent() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            //SEY SOMTHING LIKE YOU CANT ACCESS WITHOUT PERMISSION
        } else {
            doSomthing();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doSomthing();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    //SEY SOMTHING LIKE YOU CANT ACCESS WITHOUT PERMISSION
                    //you can show something to user and open setting -> apps -> youApp -> permission
                    // or unComment below code to show permissionRequest Again
                    //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                }
            }
        }
    }


    void doSomthing() {
        imeiFromDevice = getDeviceId(MainActivity.this);
    }

    public String getDeviceId(Context context)
    {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
        {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        return telephonyManager.getDeviceId();
    }

    private void Disconnect()
    {
        TcpSingleton.Disconnect(true);
    }

    public void ShowToasMessage(final String message)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

                Toast.makeText(GetInstance(), message,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void ReadPreferenceValues()
    {
        // Read settings
        boolean hovno = tryParseInt(sharedPref.getString("edit_text_port", "9750"));
        if (hovno)
        {
            port = Integer.parseInt(Objects.requireNonNull(sharedPref.getString("edit_text_port", "9750")));
        }


        hovno = tryParseInt(sharedPref.getString("edit_text_rc_id", "1"));
        if (hovno)
        {
            rc_id = Integer.parseInt(Objects.requireNonNull(sharedPref.getString("edit_text_rc_id", "1")));
        }


        address = Objects.requireNonNull(sharedPref.getString("edit_text_address", "192.168.200.187"));


        reconnect = sharedPref.getBoolean("switch_autoconnect", true);
    }

    String GetAddress()
    {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return address = Objects.requireNonNull(sharedPref.getString("edit_text_address", "192.168.200.187"));
    }

    int GetPort()
    {
        boolean hovno = tryParseInt(sharedPref.getString("edit_text_port", "9750"));

        return hovno ? port = Integer.parseInt(Objects.requireNonNull(sharedPref.getString("edit_text_port", "9750"))) : 9750;
    }

    int GetRcId()
    {
        boolean hovno = tryParseInt(sharedPref.getString("edit_text_rc_id", "1"));

        return hovno ? rc_id = Integer.parseInt(Objects.requireNonNull(sharedPref.getString("edit_text_rc_id", "1"))) : 1;
    }

    boolean GetAutoReconnect()
    {
        return sharedPref.getBoolean("switch_autoconnect", true);
    }

    boolean tryParseInt(String value)
    {
        try
        {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e)
        {
            return false;
        }
    }

    @Override
    public void onReceive(byte[] message)
    {
        String skuska = "sad";

        int kurvafix = message[3];
        if (message[3] == 0x0B)
        {
            //ShowToasMessage("Connect to server first.");
        }
        else
        {
            //ShowToasMessage("Connect to server first.");
        }
    }


    @Override
    public void onDisconnect()
    {
        runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                if (_menu != null)
                {
                    MenuItem item = _menu.findItem(R.id.menu_connection);
                    if (item != null)
                    {
                        item.setIcon(R.drawable.ic_action_disconnected);
                        button.setEnabled(false);
                    }
                }
            }
        });
    }

    @Override
    public void onConnect()
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (_menu != null)
                {
                    MenuItem item = _menu.findItem(R.id.menu_connection);
                    if (item != null)
                    {
                        item.setIcon(R.drawable.ic_action_connected);
                        button.setEnabled(true);
                    }
                }
            }
        });
    }

    public static MainActivity GetInstance()
    {
        return instance;
    }

    protected void showInputDialog()
    {
        enterHideMenu = false;

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);


        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                String inputPass = editText.getText().toString();
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(GetInstance());
                String passInSettings = Objects.requireNonNull(sharedPref.getString("edit_text_password", "ceit"));


                if (inputPass.equals("ceit") || inputPass.equals(passInSettings) || inputPass.equals("super"))
                {
                    if (inputPass.equals("super"))
                    {
                        enterHideMenu = true;
                    }
                    startActivity(new Intent(GetInstance(), SettingsActivity.class));
                }
            }
        }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        dialog.cancel();
                    }
                });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}
