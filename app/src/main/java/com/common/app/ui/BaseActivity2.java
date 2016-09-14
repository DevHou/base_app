package com.common.app.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.common.app.R;
import com.common.app.base.utils.EventUtils;
import com.common.app.event.ExitAppEvent;
import com.common.app.uikit.Tips;
import com.common.utils.AppLog;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by houlijiang on 16/9/14.
 * 
 * 使用toolbar做title的基础类
 */
public abstract class BaseActivity2 extends AppCompatActivity {

    private static final String TAG = BaseActivity2.class.getSimpleName();
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindContentView();

        mToolbar = (Toolbar) findViewById(R.id.layout_title_toolbar_toolbar);
        setSupportActionBar(mToolbar);

        mToolbar.setTitle("Title");
        // mToolbar.setSubtitle("SubTitle");
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Tips.showMessage(BaseActivity2.this, item.getTitle().toString());
                return false;
            }
        });

        EventUtils.register(this);
    }

    @Override
    protected void onDestroy() {
        EventUtils.unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        AppLog.d(TAG, "onCreateOptionsMenu");
        MenuItem item = menu.add("Menu 1");
        item.setTitle("Menu Title");
        item.setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    /**
     * 退出软件，所有activity直接finish
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onExitApp(ExitAppEvent event) {
        finish();
    }

    protected abstract boolean bindContentView();

}
