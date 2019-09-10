package com.example.rc20;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Objects;

public class MultipleButtonsActivity extends AppCompatActivity implements TcpSingleton.IOnActionOnTcp
{
    private TcpSingleton sing;
    private Menu _menu;
    Button buttonA;
    Button buttonB;
    Button buttonC;
    Button buttonD;

    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_buttons);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        TcpSingleton.initContext(this);
        sing = TcpSingleton.getInstance(this, this, this);

        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);
        buttonC = findViewById(R.id.buttonC);
        buttonD = findViewById(R.id.buttonD);


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


        buttonA.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses button_single
                int id = getRcId();
                byte[] message = {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, (byte) (id & 0xff), 0x11, 0x01, 0x06, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};
                if (sing != null && sing.IsRunning())
                {
                    sing.sendMsg(message);
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
                int id = getRcId();
                byte[] message = {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, (byte) (id & 0xff), 0x11, 0x01, 0x07, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};
                if (sing != null && sing.IsRunning())
                {
                    sing.sendMsg(message);
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
                int id = getRcId();
                byte[] message = {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, (byte) (id & 0xff), 0x11, 0x01, 0x08, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};
                if (sing != null && sing.IsRunning())
                {
                    sing.sendMsg(message);
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
                // Code here executes on main thread after user presses button_single
                int id = getRcId();
                byte[] message = {(byte) 0x80, 0x02, 0x00, 0x0b, 0x14, 0x02, 0x02, (byte) (id & 0xff), 0x11, 0x01, 0x09, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};
                if (sing != null && sing.IsRunning())
                {
                    sing.sendMsg(message);
                }
                else
                {
                    //ShowToasMessage("Connect to server first.");
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

    private int getRcId()
    {
        return Integer.parseInt(Objects.requireNonNull(sharedPreferences.getString("edit_text_rc_id", "9750")));
    }

    @Override
    protected void onResume()
    {
        super.onResume();
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        else if (id == R.id.menu_threeButtons)
        {
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
                //
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void Connect()
    {
        TcpSingleton.Connect(GetAddress(), GetPort());
    }

    private void Disconnect(boolean fromUi)
    {
        TcpSingleton.Disconnect(fromUi);
    }

    @Override
    public void onReceive(byte[] message)
    {
        if (byteArrayOnlyZeros(message))
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
}
