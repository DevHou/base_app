package com.common.app.uikit;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.common.app.R;
import com.common.utils.AppLog;


/**
 * 弹出框通用类
 * 
 * EditView id必须是 common_dialog_et，其他都是是每种不同弹出框自定义的
 */
public class CommonDialog extends DialogFragment implements View.OnClickListener {

    private static final String TAG = CommonDialog.class.getSimpleName();

    private String currentTag;
    private static CommonDialog dialog;
    private Builder mBuilder;
    private View mView;
    private ViewStub mViewStub;
    private FragmentManager mManager;

    public static CommonDialog getInstance(Builder builder) {
        if (dialog == null) {
            dialog = new CommonDialog();
            dialog.setCancelable(false);
        }
        dialog.setBuilder(builder);
        return dialog;
    }

    public static CommonDialog getNewInstance(Builder builder) {
        CommonDialog d = new CommonDialog();
        d.setCancelable(false);
        d.setBuilder(builder);
        return d;
    }

    private void setBuilder(Builder builder) {
        mBuilder = builder;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AppLog.v(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.AppDialog);
        setCancelable(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppLog.v(TAG, "onCreateView");
        // 去掉四周的黑边
        // getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(0));
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.shape_transparent_corner);
        View view = inflater.inflate(R.layout.dialog_common, null);
        mViewStub = (ViewStub) view.findViewById(R.id.dialog_common_vs);
        mView = buildView();
        return view;
    }

    private void showInputMethod(final EditText et) {
        if (et != null) {
            new Handler() {
            }.postDelayed(new Runnable() {
                @Override
                public void run() {
                    et.requestFocus();
                }
            }, 500);

        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AppLog.v(TAG, "onCreateDialog");
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (mBuilder.mCancelable) {
            dialog.setCanceledOnTouchOutside(true);
        } else {
            dialog.setCanceledOnTouchOutside(false);
        }
        return dialog;
    }

    private View buildView() {
        DialogMode mode = mBuilder.mMode;
        View view = null;
        switch (mode) {
            case MODE_TEXT_MESSAGE: {
                mViewStub.setLayoutResource(R.layout.dialog_text_message);
                view = mViewStub.inflate();
                buildTextMessageView(view);
                break;
            }
            case MODE_CUSTOM: {
                mViewStub.setLayoutResource(R.layout.dialog_custom);
                view = mViewStub.inflate();
                buildCustomView(view);
                break;
            }
        }
        return view;
    }

    /**
     * 自定义view
     */
    private View buildCustomView(View view) {
        LinearLayout root = (LinearLayout) view.findViewById(R.id.dialog_custom_content);
        root.removeAllViews();
        if (mBuilder.mCustomViewId > 0) {
            View custom = LayoutInflater.from(mBuilder.mContext).inflate(mBuilder.mCustomViewId, root, false);
            root.addView(custom);
        } else if (mBuilder.mCustomView != null) {
            try {
                ((ViewGroup) mBuilder.mCustomView.getParent()).removeView(mBuilder.mCustomView);
            } catch (Exception e) {
                AppLog.e(TAG, "remove view for custom view, exception e:" + e.getLocalizedMessage());
            }
            root.addView(mBuilder.mCustomView);
        }
        return view;
    }

    /**
     * 普通通知框，只有一个标题，一个content加两个或者一个按钮
     */
    private View buildTextMessageView(View view) {
        view.findViewById(R.id.dialog_text_message_ll_content).setOnClickListener(this);
        LinearLayout layoutBtns = (LinearLayout) view.findViewById(R.id.dialog_text_message_ll_btns);
        layoutBtns.setWeightSum(mBuilder.mButtons.length);
        // 把按钮加到view中
        for (int i = 0; i < mBuilder.mButtons.length; ++i) {
            TextView btn;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
            params.weight = 1;
            // params.leftMargin = 5;
            // params.rightMargin = 5;
            btn = (TextView) LayoutInflater.from(mBuilder.mContext).inflate(R.layout.dialog_normal_btn, null);
            btn.setGravity(Gravity.CENTER);
            btn.setLayoutParams(params);
            btn.setText(mBuilder.mButtons[i]);
            if (mBuilder.mButtonColors != null && mBuilder.mButtonColors.length > i) {
                btn.setTextColor(mBuilder.mButtonColors[i]);
            }
            btn.setOnClickListener(this);
            btn.setTag(i);

            layoutBtns.addView(btn);

            if (mBuilder.mButtons.length > 1 && i != mBuilder.mButtons.length - 1) {
                View line = new View(mBuilder.mContext);
                line.setLayoutParams(new LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT));
                line.setBackgroundColor(getResources().getColor(R.color.app_divider));
                layoutBtns.addView(line);
            }
        }

        TextView titleView = (TextView) view.findViewById(R.id.dialog_text_message_tv_title);
        if (!TextUtils.isEmpty(mBuilder.mTitle)) {
            titleView.setText(mBuilder.mTitle);
            titleView.setVisibility(View.VISIBLE);
            if (mBuilder.mTitleColor != -1) {
                titleView.setTextColor(mBuilder.mTitleColor);
            }
        } else {
            titleView.setVisibility(View.GONE);
        }

        TextView contentView = (TextView) view.findViewById(R.id.dialog_text_message_tv_content);
        if (!TextUtils.isEmpty(mBuilder.mMessage)) {
            contentView.setText(mBuilder.mMessage);
            contentView.setVisibility(View.VISIBLE);
            // color
            if (mBuilder.mMessageColor != -1) {
                contentView.setTextColor(mBuilder.mMessageColor);
            }
        } else {
            contentView.setVisibility(View.GONE);
        }

        View content_line = view.findViewById(R.id.dialog_text_message_content_line);
        if (!TextUtils.isEmpty(mBuilder.mTitle) && !TextUtils.isEmpty(mBuilder.mMessage)) {
            content_line.setVisibility(View.VISIBLE);
        } else {
            content_line.setVisibility(View.GONE);
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        int tag = v.getTag() == null ? Integer.MAX_VALUE : (Integer) v.getTag();
        if (tag == mBuilder.mCancelIndex) {
            dismiss();
        } else {
            if (mBuilder.mDialogListener == null) {
                dismiss();
            } else {
                if (!mBuilder.mDialogListener.onClick(v, tag)) {
                    dismiss(true);
                }
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        AppLog.v(TAG, "onCancel");
        currentTag = null;
        if (mBuilder.mDismissListener != null) {
            mBuilder.mDismissListener.onDismiss(false);
        }
        try {
            mManager.beginTransaction().remove(this).commitAllowingStateLoss();
        } catch (Exception e) {
            AppLog.e(TAG, "catch exception when remove fragment, e:" + e.getMessage());
        }
        super.onCancel(dialog);
    }

    @Override
    public void dismiss() {
        AppLog.v(TAG, "dismiss");
        dismiss(false);
    }

    public void dismiss(boolean isClickBtn) {
        if (mBuilder.mDismissListener != null) {
            mBuilder.mDismissListener.onDismiss(isClickBtn);
        }
        currentTag = null;
        try {
            super.dismissAllowingStateLoss();
            // super.dismiss();
            mManager.beginTransaction().remove(this).commitAllowingStateLoss();
        } catch (Exception e) {
            AppLog.e(TAG, "catch exception when dismiss, e:" + e.getMessage());
        }
    }

    public void show(FragmentManager manager, String tag) {
        mManager = manager;
        if (currentTag != null) {
            AppLog.d(TAG, "is showing, will not show one more");
            return;
        }
        currentTag = tag;
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment != null || isAdded() || isVisible()) {
            AppLog.d(TAG, "find fragment in manager, will not show again, tag:" + tag);
            return;
        }
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

    public enum DialogMode {
        MODE_TEXT_MESSAGE, /* 普通提示框 */
        MODE_EDIT_NUM, /* 纯数字输入框 */
        MODE_EDIT_FLOAT, /* 浮点数输入框 */
        MODE_EDIT_TEXT, /* 普通输入框 */
        MODE_EDIT_NONEABLE_TEXT, /* 不可控输入框，如果空会有红叹号的输入错误提示 */
        MODE_EDIT_PHONE, /* 电话输入框 */
        MODE_EDIT_EMAIL, /* 邮箱输入框 */
        MODE_LINES_EDIT, /* 多行输入框 */
        MODE_NONEABLE_LINES_EDIT, /* 不可空的多行输入框 */
        MODE_MULTI_EDIT, /* 多个输入框，每个可以自定义前面的提示和输入框里的提示 */
        MODE_ITEMS, /* 单选列表 */
        MODE_MULTI_SELECT, /* 多选列表 */
        MODE_CUSTOM, /* 完全用户自定义的 */
    }

    public interface OnDialogButtonClick {

        /**
         * @param button 被点击的button
         * @param buttonIndex button 对应的顺序
         * @return false dialog 消失, true dialog 继续保留
         */
        boolean onClick(View button, int buttonIndex);
    }

    public interface OnMultiEditFinish {

        /**
         * 多输入项
         * 
         * @param editTexts 多个editText
         * @return false dialog 消失, true dialog 继续保留
         */
        boolean onMultiEditClick(EditText[] editTexts);
    }

    public interface OnMultiSelectFinish {

        /**
         * 多输入项
         *
         * @param selected 选中的index
         * @return false dialog 消失, true dialog 继续保留
         */
        boolean onMultiSelectClick(Integer[] selected);
    }

    public static class Builder {
        private Activity mContext = null;
        private String mTitle = null;// 标题
        private String mMessage = null;// 输入框的默认文字
        private String[] mButtons = null;
        private String[] mEditTextPre;// 多输入框，每个输入框前面的文字
        private String[] mEditTextHint;// 多输入框，每个输入框里面的hint
        private String[] mEditTextMsg;// 多输入框，每个输入框里面的默认文本
        private DialogMode[] mEditMode;// 多输入框，每个输入框的输入模式
        private IOnValid[] mOnValid;// 输入框检测接口，如果不设置就是可以随意输入
        private boolean mCancelable = false;
        private int mCancelIndex = -1;// 取消按钮index
        private String mHint = null;// 输入框中的提示
        private String mNotice = null;// 输入框下面的提示
        private String mTopNotice = null;// 输入框上面的提示
        private DialogMode mMode = DialogMode.MODE_TEXT_MESSAGE;
        private OnDialogButtonClick mDialogListener = null;
        private OnMultiEditFinish mMultiEditFinish = null;
        private OnMultiSelectFinish mMultiSelectedFinish = null;
        private int mTitleColor = -1;
        private int mMessageColor = -1;
        private int[] mButtonColors = null;
        private int mCustomViewId = -1;
        private View mCustomView = null;
        private OnDismiss mDismissListener;

        public Builder(Activity activity) {
            assert (activity != null);
            mContext = activity;
        }

        public Builder setTitle(String title) {
            mTitle = title;
            return this;
        }

        public Builder setNotice(String notice) {
            mNotice = notice;
            return this;
        }

        public Builder setTopNotice(String noticeTop) {
            mTopNotice = noticeTop;
            return this;
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Builder setHint(String hint) {
            mHint = hint;
            return this;
        }

        public Builder setTitle(int resId) {
            mTitle = (String) mContext.getText(resId);
            return this;
        }

        public Builder setNotice(int noticeId) {
            mNotice = (String) mContext.getText(noticeId);
            return this;
        }

        public Builder setMessage(int resId) {
            mMessage = (String) mContext.getText(resId);
            return this;
        }

        public Builder setHint(int hintId) {
            mHint = (String) mContext.getText(hintId);
            return this;
        }

        public Builder setButtons(int resId[]) {
            assert (resId != null);
            mButtons = new String[resId.length];
            for (int i = 0; i < resId.length; ++i) {
                mButtons[i] = (String) mContext.getText(resId[i]);
            }
            return this;
        }

        public Builder setButtons(String[] btns) {
            assert (btns != null);
            mButtons = btns;
            return this;
        }

        public Builder setEditTextHint(String[] hints) {
            assert (hints != null);
            mEditTextHint = hints;
            return this;
        }

        public Builder setEditTextPre(String[] pres) {
            assert (pres != null);
            mEditTextPre = pres;
            return this;
        }

        public Builder setEditTextMsg(String[] pres) {
            assert (pres != null);
            mEditTextMsg = pres;
            return this;
        }

        public Builder setEditModes(DialogMode[] modes) {
            assert (modes != null);
            mEditMode = modes;
            return this;
        }

        public Builder setValidInterface(IOnValid[] valid) {
            assert (valid != null);
            mOnValid = valid;
            return this;
        }

        public Builder setCancelable(boolean cancelable) {
            mCancelable = cancelable;
            return this;
        }

        public Builder setCancelIndex(int index) {
            mCancelIndex = index;
            return this;
        }

        public Builder setDialogMode(DialogMode mode) {
            mMode = mode;
            return this;
        }

        public Builder setTitleColor(int resId) {
            mTitleColor = resId;
            return this;
        }

        public Builder setMessageColor(int resId) {
            mMessageColor = resId;
            return this;
        }

        public Builder setButtonColors(int resId[]) {
            if (resId != null) {
                mButtonColors = resId;
            }
            return this;
        }

        public Builder setOnDialogButtonClick(OnDialogButtonClick l) {
            mDialogListener = l;
            return this;
        }

        public Builder setMultiEditFinish(OnMultiEditFinish l) {
            mMultiEditFinish = l;
            return this;
        }

        public Builder setMultiSelectFinish(OnMultiSelectFinish l) {
            mMultiSelectedFinish = l;
            return this;
        }

        public Builder setCustomViewId(int id) {
            mCustomViewId = id;
            return this;
        }

        public Builder setCustomView(View view) {
            mCustomView = view;
            return this;
        }

        public CommonDialog build() {
            return getNewInstance(this);
        }

        public Builder setDismissListener(OnDismiss listener) {
            mDismissListener = listener;
            return this;
        }
    }

    public interface OnDismiss {
        void onDismiss(boolean isClickBtn);
    }

    /**
     * 对输入的数据进行有效性检测
     */
    public interface IOnValid {
        boolean isValid(EditText et);

        String getErrorMessage();
    }

}
