package com.houlijiang.common.ui.activity;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.houlijiang.common.R;
import com.houlijiang.common.listview.BaseListCell;
import com.houlijiang.common.listview.BaseListDataAdapter;
import com.houlijiang.common.listview.ptr.PtrFrameLayout;
import com.houlijiang.common.listview.ptr.PtrHandler;
import com.houlijiang.common.ui.BaseActivity;

/**
 * Created by houlijiang on 16/2/1.
 * 
 * 测试下拉刷新控件
 */
public class TestPtrActivity extends BaseActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = TestPtrActivity.class.getSimpleName();

    private PtrFrameLayout mPtr;
    private RecyclerView mRv;

    private AppBarLayout mAppBarLayout;
    private int mAppBarOffset;

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_ptr);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPtr = (PtrFrameLayout) findViewById(R.id.test_ptr_ptr);
        mRv = (RecyclerView) findViewById(R.id.test_ptr_rv);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.test_ptr_appbar);

        final BaseListDataAdapter adapter = new BaseListDataAdapter<>(TestCell.class);
        mRv.setLayoutManager(new LinearLayoutManager(this));
        mRv.setAdapter(adapter);
        TestData[] data = createData(10);
        adapter.addAll(data);

        mPtr.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                return checkCanRefresh();
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mPtr.refreshComplete();
                        TestData[] data = createData(10);
                        adapter.clearData();
                        adapter.addAll(data);
                    }
                }, 2000);
            }
        });
        PtrClassicDefaultHeader header = new PtrClassicDefaultHeader(this);
        mPtr.setHeaderView(header);
        mPtr.addPtrUIHandler(header);

        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    int index = 0;

    private TestData[] createData(int num) {
        TestData[] data = new TestData[num];
        for (int i = 0; i < data.length; i++) {
            data[i] = new TestData();
            data[i].str = "str" + index++;
        }
        return data;
    }

    public boolean checkCanRefresh() {
        if (mAppBarOffset != 0) {
            return false;
        }
        if (mRv.getChildCount() == 0) {
            return true;
        }
        int top = mRv.getChildAt(0).getTop();
        if (top != 0) {
            return false;
        }
        final RecyclerView recyclerView = mRv;
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            int position = ((LinearLayoutManager) layoutManager).findFirstCompletelyVisibleItemPosition();
            if (position == 0) {
                return true;
            } else if (position == -1) {
                position = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
                return position == 0;
            }
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            boolean allViewAreOverScreen = true;
            int[] positions = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
            for (int position : positions) {
                if (position == 0) {
                    return true;
                }
                if (position != -1) {
                    allViewAreOverScreen = false;
                }
            }
            if (allViewAreOverScreen) {
                positions = ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(null);
                for (int position : positions) {
                    if (position == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        Log.d(TAG, "AppBarLayout offset:" + verticalOffset);
        mAppBarOffset = verticalOffset;
    }

    public static class TestCell implements BaseListCell<TestData> {

        private TextView mTvHead;

        public TestCell() {
        }

        @Override
        public void setData(TestData model, int position) {
            mTvHead.setText(model.str);
        }

        @Override
        public int getCellResource() {
            return R.layout.item_test_listview;
        }

        @Override
        public void initialChildViews(View view) {
            mTvHead = (TextView) view.findViewById(R.id.item_test_listview_tv);
        }
    }

    public static class TestData {
        public String str;
    }
}
