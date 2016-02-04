package com.houlijiang.common.ui.test;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.houlijiang.common.R;
import com.houlijiang.common.cache.disk.DiskCache;
import com.houlijiang.common.ui.BaseActivity;


/**
 * Created by houlijiang on 15/12/23.
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
