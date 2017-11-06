package com.sunnymoon.samplegeoapplication1.fragments;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.widget.Toast;
import com.sunnymoon.samplegeoapplication1.R;
import com.sunnymoon.samplegeoapplication1.sync.LocationJobService;

/**
 * Project SampleGeoApplication1
 * Working on ContentSettingsFragment
 * Created by Shion T. Fujie on 2017/11/04.
 */
public class ContentSettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public ContentSettingsFragment() {
        // Required empty public constructor
    }

    public static ContentSettingsFragment newInstance() {
        final ContentSettingsFragment fragment = new ContentSettingsFragment();
        return fragment;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        setPreferencesFromResource(R.xml.app_settings, s);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
            if(key.equals(getString(R.string.background_retrieval_enabled))) {
                final boolean retrievalEnabled = getPreferenceScreen().getSharedPreferences().getBoolean(key, false);
                final JobScheduler jobScheduler = (JobScheduler) getActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE);

                if(retrievalEnabled) {
                    jobScheduler.schedule(new JobInfo.Builder(005,
                            new ComponentName(getContext(), LocationJobService.class))
                            .setPeriodic(60000)
                            .build());
                }else{
                    jobScheduler.cancel(005);
                }
            }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
