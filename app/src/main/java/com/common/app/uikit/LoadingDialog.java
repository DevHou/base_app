package com.common.app.uikit;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.common.app.R;
import com.common.utils.AppLog;


/**
 * Created by houlijiang on 2014/3/23.
 * <p/>
 * 通用加载中提示框
 */
public class LoadingDialog extends DialogFragment {

    private static final String TAG = "LoadingDialog";

    private static final int MSG_DISMISS_DIALOG = 1;

    private static LoadingDialog dialog;
    private LoadingDialogListener listener;
    private TextView mTextView;
    private String loadingText;
    private String currentTag;
    private FragmentManager mManager;

    public static LoadingDialog getInstance() {
        if (dialog == null) {
            dialog = new LoadingDialog();
            dialog.setCancelable(false);
        }
        return dialog;
    }

    public static LoadingDialog getNewIntance() {
        LoadingDialog d = new LoadingDialog();
        d.setCancelable(false);
        return d;
    }

    public LoadingDialog() {
    }

    public LoadingDialogListener getListener() {
        return this.listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppDialog);
        setCancelable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 去掉四周的黑边
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        View view = inflater.inflate(R.layout.dialog_loading, null);
        mTextView = (TextView) view.findViewById(R.id.tv_dialog_loading);
        if (loadingText != null) {
            mTextView.setText(loadingText);
        }

        Button cancelBtn = (Button) view.findViewById(R.id.btn_dialog_cancel);
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentTag = null;
                if (LoadingDialog.this.listener != null) {
                    LoadingDialog.this.listener.onCancel();
                }
                dismiss();
            }
        });
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppLog.d(TAG, "onCreateDialog for tag:" + currentTag);
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        if (currentTag == null) {
            // 可能是show还没有构建好就调用了dismiss
            // loading那个动画可能有点慢
            mHandler.sendEmptyMessageDelayed(MSG_DISMISS_DIALOG, 500);
        }
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        currentTag = null;
        if (this.listener != null) {
            this.listener.onCancel();
        }
        try {
            mManager.beginTransaction().remove(this).commitAllowingStateLoss();
        } catch (Exception e) {
            AppLog.e(TAG, "catch exception when remove fragment, e:" + e.getLocalizedMessage());
        }
        super.onCancel(dialog);
    }

    public void show(FragmentManager manager, String tag, String msg, LoadingDialogListener listener) {
        this.listener = listener;
        show(manager, tag, msg);
    }

    public void show(FragmentManager manager, String tag, LoadingDialogListener listener) {
        this.listener = listener;
        String msg = null;
        show(manager, tag, msg);
    }

    public void show(FragmentManager manager, String tag, String msg) {
        mManager = manager;
        loadingText = msg;
        if (mTextView != null && loadingText != null) {
            mTextView.setText(loadingText);
        }
        if (currentTag != null) {
            AppLog.d(TAG, "is showing, will not show one more");
            return;
        }
        currentTag = tag;
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment != null || isAdded() || isVisible()) {
            AppLog.d(TAG, "find fragment in manager, will not show again");
            return;
        }
        // super.show(manager, tag);
        AppLog.d(TAG, "will show dialog tag:" + currentTag);
        try {
            FragmentTransaction ft = manager.beginTransaction();
            ft.add(this, tag);
            ft.commitAllowingStateLoss();
            // ft.commit();
        } catch (IllegalStateException e) {
            AppLog.e(TAG, "get exception e:" + e.getMessage());
            currentTag = null;
        } catch (Exception e) {
            AppLog.e(TAG, "get exception e:" + e.getMessage());
            currentTag = null;
        }
    }

    public boolean isShowing() {
        return currentTag != null;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        String msg = null;
        show(manager, tag, msg);
    }

    @Override
    public void dismiss() {
        AppLog.d(TAG, "dismiss for tag:" + currentTag);
        currentTag = null;
        try {
            super.dismissAllowingStateLoss();
            // super.dismiss();
            mManager.beginTransaction().remove(this).commitAllowingStateLoss();
        } catch (Exception e) {
            AppLog.e(TAG, "catch exception when dismiss, e:" + e.getMessage());
        }
    }

    public interface LoadingDialogListener {
        public void onCancel();
    }

    public static class CheckIsCancel implements LoadingDialogListener {
        private boolean isCancel = false;
        private LoadingDialogListener listener;

        public CheckIsCancel() {
            isCancel = false;
        }

        public CheckIsCancel(LoadingDialogListener listener) {
            this.listener = listener;
        }

        public void reset() {
            isCancel = false;
        }

        public boolean isCanceled() {
            return isCancel;
        }

        @Override
        public void onCancel() {
            isCancel = true;
            if (this.listener != null) {
                this.listener.onCancel();
            }
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_DISMISS_DIALOG:
                    LoadingDialog.this.dismiss();
                    if (dialog != null) {
                        dialog.dismiss();
                    }
                    break;
            }
        }
    };

}
