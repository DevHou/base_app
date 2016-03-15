package com.common.app.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.app.R;
import com.common.app.event.ExitAppEvent;
import com.common.image.CommonImageView;
import com.common.image.ImageLoader;
import com.common.utils.DisplayUtils;
import com.common.utils.InputMethodUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;


/**
 * Created by houlijiang on 16/1/25.
 * <p/>
 * 处理通用title bar
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getSimpleName();

    private static final int RIGHT_STATE_UNSET = -1;
    private static final int RIGHT_STATE_ENABLE = 0;
    private static final int RIGHT_STATE_DISABLE = 1;

    private View.OnClickListener mLeftClickListener;// 左侧返回按钮点击处理

    private int mRightState = RIGHT_STATE_UNSET;// title bar按钮是否可点击
    // 自定义菜单项
    private CustomMenuItem[] mCustomMenuItems = null;
    private IOnMenuClick mOnMenuClick;

    protected View mTitle;// 整个title bar
    private View mViewBackBtn;// 返回按钮的父view，便于点击
    private ViewGroup mVgRightIcons;// 右侧显示的按钮列表
    private TextView mTvTitle;

    private PopupWindow mPopupWindow;// 更多菜单弹窗
    private ViewGroup mVgPopItems;// 弹窗列表

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindContentView();

        mTitle = findViewById(R.id.layout_title_fl);
        if (mTitle != null) {
            mViewBackBtn = findViewById(R.id.layout_title_fl_back);
            mTvTitle = (TextView) findViewById(R.id.layout_title_tv_title);
            mVgRightIcons = (ViewGroup) findViewById(R.id.layout_title_ll_buttons);
            mVgPopItems = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_title_popup, null);
        }
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    /**
     * 刷新右侧菜单
     */
    private void refreshTitleButtons() {
        if (mTitle == null) {
            return;
        }

        mVgRightIcons.removeAllViews();
        mVgPopItems.removeAllViews();
        if (mCustomMenuItems != null && mCustomMenuItems.length > 0) {
            boolean showMoreIcon = false;
            boolean firstPopItem = true;
            for (final CustomMenuItem item : mCustomMenuItems) {
                View v;
                if (item.showType == CustomMenuShowType.TYPE_ALWAYS) {
                    // 显示在右侧
                    v = LayoutInflater.from(this).inflate(R.layout.item_title_bar_icon, mVgRightIcons, false);
                    CommonImageView iv = (CommonImageView) v.findViewById(R.id.common_item_title_bar_icon_iv);
                    TextView tv = (TextView) v.findViewById(R.id.common_item_title_bar_icon_tv);
                    if (item.icon > 0) {
                        ImageLoader.displayImage(item.icon, iv, null);
                    } else if (!TextUtils.isEmpty(item.iconUri)) {
                        ImageLoader.displayImage(item.iconUri, iv, null);
                    } else if (!TextUtils.isEmpty(item.text)) {
                        tv.setText(item.text);
                    }
                    mVgRightIcons.addView(v);
                } else {
                    showMoreIcon = true;
                    // 弹出菜单
                    v = LayoutInflater.from(this).inflate(R.layout.item_title_bar_popup, mVgPopItems, false);
                    TextView tv = (TextView) v.findViewById(R.id.common_item_title_bar_popup_tv);
                    tv.setText(item.text);
                    View divider = v.findViewById(R.id.common_item_title_bar_popup_line);
                    if (firstPopItem) {
                        divider.setVisibility(View.GONE);
                    } else {
                        divider.setVisibility(View.VISIBLE);
                    }
                    firstPopItem = false;
                    mVgPopItems.addView(v);
                }
                if (mRightState != RIGHT_STATE_UNSET) {
                    v.setEnabled(mRightState == RIGHT_STATE_ENABLE);
                }
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mOnMenuClick != null) {
                            mOnMenuClick.onMenuClick(item.id, item.param);
                        }
                        if (mPopupWindow != null) {
                            mPopupWindow.dismiss();
                        }
                    }

                });
            }
            if (showMoreIcon) {
                final View v = LayoutInflater.from(this).inflate(R.layout.item_title_bar_icon, mVgRightIcons, false);
                CommonImageView iv = (CommonImageView) v.findViewById(R.id.common_item_title_bar_icon_iv);
                ImageLoader.displayImage(R.drawable.ic_title_more_action, iv, null);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int windowsWidth = DisplayUtils.getScreenWidthPixels(BaseActivity.this) / 3;
                        if (mPopupWindow == null) {
                            mPopupWindow =
                                    new PopupWindow(mVgPopItems, windowsWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
                            mPopupWindow.setFocusable(true);
                            mPopupWindow.setOutsideTouchable(true);
                            mPopupWindow.setBackgroundDrawable(new ColorDrawable(getResources().getColor(
                                    android.R.color.transparent)));
                        }

                        int x = windowsWidth / 2;
                        // int[] location = new int[2];
                        // v.getLocationOnScreen(location);
                        // mPopupWindow.showAtLocation(mRightMore, Gravity.NO_GRAVITY, location[0] - x, location[1]
                        // + mRightMore.getHeight());
                        mPopupWindow.showAsDropDown(v, -x, 0);
                    }
                });
                mVgRightIcons.addView(v);
            }
        }
    }

    /**
     * 隐藏titleBar
     */
    public void hideTitleBar() {
        if (mTitle != null) {
            mTitle.setVisibility(View.GONE);
        }
    }

    public void showTitleBar() {
        if (mTitle != null) {
            mTitle.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示返回按键
     */
    protected void showBackBtn() {
        if (mViewBackBtn != null) {
            mViewBackBtn.setVisibility(View.VISIBLE);
        }
        mLeftClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
        if (mViewBackBtn != null) {
            mViewBackBtn.setOnClickListener(mLeftClickListener);
        }
    }

    /**
     * 显示返回按键
     *
     * @param listener 返回键点击回调
     */
    protected void showBackBtn(final View.OnClickListener listener) {
        if (mViewBackBtn != null) {
            mViewBackBtn.setVisibility(View.VISIBLE);
        }
        mLeftClickListener = listener;
        if (mViewBackBtn != null) {
            mViewBackBtn.setOnClickListener(mLeftClickListener);
        }
    }

    /**
     * 显示返回键以及返回键右面的文本
     *
     * @param title 返回按钮右面的文本
     */
    public void showBackBtnWithText(String title) {
        showBackBtn();
        if (mViewBackBtn != null && !TextUtils.isEmpty(title)) {
            mViewBackBtn.setVisibility(View.VISIBLE);
            if (mTvTitle != null) {
                mTvTitle.setText(title);
            }
        }
    }

    /**
     * 显示返回键以及返回键右面的文本
     *
     * @param title    返回按钮右面的文本
     * @param listener 返回键点击回调
     */
    protected void showBackBtnWithText(String title, final View.OnClickListener listener) {
        showBackBtn(listener);
        if (mViewBackBtn != null && !TextUtils.isEmpty(title)) {
            mViewBackBtn.setVisibility(View.VISIBLE);
            if (mTvTitle != null) {
                mTvTitle.setText(title);
            }
        }
    }

    /**
     * 设置中间标题栏
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        }
    }

    /**
     * 设置中间标题栏
     *
     * @param id 资源ID
     */
    public void setTitle(int id) {
        setTitle(getString(id));
    }

    /**
     * 设置标题栏右侧按钮
     */
    public void setRight(String str, final View.OnClickListener listener) {
        CustomMenuItem[] items = new CustomMenuItem[1];
        items[0] = new CustomMenuItem();
        items[0].text = str;
        items[0].showType = CustomMenuShowType.TYPE_ALWAYS;
        IOnMenuClick menuClickListener = null;
        if (listener != null) {
            menuClickListener = new IOnMenuClick() {
                @Override
                public void onMenuClick(int id, Object param) {
                    listener.onClick(mTitle);
                }
            };
        }
        setCustomMenu(items, menuClickListener);
    }

    /**
     * 设置标题栏右侧按钮
     */
    public void setRight(int iconResId, final View.OnClickListener listener) {
        CustomMenuItem[] items = new CustomMenuItem[1];
        items[0] = new CustomMenuItem();
        items[0].text = "";
        items[0].icon = iconResId;
        items[0].showType = CustomMenuShowType.TYPE_ALWAYS;
        IOnMenuClick menuClickListener = null;
        if (listener != null) {
            menuClickListener = new IOnMenuClick() {
                @Override
                public void onMenuClick(int id, Object param) {
                    listener.onClick(mTitle);
                }
            };
        }
        setCustomMenu(items, menuClickListener);
    }

    /**
     * 设置右侧自定义按钮
     *
     * @param menus    菜单
     * @param listener 回调
     */
    public void setCustomMenu(CustomMenuItem[] menus, IOnMenuClick listener) {
        Log.v(TAG, "setCustomMenu items");
        mCustomMenuItems = menus;
        mOnMenuClick = listener;
        refreshTitleButtons();
    }

    /**
     * 设置标题右侧按钮状态，是否可点击
     *
     * @param enable 是否可用
     */
    public void setRightState(boolean enable) {
        Log.v(TAG, "setRightState, will set enable:" + enable);
        mRightState = enable ? RIGHT_STATE_ENABLE : RIGHT_STATE_DISABLE;
        refreshTitleButtons();
    }

    /**
     * 左侧按钮点击
     */
    public void onLeftButtonClick() {
        if (mLeftClickListener != null) {
            mLeftClickListener.onClick(mTitle);
        }
    }

    /**
     * 隐藏输入法
     */
    public static void hideInputMethod(Activity activity) {
        InputMethodUtils.hideSoftInput(activity);
    }

    /**
     * 显示输入法
     */
    public static void showInputMethod(View view) {
        if (view == null) {
            return;
        }
        InputMethodUtils.showSoftInput(view);
    }

    /**
     * 退出软件，所有activity直接finish
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExitApp(ExitAppEvent event) {
        finish();
    }

    protected void userLogout() {
    }

    protected void userBeLogout() {
    }

    protected abstract boolean bindContentView();

    public static class CustomMenuShowType {
        public static final int TYPE_ALWAYS = MenuItem.SHOW_AS_ACTION_ALWAYS;// 作为一个按钮
        public static final int TYPE_NEVER = MenuItem.SHOW_AS_ACTION_NEVER;
    }

    /**
     * 自定义菜单
     */
    public static class CustomMenuItem {
        public int id;
        public String text;
        public Object param;
        public int icon;
        public String iconUri;
        public int showType;
    }

    /**
     * 自定义菜单点击回调
     */
    public interface IOnMenuClick {
        void onMenuClick(int id, Object param);
    }
}
