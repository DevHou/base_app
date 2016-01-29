package com.houlijiang.common.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.houlijiang.app.base.error.ErrorModel;
import com.houlijiang.common.R;
import com.houlijiang.common.listview.MySectionIndexer;

/**
 * Created by houlijiang on 15/12/7.
 * 
 * 和Fragment一样，只是通过activity实现
 */
public abstract class BaseListActivity extends AbsListActivity {

    /**
     * 有一个默认的layout，可以根据需求重写layout，默认布局中空、加载中、错误等都使用的默认的
     */
    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_base_list);
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * 如果重写了layout文件则要重载这个方法，返回layout里的AbsListView id
     */
    @Override
    protected int getListViewId() {
        return R.id.layout_listview_lv;
    }

    /**
     * 如果需要显示右侧的indexer，这里就需要返回实现接口的对象
     */
    @Override
    protected MySectionIndexer getIndexer() {
        return null;
    }

    /**
     * 默认是Linear，可以根据布局重载，返回不同的manager
     */
    @Override
    protected RecyclerView.LayoutManager getLayoutManager() {
        return new LinearLayoutManager(this);
    }

    public void showErrorView(ErrorModel result) {
        // 根据 result code 的情况显示不同的 errorView
       // TXErrorUtils.showErrorView(this, mListErrorView, result, noNetworkErrorClickListener);
        mRecyclerListView.showErrorView();
        // TODO 根据 code 显示不同的 Error.
    }

}
