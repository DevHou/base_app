package com.common.listview;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.AppLog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by houlijiang on 2014/9/26.
 * 
 * 列表数据的通用adapter，使用AbsListView时需要继承这个adapter
 * 子类基本只需要重载setData getItemViewHolder两个方法即可
 *
 * 数据的操作使用handler做成串行的
 *
 * 使用注意事项：
 * > 任何对数据的操作都调用notifyDataChanged了，使用时不需要再次调用
 */
public abstract class AbsListDataAdapter<T> extends RecyclerView.Adapter<AbsListDataAdapter.ViewHolder> implements
    IAbsListDataAdapter {

    private static final String TAG = AbsListDataAdapter.class.getSimpleName();

    protected static final int TYPE_LOAD_MORE = Integer.MAX_VALUE;
    protected static final int TYPE_EMPTY_FOOTER = Integer.MAX_VALUE - 1;

    public static final int HANDLE_ADD_TO_FRONT = 1;
    public static final int HANDLE_ADD_ALL = 2;
    public static final int HANDLE_ADD_ONE = 3;
    public static final int HANDLE_INSERT = 4;
    public static final int HANDLE_REPLACE = 5;
    public static final int HANDLE_REMOVE = 6;
    public static final int HANDLE_CLEAR_DATA = 7;
    public static final int HANDLE_CLEAR = 8;
    public static final int HANDLE_NO_DATA_CHANGED = 9;
    public static final int HANDLE_EXCHANGE = 11;
    public static final int HANDLE_LOAD_MORE = 12;
    public static final int HANDLE_REFRESH_FOOTER = 13;

    protected List<T> mData;
    protected boolean mIsReloading = false;
    protected boolean mHasMore = true;
    protected int mLoadMoreId = 0;// 加载更多view ID
    private IOnLoadMore mIOnLoadMore;
    // 如果数据变换太频繁会抛Cannot call this method while RecyclerView is computing a layout or scrolling异常
    // 所以这里使用handler把数据修改串行起来
    private Handler mHandler;

    public AbsListDataAdapter() {
        this(null);
    }

    public AbsListDataAdapter(IOnLoadMore loadMore) {
        mData = new ArrayList<>();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLE_ADD_TO_FRONT: {
                        T[] data = (T[]) msg.obj;
                        if (mData == null) {
                            mData = new ArrayList<>();
                        }
                        if (data != null && data.length > 0) {
                            int startIndex = 0;
                            mData.addAll(startIndex, Arrays.asList(data));
                            mIsReloading = false;
                            notifyItemRangeInserted(startIndex, data.length);
                        } else {
                            mIsReloading = false;
                        }
                        break;
                    }
                    case HANDLE_ADD_ALL: {
                        T[] data = (T[]) msg.obj;
                        if (mData == null) {
                            mData = new ArrayList<>();
                        }
                        int oldPosition = mData.size();
                        if (data != null && data.length > 0) {
                            Collections.addAll(mData, data);
                            mIsReloading = false;
                            if (oldPosition > 0) {
                                notifyItemRangeInserted(oldPosition, data.length);
                            } else {
                                notifyDataSetChanged();
                            }
                        } else {
                            mIsReloading = false;
                        }
                        break;
                    }
                    case HANDLE_ADD_ONE: {
                        mData.add((T) msg.obj);
                        mIsReloading = false;
                        notifyItemInserted(mData.size() - 1);
                        break;
                    }
                    case HANDLE_INSERT: {
                        int position = msg.arg1;
                        mData.add(position, (T) msg.obj);
                        mIsReloading = false;
                        notifyItemInserted(position);
                        break;
                    }
                    case HANDLE_REPLACE: {
                        int position = msg.arg1;
                        if (mData == null || position < 0 || position > mData.size()) {
                            mIsReloading = false;
                            return;
                        }
                        mData.set(position, (T) msg.obj);
                        mIsReloading = false;
                        notifyItemChanged(position);
                        break;
                    }
                    case HANDLE_REMOVE: {
                        int position = msg.arg1;
                        mData.remove(position);
                        mIsReloading = false;
                        notifyItemRemoved(position);
                        break;
                    }
                    case HANDLE_CLEAR_DATA: {
                        if (mData == null) {
                            return;
                        }
                        mData.clear();
                        break;
                    }
                    case HANDLE_CLEAR: {
                        if (mData == null) {
                            mIsReloading = false;
                            return;
                        }
                        mData.clear();
                        mIsReloading = false;
                        notifyDataSetChanged();
                        break;
                    }
                    case HANDLE_NO_DATA_CHANGED: {
                        mIsReloading = false;
                        // 通过假的数据变化通知，来更新列表显示，隐藏加载中等进度
                        notifyDataSetChanged();
                        break;
                    }
                    case HANDLE_EXCHANGE: {
                        int i = msg.arg1;
                        int j = msg.arg2;
                        int len = mData == null ? 0 : mData.size();
                        if (i < 0 || i >= len || j < 0 || j >= len || i == j) {
                            mIsReloading = false;
                            break;
                        }
                        T obj = mData.get(i);
                        mData.set(i, mData.get(j));
                        mData.set(j, obj);
                        mIsReloading = false;
                        notifyItemChanged(i);
                        notifyItemChanged(j);
                        break;
                    }
                    case HANDLE_REFRESH_FOOTER: {
                        // AppLog.d(TAG, "notify footer changed, position:" + (mData == null ? 0 : mData.size()));
                        if (mData != null) {
                            notifyItemChanged(mData.size());
                        }
                        break;
                    }
                    case HANDLE_LOAD_MORE: {
                        if (mHasMore && mIOnLoadMore != null) {
                            mIOnLoadMore.onLoadMore();
                        }
                        break;
                    }
                }
            }
        };
        mIOnLoadMore = loadMore;
    }

    public void addToFront(T[] data) {
        mIsReloading = true;
        mHandler.obtainMessage(HANDLE_ADD_TO_FRONT, data).sendToTarget();
    }

    public void addAll(T[] data) {
        mIsReloading = true;
        if (data == null || data.length == 0) {
            refreshList();
        } else {
            mHandler.obtainMessage(HANDLE_ADD_ALL, data).sendToTarget();
        }
    }

    public void add(T obj) {
        mIsReloading = true;
        mHandler.obtainMessage(HANDLE_ADD_ONE, obj).sendToTarget();
    }

    public void insert(T obj, int position) {
        mIsReloading = true;
        mHandler.obtainMessage(HANDLE_INSERT, position, 0, obj).sendToTarget();
    }

    public void replace(T obj, int position) {
        mHandler.obtainMessage(HANDLE_REPLACE, position, 0, obj).sendToTarget();
    }

    public void remove(int position) {
        mIsReloading = true;
        mHandler.obtainMessage(HANDLE_REMOVE, position, 0).sendToTarget();
    }

    public void exchange(int i, int j) {
        mHandler.obtainMessage(HANDLE_EXCHANGE, i, j).sendToTarget();
    }

    public void refreshList() {
        mHandler.obtainMessage(HANDLE_NO_DATA_CHANGED).sendToTarget();
    }

    public void reloadFooterView() {
        mHandler.obtainMessage(HANDLE_REFRESH_FOOTER).sendToTarget();
    }

    /**
     * 清空数据
     */
    public void clearData() {
        mIsReloading = true;
        mHandler.obtainMessage(HANDLE_CLEAR_DATA).sendToTarget();
    }

    /**
     * 清空数据并通知数据变化
     */
    public void clear() {
        mHandler.obtainMessage(HANDLE_CLEAR).sendToTarget();
    }

    /**
     * 主动表示在加载中，AbsListView中会判断来显示加载中view
     */
    public void setIsLoading() {
        mIsReloading = true;
    }

    public void setIfHasMore(boolean hasMore) {
        // AppLog.d(TAG, "set has more:" + hasMore);
        mHasMore = hasMore;
        reloadFooterView();
    }

    public void dismissLoadMore() {
        mHasMore = false;
        reloadFooterView();
    }

    /**
     * 是否是底部的view
     */
    public boolean isFooterView(int position) {
        return mData != null && (position == mData.size());
    }

    public List<T> getAllData() {
        return mData;
    }

    public T getData(int position) {
        // AppLog.v(TAG, "get Data for " + position + " item count:" + getItemCount());
        if (position >= getDataSize()) {
            return null;
        }
        return mData.get(position);
    }

    @Override
    final public int getItemViewType(int position) {
        if (position == mData.size()) {
            // AppLog.v(TAG, "get view type for last " + position + ", has more:" + mHasMore);
            if (!mHasMore) {
                return TYPE_EMPTY_FOOTER;
            } else {
                return TYPE_LOAD_MORE;
            }
        }
        // AppLog.v(TAG, "get view type for " + position);
        return getViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_LOAD_MORE) {
            View v;
            // AppLog.v(TAG, "create view holder for load more, id:" + mLoadMoreId);
            if (mLoadMoreId != 0) {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(mLoadMoreId, viewGroup, false);
            } else {
                v =
                    LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_list_layout_load_more,
                        viewGroup, false);
            }
            return new LoadMoreViewHolder(v, useBinding());
        } else if (viewType == TYPE_EMPTY_FOOTER) {
            View v =
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_list_layout_empty_footer,
                    viewGroup, false);
            // AppLog.v(TAG, "create view holder for empty");
            return new EmptyViewHolder(v, useBinding());
        }
        // AppLog.v(TAG, "create view holder for type " + viewType);
        return getItemViewHolder(viewGroup, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (viewHolder instanceof LoadMoreViewHolder) {
            // AppLog.v(TAG, "bind view holder for load more");
            mHandler.obtainMessage(HANDLE_LOAD_MORE).sendToTarget();
            return;
        } else if (viewHolder instanceof EmptyViewHolder) {
            // AppLog.v(TAG, "bind view holder for empty");
            return;
        }
        // AppLog.v(TAG, "bind view holder for " + position);
        bindData(viewHolder, position, getData(position));
        viewHolder.getBinding().executePendingBindings();
    }

    @Override
    public long getItemId(int position) {
        // AppLog.v(TAG, "get view id for " + position);
        return position;
    }

    public int getDataSize() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    final public int getItemCount() {
        // AppLog.v(TAG, "get count, size:" + (mData == null ? 0 : mData.size()));
        // if (mIsReloading) {
        // return mData == null ? 0 : mData.size();
        // } else {
        // return (mData == null || mData.size() == 0) ? 0 : (mHasMore ? mData.size() + 1 : mData.size());
        return (mData == null || mData.size() == 0) ? 0 : mData.size() + 1;
        // }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ViewDataBinding binding;

        public ViewHolder(View itemView, boolean useBinding) {
            super(itemView);
            if (useBinding) {
                binding = DataBindingUtil.bind(itemView);
            }
        }

        public ViewDataBinding getBinding() {
            return binding;
        }
    }

    private static class LoadMoreViewHolder extends ViewHolder {

        public LoadMoreViewHolder(View itemView, boolean useBinding) {
            super(itemView, useBinding);
            // setIsRecyclable(false);
        }

    }

    private static class EmptyViewHolder extends ViewHolder {

        public EmptyViewHolder(View itemView, boolean useBinding) {
            super(itemView, useBinding);
            // setIsRecyclable(false);
        }

    }

    /**
     * 判断是否为空，子类可以重载加入自己的逻辑
     * AbsListView在显示时会用来判断以显示empty view
     */
    @Override
    public boolean isEmpty() {
        return mData == null || mData.size() == 0;
    }

    /**
     * AbsListView在显示时会用来判断以显示progress view
     * 同时如果在加载中则不让用户滚动列表，以免导致异常
     */
    @Override
    public boolean isReloading() {
        return mIsReloading;
    }

    /**
     * 设置加载更多
     */
    @Override
    public void setLoadMoreView(int resourceId) {
        AppLog.d(TAG, "set load more view id:" + resourceId);
        mLoadMoreId = resourceId;
        if (resourceId <= 0) {
            mHasMore = false;
        }
    }

    /**
     * 子类可以重载这个方法，来实现多类型item
     * 
     * @param position index
     * @return 类型
     */
    public int getViewType(int position) {
        return super.getItemViewType(position);
    }

    /**
     * 向每一个item设置数据
     *
     * @param viewHolder 视图
     * @param position index
     */
    protected abstract void bindData(ViewHolder viewHolder, int position, T data);

    /**
     * 获取每一条的视图
     *
     * @param type holder类型
     * @return 每一条的视图holder
     */
    protected abstract ViewHolder getItemViewHolder(ViewGroup viewGroup, int type);

    /**
     * 是否使用DataBinding
     */
    protected abstract boolean useBinding();

    /**
     * 加载跟多回调
     */
    public interface IOnLoadMore {
        void onLoadMore();
    }
}
