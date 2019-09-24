package com.example.rc20;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
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

import static com.example.rc20.Utils.byteArrayOnlyZeros;

public class FourButtonsActivity extends AppCompatActivity implements TcpSingleton.IOnActionOnTcp, SharedPreferences.OnSharedPreferenceChangeListener
{
    private TcpSingleton sing;
    private Menu _menu;
    Button buttonA;
    Button buttonB;
    Button buttonC;
    Button buttonD;

    SharedPreferences sharedPreferences;
    private byte[] buttonAMessage;
    private byte[] buttonBMessage;
    private byte[] buttonCMessage;
    private byte[] buttonDMessage;

    private Toast _toast;
    private ArrayList<Pair<Integer, Boolean>> incrementValueIndexA, incrementValueIndexB, incrementValueIndexC,incrementValueIndexD;
    private byte incrementForAck;
    private byte incrementForValue;

    Handler mHandler;

    Handler dimmerHandler;
    Runnable dimmerRunable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(null);
        setContentView(R.layout.activity_four_buttons);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);



        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);
        buttonC = findViewById(R.id.buttonC);
        buttonD = findViewById(R.id.buttonD);


        incrementValueIndexA = new ArrayList<Pair<Integer, Boolean>>();
        incrementValueIndexB = new ArrayList<Pair<Integer, Boolean>>();
        incrementValueIndexC = new ArrayList<Pair<Integer, Boolean>>();
        incrementValueIndexD = new ArrayList<Pair<Integer, Boolean>>();

        incrementForAck = 0x01;
        incrementForValue = 0x01;


        SetColorsAndLabelsOfButtons();





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

        TcpSingleton.initContext(this);
        sing = TcpSingleton.getInstance(this, this, this, mHandler);

        buttonA.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses buttonA

                if (sing != null && sing.IsRunning() && buttonAMessage.length != 0)
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

                    sing.sendMsg(buttonAMessage);
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
                if (sing != null && sing.IsRunning() && buttonBMessage.length != 0)
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

                    sing.sendMsg(buttonBMessage);
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
                if (sing != null && sing.IsRunning() && buttonCMessage.length != 0)
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

                    sing.sendMsg(buttonCMessage);
                }
                else
                {
                    //ShowToasMessage("Connect to server first.");
                }
            }
        });

        buttonD.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses buttonA
                if (sing != null && sing.IsRunning() && buttonDMessage.length != 0)
                {
                    for (Pair<Integer, Boolean> iter : incrementValueIndexD)
                    {
                        if (iter.second == true)
                        {
                            buttonDMessage[iter.first] = incrementForAck;
                            incrementForAck++;
                        }
                        else
                        {
                            buttonDMessage[iter.first] = incrementForValue;
                            incrementForValue++;
                        }
                    }

                    sing.sendMsg(buttonDMessage);
                }
                else
                {
                    //ShowToasMessage("Connect to server first.");
                }
            }
        });

        if (sing.IsRunning())
        {
            buttonA.setEnabled(true);
            buttonB.setEnabled(true);
            buttonC.setEnabled(true);
            buttonD.setEnabled(true);
        }
        else
        {
            buttonA.setEnabled(false);
            buttonB.setEnabled(false);
            buttonC.setEnabled(false);
            buttonD.setEnabled(false);
        }

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
        String buttonAColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_four_first_button_color", "#FFFFFF"));
        String buttonBColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_four_second_button_color", "#FFFFFF"));
        String buttonCColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_four_third_button_color", "#FFFFFF"));
        String buttonDColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_four_fourth_button_color", "#FFFFFF"));

        buttonA.setBackgroundColor(Color.parseColor(buttonAColor));
        buttonB.setBackgroundColor(Color.parseColor(buttonBColor));
        buttonC.setBackgroundColor(Color.parseColor(buttonCColor));
        buttonD.setBackgroundColor(Color.parseColor(buttonDColor));

        String buttonALabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_four_first_button_text", "ButtonA"));
        String buttonBLabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_four_second_button_text", "ButtonB"));
        String buttonCLabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_four_third_button_text", "ButtonC"));
        String buttonDLabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_four_fourth_button_text", "ButtonD"));

        buttonA.setText(buttonALabel);
        buttonB.setText(buttonBLabel);
        buttonC.setText(buttonCLabel);
        buttonD.setText(buttonDLabel);


        try
        {
            buttonAMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_four_first_button_message", "14 02 02 01")), "A");
        }
        catch (Exception e)
        {
            incrementValueIndexA.clear();
            incrementValueIndexA.add(new Pair<Integer, Boolean>(14, true));
            buttonAMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x01, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
        }


        try
        {
            buttonBMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_four_second_button_message", "14 02 02 01")), "B");
        }
        catch (Exception e)
        {
            incrementValueIndexB.clear();
            incrementValueIndexB.add(new Pair<Integer, Boolean>(14, true));
            buttonBMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x02, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
        }


        try
        {
            buttonCMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_four_third_button_message", "14 02 02 01")), "C");
        }
        catch (Exception e)
        {
            incrementValueIndexC.clear();
            incrementValueIndexC.add(new Pair<Integer, Boolean>(14, true));
            buttonCMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x03, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
        }


        try
        {
            buttonDMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_four_fourth_button_message", "14 02 02 01")), "D");
        }
        catch (Exception e)
        {
            incrementValueIndexD.clear();
            incrementValueIndexD.add(new Pair<Integer, Boolean>(14, true));
            buttonDMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x04, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.commonmenus, menu);
        MenuItem item = menu.findItem(R.id.menu_fourButtons);
        item.setEnabled(false);
        _menu = menu;

        if (sing.IsRunning())
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

        //sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        if (id == R.id.menu_settings)
        {
            showSettingsInputDialog(false);
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
        else if (id == R.id.menu_threeButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, ThreeButtonsActivity.class));
            finish();
        }

        else if (id == R.id.menu_connection)
        {
            if (CheckImei())
            {
                if (sing == null || !sing.IsRunning())
                {
                    Connect();
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));

                    buttonA.setEnabled(true);
                    buttonB.setEnabled(true);
                    buttonC.setEnabled(true);
                    buttonD.setEnabled(true);
                }
                else
                {
                    Disconnect(true);
                    item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_disconnected));

                    buttonA.setEnabled(false);
                    buttonB.setEnabled(false);
                    buttonC.setEnabled(false);
                    buttonD.setEnabled(false);
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

    private void showSettingsInputDialog(final boolean fromBoot)
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
                    if (inputPass.equals("Fiskijeladyboy123") || inputPass.equals("superceit") || inputPass.equals("radko"))
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
        TcpSingleton.Connect(GetAddress(), GetPort());
    }

    private void Disconnect(boolean fromUi)
    {
        TcpSingleton.Disconnect(fromUi);
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
                        buttonD.setEnabled(false);
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
                        buttonD.setEnabled(true);
                    }
                }
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("list_pref_four_first_button_color"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonA.setBackgroundColor(Color.parseColor(newValue));
        }

        if (key.equals("list_pref_four_second_button_color"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonB.setBackgroundColor(Color.parseColor(newValue));
        }

        if (key.equals("list_pref_four_third_button_color"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonC.setBackgroundColor(Color.parseColor(newValue));
        }

        if (key.equals("list_pref_four_fourth_button_color"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonD.setBackgroundColor(Color.parseColor(newValue));
        }



        if (key.equals("edit_text_four_first_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonA.setText(newValue);
        }

        if (key.equals("edit_text_four_second_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonB.setText(newValue);
        }

        if (key.equals("edit_text_four_third_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonC.setText(newValue);
        }

        if (key.equals("edit_text_four_fourth_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonD.setText(newValue);
        }



        if (key.equals("edit_text_four_first_button_message"))
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

        if (key.equals("edit_text_four_second_button_message"))
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

        if (key.equals("edit_text_four_third_button_message"))
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

        if (key.equals("edit_text_four_fourth_button_message"))
        {
            String newValue = sharedPreferences.getString(key, "");

            try
            {
                incrementValueIndexD.clear();
                buttonDMessage = ParseMessageFromSettingsWithIncementPosition(newValue, "D");
            }
            catch (Exception e)
            {
                incrementValueIndexD.clear();
                incrementValueIndexD.add(new Pair<Integer, Boolean>(14, true));
                buttonDMessage = new byte[] {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, 0x04, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, (byte) 0xff};
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
                    if (message.equals("D"))
                    {
                        if (data[i / 2 - 3] == 0x11 && data[i / 2 - 2] == 0x02 && data[i / 2 - 1] == 0x0a)
                        {
                            incrementValueIndexD.add(new Pair<Integer, Boolean>(i / 2, true));
                        }
                        else
                        {
                            incrementValueIndexD.add(new Pair<Integer, Boolean>(i / 2, false));
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

    private void stopHandler() {
        dimmerHandler.removeCallbacks(dimmerRunable);
    }
    private void startHandler() {
        dimmerHandler.postDelayed(dimmerRunable, 8*1000); //for 5 minutes
    }

    @Override
    public void onUserInteraction() {
        // TODO Auto-generated method stub
        super.onUserInteraction();

        Utils.changeScreenBrightness(getApplicationContext(), 100);

        stopHandler();//stop first and then start
        startHandler();
    }
}
