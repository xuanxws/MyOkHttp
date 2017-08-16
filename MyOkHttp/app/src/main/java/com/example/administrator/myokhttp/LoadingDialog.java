package com.example.administrator.myokhttp;

import android.app.Dialog;
import android.content.Context;

import com.example.administrator.myokhttp.tools.ActivityUtils;

/**
 * Created by Administrator on 2017/8/16.
 */

public class LoadingDialog {

    private Dialog mDialog;

    public LoadingDialog(Context context) {
        mDialog = initLoadingDialog(context, false);
    }

    /**
     * 初始化对话框
     */
    private Dialog initLoadingDialog(Context context, boolean canceledOnTouchOutside) {
        Dialog dialog = new Dialog(context, R.style.LoadingDialog);
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.loading_layout);
        return dialog;
    }

    /**
     * 展示对话框
     */
    public void showLoadingDialog(Context context) {
        if (null == mDialog || ActivityUtils.isActivityDestroy(context)) {
            return;
        }
        try {
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏对话框
     */
    public void dismissLoadingDialog(Context context) {
        if (ActivityUtils.isActivityDestroy(context)) {
            return;
        }
        if (null != mDialog && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

}
