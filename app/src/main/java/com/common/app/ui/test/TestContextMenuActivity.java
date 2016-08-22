package com.common.app.ui.test;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.common.app.R;
import com.common.app.ui.BaseListActivity;
import com.common.app.uikit.Tips;
import com.common.listview.AbsListDataAdapter;
import com.common.listview.BaseListCell;
import com.common.listview.BaseListDataAdapter;

/**
 * Created by houlijiang on 16/2/26.
 *
 * 测试上下文菜单
 */
public class TestContextMenuActivity extends BaseListActivity {

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_context_menu);
        return true;
    }

    @Override
    protected int getListViewId() {
        return R.id.test_context_menu_list;
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

    private void loadList() {
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

        /*
         * mDataService.getTeacherList(this, mCategoryId, mPageNum, new IDataServiceCallback<TeacherListModel>() {
         * 
         * @Override
         * public void onSuccess(DataServiceResultModel result, TeacherListModel obj, Object param) {
         * int pageNum = (int) param;
         * if (pageNum == ApiConstants.API_LIST_FIRST_PAGE) {
         * mAdapter.clearData();
         * }
         * mAdapter.addAll(obj.list);
         * mHasMore = obj.pageInfo.hasMore;
         * mPageNum++;
         * }
         * 
         * @Override
         * public void onError(ErrorModel result, Object param) {
         * int pageNum = (int) param;
         * if (pageNum == ApiConstants.API_LIST_FIRST_PAGE) {
         * showErrorView(result);
         * } else {
         * Tips.showMessage(TeacherListActivity.this, result.message);
         * }
         * }
         * }, mPageNum);
         */

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

        private ActionMode mActionMode;

        private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

            // Called when the action mode is created; startActionMode() was called
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate a menu resource providing context menu items
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            // Called each time the action mode is shown. Always called after onCreateActionMode, but
            // may be called multiple times if the mode is invalidated.
            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false; // Return false if nothing is done
            }

            // Called when the user selects a contextual menu item
            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.context_menu_del: {
                        Tips.showMessage(TestContextMenuActivity.this, "删除");
                        mode.finish();
                        return true;
                    }
                    case R.id.context_menu_save: {
                        Tips.showMessage(TestContextMenuActivity.this, "保存");
                        mode.finish();
                        return true;
                    }
                    default:
                        return false;
                }
            }

            // Called when the user exits the action mode
            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mActionMode = null;
            }
        };

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

            view.setOnLongClickListener(new View.OnLongClickListener() {
                // Called when the user long-clicks on someView
                public boolean onLongClick(View view) {
                    if (mActionMode != null) {
                        return false;
                    }

                    // Start the CAB using the ActionMode.Callback defined above
                    //mActionMode = startActionMode(mActionModeCallback);
                    view.setSelected(true);
                    return true;
                }
            });
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
