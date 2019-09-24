package com.example.rc20;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener
{

    static boolean normalSettings = true;
    private SharedPreferences preferences;
    SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());



        normalSettings = getIntent().getExtras().getBoolean("normalSettings");
        settingsFragment = new SettingsFragment();

        setContentView(R.layout.settings_activity);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, settingsFragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        if (key.equals("edit_text_rc_id"))
        {
            String newValue = sharedPreferences.getString(key, "");

            if (tryParseInt(newValue))
            {
                if (Integer.parseInt(newValue) < 0 || Integer.parseInt(newValue) > 256)
                {
                    EditTextPreference hovno = settingsFragment.findPreference(key);
                    hovno.setText("1");
                    hovno.setSummary("1");
                }
                else
                {
                    settingsFragment.findPreference(key).setSummary(newValue);
                }
            }
            else
            {
                EditTextPreference hovno = settingsFragment.findPreference(key);
                hovno.setText("1");
                hovno.setSummary("1");
            }
        }

        if (key.equals("edit_text_port"))
        {
            String newValue = sharedPreferences.getString(key, "");

            if (tryParseInt(newValue))
            {
                if (Integer.parseInt(newValue) < 0 || Integer.parseInt(newValue) > 65535)
                {
                    EditTextPreference hovno = settingsFragment.findPreference(key);
                    hovno.setText("9750");
                    hovno.setSummary("9750");
                }
                else
                {
                    settingsFragment.findPreference(key).setSummary(newValue);
                }
            }
            else
            {
                EditTextPreference hovno = settingsFragment.findPreference(key);
                hovno.setText("9750");
                hovno.setSummary("9750");
            }
        }

        if (key.equals("edit_text_address"))
        {
            String newValue = sharedPreferences.getString(key, "");

            if (validIP(newValue))
            {
                settingsFragment.findPreference(key).setSummary(newValue);
            }
            else
            {
                EditTextPreference hovno = settingsFragment.findPreference(key);
                hovno.setText("192.168.1.1");
                hovno.setSummary("192.168.1.1");
            }
        }

        if (key.equals("switch_autoconnect"))
        {
            boolean newValue = sharedPreferences.getBoolean(key, false);
            settingsFragment.findPreference(key).setSummary(newValue ? "true" : "false");
        }
    }

    private void consolideTextAndSummary()
    {
        EditTextPreference hovno = settingsFragment.findPreference("edit_text_address");
        hovno.setSummary(hovno.getText());

        EditTextPreference hovno1 = settingsFragment.findPreference("edit_text_port");
        hovno.setSummary(hovno.getText());

        EditTextPreference hovno2 = settingsFragment.findPreference("edit_text_rc_id");
        hovno.setSummary(hovno.getText());
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
    {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
        {
            if (normalSettings)
            {
                setPreferencesFromResource(R.xml.user_preferences, rootKey);
            }
            else
            {
                setPreferencesFromResource(R.xml.admin_preferences, rootKey);
            }
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        preferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    boolean tryParseInt(String value)
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

    private boolean validIP(String ip)
    {
        try
        {
            if (ip == null || ip.isEmpty())
            {
                return false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4)
            {
                return false;
            }

            for (String s : parts)
            {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255))
                {
                    return false;
                }
            }
            if (ip.endsWith("."))
            {
                return false;
            }

            return true;
        }
        catch (NumberFormatException nfe)
        {
            return false;
        }
    }
}