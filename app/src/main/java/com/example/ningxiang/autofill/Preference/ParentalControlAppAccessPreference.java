package com.example.ningxiang.autofill.Preference;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;

/**
 * Created by ningxiang on 5/10/17.
 */

public class ParentalControlAppAccessPreference extends ParentalControlPreference {

    private boolean locked;

    public ParentalControlAppAccessPreference(Context context) {
        this(context, null);
    }

    public ParentalControlAppAccessPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
    }

    @Override
    protected void onClick() {
//        getPreferenceManager().showDialog(this);
    }
}
