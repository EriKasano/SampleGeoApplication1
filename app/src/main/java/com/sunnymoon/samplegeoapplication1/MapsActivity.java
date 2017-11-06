package com.sunnymoon.samplegeoapplication1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.os.Bundle;

import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.sunnymoon.samplegeoapplication1.fragments.ContentMapsFragment;
import com.sunnymoon.samplegeoapplication1.fragments.ContentSettingsFragment;

public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final private static String TAG = "MapsActivity";

    final public static String BUNDLE_LOCATION_IS_SET = "Bundle key has location is set";
    final public static String BUNDLE_LATITUDE = "Bundle key latitude";
    final public static String BUNDLE_LONGITUDE = "Bundle key longitude";

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        //Add a NavigationDrawer.
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        nav.setNavigationItemSelectedListener(this);

        toggle.syncState();
        onNavigationItemSelected(nav.getMenu().findItem(R.id.nav_maps));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.nav_maps: {
                final Intent data = getIntent();
                if(data.getBooleanExtra(BUNDLE_LOCATION_IS_SET, false)){
                    fragment = ContentMapsFragment.newInstance(
                            data.getDoubleExtra(BUNDLE_LATITUDE,0),
                            data.getDoubleExtra(BUNDLE_LONGITUDE,0)
                    );
                }else {
                    fragment = ContentMapsFragment.newInstance();
                }
                break;
            }
            case R.id.nav_setting: {
                fragment = ContentSettingsFragment.newInstance();
                break;
            }
        }

        //Replace the fragment
        if (fragment != null) {
            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, fragment);
            ft.commit();
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
