package com.houlijiang.common.listview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewStub;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 包含的功能：下拉刷新，自动加载更多，右侧快速索引
 *
 * 实现方式：
 * > 使用RecyclerView实现，预定义了加载中 为空 错误 加载更多等view，可以通过attribute设置自己的
 * > 目前 加载中 加载更多 为空 都有默认值，可以不设置
 * > 如果不使用默认的layout而自己重写，注意里面定义的ViewStub id要和layout_abs_listview一致
 * 
 * 使用时的注意事项：
 * > 默认的空view是一个文本，可以通过setEmptyText接口设置需要显示的问题
 * > 如果需要快速检索功能需要通过setIndex设置数据源，当数据源变化时通过reloadSections重新构建检索
 * > 如果需要下拉刷新需要设置setRefreshListener，不设置则没有下拉刷新功能
 * > 因为是用的support包里的refreshLayout，可以通过setRefreshColor设置控件显示的颜色
 * > 如果需要自动加载下一页功能需要设置setOnLoadMoreListener，同时加载数据后记得调用stopLoadMore隐藏加载更多中的view
 * > 如果第一页加载数据错误后记得调用一下ShowErrorView来显示错误view，否则不会显示，第二页及以后根据具体业务自己做处理
 */
public class AbsListView extends RelativeLayout {

    private static final String TAG = AbsListView.class.getSimpleName();
    protected RecyclerView mRecycler;
    protected ViewStub mHeader;// 列表头
    protected ViewStub mProgress;// 初始加载中
    protected ViewStub mEmpty;// 无数据时页面
    protected ViewStub mError;// 错误时页面
    protected ViewStub mLoadMore;// 加载更多
    protected View mProgressView;
    protected View mHeaderView;
    protected View mEmptyView;
    protected View mErrorView;
    protected Sidebar mSideBar;
    protected View mLoadMoreView;

    protected boolean mClipToPadding;
    protected boolean mHeightWrapContent;
    protected int mPadding;
    protected int mPaddingTop;
    protected int mPaddingBottom;
    protected int mPaddingLeft;
    protected int mPaddingRight;
    protected int mScrollbarStyle;
    protected int mHeaderId;
    protected int mEmptyId;
    protected int mErrorId;
    protected int mMoreProgressId;

    protected boolean bEnableRefresh;
    protected boolean bEnableLoadMore;

    // view自己用的listener，会调用外部设置的listener
    protected RecyclerView.OnScrollListener mInternalOnScrollListener;
    // 外部设置的listener
    protected RecyclerView.OnScrollListener mExternalOnScrollListener;

    protected SwipeRefreshLayout mRefreshLayout;
    protected SwipeRefreshLayout.OnRefreshListener mOutRefreshListener;
    protected SwipeRefreshLayout.OnRefreshListener mInternalRefreshListener;

    protected int mSuperRecyclerViewMainLayout;
    private int mProgressId;

    private IOnLoadMore mIOnLoadMore;

    public AbsListView(Context context) {
        super(context);
        initView();
    }

    public AbsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initView();
    }

    public AbsListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initAttrs(attrs);
        initView();
    }

    protected void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.common_list_attrs);
        try {
            mSuperRecyclerViewMainLayout =
                a.getResourceId(R.styleable.common_list_attrs_common_list_main_layout_id,
                    R.layout.common_list_layout_main);
            mClipToPadding = a.getBoolean(R.styleable.common_list_attrs_common_list_clip_to_padding, false);
            mHeightWrapContent = a.getBoolean(R.styleable.common_list_attrs_common_list_height_wrap_content, false);
            mPadding = (int) a.getDimension(R.styleable.common_list_attrs_common_list_view_padding, -1.0f);
            mPaddingTop = (int) a.getDimension(R.styleable.common_list_attrs_common_list_view_padding_top, 0.0f);
            mPaddingBottom = (int) a.getDimension(R.styleable.common_list_attrs_common_list_view_padding_bottom, 0.0f);
            mPaddingLeft = (int) a.getDimension(R.styleable.common_list_attrs_common_list_view_padding_left, 0.0f);
            mPaddingRight = (int) a.getDimension(R.styleable.common_list_attrs_common_list_view_padding_right, 0.0f);
            mScrollbarStyle = a.getInt(R.styleable.common_list_attrs_common_list_view_scrollbar_style, -1);
            mHeaderId = a.getResourceId(R.styleable.common_list_attrs_common_list_layout_header, -1);
            mEmptyId =
                a.getResourceId(R.styleable.common_list_attrs_common_list_layout_empty,
                    R.layout.common_list_layout_empty);
            mErrorId =
                a.getResourceId(R.styleable.common_list_attrs_common_list_layout_error,
                    R.layout.common_list_layout_error);
            mMoreProgressId =
                a.getResourceId(R.styleable.common_list_attrs_common_list_layout_more_progress,
                    R.layout.common_list_layout_load_more);
            mProgressId =
                a.getResourceId(R.styleable.common_list_attrs_common_list_layout_progress,
                    R.layout.common_list_layout_loading);

            bEnableRefresh = a.getBoolean(R.styleable.common_list_attrs_common_list_enable_refresh, true);
            bEnableLoadMore = a.getBoolean(R.styleable.common_list_attrs_common_list_enable_load_more, true);

        } finally {
            a.recycle();
        }
    }

    private void initView() {
        setWillNotDraw(false);// 不加不调用draw
        if (isInEditMode()) {
            return;
        }
        View v = LayoutInflater.from(getContext()).inflate(mSuperRecyclerViewMainLayout, this);
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.common_list_abs_list_view_swipe_refresh);
        mRefreshLayout.setEnabled(false);

        mHeader = (ViewStub) v.findViewById(R.id.common_list_abs_list_view_header);
        if (mHeaderId > 0) {
            mHeader.setLayoutResource(mHeaderId);
            mHeaderView = mHeader.inflate();

            // final View fragmentView = findViewById(R.id.my_money_fl);
            // final ScrollableLayout scrollableLayout = (ScrollableLayout)
            // v.findViewById(R.id.gsx_list_abs_list_scroll);
            // scrollableLayout.setCanScrollVerticallyDelegate(new CanScrollVerticallyDelegate() {
            // @Override
            // public boolean canScrollVertically(int direction, int currentY) {
            // // >0向上拽，<0向下拽
            // return AbsListView.this.canScrollVertically(direction);
            // }
            //
            // @Override
            // public boolean isHeightEnough(int height) {
            // int listHeight = 0;
            // if (mRecycler != null) {
            // for (int i = 0; i < mRecycler.getChildCount(); i++) {
            // int childHeight = mRecycler.getChildAt(i).getHeight();
            // listHeight += childHeight;
            // if (listHeight > height) {
            // return false;
            // }
            // }
            // }
            // return true;
            // }
            //
            // });

        }

        mLoadMore = (ViewStub) v.findViewById(R.id.common_list_abs_list_view_load_more);
        mLoadMore.setLayoutResource(mMoreProgressId);
        mLoadMoreView = mLoadMore.inflate();
        mLoadMoreView.setVisibility(View.GONE);

        mProgress = (ViewStub) v.findViewById(R.id.common_list_abs_list_view_progress);
        mProgress.setLayoutResource(mProgressId);
        mProgressView = mProgress.inflate();

        mError = (ViewStub) v.findViewById(R.id.common_list_abs_list_view_error);
        mError.setLayoutResource(mErrorId);
        if (mErrorId != 0) {
            mErrorView = mError.inflate();
        }
        mError.setVisibility(View.GONE);

        mEmpty = (ViewStub) v.findViewById(R.id.common_list_abs_list_view_empty);
        mEmpty.setLayoutResource(mEmptyId);
        if (mEmptyId != 0) {
            mEmptyView = mEmpty.inflate();
        }
        mEmpty.setVisibility(View.GONE);
        mSideBar = (Sidebar) v.findViewById(R.id.common_list_abs_sidebar);
        TextView header = (TextView) v.findViewById(R.id.common_list_abs_floating_header);
        if (mSideBar != null && header != null) {
            mSideBar.setHeader(header);
        }

        initRecyclerView(v);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mHeightWrapContent) {
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, expandSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public boolean canScrollVertically(int direction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return mRecycler.canScrollVertically(direction);
        } else {
            final int offset = computeVerticalScrollOffset();
            final int range = computeVerticalScrollRange() - computeVerticalScrollExtent();
            if (range == 0)
                return false;
            if (direction < 0) {
                return offset > 0;
            } else {
                return offset < (range - 1);
            }
        }

    }

    public enum LAYOUT_MANAGER_TYPE {
        LINEAR, GRID, STAGGERED_GRID
    }

    /**
     * 初始化RecyclerView
     */
    protected void initRecyclerView(View view) {
        View recyclerView = view.findViewById(R.id.common_list_abs_list_view_lv);

        if (recyclerView instanceof RecyclerView)
            mRecycler = (RecyclerView) recyclerView;
        else
            throw new IllegalArgumentException("must has a RecyclerView with id list!");

        mRecycler.setClipToPadding(mClipToPadding);
        mInternalOnScrollListener = new RecyclerView.OnScrollListener() {

            /**
             * layoutManager的类型（枚举）
             */
            protected LAYOUT_MANAGER_TYPE layoutManagerType;

            /**
             * 最后一个的位置
             */
            private int[] lastPositions;

            /**
             * 最后一个可见的item的位置
             */
            private int lastVisibleItemPosition;

            /**
             * 当前滑动的状态
             */
            private int currentScrollState = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mExternalOnScrollListener != null)
                    mExternalOnScrollListener.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                // int lastVisibleItemPosition = -1;
                if (layoutManagerType == null) {
                    if (layoutManager instanceof LinearLayoutManager) {
                        layoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR;
                    } else if (layoutManager instanceof GridLayoutManager) {
                        layoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
                    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                        layoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID;
                    } else {
                        throw new RuntimeException(
                            "Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
                    }
                }

                switch (layoutManagerType) {
                    case LINEAR:
                        lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                        break;
                    case GRID:
                        lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                        break;
                    case STAGGERED_GRID:
                        StaggeredGridLayoutManager staggeredGridLayoutManager =
                            (StaggeredGridLayoutManager) layoutManager;
                        if (lastPositions == null) {
                            lastPositions = new int[staggeredGridLayoutManager.getSpanCount()];
                        }
                        staggeredGridLayoutManager.findLastVisibleItemPositions(lastPositions);
                        lastVisibleItemPosition = findMax(lastPositions);
                        break;
                }

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mExternalOnScrollListener != null)
                    mExternalOnScrollListener.onScrollStateChanged(recyclerView, newState);

                currentScrollState = newState;
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                if (bEnableLoadMore
                    && (visibleItemCount > 0 && currentScrollState == RecyclerView.SCROLL_STATE_IDLE && (lastVisibleItemPosition) >= totalItemCount - 1)) {
                    if (mIOnLoadMore != null) {
                        // Log.v(TAG, "is loading more");
                        showLoadMore(true);
                        mIOnLoadMore.onLoadMore();
                    }
                }
            }

            private int findMax(int[] lastPositions) {
                int max = lastPositions[0];
                for (int value : lastPositions) {
                    if (value > max) {
                        max = value;
                    }
                }
                return max;
            }
        };
        mRecycler.setOnScrollListener(mInternalOnScrollListener);

        if (mPadding != -1.0f) {
            mRecycler.setPadding(mPadding, mPadding, mPadding, mPadding);
        } else {
            mRecycler.setPadding(mPaddingLeft, mPaddingTop, mPaddingRight, mPaddingBottom);
        }

        if (mScrollbarStyle != -1) {
            mRecycler.setScrollBarStyle(mScrollbarStyle);
        }

        mRecycler.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (((IAbsListDataAdapter) mRecycler.getAdapter()).isReloading()) {
                    // 如果是正在刷新则不让滑动，以免出现IndexOutOfBound
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    /**
     * 设置layout manager
     */
    public void setLayoutManager(RecyclerView.LayoutManager manager) {
        mRecycler.setLayoutManager(manager);
    }

    /**
     * 设置adapter
     * 先显示progress view，通过adapter add数据后在更新显示
     */
    public void setAdapter(final RecyclerView.Adapter adapter) {
        mRecycler.setAdapter(adapter);
        mProgress.setVisibility(View.VISIBLE);
        mEmpty.setVisibility(View.GONE);
        mRecycler.setVisibility(View.VISIBLE);
        mRefreshLayout.setRefreshing(false);
        // 有数据变化时会重置刷新显示状态
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                update();
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (positionStart == 0) {
                    try {
                        mRecycler.getLayoutManager().scrollToPosition(0);
                    } catch (Exception e) {
                        // 可能有些不支持这个方法
                    }
                }
                update();
                showLoadMore(false);
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                update();
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                update();
            }

            @Override
            public void onChanged() {
                super.onChanged();
                update();
                showLoadMore(false);
            }

            private void update() {
                mProgress.setVisibility(View.GONE);
                mRefreshLayout.setRefreshing(false);
                if (adapter instanceof IAbsListDataAdapter) {
                    // 调用自定义判断是否是空的列表，可能有些特殊处理，比如第一个元素是个搜索框等
                    if (((IAbsListDataAdapter) adapter).isReloading()) {
                        mProgress.setVisibility(View.VISIBLE);
                        mEmpty.setVisibility(View.GONE);
                        mRecycler.setVisibility(View.GONE);
                        mError.setVisibility(View.GONE);
                    } else if (((IAbsListDataAdapter) adapter).isEmpty()) {
                        mEmpty.setVisibility(View.VISIBLE);
                        mRecycler.setVisibility(View.GONE);
                        mError.setVisibility(View.GONE);
                    } else {
                        mEmpty.setVisibility(View.GONE);
                        mRecycler.setVisibility(View.VISIBLE);
                        mError.setVisibility(View.GONE);
                    }
                } else {
                    if (getChildCount() == 0) {
                        mEmpty.setVisibility(View.VISIBLE);
                        mRecycler.setVisibility(View.GONE);
                        mError.setVisibility(View.GONE);
                    } else {
                        mEmpty.setVisibility(View.GONE);
                        mRecycler.setVisibility(View.VISIBLE);
                        mError.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    /**
     * 设置快速检索
     */
    public void setIndex(MySectionIndexer indexer) {
        if (mSideBar != null && indexer != null) {
            mSideBar.setVisibility(View.VISIBLE);
            mSideBar.setIndexer(indexer);
        }
    }

    /**
     * 重新加载检索索引
     */
    public void reloadSections() {
        if (mSideBar != null) {
            mSideBar.reloadSections();
        }
    }

    /**
     * 移除adapter
     */
    public void clear() {
        mRecycler.setAdapter(null);
    }

    /**
     * 设置下拉刷新回调
     */
    public void setRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mInternalRefreshListener = new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 如果非list显示则不支持下拉刷新
                if (mProgress.getVisibility() == View.VISIBLE || mEmpty.getVisibility() == View.VISIBLE
                    || mError.getVisibility() == View.VISIBLE) {
                    return;
                }

                // 如果不是在列表页则不处理下拉刷新
                if (mRecycler.getVisibility() != View.VISIBLE) {
                    return;
                }
                if (mOutRefreshListener != null) {
                    mOutRefreshListener.onRefresh();
                }
            }
        };
        mOutRefreshListener = listener;
        mRefreshLayout.setEnabled(bEnableRefresh);
        mRefreshLayout.setOnRefreshListener(mInternalRefreshListener);
        setRefreshingColorResources(R.color.common_list_blue, R.color.common_list_green, R.color.common_list_orange,
            R.color.common_list_red);
    }

    /**
     * 设置下拉刷新控件的颜色集
     */
    public void setRefreshingColorResources(@ColorRes int colRes1, @ColorRes int colRes2, @ColorRes int colRes3,
        @ColorRes int colRes4) {
        mRefreshLayout.setColorSchemeResources(colRes1, colRes2, colRes3, colRes4);
    }

    /**
     * 设置下拉刷新控件的颜色集
     */
    public void setRefreshingColor(int col1, int col2, int col3, int col4) {
        mRefreshLayout.setColorSchemeColors(col1, col2, col3, col4);
    }

    /**
     * 设置滚动监听器
     */
    public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
        mExternalOnScrollListener = listener;
    }

    /**
     * 设置item的touch listener，不过一般不用这个，自己实现onclick
     */
    public void addOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecycler.addOnItemTouchListener(listener);
    }

    /**
     * 移除item的touch listener
     */
    public void removeOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecycler.removeOnItemTouchListener(listener);
    }

    public RecyclerView getRecyclerView() {
        return mRecycler;
    }

    /**
     * 获取Adapter
     */
    public RecyclerView.Adapter getAdapter() {
        return mRecycler.getAdapter();
    }

    public void setOnTouchListener(OnTouchListener listener) {
        mRecycler.setOnTouchListener(listener);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        mRecycler.addItemDecoration(itemDecoration);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration, int index) {
        mRecycler.addItemDecoration(itemDecoration, index);
    }

    public void removeItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        mRecycler.removeItemDecoration(itemDecoration);
    }

    /**
     * 获取加载中view
     */
    public View getProgressView() {
        return mProgressView;
    }

    /**
     * 获取空view
     */
    public View getHeaderView() {
        return mHeaderView;
    }

    /**
     * 获取空view
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * 获取发生错误view
     */
    public View getErrorView() {
        return mErrorView;
    }

    /**
     * 重新加载数据，把空和加载中隐藏，下拉刷新时用
     */
    private void hideEmptyAndProgressView() {
        mEmpty.setVisibility(View.GONE);
        mProgress.setVisibility(View.GONE);
    }

    /**
     * 显示错误页面，加载数据发生错误时用
     */
    public void showErrorView() {
        hideEmptyAndProgressView();
        mRecycler.setVisibility(View.GONE);
        mError.setVisibility(View.VISIBLE);
    }

    /**
     * 加载更多完成后主动调用这个方法，如果有数据加载即调用了add相关方法则不用主动调用这个方法
     */
    public void stopLoadMore() {
        showLoadMore(false);
    }

    public void stopRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    public void setEnableRefresh(boolean enable) {
        this.bEnableRefresh = enable;
        mRefreshLayout.setEnabled(enable);
    }

    public void setEnableLoadMore(boolean enable) {
        this.bEnableLoadMore = enable;
        mLoadMore.setEnabled(enable);
        mLoadMore.setVisibility(enable ? VISIBLE : GONE);
    }

    /**
     * 显示或隐藏加载更多view
     *
     * @param show 是否显示
     */
    private void showLoadMore(boolean show) {
        // Log.v(TAG, "show load more " + show);
        if (show) {
            mLoadMoreView.setVisibility(View.VISIBLE);
        } else {
            mLoadMoreView.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置加载更多回调，当需要分页时使用，不设置的话列表最后不会显示加载更多
     */
    public void setOnLoadMoreListener(IOnLoadMore l) {
        mIOnLoadMore = l;
    }

    public interface IOnLoadMore {
        void onLoadMore();
    }

}
