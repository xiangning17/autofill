package com.example.ningxiang.autofill;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Switch;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by ningxiang on 3/30/17.
 */

public class ConfigDialog extends AlertDialog implements DialogInterface.OnClickListener{

    @Bind(R.id.no_show_again)
    protected CheckBox mDontAsk;

    private Scene mScene;

    public ConfigDialog(Context context) {
        this(context, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert);
    }

    protected ConfigDialog(Context context, int themeResId) {
        super(context, themeResId);

        setTitle("Auto Fill");
        View content = getLayoutInflater().inflate(R.layout.config_dialog, null);
        int dlgMargin = content.getResources().getDimensionPixelOffset(R.dimen.dlg_margin);
        setView(content, dlgMargin, dlgMargin, dlgMargin, dlgMargin);
        setButton(AlertDialog.BUTTON_POSITIVE, "Config", this);
        setButton(AlertDialog.BUTTON_NEGATIVE, "Cancel", this);
    }

    public void setScene(Scene s) {
        mScene = s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mScene == null
                || (!mScene.isAutoFill() && mScene.isNoAsk())) {
            dismiss();
            return;
        }

        ButterKnife.bind(this);
    }

    @NonNull
    @Override
    public Bundle onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                Intent intent = new Intent(getContext(), AutoFillConfigSettingsActivity.class);
                intent.putExtra("scene_id", mScene.getId());
                getContext().startActivity(intent);
                break;
            case  BUTTON_NEGATIVE:
                mScene.setNoAsk(mDontAsk.isChecked());
                break;
        }
    }
}
