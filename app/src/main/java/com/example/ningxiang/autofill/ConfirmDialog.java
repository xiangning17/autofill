package com.example.ningxiang.autofill;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ningxiang on 3/30/17.
 */

public class ConfirmDialog extends Dialog {

    @Bind(R.id.button_ok)
    protected Button mOk;

    @Bind(R.id.config)
    protected Button mConfig;

    private Scene mScene;

    public ConfirmDialog(Context context) {
        this(context, 0/*android.R.style.Theme_Material_Light_Dialog*/);
    }

    protected ConfirmDialog(Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setScene(Scene s) {
        mScene = s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        if (mScene == null) {
            dismiss();
            return;
        }

        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setTitle(null);
        setContentView(R.layout.confirm_dialog);
        ButterKnife.bind(this);
        updateUI();
    }

    void updateUI() {
    }

    @OnClick({R.id.config, R.id.button_ok})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_ok:
                if (mScene == null) {
                    return;
                }

                mScene.setDataReady(true);
                break;
            case R.id.config:
                Intent intent = new Intent(getContext(), AutoFillConfigSettingsActivity.class);
                intent.putExtra("scene_id", mScene.getId());
                getContext().startActivity(intent);
                break;
        }

        dismiss();
    }
}
