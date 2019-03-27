package com.example.rc20;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MultipleButtonsActivity extends MainActivity implements TcpSingleton.OnActionListener
{
    private TcpSingleton sing;
    private Menu _menu;
    Button buttonA;
    Button buttonB;
    Button buttonC;
    Button buttonD;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multiple_buttons);

        sing = TcpSingleton.getInstance(GetAddress(), GetPort(), this, this, this);

        buttonA = findViewById(R.id.buttonA);
        buttonB = findViewById(R.id.buttonB);
        buttonC = findViewById(R.id.buttonC);
        buttonD = findViewById(R.id.buttonD);


        if (sing.GetRunning())
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
                // Code here executes on main thread after user presses button
                int id = rc_id;
                char[] message = {0x80, 0x02, 0x00, 0x0B, 0x14, 0x02, 0x02, (char) id, 0x11, 0x01, 0x06, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};
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

        buttonB.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses button
                int id = rc_id;
                char[] message = {0x80, 0x02, 0x00, 0x0B, 0x14, 0x02, 0x02, (char) id, 0x11, 0x01, 0x07, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};
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

        buttonC.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses button
                int id = rc_id;
                char[] message = {0x80, 0x02, 0x00, 0x0B, 0x14, 0x02, 0x02, (char) id, 0x11, 0x01, 0x08, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};
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

        buttonD.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                // Code here executes on main thread after user presses button
                int id = rc_id;
                char[] message = {0x80, 0x02, 0x00, 0x0B, 0x14, 0x02, 0x02, (char) id, 0x11, 0x01, 0x09, 0x11, 0x01, 0x03, 0x11, 0x02, 0x0A, 0x01};
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.commonmenus, menu);
        MenuItem item = menu.findItem(R.id.menu_fourButtons);
        item.setEnabled(false);
        _menu = menu;

        if (sing.GetRunning())
        {
            item = menu.findItem(R.id.menu_connection);
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_action_connected));
        }

        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {

        int id = item.getItemId();

        if (id == R.id.menu_settings)
        {
            //startActivity(new Intent(this, SettingsActivity.class));
        }
        else if (id == R.id.menu_oneButton)
        {
            startActivity(new Intent(this, MainActivity.class));
        }
        else if (id == R.id.menu_connection)
        {
            if (sing == null || !sing.GetRunning())
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onReceive(byte[] message)
    {

    }

    @Override
    public void onDisconnect()
    {
        runOnUiThread(new Runnable()
        {

            @Override
            public void run()
            {
                if (_menu != null) {
                    MenuItem item = _menu.findItem(R.id.menu_connection);
                    if (item != null) {
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
                if (_menu != null) {
                    MenuItem item = _menu.findItem(R.id.menu_connection);
                    if (item != null) {
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
}
