package com.common.app.ui;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.common.app.R;
import com.common.app.base.utils.EventUtils;
import com.common.app.event.ExitAppEvent;
import com.common.image.CommonImageView;
import com.common.image.ImageLoader;
import com.common.utils.AppLog;
import com.common.utils.DisplayUtils;
import com.common.utils.InputMethodUtils;

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

    // 自定义方式的title
    protected View mTitle;// 整个title bar
    private View mViewBackBtn;// 返回按钮的父view，便于点击
    private ViewGroup mVgRightIcons;// 右侧显示的按钮列表
    private TextView mTvTitle;
    private PopupWindow mPopupWindow;// 更多菜单弹窗
    private ViewGroup mVgPopItems;// 弹窗列表
    // toolbar方式实现的title
    private Toolbar mToolbar;

    /**
     * 简化findViewById
     */
    public <T extends View> T $(int id) {
        return (T) findViewById(id);
    }

    public static <T extends View> T $(View parent, int id) {
        if (parent == null) {
            return null;
        } else {
            return (T) parent.findViewById(id);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindContentView();

        mTitle = findViewById(R.id.layout_title_custom_fl);
        if (mTitle != null) {
            mViewBackBtn = findViewById(R.id.layout_title_custom_fl_back);
            mTvTitle = (TextView) findViewById(R.id.layout_title_custom_tv_title);
            mVgRightIcons = (ViewGroup) findViewById(R.id.layout_title_custom_ll_buttons);
            mVgPopItems = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.layout_title_custom_popup, null);
        }
        mToolbar = (Toolbar) findViewById(R.id.layout_title_toolbar_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
        }

        EventUtils.register(this);
    }

    @Override
    protected void onDestroy() {
        EventUtils.unregister(this);
        super.onDestroy();
    }

    /**
     * 刷新右侧菜单
     */
    private void refreshTitleButtons() {
        if (mTitle != null) {
            refreshCustomButtons();
        } else if (mToolbar != null) {
            refreshToolbarMenus();
        }
    }

    /**
     * 自定义的菜单
     */
    private void refreshCustomButtons() {
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
                        ImageLoader.displayImage(this, item.icon, iv, null);
                    } else if (!TextUtils.isEmpty(item.iconUri)) {
                        ImageLoader.displayImage(this, item.iconUri, iv, null);
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
                v.setEnabled(item.enable);// 先按照每个item设置
                // 再根据全局设置
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
                ImageLoader.displayImage(this, R.drawable.ic_title_more_action, iv, null);
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

    private void refreshToolbarMenus() {
        invalidateOptionsMenu();// 告诉系统重构menu
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.clear();
        if (mCustomMenuItems != null && mCustomMenuItems.length > 0) {
            int i = 0;
            for (final CustomMenuItem item : mCustomMenuItems) {
                MenuItem mi = menu.add(String.valueOf(i++));
                if (!TextUtils.isEmpty(item.text)) {
                    mi.setTitle(item.text);
                }
                if (item.icon > 0) {
                    mi.setIcon(item.icon);
                }
                if (!TextUtils.isEmpty(item.iconUri)) {
                    View v = LayoutInflater.from(this).inflate(R.layout.item_title_bar_icon, null);
                    CommonImageView iv = (CommonImageView) v.findViewById(R.id.common_item_title_bar_icon_iv);
                    if (item.isLast && item.showType == CustomMenuShowType.TYPE_ALWAYS) {
                        // 最有一个并且要在标题栏上直接显示的最右面要加个空
                        View pad = v.findViewById(R.id.common_item_title_bar_icon_pad);
                        pad.setVisibility(View.VISIBLE);
                    }
                    ImageLoader.displayImage(this, item.iconUri, iv, null);
                    mi.setActionView(v);
                    v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (mOnMenuClick != null) {
                                mOnMenuClick.onMenuClick(item.id, item.param);
                            }
                        }
                    });
                }
                mi.setEnabled(item.enable);
                mi.setShowAsActionFlags(item.showType);

                mi.setEnabled(item.enable);// 先按照每个item设置
                // 再根据全局设置
                if (mRightState != RIGHT_STATE_UNSET) {
                    mi.setEnabled(mRightState == RIGHT_STATE_ENABLE);
                }
                mi.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (mOnMenuClick != null) {
                            mOnMenuClick.onMenuClick(item.id, item.param);
                        }
                        return false;
                    }
                });
            }
        }
        return true;
    }

    /**
     * 隐藏titleBar
     */
    public void hideTitleBar() {
        if (mTitle != null) {
            mTitle.setVisibility(View.GONE);
        } else if (mToolbar != null) {
            mToolbar.setVisibility(View.GONE);
        }
    }

    public void showTitleBar() {
        if (mTitle != null) {
            mTitle.setVisibility(View.VISIBLE);
        } else if (mToolbar != null) {
            mToolbar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 显示返回按键
     */
    protected void showBackBtn() {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        };
        showBackBtn(listener);
    }

    /**
     * 显示返回按键
     *
     * @param listener 返回键点击回调
     */
    protected void showBackBtn(final View.OnClickListener listener) {
        mLeftClickListener = listener;
        if (mViewBackBtn != null) {
            mViewBackBtn.setVisibility(View.VISIBLE);
        }
        if (mViewBackBtn != null) {
            mViewBackBtn.setOnClickListener(mLeftClickListener);
        } else if (mToolbar != null) {
            mToolbar.setNavigationOnClickListener(mLeftClickListener);
        }
    }

    /**
     * 显示返回键以及返回键右面的文本
     *
     * @param title 返回按钮右面的文本
     */
    public void showBackBtnWithText(String title) {
        showBackBtn();
        setTitle(title);
    }

    /**
     * 显示返回键以及返回键右面的文本
     *
     * @param title 返回按钮右面的文本
     * @param listener 返回键点击回调
     */
    protected void showBackBtnWithText(String title, final View.OnClickListener listener) {
        showBackBtn(listener);
        setTitle(title);
    }

    /**
     * 设置中间标题栏
     *
     * @param title 标题
     */
    public void setTitle(String title) {
        if (mTvTitle != null && !TextUtils.isEmpty(title)) {
            mTvTitle.setText(title);
        } else if (mToolbar != null && !TextUtils.isEmpty(title)) {
            mToolbar.setTitle(title);
            getSupportActionBar().setTitle(title);
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
     * @param menus 菜单
     * @param listener 回调
     */
    public void setCustomMenu(CustomMenuItem[] menus, IOnMenuClick listener) {
        AppLog.v(TAG, "setCustomMenu items");
        mCustomMenuItems = menus;
        if (mCustomMenuItems != null && mCustomMenuItems.length > 0) {
            mCustomMenuItems[mCustomMenuItems.length - 1].isLast = true;
        }
        mOnMenuClick = listener;
        refreshTitleButtons();
    }

    /**
     * 设置标题右侧按钮状态，是否可点击
     *
     * @param enable 是否可用
     */
    public void setRightState(boolean enable) {
        AppLog.v(TAG, "setRightState, will set enable:" + enable);
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
        public boolean enable = true;
        private boolean isLast = false;
    }

    /**
     * 自定义菜单点击回调
     */
    public interface IOnMenuClick {
        void onMenuClick(int id, Object param);
    }
}
