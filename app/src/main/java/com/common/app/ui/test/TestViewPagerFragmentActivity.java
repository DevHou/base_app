package com.common.app.ui.test;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.common.app.R;
import com.common.app.ui.AbsViewPagerFragment;
import com.common.app.ui.BaseActivity;

/**
 * Created by houlijiang on 16/3/29.
 * 
 * 测试AbsViewPagerFragment
 */
public class TestViewPagerFragmentActivity extends BaseActivity {

    @Override
    protected boolean bindContentView() {
        setContentView(R.layout.activity_test_view_pager_fragment);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment f = new ViewPagerFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.test_view_pager_fragment_fl, f)
            .commitAllowingStateLoss();
    }

    public static class ViewPagerFragment extends AbsViewPagerFragment {

        private String[] titles = new String[] { "TEST1", "TEST2", "TEST3" };
        private Fragment[] fragments;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            fragments = new Fragment[titles.length];
        }

        @Override
        protected int getCount() {
            return titles.length;
        }

        @Override
        protected Fragment getFragment(int position) {
            if (fragments[position] == null) {
                fragments[position] = new ItemFragment();
                Bundle args = new Bundle();
                args.putString("title", titles[position]);
                fragments[position].setArguments(args);
            }
            return fragments[position];
        }

        @Override
        protected CharSequence getFragmentTitle(int position) {
            return titles[position];
        }
    }

    public static class ItemFragment extends Fragment {
        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_test_not_support, container, false);
        }
    }
}
