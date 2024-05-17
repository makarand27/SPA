package com.app.spa;

import androidx.preference.PreferenceFragmentCompat;
import android.os.Bundle;

public class PreferenceActivityFragment extends PreferenceFragmentCompat {

    public PreferenceActivityFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }
}
