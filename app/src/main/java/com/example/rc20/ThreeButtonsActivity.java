package com.example.rc20;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

public class ThreeButtonsActivity extends AppCompatActivity implements TcpSingleton.IOnActionOnTcp, SharedPreferences.OnSharedPreferenceChangeListener
{
    private TcpSingleton sing;
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


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(null);
        setContentView(R.layout.activity_three_buttons);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        TcpSingleton.initContext(this);
        sing = TcpSingleton.getInstance(this, this, this);

        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);
        buttonC = findViewById(R.id.buttonC);


        incrementValueIndexA = new ArrayList<Pair<Integer, Boolean>>();
        incrementValueIndexB = new ArrayList<Pair<Integer, Boolean>>();
        incrementValueIndexC = new ArrayList<Pair<Integer, Boolean>>();
        //incrementValueIndexA = 0x01;
        //incrementValueIndexB = 0x01;
        //incrementValueIndexC = 0x01;
        incrementForAck = 0x01;
        incrementForValue = 0x01;


        SetColorsAndLabelsOfButtons();

        if (sing.IsRunning())
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
                // Code here executes on main thread after user presses button_single

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
                // Code here executes on main thread after user presses button_single
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
                // Code here executes on main thread after user presses button_single
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
    }

    private void SetColorsAndLabelsOfButtons()
    {
        String buttonAColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_three_first_button_color", "#FFFFFF"));
        String buttonBColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_three_second_button_color", "#FFFFFF"));
        String buttonCColor = Objects.requireNonNull(sharedPreferences.getString("list_pref_three_third_button_color", "#FFFFFF"));

        buttonA.setBackgroundColor(Color.parseColor(buttonAColor));
        buttonB.setBackgroundColor(Color.parseColor(buttonBColor));
        buttonC.setBackgroundColor(Color.parseColor(buttonCColor));

        String buttonALabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_first_button_text", "ButtonA"));
        String buttonBLabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_second_button_text", "ButtonB"));
        String buttonCLabel = Objects.requireNonNull(sharedPreferences.getString("edit_text_third_button_text", "ButtonC"));

        buttonA.setText(buttonALabel);
        buttonB.setText(buttonBLabel);
        buttonC.setText(buttonCLabel);

        buttonAMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_first_button_message", "14 02 02 01")), "A");
        buttonBMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_second_button_message", "14 02 02 01")), "B");
        buttonCMessage = ParseMessageFromSettingsWithIncementPosition(Objects.requireNonNull(sharedPreferences.getString("edit_text_third_button_message", "14 02 02 01")), "C");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.commonmenus, menu);
        MenuItem item = menu.findItem(R.id.menu_threeButtons);
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
        else if (id == R.id.menu_fourButtons)
        {
            Disconnect(true);
            startActivity(new Intent(this, MultipleButtonsActivity.class));
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
                ShowToasMessage("Bad IMEI configuration. Please contact CEIT customer support.");
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
        Intent intent = new Intent(this, SettingsActivity.class);

        Bundle bundle = new Bundle();
        bundle.putBoolean("normalSettings", normalSettings);
        intent.putExtras(bundle);
        startActivity(intent);
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
        if (byteArrayOnlyZeros(message))
        {
            Disconnect(false);
        }

        if (Utils.checkAckResponse(message, incrementForAck))
        {
            ShowToasMessage("Message sent successfully!");
        }
        else
        {
            //ShowToasMessage("Connect to server first.");
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

        if (key.equals("edit_text_first_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonA.setText(newValue);
        }

        if (key.equals("edit_text_second_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonB.setText(newValue);
        }

        if (key.equals("edit_text_third_button_text"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonC.setText(newValue);
        }

        if (key.equals("edit_text_first_button_message"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonAMessage = ParseMessageFromSettingsWithIncementPosition(newValue, "A");
        }

        if (key.equals("edit_text_second_button_message"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonBMessage = ParseMessageFromSettingsWithIncementPosition(newValue, "B");
        }

        if (key.equals("edit_text_third_button_message"))
        {
            String newValue = sharedPreferences.getString(key, "");

            buttonCMessage = ParseMessageFromSettingsWithIncementPosition(newValue, "C");
        }
    }

    private byte[] ParseMessageFromSettingsWithIncementPosition(String s, String message)
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
}

