package com.common.listview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by houlijiang on 15/11/23.
 * 
 * 对AbsListDataAdapter的简单封装，简化子类写法
 * 每种类型数据对应一个cell
 */
public class BaseListDataAdapter<T> extends AbsListDataAdapter<T> {

    protected static final int DEFAULT_CELL_TYPE = 0;
    private Class<? extends BaseListCell<T>> mDefaultCellClass;

    public BaseListDataAdapter() {
        super();
    }

    /**
     * 如果列表只有一种cell，则构造时直接指定，否则需要重载bindCellType
     * 
     * @param defaultClass 默认cell
     */
    public BaseListDataAdapter(Class<? extends BaseListCell<T>> defaultClass) {
        super();
        mDefaultCellClass = defaultClass;
    }

    @Override
    protected void setData(ViewHolder viewHolder, int position, T data) {
        BaseViewHolder holder = (BaseViewHolder) viewHolder;
        holder.listCell.setData(data, position);
    }

    @Override
    public int getItemViewType(int position) {
        return DEFAULT_CELL_TYPE;
    }

    /**
     * 如果有多中类型则子类需要重载这个方法来绑定view类型，同时返回类型
     * 
     * @return cell实例
     */
    protected BaseListCell<T> createCell(int type) {
        // 根据类型查找对应cell并创建cell实例
        try {
            if (type == DEFAULT_CELL_TYPE && mDefaultCellClass != null) {
                return mDefaultCellClass.newInstance();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected BaseViewHolder getItemViewHolder(ViewGroup viewGroup, int type) {
        BaseViewHolder holder = null;
        try {
            BaseListCell<T> listCell = createCell(type);
            int layoutId = listCell.getCellResource();

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            if (view == null) {
                return null;
            }
            listCell.initialChildViews(view);
            holder = new BaseViewHolder(view, listCell);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return holder;
    }

    protected class BaseViewHolder extends AbsListDataAdapter.ViewHolder {

        protected BaseListCell<T> listCell;

        protected BaseViewHolder(View itemView, BaseListCell<T> listCell) {
            super(itemView);
            this.listCell = listCell;
        }
    }
}
