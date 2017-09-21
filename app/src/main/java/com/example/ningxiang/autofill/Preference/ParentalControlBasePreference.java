package com.example.ningxiang.autofill.Preference;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ningxiang.autofill.R;

/**
 * Created by ningxiang on 5/10/17.
 */

public class ParentalControlBasePreference extends DialogPreference {

    private ColorStateList iconTint;

    private ColorStateList origSummaryColor;
    private ColorStateList summaryColor;

    private String flag;

    public ParentalControlBasePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ParentalControlBasePreference,
                defStyleAttr, 0);

        iconTint = a.getColorStateList(R.styleable.ParentalControlBasePreference_iconTint);

        a.recycle();
    }

    public ParentalControlBasePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ParentalControlBasePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.ParentalControlBasePreferenceStyle);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        ImageView imageView = (ImageView) holder.findViewById(android.R.id.icon);
        if (imageView != null) {
            imageView.setImageTintList(iconTint);
        }


        View flagView = holder.findViewById(R.id.flag);
        if (flagView instanceof TextView) {
            ((TextView)flagView).setText(flag);
            flagView.setVisibility(TextUtils.isEmpty(flag) ? View.GONE : View.VISIBLE);
        }

        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        if (summaryView != null) {
            if (origSummaryColor == null) {
                origSummaryColor = summaryView.getTextColors();
            }

            summaryView.setTextColor(summaryColor != null ? summaryColor : origSummaryColor);
        }

    }

    public void setSummaryColor(ColorStateList summaryColor) {
        this.summaryColor = summaryColor;
        notifyChanged();
    }

    public void setFlag(String flag) {
        this.flag = flag;
        notifyChanged();
    }
}
