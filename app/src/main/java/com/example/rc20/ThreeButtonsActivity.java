package com.example.rc20;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Objects;

public class ThreeButtonsActivity extends AppCompatActivity implements TcpObject.IOnActionOnTcp, SharedPreferences.OnSharedPreferenceChangeListener
{
    private TcpObject _tcpObj;
    private Menu _menu;
    Button buttonA;
    Button buttonB;
    Button buttonC;

    SharedPreferences sharedPreferences;
    private byte[] buttonAMessage;
    private byte[] buttonBMessage;
    private byte[] buttonCMessage;
    private Toast _toast;
    private ArrayList<Pair<Integer, Boolean>> incrementValueIndexA, incrementValueIndexB, incrementValueIndexC;
    private byte incrementForAck;
    private byte incrementForValue;
    Handler mHandler;

    Handler dimmerHandler;
    Runnable dimmerRunable;

    private String _userPassword;
    private int _dimmerTimeout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(null);
        setContentView(R.layout.activity_three_buttons);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                // This is where you do your work in the UI thread.
                // Your worker tells you in the message what to do.

                if (_toast != null) _toast.cancel();

                if (message.what == 1)
                {
                    _toast = Toast.makeText(getApplicationContext(), "Connected to server!", Toast.LENGTH_SHORT);
                    _toast.show();
                }
                else if(message.what == 0)
                {
                    _toast = Toast.makeText(getApplicationContext(), "Disconnected from server!", Toast.LENGTH_SHORT);
                    _toast.show();
                }
                else if(message.what == 2)
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


        _tcpObj = new TcpObject(this, this, this, mHandler, this);

        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);
        buttonC = findViewById(R.id.buttonC);


        incrementValueIndexA = new ArrayList<Pair<Integer, Boolean>>();
        incrementValueIndexB = new ArrayList<Pair<Integer, Boolean>>();
        incrementValueIndexC = new ArrayList<Pair<Integer, Boolean>>();

        incrementForAck = 0x01;
        incrementForValue = 0x01;


        SetColorsAndLabelsOfButtons();

        if (_tcpObj.IsRunning())
        {
            buttonA.setEnabled(true);
            buttonB.setEnabled(true);
            buttonC.setEnabled(true);
        }
        else
        {
            buttonA.setEnabled(false);
            buttonB.setEnabled(false);
            buttonC.setEnabled(false);
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
                else
                {
                    //ShowToasMessage("Connect to server first.");
                }
            }
        });

        buttonB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses buttonA
                if (_tcpObj != null && _tcpObj.IsRunning() && buttonBMessage.length != 0)
                {
                    for (Pair<Integer, Boolean> iter : incrementValueIndexB)
                    {
                        if (iter.second == true)
                        {
                            buttonBMessage[iter.first] = incrementForAck;
                            incrementForAck++;
                        }
                        else
                        {
                            buttonBMessage[iter.first] = incrementForValue;
                            incrementForValue++;
                        }
                    }

                    _tcpObj.sendMsg(buttonBMessage);
                }
                else
                {
                    //ShowToasMessage("Connect to server first.");
                }
            }
        });

        buttonC.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses buttonA
                if (_tcpObj != null && _tcpObj.IsRunning() && buttonCMessage.length != 0)
                {
                    for (Pair<Integer, Boolean> iter : incrementValueIndexC)
                    {
                        if (iter.second == true)
                        {
                            buttonCMessage[iter.first] = incrementForAck;
                            incrementForAck++;
                        }
                        else
                        {
                            buttonCMessage[iter.first] = incrementForValue;
                            incrementForValue++;
                        }
                    }

                    _tcpObj.sendMsg(buttonCMessage);
                }
                else
                {
                    //ShowToasMessage("Connect to server first.");
                }
            }
        });

        dimmerHandler = new Handler();
        dimmerRunable = new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                //Toast.makeText(MainActivity.this, "user is inactive from last 5 minutes",Toast.LENGTH_SHORT).show();

                // Get app context object.
                Context context = getApplicationContext();

                // Check whether has the write settings permission or not.
                boolean settingsCanWrite = Utils.hasWriteSettingsPermission(context);

                // If do not have then open the Can modify system settings panel.
                if(!settingsCanWrite) {
                    Utils.changeWriteSettingsPermission(context);
                }else {
                    Utils.changeScreenBrightness(context, 20);
                }
            }
        };
        startHandler();
    }

    private void SetColorsAndLabelsOfButtons()
    {
        _userPassword = Objects.requireNonNull(sharedPreferences.getString("edit_text_user_password", "user"));

        _dimmerTimeout = 1000 * Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("list_pref_dimmer_time", "120")));


        String buttonAColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_three_first_button_color", "#FFFFFF"));
        String buttonBColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_three_second_button_color", "#FFFFFF"));
        String buttonCColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_three_third_button_color", "#FFFFFF"));

        buttonA.setBackgroundColor(Color.parseColor(buttonAColor));
        buttonB.setBackgroundColor(Color.parseColor(buttonBColor));
        buttonC.setBackgroundColor(Color.parseColor(buttonCColor));

        String buttonALabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_three_first_button_text", "ButtonA"));
        String buttonBLabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_three_second_button_text", "ButtonB"));
        String buttonCLabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_three_third_button_text", "ButtonC"));

        buttonA.setText(buttonALabel);
        buttonB.setText(buttonBLabel);
        buttonC.setText(buttonCLabel);


        try
        {
            incrementValueIndexA.clear();
            buttonAMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_three_first_button_message", "14 02 02 01")), "A");
        }
        catch (Exception e)
        {
            incrementValueIndexA.clear();
            incrementValueIndexA.add(new Pair<Integer, Boolean>(14, true));
            buttonAMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x01, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
        }


        try
        {
            incrementValueIndexB.clear();
            buttonBMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_three_second_button_message", "14 02 02 01")), "B");
        }
        catch (Exception e)
        {
            incrementValueIndexB.clear();
            incrementValueIndexB.add(new Pair<Integer, Boolean>(14, true));
            buttonBMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x02, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
        }


        try
        {
            incrementValueIndexC.clear();
            buttonCMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_three_third_button_message", "14 02 02 01")), "C");
        }
        catch (Exception e)
        {
            incrementValueIndexC.clear();
            incrementValueIndexC.add(new Pair<Integer, Boolean>(14, true));
            buttonCMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x03, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.commonmenus, menu);
        MenuItem item = menu.findItem(R.id.menu_threeButtons);
        item.setEnabled(false);
        _menu = menu;

        if (_tcpObj.IsRunning())
        {
            item = menu.findItem(R.id.menu_connection);
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));
        }

        return true;
    }

    private int GetPort()
    {
        return Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("edit_text_port", "9750")));
    }

    private String GetAddress()
    {
        return Objects.requireNonNull(sharedPreferences.getString("edit_text_address", "192.168.200.187"));
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        if (_toast != null) _toast.cancel();
        //sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        if (id == R.id.menu_settings)
        {
            showSettingsInputDialog();
        }
        else if (id == R.id.menu_oneButton)
        {
            Disconnect(true);
            startActivity(new Intent(this, MainActivity.class));

            finish();
        }
        else if (id == R.id.menu_twoButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, TwoButtonsActivity.class));

            finish();
        }
        else if (id == R.id.menu_fourButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, FourButtonsActivity.class));
            finish();

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
                    buttonB.setEnabled(true);
                    buttonC.setEnabled(true);
                }
                else
                {
                    Disconnect(true);
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_disconnected));

                    buttonA.setEnabled(false);
                    buttonB.setEnabled(false);
                    buttonC.setEnabled(false);
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

    private void showSettingsInputDialog()
    {
        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View promptView = layoutInflater.inflate(R.layout.password_input_dialog, null);
        android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(this);
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
        Intent intent = new Intent(this, SettingsActivity.class);

        Bundle bundle = new Bundle();
        bundle.putBoolean("normalSettings", normalSettings);
        intent.putExtras(bundle);
        startActivity(intent);
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

    private void Connect()
    {
        _tcpObj.Connect(GetAddress(), GetPort(), false);
    }

    private void Disconnect(boolean fromUi)
    {
        _tcpObj.Disconnect(fromUi);
    }

    private boolean byteArrayOnlyZeros(final byte[] array)
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
                        buttonB.setEnabled(false);
                        buttonC.setEnabled(false);
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
                        buttonB.setEnabled(true);
                        buttonC.setEnabled(true);
                    }
                }
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
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


        if (key.equals("list_pref_three_first_button_color"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonA.setBackgroundColor(Color.parseColor(newValue));
        }

        if (key.equals("list_pref_three_second_button_color"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonB.setBackgroundColor(Color.parseColor(newValue));
        }

        if (key.equals("list_pref_three_third_button_color"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonC.setBackgroundColor(Color.parseColor(newValue));
        }

        if (key.equals("edit_text_three_first_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonA.setText(newValue);
        }

        if (key.equals("edit_text_three_second_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonB.setText(newValue);
        }

        if (key.equals("edit_text_three_third_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonC.setText(newValue);
        }

        if (key.equals("edit_text_three_first_button_message"))
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
                buttonAMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x01, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
            }
        }

        if (key.equals("edit_text_three_second_button_message"))
        {
            String newValue = sharedPreferences.getString(key, "");

            try
            {
                incrementValueIndexB.clear();
                buttonBMessage = ParseMessageFromSettingsWithIncementPosition(newValue, "B");
            }
            catch (Exception e)
            {
                incrementValueIndexB.clear();
                incrementValueIndexB.add(new Pair<Integer, Boolean>(14, true));
                buttonBMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x02, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
            }
        }

        if (key.equals("edit_text_three_third_button_message"))
        {
            String newValue = sharedPreferences.getString(key, "");

            try
            {
                incrementValueIndexC.clear();
                buttonCMessage = ParseMessageFromSettingsWithIncementPosition(newValue, "C");
            }
            catch (Exception e)
            {
                incrementValueIndexC.clear();
                incrementValueIndexC.add(new Pair<Integer, Boolean>(14, true));
                buttonCMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x03, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
            }
        }
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
                    if (message.equals("B"))
                    {
                        if (data[i / 2 - 3] == 0x11 && data[i / 2 - 2] == 0x02 && data[i / 2 - 1] == 0x0a)
                        {
                            incrementValueIndexB.add(new Pair<Integer, Boolean>(i / 2, true));
                        }
                        else
                        {
                            incrementValueIndexB.add(new Pair<Integer, Boolean>(i / 2, false));
                        }

                        data[i / 2] = (byte) 0xff;
                    }
                    if (message.equals("C"))
                    {
                        if (data[i / 2 - 3] == 0x11 && data[i / 2 - 2] == 0x02 && data[i / 2 - 1] == 0x0a)
                        {
                            incrementValueIndexC.add(new Pair<Integer, Boolean>(i / 2, true));
                        }
                        else
                        {
                            incrementValueIndexC.add(new Pair<Integer, Boolean>(i / 2, false));
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
}

