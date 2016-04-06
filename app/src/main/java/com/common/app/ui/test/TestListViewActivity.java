package com.common.app.ui.test;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.common.app.R;
import com.common.app.ui.BaseListActivity;
import com.common.listview.AbsListDataAdapter;
import com.common.listview.BaseListCell;
import com.common.listview.BaseListDataAdapter;

/**
 * Created by houlijiang on 15/12/18.
 * 
 * 测试列表控件
 */
public class TestListViewActivity extends BaseListActivity {

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
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Data[] data = new Data[30];
                        for (int i = 0; i < data.length; i++) {
                            data[i] = new Data();
                            data[i].name = "这是测试文本，index：" + i;
                        }
                        mAdapter.addAll(data);
                    }
                }, 2000);
            }
        });
    }

    @Override
    protected void loadFirstPage() {
        loadList();
    }

    @Override
    public void onListRefresh() {
        loadList();
    }

    private void loadList(){
        final Data[] data = new Data[30];
        for (int i = 0; i < data.length; i++) {
            data[i] = new Data();
            data[i].name = "这是测试文本，index：" + i;
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mRecyclerListView.stopRefresh();
                mAdapter.addAll(data);
            }
        }, 2000);


       /* mDataService.getTeacherList(this, mCategoryId, mPageNum, new IDataServiceCallback<TeacherListModel>() {
            @Override
            public void onSuccess(DataServiceResultModel result, TeacherListModel obj, Object param) {
                int pageNum = (int) param;
                if (pageNum == ApiConstants.API_LIST_FIRST_PAGE) {
                    mAdapter.clearData();
                }
                mAdapter.addAll(obj.list);
                mHasMore = obj.pageInfo.hasMore;
                mPageNum++;
            }

            @Override
            public void onError(ErrorModel result, Object param) {
                int pageNum = (int) param;
                if (pageNum == ApiConstants.API_LIST_FIRST_PAGE) {
                    showErrorView(result);
                } else {
                    Tips.showMessage(TeacherListActivity.this, result.message);
                }
            }
        }, mPageNum);*/

    }

    public class MyAdapter extends BaseListDataAdapter<Data> {

        public MyAdapter(IOnLoadMore listener) {
            super(listener);
        }

        @Override
        protected BaseListCell<Data> createCell(int type) {
            return new ItemCell();
        }
    }

    public class ItemCell implements BaseListCell<Data>, View.OnClickListener {

        private TextView tv;
        private View btn1;
        private View btn2;

        @Override
        public void setData(Data model, int position) {
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

    public class Data {
        public String name;
    }
}
