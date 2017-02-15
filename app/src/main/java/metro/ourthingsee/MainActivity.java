package metro.ourthingsee;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import metro.ourthingsee.adapters.OptionsAdapter;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<ThingSee> {
    SharedPreferences prefs;

    /** Tag for the log messages */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    private RecyclerView recv_options;
    private OptionsAdapter optionsAdapter;
    private NavigationView nav_view;
    private DrawerLayout drawer;
    private Toolbar tb_main;
    private View navHeader;
    private TextView tv_name;
    private ImageView imgv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
        // If user did not login before, open login activity first
        if (prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "").isEmpty()) {
            Intent intent = new Intent (this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.e(LOG_TAG, prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME,""));
            Log.e(LOG_TAG, prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME,""));
        }
        //Set up toolbar
        tb_main = (Toolbar) findViewById(R.id.tb_main);
        setSupportActionBar(tb_main);
        //Set up recycler view for user's options
        recv_options = (RecyclerView) findViewById(R.id.recv_options);
        optionsAdapter = new OptionsAdapter(this);
        recv_options.setAdapter(optionsAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recv_options.setLayoutManager(linearLayoutManager);
        //Set up navigation bar
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nav_view = (NavigationView) findViewById(R.id.nav_view);
        setUpNavigationView();
        // Navigation view header
        navHeader = nav_view.getHeaderView(0);
        tv_name = (TextView) navHeader.findViewById(R.id.tv_name);
        imgv = (ImageView) navHeader.findViewById(R.id.imgv);
        Glide.with(this)
                .load(R.drawable.navheader)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .animate(android.R.anim.fade_in)
                .approximate()
                .into(imgv);
    }

    @Override
    public Loader<ThingSee> onCreateLoader(int id, Bundle args) {
        return null;

//        // Define a projection that specifies the columns from the table we care about.
//        String[] projection = {
//                TCEntry._ID,
//                TCEntry.COLUMN_TC_TIMESTAMP,
//                TCEntry.COLUMN_TC_SENSOR_ID,
//                TCEntry.COLUMN_TC_SENSOR_VALUE
//        };
//
//        // This loader will execute the ContentProvider's query method on a background thread
//        return new CursorLoader(this,           // Parent activity context
//                TCEntry.CONTENT_URI,            // Provider content URI to query
//                projection,                     // Columns to include in the resulting Cursor
//                null,                           // No selection clause
//                null,                           // No selection arguments
//                null);                          // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<ThingSee> loader, ThingSee data) {
        // Update {@link InvCursorAdapter} with this new cursor containing updated inv data
        //mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<ThingSee> loader) {
    }
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    case R.id.log_out:
                        delLoginData();
                        break;
                    case R.id.about_us:
                        break;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);
                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, tb_main, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.addDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }
    public void delLoginData() {
        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
        prefs.edit().putString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "").apply();
        prefs.edit().putString(OurContract.PREF_AUTH_EMAIL,"").apply();
        prefs.edit().putString(OurContract.PREF_AUTH_PASSWORD,"").apply();
        prefs.edit().putString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "").apply();
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
