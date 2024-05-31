package com.android.app.fakechatapp.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.app.fakechatapp.R;

public class DialogCustomProgress extends Dialog {
    LinearLayout ll_progress_dialog;
    Activity activity;
    ProgressBar progressBar2;

    public DialogCustomProgress(Activity a) {
        super(a);
        this.activity = a;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        setContentView(R.layout.custom_progress_dialog_style);
        initUI();
    }

    private void initUI() {
        ll_progress_dialog = (LinearLayout) findViewById(R.id.ll_progress_dialog);
        progressBar2 = (ProgressBar) findViewById(R.id.progressBar2);
        progressBar2.getIndeterminateDrawable().setColorFilter(activity.getResources().getColor(R.color.whatsapp_green), android.graphics.PorterDuff.Mode.MULTIPLY);
    }

    public void showProgressDialog() {
        if (this != null) {
            if (this.isShowing()) {
                return;
            }
        }
        show();
    }

    public void dismissProgressDialog() {
        dismiss();
    }

}
