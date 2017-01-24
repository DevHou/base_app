package com.common.listview;

import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.annotations.Nullable;
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

    private static final int TYPE_LOAD_MORE = Integer.MAX_VALUE;
    private static final int TYPE_EMPTY_FOOTER = Integer.MAX_VALUE - 1;

    private List<T> mData;
    private boolean mIsReloading = false;// 是否是正在从数据中心加载数据
    private boolean mHasMore = true;
    private int mLoadMoreId = 0;// 加载更多view ID
    private int mLastLoadMorePosition = -1;// 上次加载更多时的位置，用于判断避免重复调用load more
    private IOnLoadMore mIOnLoadMore;

    private static final Object DB_PAYLOAD = new Object();
    @Nullable
    private RecyclerView mRecyclerView;

    /**
     * This is used to block items from updating themselves. RecyclerView wants to know when an
     * item is invalidated and it prefers to refresh it via onRebind. It also helps with performance
     * since data binding will not update views that are not changed.
     */
    private final OnRebindCallback mOnRebindCallback = new OnRebindCallback() {
        @Override
        public boolean onPreBind(ViewDataBinding binding) {
            AppLog.d(TAG, "onPreBind");
            if (mRecyclerView == null || mRecyclerView.isComputingLayout()) {
                return true;
            }
            int childAdapterPosition = mRecyclerView.getChildAdapterPosition(binding.getRoot());
            if (childAdapterPosition == RecyclerView.NO_POSITION) {
                return true;
            }
            notifyItemChanged(childAdapterPosition, DB_PAYLOAD);
            return false;
        }
    };

    public AbsListDataAdapter() {
        this(null);
    }

    public AbsListDataAdapter(IOnLoadMore loadMore) {
        mData = new ArrayList<>();
        mIOnLoadMore = loadMore;
    }

    public void addToFront(final T[] data) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
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
            }
        });
    }

    public void addAll(final T[] data) {
        addAll(data, false);
    }

    public void addAll(final T[] data, final boolean removeOld) {
        if (data == null || data.length == 0) {
            AppLog.d(TAG, "add all null");
            if (removeOld) {
                clear();
            }
        } else {
            if (removeOld) {
                mLastLoadMorePosition = -1;
            }
            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    AppLog.d(TAG, "add all " + data.length);
                    if (mData == null) {
                        mData = new ArrayList<>();
                    } else if (removeOld) {
                        mData.clear();
                    }
                    int oldPosition = mData.size();
                    if (data.length > 0) {
                        Collections.addAll(mData, data);
                        mIsReloading = false;
                        if (oldPosition > 0) {
                            notifyItemRangeInserted(oldPosition, data.length);
                            // 通知一下最后一个更新，用来解决当一页不满时最后一个loadmore不调用bindView的bug
                            if (mHasMore) {
                                notifyItemChanged(mData.size(), DB_PAYLOAD);
                            }
                        } else {
                            notifyDataSetChanged();
                        }
                    } else {
                        mIsReloading = false;
                        if (removeOld) {
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }
    }

    public void add(final T obj) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (mData == null) {
                    AppLog.e(TAG, "add but data list is null");
                    mIsReloading = false;
                    return;
                }
                mData.add(obj);
                mIsReloading = false;
                notifyItemInserted(mData.size() - 1);
                // 通知一下最后一个更新，用来解决当一页不满时最后一个loadmore不调用bindView的bug
                if (mHasMore) {
                    notifyItemChanged(mData.size(), DB_PAYLOAD);
                }
            }
        });
    }

    public void insert(final T obj, final int position) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (mData == null || position < 0) {
                    AppLog.e(TAG, "insert position invalid " + position);
                    mIsReloading = false;
                    return;
                }
                mData.add(position, obj);
                mIsReloading = false;
                notifyItemInserted(position);
            }
        });
    }

    public void replace(final T obj, final int position) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (mData == null || position < 0 || position >= getDataSize()) {
                    AppLog.e(TAG, "replace position invalid " + position);
                    mIsReloading = false;
                    return;
                }
                mData.set(position, obj);
                mIsReloading = false;
                notifyItemChanged(position);
            }
        });
    }

    public void remove(final int position) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (mData == null || position < 0 || position >= getDataSize()) {
                    AppLog.e(TAG, "remove position invalid " + position);
                    mIsReloading = false;
                    return;
                }
                mData.remove(position);
                mIsReloading = false;
                notifyItemRemoved(position);
            }
        });
    }

    public void exchange(final int i, final int j) {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                int len = mData == null ? 0 : mData.size();
                if (i < 0 || i >= len || j < 0 || j >= len || i == j) {
                    mIsReloading = false;
                    return;
                }
                T obj = mData.get(i);
                mIsReloading = false;
                mData.set(i, mData.get(j));
                notifyItemChanged(i);
                mData.set(j, obj);
                notifyItemChanged(j);
            }
        });
    }

    public void refreshList() {
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                mIsReloading = false;
                // 通过假的数据变化通知，来更新列表显示，隐藏加载中等进度
                notifyDataSetChanged();
            }
        });
    }

    public void reloadFooterView() {
        if (mData != null) {
            notifyItemChanged(mData.size());
        }
    }

    /**
     * 清空数据并通知数据变化
     */
    public void clear() {
        mLastLoadMorePosition = -1;
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                if (mData == null) {
                    mIsReloading = false;
                    return;
                }
                mData.clear();
                mIsReloading = false;
                notifyDataSetChanged();
            }
        });
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
        if (mData == null || position >= getDataSize()) {
            return null;
        }
        return mData.get(position);
    }

    @Override
    final public int getItemViewType(int position) {
        if (position == mData.size()) {
            // AppLog.v(TAG, "get view type for last " + position + ", has more:" + mHasMore);
            // 当没有数据时不显示加载更多的view，避免第一页加载中状态时显示load more view
            if (!mHasMore || getDataSize() == 0) {
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
        ViewHolder vh;
        if (viewType == TYPE_LOAD_MORE) {
            View v;
            // AppLog.v(TAG, "create view holder for load more");
            if (mLoadMoreId != 0) {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(mLoadMoreId, viewGroup, false);
            } else {
                v =
                    LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_list_layout_load_more,
                        viewGroup, false);
            }
            vh = new LoadMoreViewHolder(v, useBinding());
        } else if (viewType == TYPE_EMPTY_FOOTER) {
            View v =
                LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.common_list_layout_empty_footer,
                    viewGroup, false);
            // AppLog.v(TAG, "create view holder for empty");
            vh = new EmptyViewHolder(v, useBinding());
        } else {
            // AppLog.v(TAG, "create view holder for normal " + viewType);
            vh = getItemViewHolder(viewGroup, viewType);
        }
        if (useBinding()) {
            vh.binding.addOnRebindCallback(mOnRebindCallback);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position, List<Object> payloads) {
        int pos = position; // viewHolder.getAdapterPosition();
        // AppLog.v(TAG, "bind view holder for " + pos);
        if (viewHolder instanceof LoadMoreViewHolder) {
            AppLog.v(TAG, "bind view holder for load more pos:" + pos);
            if (isReloading()) {
                AppLog.v(TAG, "reloading will not call load more pos:" + pos);
            } else if (pos != mLastLoadMorePosition) {
                AppLog.v(TAG, "bind view holder for load more pos:" + pos);
                mRecyclerView.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mHasMore && mIOnLoadMore != null) {
                            mIOnLoadMore.onLoadMore();
                        }
                    }
                });
                mLastLoadMorePosition = pos;
            } else {
                AppLog.v(TAG, "bind view holder for load more at same position " + pos);
            }
            return;
        } else if (viewHolder instanceof EmptyViewHolder) {
            AppLog.v(TAG, "bind view holder for empty");
            return;
        }
        if (payloads.isEmpty() || hasNonDataBindingInvalidate(payloads)) {
            AppLog.v(TAG, "will call bindData");
            bindData(viewHolder, pos, getData(pos));
        }
        if (useBinding()) {
            viewHolder.getBinding().executePendingBindings();
        }
    }

    private boolean hasNonDataBindingInvalidate(List<Object> payloads) {
        for (Object payload : payloads) {
            if (payload != DB_PAYLOAD) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final void onBindViewHolder(ViewHolder holder, int position) {
        throw new IllegalArgumentException("just overridden to make final.");
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
        // 只要有数据永远都返回多一个，通过显示不同的footer view控制显示
        // AppLog.v(TAG, "get count for " + mRecyclerView.hashCode() + ", size:" + (mData == null ? 1 : mData.size() +
        // 1));
        return (mData == null) ? 0 : mData.size() + 1;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = null;
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
