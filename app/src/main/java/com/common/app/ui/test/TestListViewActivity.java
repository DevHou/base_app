package com.common.app.ui.test;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.common.app.R;
import com.common.app.api.ApiConstants;
import com.common.app.api.TestDataModel;
import com.common.app.base.error.ErrorModel;
import com.common.app.base.manager.DataServiceManager;
import com.common.app.base.service.DataServiceResultModel;
import com.common.app.base.service.IDataServiceCallback;
import com.common.app.service.AuthDataService;
import com.common.app.ui.BaseListActivity;
import com.common.app.uikit.Tips;
import com.common.listview.AbsListDataAdapter;
import com.common.listview.BaseListCell;
import com.common.listview.BaseListDataAdapter;

/**
 * Created by houlijiang on 15/12/18.
 * 
 * 测试列表控件
 */
public class TestListViewActivity extends BaseListActivity {

    private static final String TAG = TestListViewActivity.class.getSimpleName();

    private AuthDataService mDataService = (AuthDataService) DataServiceManager.getService(AuthDataService.SERVICE_KEY);

    private int mPageNum;
    private boolean mHasMore;

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_listview);
        return true;
    }

    @Override
    protected int getListViewId() {
        return R.id.test_listview_list;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected AbsListDataAdapter getAdapter(Context context) {
        return new MyAdapter(new AbsListDataAdapter.IOnLoadMore() {
            @Override
            public void onLoadMore() {
                loadList(false);
            }
        });
    }

    @Override
    protected void loadFirstPage() {
        initFirstPage();
        loadList(true);
    }

    @Override
    public void onListRefresh() {
        initFirstPage();
        loadList(false);
    }

    private void initFirstPage() {
        mPageNum = ApiConstants.API_LIST_FIRST_PAGE;
        mHasMore = true;
    }

    private void loadList(boolean readCache) {

        mDataService.getTestList(this, readCache, mPageNum, new IDataServiceCallback<TestDataModel>() {
            @Override
            public void onSuccess(DataServiceResultModel result, TestDataModel obj, Object param) {
                int pageNum = (int) param;
                if (pageNum == ApiConstants.API_LIST_FIRST_PAGE) {
                    mAdapter.clearData();
                    mRecyclerListView.stopRefresh();
                    // 先置成不可加载更多防止因为读取缓存导致循环读取
                    mAdapter.setIsLoading();
                    mAdapter.setIfHasMore(false);
                }
                mHasMore = obj.pageInfo.hasMore;
                if (!result.isCache) {
                    // 不是缓存的才设置是否能加载更多并且增加页码数
                    mAdapter.setIfHasMore(mHasMore);
                    mPageNum++;
                }
                mAdapter.addAll(obj.list);
            }

            @Override
            public void onError(ErrorModel result, Object param) {
                int pageNum = (int) param;
                if (pageNum == ApiConstants.API_LIST_FIRST_PAGE) {
                    showErrorView(result);
                } else {
                    Tips.showMessage(TestListViewActivity.this, result.message);
                }
            }
        }, mPageNum);

    }

    public class MyAdapter extends BaseListDataAdapter<TestDataModel.DataItem> {

        public MyAdapter(IOnLoadMore listener) {
            super(listener);
        }

        @Override
        protected BaseListCell<TestDataModel.DataItem> createCell(int type) {
            return new ItemCell();
        }
    }

    public class ItemCell implements BaseListCell<TestDataModel.DataItem>, View.OnClickListener {

        private TextView tv;
        private View btn1;
        private View btn2;

        @Override
        public void setData(TestDataModel.DataItem model, int position) {
            tv.setText(model.name);
            btn1.setTag(model.name);
            btn2.setTag(model.name);
            btn1.setOnClickListener(this);
            btn2.setOnClickListener(this);
        }

        @Override
        public int getCellResource() {
            return R.layout.item_test_listview;
        }

        @Override
        public void initialChildViews(View view) {
            tv = (TextView) view.findViewById(R.id.item_test_listview_tv);
            btn1 = view.findViewById(R.id.item_test_listview_btn1);
            btn2 = view.findViewById(R.id.item_test_listview_btn2);
        }

        @Override
        public void onClick(View view) {
            String str = (String) view.getTag();
            Toast.makeText(view.getContext(), str, Toast.LENGTH_SHORT).show();
        }
    }

}
