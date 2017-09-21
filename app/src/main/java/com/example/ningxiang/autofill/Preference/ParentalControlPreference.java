package com.example.ningxiang.autofill.Preference;

import android.content.Context;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

import com.example.ningxiang.autofill.R;

/**
 * Created by ningxiang on 5/10/17.
 */

public class ParentalControlPreference extends ParentalControlBasePreference {

    protected boolean locked;

    public ParentalControlPreference(Context context) {
        this(context, null);
    }

    public ParentalControlPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        View v = holder.findViewById(R.id.widget_image);
        if (v != null) {
            v.setSelected(locked);
        }
    }

    @Override
    protected void onClick() {
        locked = !locked;
        notifyChanged();
    }
}
