package com.common.app.ui.test;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.common.app.R;
import com.common.app.ui.BaseActivity;
import com.common.app.uikit.Tips;
import com.common.cache.disk.DiskCache;

/**
 * Created by houlijiang on 15/12/23.
 * 
 * 缓存
 */
public class TestCacheActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = TestCacheActivity.class.getSimpleName();

    private TextView tv;
    private EditText et;

    private final String cacheKey = "xxxx";

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_cache);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showBackBtn();
        setTitle("测试缓存");

        CustomMenuItem[] items = new CustomMenuItem[2];
        items[0] = new CustomMenuItem();
        items[0].id = 0;
        items[0].text = "menu0";
        items[0].enable = false;
        items[0].showType = CustomMenuShowType.TYPE_ALWAYS;
        items[1] = new CustomMenuItem();
        items[1].id = 1;
        items[1].text = "";
        items[1].iconUri = "http://img.zcool.cn/community/01e50a55bee3b66ac7253f361e874b.jpg";
        items[1].enable = true;
        items[1].showType = CustomMenuShowType.TYPE_ALWAYS;
        setCustomMenu(items, new IOnMenuClick() {
            @Override
            public void onMenuClick(int id, Object param) {
                Tips.showMessage(TestCacheActivity.this, "title click:" + id);
            }
        });

        tv = (TextView) findViewById(R.id.test_cache_tv_read);
        et = (EditText) findViewById(R.id.test_cache_et);

        findViewById(R.id.test_cache_read).setOnClickListener(this);
        findViewById(R.id.test_cache_write).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.test_cache_read: {
                String str = DiskCache.getString(cacheKey);
                tv.setText("" + str);
                break;
            }
            case R.id.test_cache_write: {
                String str = et.getText().toString();
                DiskCache.put(cacheKey, str);
            }
        }
    }
}
