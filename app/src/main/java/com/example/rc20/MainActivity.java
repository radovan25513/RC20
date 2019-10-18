package com.example.rc20;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.rc20.Utils.byteArrayOnlyZeros;

public class MainActivity extends AppCompatActivity implements TcpObject.IOnActionOnTcp, SharedPreferences.OnSharedPreferenceChangeListener
{
    private TcpObject _tcpObj;
    static SharedPreferences sharedPreferences;

    private int _port;
    private String _address;
    private int _rc_id;
    private boolean _reconnect;
    private int _idOfStartingActivity;
    private Menu _menu;
    Button buttonA;
    String imeiFromDevice;
    private String _hardcodedImei = "";
    private String _hardcodedImeisony = "353748087140429";
    private String _hardcodedImeihuawei = "869785029868615";
    private String _hardcodedImeinemak1 = "358508072457716";
    private String _hardcodedImeinemak2 = "869785029868615";
    private String _hardcodedImeiEmu = "358240051111110";


    private byte[] buttonAMessage;
    private ArrayList<Pair<Integer, Boolean>> incrementValueIndexA;
    private byte incrementForAck;
    private byte incrementForValue;

    Toast _toast;
    Handler mHandler;

    Handler dimmerHandler;
    Runnable dimmerRunable;
    private String _userPassword;
    private int _dimmerTimeout;


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


        mHandler = new Handler(Looper.getMainLooper())
        {
            @Override
            public void handleMessage(Message message)
            {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.

                if (_toast != null) _toast.cancel();

                if (message.what == 1)
                {
                    _toast = Toast.makeText(getApplicationContext(), "Connected to server!", Toast.LENGTH_SHORT);
                    _toast.show();
                }
                else if (message.what == 0)
                {
                    _toast = Toast.makeText(getApplicationContext(), "Disconnected from server!", Toast.LENGTH_SHORT);
                    _toast.show();
                }
                else if (message.what == 2)
                {
                    _toast = Toast.makeText(getApplicationContext(), "Message has been sent successfully!", Toast.LENGTH_SHORT);
                    _toast.show();
                }
                else
                {

                }
            }
        };

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setupSharedPreferences();
        saveImeiToSharedPreference();


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        buttonA = findViewById(R.id.singleButton);
        incrementValueIndexA = new ArrayList<>();
        incrementForAck = 0x01;
        incrementForValue = 0x01;

        SetColorsAndLabelsOfButtons();

        _tcpObj = new TcpObject(this, this, this, mHandler, this);

        if (_tcpObj.IsRunning())
        {
            buttonA.setEnabled(true);
        }
        else
        {
            buttonA.setEnabled(false);
        }


        buttonA.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses buttonA

                if (_tcpObj != null && _tcpObj.IsRunning() && buttonAMessage.length != 0)
                {
                    for (Pair<Integer, Boolean> iter : incrementValueIndexA)
                    {
                        if (iter.second == true)
                        {
                            buttonAMessage[iter.first] = incrementForAck;
                            incrementForAck++;
                        }
                        else
                        {
                            buttonAMessage[iter.first] = incrementForValue;
                            incrementForValue++;
                        }
                    }

                    _tcpObj.sendMsg(buttonAMessage);
                }
            }
        });

        if (_idOfStartingActivity == 3)
        {
            Intent aboutScreen = new Intent(MainActivity.this, ThreeButtonsActivity.class);
            this.startActivity(aboutScreen);
        }
        else if (_idOfStartingActivity == 4)
        {
            Intent aboutScreen = new Intent(MainActivity.this, FourButtonsActivity.class);
            this.startActivity(aboutScreen);
        }
        else if (_idOfStartingActivity == 2)
        {
            Intent aboutScreen = new Intent(MainActivity.this, TwoButtonsActivity.class);
            this.startActivity(aboutScreen);
        }
        else
        {
            //Connect();
        }

        dimmerHandler = new Handler();
        dimmerRunable = new Runnable()
        {

            @Override
            public void run()
            {
                // TODO Auto-generated method stub
                //Toast.makeText(MainActivity.this, "user is inactive from last 5 minutes",Toast.LENGTH_SHORT).show();

                // Get app context object.
                Context context = getApplicationContext();

                // Check whether has the write settings permission or not.
                boolean settingsCanWrite = Utils.hasWriteSettingsPermission(context);

                // If do not have then open the Can modify system settings panel.
                if (!settingsCanWrite)
                {
                    Utils.changeWriteSettingsPermission(context);
                }
                else
                {
                    Utils.changeScreenBrightness(context, 10);
                }
            }
        };
        startHandler();
    }

    private void stopHandler()
    {
        dimmerHandler.removeCallbacks(dimmerRunable);
    }

    private void startHandler()
    {
        dimmerHandler.postDelayed(dimmerRunable, _dimmerTimeout); //for 5 minutes
    }

    @Override
    public void onUserInteraction()
    {
        // TODO Auto-generated method stub
        super.onUserInteraction();

        boolean settingsCanWrite = Utils.hasWriteSettingsPermission(getApplicationContext());

        // If do not have then open the Can modify system settings panel.
        if (!settingsCanWrite)
        {
            Utils.changeWriteSettingsPermission(getApplicationContext());
        }
        else
        {
            Utils.changeScreenBrightness(getApplicationContext(), 100);
        }

        stopHandler();//stop first and then start
        startHandler();
    }

    private void SetColorsAndLabelsOfButtons()
    {
        String buttonAColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_one_button_color", "#FFFFFF"));

        buttonA.setBackgroundColor(Color.parseColor(buttonAColor));

        String buttonALabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_one_button_text", "ButtonA"));

        buttonA.setText(buttonALabel);


        try
        {
            incrementValueIndexA.clear();
            buttonAMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_one_button_message", "14 02 02 01")), "A");
        }
        catch (Exception e)
        {
            incrementValueIndexA.clear();
            incrementValueIndexA.add(new Pair<Integer, Boolean>(14, true));
            buttonAMessage = new byte[]{(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x01, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
        }
    }

    private void saveImeiToSharedPreference()
    {
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.edit().putString("deviceImei", getDeviceId(this)).apply();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.commonmenus, menu);
        MenuItem item = menu.findItem(R.id.menu_oneButton);
        item.setEnabled(false);

        _menu = menu;

        if (_tcpObj.IsRunning())
        {
            item = menu.findItem(R.id.menu_connection);
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));
        }

        return true;
    }

    private void showSettingsInputDialog()
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

                if (inputPass.equals("Fiskijeladyboy123") || inputPass.equals(_userPassword) || inputPass.equals("radko"))
                {
                    if (inputPass.equals("radko") || inputPass.equals("Fiskijeladyboy123"))
                    {
                        startSettingsActivity(false);
                    }
                    else
                    {
                        startSettingsActivity(true);
                    }
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

    private byte[] ParseMessageFromSettingsWithIncementPosition(String s, String message)
    {
        try
        {
            s = s.replaceAll("\\s+", "");
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2)
            {
                if ((s.charAt(i) == 'x' && s.charAt(i + 1) == 'x') || (s.charAt(i) == 'X' && s.charAt(i + 1) == 'X'))
                {
                    if (message.equals("A"))
                    {
                        if (data[i / 2 - 3] == 0x11 && data[i / 2 - 2] == 0x02 && data[i / 2 - 1] == 0x0a)
                        {
                            incrementValueIndexA.add(new Pair<Integer, Boolean>(i / 2, true));
                        }
                        else
                        {
                            incrementValueIndexA.add(new Pair<Integer, Boolean>(i / 2, false));
                        }

                        data[i / 2] = (byte) 0xff;
                    }
                }
                else
                {
                    data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                            + Character.digit(s.charAt(i + 1), 16));
                }
            }
            return data;
        }
        catch (Exception e)
        {
            if (_toast != null) _toast.cancel();

            _toast = Toast.makeText(getApplicationContext(), "Exception while parsing button message: " + e.getMessage() + ". Default message has been set!", Toast.LENGTH_LONG);
            _toast.show();

            throw e;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up buttonA, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_settings)
        {
            showSettingsInputDialog();
        }
        else if (id == R.id.menu_oneButton)
        {
            startActivity(new Intent(this, MainActivity.class));
        }
        else if (id == R.id.menu_twoButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, TwoButtonsActivity.class));
        }
        else if (id == R.id.menu_threeButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, ThreeButtonsActivity.class));
        }
        else if (id == R.id.menu_fourButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, FourButtonsActivity.class));
        }
        else if (id == R.id.menu_connection)
        {
            if (CheckImei())
            {
                if (_tcpObj == null || !_tcpObj.IsRunning())
                {
                    Connect();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));
                    buttonA.setEnabled(true);
                }
                else
                {
                    Disconnect(true);
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_disconnected));
                    buttonA.setEnabled(false);
                }
            }
            else
            {
                if (_toast != null) _toast.cancel();

                _toast = Toast.makeText(getApplicationContext(), "Bad IMEI configuration. Please contact CEIT customer support.", Toast.LENGTH_SHORT);
                _toast.show();
            }
        }

        return super.onOptionsItemSelected(item);
    }


    private void Connect()
    {
        _tcpObj.Connect(GetAddress(), GetPort(), false);
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
            //ShowToasMessage("Permission Denied\n" + deniedPermissions.toString());
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
        _tcpObj.Disconnect(fromUi);
    }

    @Override
    public void onReceive(byte[] message)
    {
        if (byteArrayOnlyZeros(message))
        {
            Disconnect(false);
        }

        if (Utils.checkAckResponse(message, incrementForAck))
        {
            if (_toast != null) _toast.cancel();

            mHandler.obtainMessage(2).sendToTarget();
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        //_tcpObj.getInstance(this, this, this, mHandler);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if (_toast != null) _toast.cancel();
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
                        buttonA.setEnabled(false);
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
                        buttonA.setEnabled(true);
                    }
                }
            }
        });
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

        _userPassword = Objects.requireNonNull(sharedPreferences.getString("edit_text_user_password", "user"));

        _dimmerTimeout = 1000 * Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("list_pref_dimmer_time", "120")));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("list_pref_one_button_color"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonA.setBackgroundColor(Color.parseColor(newValue));
        }

        if (key.equals("edit_text_one_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonA.setText(newValue);
        }

        if (key.equals("edit_text_user_password"))
        {
            String newValue = sharedPreferences.getString(key, "user");

            _userPassword = newValue;
        }

        if (key.equals("list_pref_dimmer_time"))
        {
            String newValue = sharedPreferences.getString(key, "120");

            _dimmerTimeout = 1000 * Integer.parseInt(Objects.requireNonNull(newValue));
        }

        if (key.equals("edit_text_one_button_message"))
        {
            String newValue = sharedPreferences.getString(key, "");

            try
            {
                incrementValueIndexA.clear();
                buttonAMessage = ParseMessageFromSettingsWithIncementPosition(newValue, "A");
            }
            catch (Exception e)
            {
                incrementValueIndexA.clear();
                incrementValueIndexA.add(new Pair<Integer, Boolean>(14, true));
                buttonAMessage = new byte[]{(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x01, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
            }
        }
    }
}