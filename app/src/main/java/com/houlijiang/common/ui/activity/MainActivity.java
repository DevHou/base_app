package com.houlijiang.common.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.houlijiang.common.R;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener,
    View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null)
                    .show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle =
            new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        findViewById(R.id.main_btn_test_net).setOnClickListener(this);
        findViewById(R.id.main_btn_test_cache).setOnClickListener(this);
        findViewById(R.id.main_btn_test_image_loader).setOnClickListener(this);
        findViewById(R.id.main_btn_test_image_browser).setOnClickListener(this);
        findViewById(R.id.main_btn_test_listview).setOnClickListener(this);
        findViewById(R.id.main_btn_test_mp3rec).setOnClickListener(this);
        findViewById(R.id.main_btn_test_ptr).setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        // noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            LoginActivity.launch(this);
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_btn_test_net: {
                Intent i = new Intent(this, TestNetActivity.class);
                startActivity(i);
                break;
            }
            case R.id.main_btn_test_cache: {
                Intent i = new Intent(this, TestCacheActivity.class);
                startActivity(i);
                break;
            }
            case R.id.main_btn_test_image_loader: {
                Intent i = new Intent(this, TestImageLoaderActivity.class);
                startActivity(i);
                break;
            }
            case R.id.main_btn_test_image_browser: {
                Intent i = new Intent(this, TestImageBrowserActivity.class);
                startActivity(i);
                break;
            }
            case R.id.main_btn_test_listview: {
                Intent i = new Intent(this, TestListViewActivity.class);
                startActivity(i);
                break;
            }
            case R.id.main_btn_test_mp3rec: {
                Intent i = new Intent(this, TestMp3RecActivity.class);
                startActivity(i);
                break;
            }
            case R.id.main_btn_test_ptr: {
                Intent i = new Intent(this, TestPtrActivity.class);
                startActivity(i);
                break;
            }
        }
    }
}
