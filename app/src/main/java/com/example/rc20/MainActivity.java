package com.example.rc20;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements TcpSingleton.IOnActionOnTcp
{
    private TcpSingleton sing;
    private MainActivity instance;
    static SharedPreferences sharedPreferences;

    private int _port;
    private String _address;
    private int _rc_id;
    private boolean _reconnect;
    private int _idOfStartingActivity;
    private Menu _menu;
    Button button_single;
    String imeiFromDevice;
    private String _hardcodedImei = "358508072457716";
    private String _hardcodedImeisony = "353748087140429";
    private String _hardcodedImeihuawei = "869785029868615";
    private String _hardcodedImeinemak1 = "358508072457716";
    private String _hardcodedImeinemak2 = "869785029868615";

    Toast _toast;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.INTERNET, Manifest.permission.RECEIVE_BOOT_COMPLETED)
                .check();


        setupSharedPreferences();
        saveImeiToSharedPreference();


        if (android.os.Build.VERSION.SDK_INT > 9)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        instance = this;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        button_single = findViewById(R.id.singleButton);

        TcpSingleton.initContext(this);
        sing = TcpSingleton.getInstance(this, this, this);

        if (sing.IsRunning())
        {
            button_single.setEnabled(true);
        }
        else
        {
            button_single.setEnabled(false);
        }


        button_single.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                byte id = (byte) _rc_id;
                // Code here executes on main thread after user presses button_single
                byte[] message = {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, (byte) (id & 0xff), 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};


                if (sing != null && sing.IsRunning())
                {
                    sing.sendMsg(message);
                }
                else
                {
                    ShowToasMessage("Connect to server first!");
                }
            }
        });

        setupSharedPreferences();

        if (_idOfStartingActivity == 3)
        {
            Intent aboutScreen = new Intent(MainActivity.this, ThreeButtonsActivity.class);
            this.startActivity(aboutScreen);
        }
        else if (_idOfStartingActivity == 4)
        {
            Intent aboutScreen = new Intent(MainActivity.this, MultipleButtonsActivity.class);
            this.startActivity(aboutScreen);
        }
        else
        {
            //Connect();
        }
    }

    private void saveImeiToSharedPreference()
    {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("deviceImei", getDeviceId(this)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.commonmenus, menu);
        MenuItem item = menu.findItem(R.id.menu_oneButton);
        item.setEnabled(false);

        _menu = menu;

        if (sing.IsRunning())
        {
            item = menu.findItem(R.id.menu_connection);
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));
        }

        return true;
    }

    private void showSettingsInputDialog(final boolean fromBoot)
    {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.password_input_dialog, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);


        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);


        // setup a dialog window
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                String inputPass = editText.getText().toString();

                if (fromBoot)
                {
                    if (inputPass.equals("superceit"))
                    {
                        startSettingsActivity(false);
                    }
                    else
                    {
                        finish();
                        System.exit(0);
                    }
                }
                else
                {
                    if (inputPass.equals("ceit") || inputPass.equals("superceit"))
                    {
                        if (inputPass.equals("superceit"))
                        {
                            startSettingsActivity(false);
                        }
                        else
                        {
                            startSettingsActivity(true);
                        }
                    }
                }
            }
        }).setNegativeButton("Cancel",
                new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        if (fromBoot)
                        {
                            finish();
                            System.exit(0);
                        }
                        else
                        {
                            dialog.cancel();
                        }
                    }
                });

        // create an alert dialog
        android.app.AlertDialog alert = alertDialogBuilder.create();
        Objects.requireNonNull(alert.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alert.show();
    }

    private void startSettingsActivity(boolean normalSettings)
    {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);

        Bundle bundle = new Bundle();
        bundle.putBoolean("normalSettings", normalSettings);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button_single, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_settings)
        {
            showSettingsInputDialog(false);
        }
        else if (id == R.id.menu_oneButton)
        {
            //startActivity(new Intent(this, MainActivity.class));
        }
        else if (id == R.id.menu_fourButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, MultipleButtonsActivity.class));
        }
        else if (id == R.id.menu_threeButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, ThreeButtonsActivity.class));
        }
        else if (id == R.id.menu_connection)
        {
            if (CheckImei())
            {
                if (sing == null || !sing.IsRunning())
                {
                    Connect();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));
                    button_single.setEnabled(true);
                }
                else
                {
                    Disconnect(true);
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_disconnected));
                    button_single.setEnabled(false);
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


    PermissionListener permissionlistener = new PermissionListener()
    {
        @Override
        public void onPermissionGranted()
        {
            //Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(List<String> deniedPermissions)
        {
            ShowToasMessage("Permission Denied\n" + deniedPermissions.toString());
        }
    };

    private boolean CheckImei()
    {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        String imeiFromSettings = Objects.requireNonNull(sharedPreferences.getString("edit_text_imei", "00000000000000"));
        String imeiFromDevice = Objects.requireNonNull(sharedPreferences.getString("deviceImei", "00000000000000"));


        if (imeiFromSettings.equals(imeiFromDevice))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case 1:
            {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    readImeiNumber();
                }
                else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED)
                {
                    //SEY SOMTHING LIKE YOU CANT ACCESS WITHOUT PERMISSION
                    //you can show something to user and open setting -> apps -> youApp -> permission
                    // or unComment below code to show permissionRequest Again
                    //ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
                }
            }
        }
    }


    void readImeiNumber()
    {
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

    private void Disconnect(boolean fromUi)
    {
        TcpSingleton.Disconnect(fromUi);
    }

    public void ShowToasMessage(final String message)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (_toast != null)
                {
                    _toast.cancel();

                    _toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
                else
                {
                    _toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onReceive(byte[] message)
    {
        if (Utils.byteArrayOnlyZeros(message))
        {
            Disconnect(false);
        }

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
    protected void onResume()
    {
        super.onResume();
        TcpSingleton.initContext(this);
        sing = TcpSingleton.getInstance(this, this, this);
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
                        button_single.setEnabled(false);
                    }
                }
            }
        });

        ShowToasMessage("Disconnected to server!");
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
                        button_single.setEnabled(true);
                    }
                }
            }
        });

        ShowToasMessage("Connected to server!");
    }

    private int GetPort()
    {
        return Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("edit_text_port", "9750")));
    }

    private String GetAddress()
    {
        return Objects.requireNonNull(sharedPreferences.getString("edit_text_address", "192.168.200.187"));
    }

    private void setupSharedPreferences()
    {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);

        // Read settings
        boolean hovno = Utils.tryParseInt(sharedPreferences.getString("edit_text_port", "9750"));
        if (hovno)
        {
            _port = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("edit_text_port", "9750")));
        }


        hovno = Utils.tryParseInt(sharedPreferences.getString("edit_text_rc_id", "1"));
        if (hovno)
        {
            _rc_id = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("edit_text_rc_id", "1")));
        }


        _address = Objects.requireNonNull(sharedPreferences.getString("edit_text_address", "192.168.200.187"));


        _reconnect = sharedPreferences.getBoolean("switch_autoconnect", true);

        _idOfStartingActivity = Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("list_pref_starting_activity", "1")));
    }
}