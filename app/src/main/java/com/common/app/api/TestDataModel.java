package com.common.app.api;

import com.common.app.base.model.ListDataModel;

/**
 * Created by houlijiang on 16/4/19.
 */
public class TestDataModel extends ListDataModel {

    public static final String CACHE_KEY = "test_data";

    public DataItem[] list;

    @Override
    public int getListCount() {
        return list == null ? 0 : list.length;
    }

    public static class DataItem {
        public String name;
    }
}
