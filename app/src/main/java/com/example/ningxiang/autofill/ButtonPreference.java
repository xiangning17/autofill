package com.example.ningxiang.autofill;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by ningxiang on 4/25/17.
 */

public class ButtonPreference extends Preference implements View.OnClickListener {
    public ButtonPreference(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.buttonPreferenceStyle);
    }

    public ButtonPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        holder.findViewById(R.id.button).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        performClick();
    }
}
