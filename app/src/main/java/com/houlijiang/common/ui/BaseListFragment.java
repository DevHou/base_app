package com.houlijiang.common.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.houlijiang.app.base.error.ErrorModel;
import com.houlijiang.common.R;
import com.houlijiang.common.listview.MySectionIndexer;


/**
 * Created by houlijiang on 15/3/31.
 *
 * 列表fragment
 */
public abstract class BaseListFragment extends AbsListFragment {

    private static final String TAG = BaseListFragment.class.getSimpleName();

    /**
     * 有一个默认的layout，可以根据需求重写layout，默认布局中空、加载中、错误等都使用的默认的
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_layout_listview, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
        return new LinearLayoutManager(getActivity());
    }

    /**
     * 显示 ErrorView。 在数据加载失败的情况下，主动调用此方法
     */
    public void showErrorView(ErrorModel result) {
        // 根据 result code 的情况显示不同的 errorView
        //TXErrorUtils.showErrorView(getActivity(), mListErrorView, result, noNetworkErrorClickListener);
        mRecyclerListView.showErrorView();
    }

    private View.OnClickListener noNetworkErrorClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onRefresh();
        }
    };
}
