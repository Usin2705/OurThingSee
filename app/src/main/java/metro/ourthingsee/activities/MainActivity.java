package metro.ourthingsee.activities;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.RESTObjects.DeviceConfig;
import metro.ourthingsee.Utils;
import metro.ourthingsee.fragments.EnvironmentSensorFragment;
import metro.ourthingsee.fragments.LocationFragment;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import metro.ourthingsee.widget.MyHomeWidgetProvider;
import metro.ourthingsee.widget.MyHomeWidgetProviderSmall;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static metro.ourthingsee.OurContract.PREF_DEVICE_AUTH_ID_NAME;
import static metro.ourthingsee.OurContract.PREF_USER_AUTH_TOKEN_NAME;

public class MainActivity extends AppCompatActivity {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = MainActivity.class.getSimpleName();
    SharedPreferences prefs;
    private NavigationView nav_view;
    private DrawerLayout drawer;
    private Toolbar tb_main;
    private TextView tv_name;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_LOCATION = "Location";
    private static final String TAG_ENVIRONMENT = "Environment sensors";
    public static String CURRENT_TAG = TAG_LOCATION;


    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
        updateAllWidgets();
        // If user did not login before, open login activity first
        Log.e("Giang", prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""));
        if (prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "").isEmpty()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Log.e(LOG_TAG, prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""));
            Log.e(LOG_TAG, prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""));

            //Update the name of device on UI
            APIService apiService = AppUtils.getAPIService();
            apiService.getDeviceName("Bearer " + prefs.getString(PREF_USER_AUTH_TOKEN_NAME, "")
                    , prefs.getString(PREF_DEVICE_AUTH_ID_NAME, "")).enqueue(new Callback<DeviceConfig>() {
                @Override
                public void onResponse(Call<DeviceConfig> call, Response<DeviceConfig> response) {
                    Log.e(LOG_TAG, response.code() + "");
                    if (response.code() == 200) {
                        try {
                            Log.e(LOG_TAG, response.body().getDevice().getName());
                            prefs.edit().putString(OurContract.PREF_DEVICE_NAME,
                                    response.body().getDevice().getName()).apply();
                            tv_name.setText(response.body().getDevice().getName());
                            tb_main.setTitle(response.body().getDevice().getName());
                        } catch (Exception e) {
                            tv_name.setText(R.string.unknown_device);
                            tb_main.setTitle(R.string.unknown_device);
                        }
                    } else {
                        tv_name.setText(R.string.unknown_device);
                        tb_main.setTitle(R.string.unknown_device);
                    }
                }

                @Override
                public void onFailure(Call<DeviceConfig> call, Throwable t) {
                    Utils.handleFailure(MainActivity.this, t);
                    tv_name.setText(R.string.unknown_device);
                }
            });

            //Get the latest humidity, temperature and luminance
            Utils.fetchDataFromThingSee(OurContract.SENSOR_ID_HUMIDITY, MainActivity.this);
            Utils.fetchDataFromThingSee(OurContract.SENSOR_ID_TEMPERATURE, MainActivity.this);
            Utils.fetchDataFromThingSee(OurContract.SENSOR_ID_LUMINANCE, MainActivity.this);
        }

        //Set up toolbar
        tb_main = (Toolbar) findViewById(R.id.tb_main);
        setSupportActionBar(tb_main);

        mHandler = new Handler();


        //Set up navigation bar
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        nav_view = (NavigationView) findViewById(R.id.nav_view);
        setUpNavigationView();

        // Navigation view header
        View navHeader = nav_view.getHeaderView(0);
        tv_name = (TextView) navHeader.findViewById(R.id.tv_name);
        ImageView imgv = (ImageView) navHeader.findViewById(R.id.imgv);
        Glide.with(this).load(R.drawable.navheader).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).centerCrop().animate(android.R.anim.fade_in).approximate().into(imgv);

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_ENVIRONMENT;
            loadHomeFragment();
        }
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            return;
        }

        //Closing drawer on item click
        drawer.closeDrawers();

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }
        // refresh toolbar menu
        invalidateOptionsMenu();
    }
    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 1:
                // location
                return new LocationFragment();
            case 0:
                // environment sensors
                return new EnvironmentSensorFragment();
            default:
                return new LocationFragment();
        }
    }

    private void selectNavMenu() {
        nav_view.getMenu().getItem(navItemIndex).setChecked(true);
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
                    case R.id.nav_location:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_LOCATION;
                        break;
                    case R.id.nav_environment:
                        navItemIndex =0;
                        CURRENT_TAG = TAG_ENVIRONMENT;
                        break;
                    case R.id.log_out:
                        // Because Giang use a Toolbar not default ActionBar, we can't use getApplicationContext()
                        // to get the context for this AlertDialog, we have to call our activity.this
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(getString(R.string.log_out))
                                .setMessage(R.string.log_out_message)
                                .setIcon(R.drawable.warn)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        delLoginData();
                                    }
                                })
                                .setNegativeButton(android.R.string.no, null)
                                .show();
                        break;
                    case R.id.about_us:
                        Intent aboutUs = new Intent(MainActivity.this, AboutUs.class);
                        startActivityForResult(aboutUs, 1);
                        break;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);
                loadHomeFragment();
                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, tb_main,
                R.string.openDrawer, R.string.closeDrawer) {

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
        //delete all shared preferences
        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        updateAllWidgets();
        //move back to log in screen
        Intent intent2 = new Intent(this, LoginActivity.class);
        startActivity(intent2);
        finish();
    }

    private void updateAllWidgets() {
        Intent intent = new Intent(this,MyHomeWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), MyHomeWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);

        Intent intent1 = new Intent(this,MyHomeWidgetProviderSmall.class);
        intent1.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids1 = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), MyHomeWidgetProviderSmall.class));
        intent1.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids1);
        sendBroadcast(intent1);
    }
}
