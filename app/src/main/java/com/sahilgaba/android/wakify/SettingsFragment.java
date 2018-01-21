package com.sahilgaba.android.wakify;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by sahilgaba on 7/17/17.
 */

public class SettingsFragment extends PreferenceFragmentCompat {

    ListPreference customPreference;

    RecyclerView recyclerView;
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.pref_settings);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        recyclerView = this.getListView();
        recyclerView.setPadding(0, 0, 0, 0);
        view.setBackgroundColor(Color.parseColor("#0C0C0C"));
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }
}

