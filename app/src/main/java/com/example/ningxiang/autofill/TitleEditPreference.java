package com.example.ningxiang.autofill;

import android.content.Context;
import android.support.v7.preference.EditTextPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * Created by ningxiang on 4/17/17.
 */

public class TitleEditPreference extends EditTextPreference {

    private EditText mEditText;

    public TitleEditPreference(Context context) {
        this(context, null);
    }

    public TitleEditPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPersistent(false);

        mEditText = new EditText(context, attrs);
        mEditText.setId(android.R.id.edit);
    }

    @Override
    public void setText(String text) {
        setTextAndSummary(text);
    }

    private void setTextAndSummary(CharSequence value) {
        if (TextUtils.isEmpty(value)) {
            setSummary("please enter your value...");
            value = "";
        } else {
            setSummary(value);
        }

        super.setText(value.toString());
    }

    public EditText getEditText() {
        return mEditText;
    }
}
