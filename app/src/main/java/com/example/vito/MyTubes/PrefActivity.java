package com.example.vito.MyTubes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

/**
 * Created by melissabeuze on 04/03/16.
 */

public class PrefActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {


    public static final String KEY_LIST_PREFERENCE = "listPref";
    private ListPreference mListPreference;

    //declarer cette activité dans le manifest
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Get a reference to the preferences
        mListPreference = (ListPreference)getPreferenceScreen().findPreference(KEY_LIST_PREFERENCE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Setup the initial values
        mListPreference.setSummary("Current value is " + mListPreference.getEntry().toString());

        // Set up a listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister the listener whenever a key changes
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // Set new summary, when a preference value changes
        if (key.equals(KEY_LIST_PREFERENCE)) {
            mListPreference.setSummary("Current value is " + mListPreference.getEntry().toString());
            switch(mListPreference.getValue()){
                case "1": break;
                case "2": break;
            }
        }
    }
}
