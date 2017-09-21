package com.example.ningxiang.autofill;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

/**
 * Created by ningxiang on 5/2/17.
 */

public class EditablePreference extends Preference {

    public EditablePreference(Context context) {
        this(context, null);
    }

    public EditablePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
