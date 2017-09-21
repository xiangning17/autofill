package com.example.ningxiang.autofill;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v14.preference.EditTextPreferenceDialogFragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ningxiang on 4/20/17.
 */

public class TitleEditPreferenceFragment extends EditTextPreferenceDialogFragment {

    @Bind(android.R.id.edit)
    protected EditText mEditText;

    @Bind(android.R.id.title)
    protected EditText mTitleEditText;

    private CharSequence mTitle;

    public static TitleEditPreferenceFragment newInstance(String key) {
        final TitleEditPreferenceFragment
                fragment = new TitleEditPreferenceFragment();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getPwdEditTextPreference().getTitle();
    }

    @Override
    protected void onBindDialogView(View view) {
        try {
            ButterKnife.bind(this, view);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ViewGroup.LayoutParams layoutParams = mEditText.getLayoutParams();

        EditText newEdit = getPwdEditTextPreference().getEditText();
        newEdit.setLayoutParams(layoutParams);

        ViewGroup parent = (ViewGroup) mEditText.getParent();
        int index = parent.indexOfChild(mEditText);
        parent.removeView(mEditText);
        parent.addView(newEdit, index);
        newEdit.requestFocus();

        super.onBindDialogView(view);
        if(mTitleEditText != null) {
            mTitleEditText.setText(mTitle);
        }
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        //we don't need the dialog title.
        builder.setTitle(null);
    }

    protected TitleEditPreference getPwdEditTextPreference() {
        return (TitleEditPreference) getPreference();
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult && mTitleEditText != null) {
            String value = mTitleEditText.getText().toString();
            getPwdEditTextPreference().setTitle(value);
        }

        EditText editText = getPwdEditTextPreference().getEditText();
        ((ViewGroup)editText.getParent()).removeView(editText);
    }
}
