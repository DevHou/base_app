package com.common.listview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
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

import com.common.listview.ptr.PtrFrameLayout;
import com.common.listview.ptr.PtrHandler;
import com.common.listview.ptr.PtrUIHandler;
import com.common.listview.ptr.indicator.PtrIndicator;
import com.common.utils.AppLog;

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
public class AbsListView extends RelativeLayout implements AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = AbsListView.class.getSimpleName();
    protected RecyclerView mRecycler;
    protected ViewStub mHeader;// 列表头
    protected ViewStub mProgress;// 初始加载中
    protected ViewStub mEmpty;// 无数据时页面
    protected ViewStub mError;// 错误时页面
    protected View mProgressView;
    protected View mHeaderView;
    protected View mEmptyView;
    protected View mErrorView;
    protected Sidebar mSideBar;

    protected boolean mClipToPadding;
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

    protected PtrFrameLayout mRefreshLayout;
    protected IOnPullToRefresh mOutRefreshListener;

    // 记录header view的偏移，在判断能否下拉刷新时用到
    private AppBarLayout mAppBarLayout;
    private int mAppBarOffset;

    protected int mSuperRecyclerViewMainLayout;
    private int mProgressId;

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
        mRefreshLayout = (PtrFrameLayout) v.findViewById(R.id.common_list_abs_list_view_swipe_refresh);
        mAppBarLayout = (AppBarLayout) v.findViewById(R.id.common_list_abs_list_view_app_bar);

        mHeader = (ViewStub) v.findViewById(R.id.common_list_abs_list_view_header);
        if (mHeaderId > 0) {
            mHeader.setLayoutResource(mHeaderId);
            mHeaderView = mHeader.inflate();
        }

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

        mAppBarLayout.addOnOffsetChangedListener(this);
        initRecyclerView(v);
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

    /**
     * AppBar的回调
     */
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        AppLog.d(TAG, "AppBarLayout offset:" + verticalOffset);
        mAppBarOffset = verticalOffset;
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
        if (adapter instanceof IAbsListDataAdapter) {
            ((IAbsListDataAdapter) adapter).setLoadMoreView(mMoreProgressId);
        }
        // mRefreshLayout.refreshComplete();
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
            }

            private void update() {
                mProgress.setVisibility(View.GONE);
                // mRefreshLayout.refreshComplete();
                if (adapter instanceof IAbsListDataAdapter) {
                    // 调用自定义判断是否是空的列表，可能有些特殊处理，比如第一个元素是个搜索框等
                    if (((IAbsListDataAdapter) adapter).isReloading()) {
                        if (((IAbsListDataAdapter) adapter).isEmpty()) {
                            // 为空认为是第一次加载，显示加载中view
                            mProgress.setVisibility(View.VISIBLE);
                            mRefreshLayout.setVisibility(View.GONE);
                        } else {
                            // 否则认为是下拉刷新，不显示加载中view
                            mProgress.setVisibility(View.GONE);
                            mRefreshLayout.setVisibility(View.VISIBLE);
                        }
                        mEmpty.setVisibility(View.GONE);
                        mError.setVisibility(View.GONE);
                    } else if (((IAbsListDataAdapter) adapter).isEmpty()) {
                        mEmpty.setVisibility(View.VISIBLE);
                        mRefreshLayout.setVisibility(View.GONE);
                        mError.setVisibility(View.GONE);
                    } else {
                        mEmpty.setVisibility(View.GONE);
                        mRefreshLayout.setVisibility(View.VISIBLE);
                        mError.setVisibility(View.GONE);
                    }
                } else {
                    if (getChildCount() == 0) {
                        mEmpty.setVisibility(View.VISIBLE);
                        mRefreshLayout.setVisibility(View.GONE);
                        mError.setVisibility(View.GONE);
                    } else {
                        mEmpty.setVisibility(View.GONE);
                        mRefreshLayout.setVisibility(View.VISIBLE);
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
    public void setRefreshListener(IOnPullToRefresh listener) {
        mOutRefreshListener = listener;
        mRefreshLayout.setPtrHandler(new PtrHandler() {
            @Override
            public boolean checkCanDoRefresh(PtrFrameLayout frame, View content, View header) {
                if (!bEnableRefresh) {
                    return false;
                }
                if (mAppBarOffset != 0) {
                    return false;
                }
                AppLog.d(TAG, "empty:" + (mEmpty.getVisibility() == View.VISIBLE) + " error:"
                    + (mError.getVisibility() == View.VISIBLE));
                if (mEmpty.getVisibility() == View.VISIBLE || mError.getVisibility() == View.VISIBLE) {
                    return false;
                }
                if (mRecycler.getChildCount() == 0) {
                    return true;
                }
                int top = mRecycler.getChildAt(0).getTop();
                if (top < 0) {
                    // 这里不能用!=0判断，因为子view可能有marginTop，这样其显示在第一个时top永远>0
                    // 所以用<0判断第一个view是否显示
                    return false;
                }
                final RecyclerView recyclerView = mRecycler;
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
                // todo 支持更多LayoutManager的判断
                return false;
            }

            @Override
            public void onRefreshBegin(PtrFrameLayout frame) {
                AppLog.d(TAG, "onRefreshBegin");
                mOutRefreshListener.onRefreshBegin();
            }
        });
    }

    public void setRefreshHeaderView(View view, final IPtrHeaderUI handler) {
        if (handler != null) {
            mRefreshLayout.addPtrUIHandler(new PtrUIHandler() {
                @Override
                public void onUIReset(PtrFrameLayout frame) {
                    // AppLog.d(TAG, "onUIReset");
                    handler.onUIReset();
                }

                @Override
                public void onUIRefreshPrepare(PtrFrameLayout frame) {
                    // AppLog.d(TAG, "onUIRefreshPrepare");
                    handler.onUIRefreshPrepare();
                }

                @Override
                public void onUIRefreshBegin(PtrFrameLayout frame) {
                    // AppLog.d(TAG, "onUIRefreshBegin");
                    handler.onUIRefreshBegin();
                }

                @Override
                public void onUIRefreshComplete(PtrFrameLayout frame) {
                    // AppLog.d(TAG, "onUIRefreshComplete");
                    handler.onUIRefreshComplete();
                }

                @Override
                public void onUIPositionChange(PtrFrameLayout frame, boolean isUnderTouch, byte status,
                    PtrIndicator ptrIndicator) {
                    AppLog.d(TAG, "onUIPositionChange");
                    PtrStatus ptrStatus;
                    switch (status) {
                        case PtrFrameLayout.PTR_STATUS_INIT:
                            ptrStatus = PtrStatus.INIT;
                            break;
                        case PtrFrameLayout.PTR_STATUS_PREPARE:
                            ptrStatus = PtrStatus.PREPARE;
                            break;
                        case PtrFrameLayout.PTR_STATUS_LOADING:
                            ptrStatus = PtrStatus.LOADING;
                            break;
                        case PtrFrameLayout.PTR_STATUS_COMPLETE:
                        default:
                            ptrStatus = PtrStatus.COMPLETE;
                            break;
                    }
                    handler.onUIPositionChange(isUnderTouch, ptrStatus, frame.getOffsetToRefresh(),
                        ptrIndicator.getCurrentPosY(), ptrIndicator.getLastPosY());
                }
            });
        }
        mRefreshLayout.setHeaderView(view);
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
        mRefreshLayout.setVisibility(View.GONE);
        mError.setVisibility(View.VISIBLE);
    }

    /**
     * 把空和错误隐藏
     */
    private void hideEmptyAndErrorView() {
        mEmpty.setVisibility(View.GONE);
        mError.setVisibility(View.GONE);
    }

    /**
     * 显示加载中页面
     */
    public void showLoadingView() {
        hideEmptyAndErrorView();
        mRefreshLayout.setVisibility(View.GONE);
        mProgress.setVisibility(View.VISIBLE);
    }

    public void stopRefresh() {
        mRefreshLayout.refreshComplete();
    }

    public void setEnableRefresh(boolean enable) {
        this.bEnableRefresh = enable;
    }

    /**
     * 下拉刷新回调
     */
    public interface IOnPullToRefresh {
        void onRefreshBegin();
    }

    /**
     * 下拉刷新UI的回调
     */
    public interface IPtrHeaderUI {

        /**
         * Content重新回到顶部，Header消失，整个下拉刷新过程完全结束以后
         */
        void onUIReset();

        /**
         * 准备刷新，Header将要出现时调用
         */
        void onUIRefreshPrepare();

        /**
         * 开始刷新，即调用数据刷新之前
         */
        void onUIRefreshBegin();

        /**
         * 刷新结束，Header 开始向上移动之前调用，即调用complete之后
         */
        void onUIRefreshComplete();

        /**
         * 下拉过程中位置变化回调
         * 
         * @param isUnderTouch 是否是按下
         * @param status 状态
         * @param offset 设定的下拉刷新高度
         * @param current 当前纵坐标
         * @param last 上次的纵坐标
         */
        void onUIPositionChange(boolean isUnderTouch, PtrStatus status, float offset, float current, float last);
    }

    public enum PtrStatus {
        INIT(1), // 初始化
        PREPARE(2), // 将要显示header
        LOADING(3), // 刷新中
        COMPLETE(4);// 刷新完成

        private int value;

        public int getValue() {
            return this.value;
        }

        PtrStatus(int value) {
            this.value = value;
        }

        public static PtrStatus valueOf(int value) {
            switch (value) {
                case 1:
                    return INIT;
                case 2:
                    return PREPARE;
                case 3:
                    return LOADING;
                case 4:
                default:
                    return COMPLETE;
            }
        }
    }

}
