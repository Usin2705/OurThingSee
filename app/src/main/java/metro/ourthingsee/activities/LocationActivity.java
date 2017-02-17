package metro.ourthingsee.activities;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends AppCompatActivity {
    GoogleMap mGoogleMap;
    ProgressDialog progressDialog;
    View query_view;
    FloatingActionButton fab_show_path, fab_current_location;
    TextView tv_startDate, tv_startTime, tv_endDate, tv_endTime;
    Button btn_showPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        Log.e("Giang",""+calculateArcLengthBaseOnLatLng(new LatLng(39.9,116.4), new LatLng(-31.95,115.867)));
        addControls();
    }

    private void addControls() {
        MapFragment mapFragment = (MapFragment)
                getFragmentManager().findFragmentById(R.id.fragment_map);
        progressDialog = new ProgressDialog(LocationActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Getting current location...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        query_view = findViewById(R.id.query_view);
        query_view.setVisibility(View.INVISIBLE);
        fab_show_path = (FloatingActionButton) findViewById(R.id.fab_show_path);
        fab_current_location = (FloatingActionButton) findViewById(R.id.fab_current_location);
        fab_current_location.hide();
        tv_startDate = (TextView) findViewById(R.id.tv_startDate);
        tv_startTime = (TextView) findViewById(R.id.tv_startTime);
        tv_endDate = (TextView) findViewById(R.id.tv_endDate);
        tv_endTime = (TextView) findViewById(R.id.tv_endTime);
        btn_showPath = (Button) findViewById(R.id.btn_showPath);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
                        getDeviceCurrentLocation(null);
                        addEvents();
                    }
                });
            }
        });
    }

    private void addEvents() {
        fab_show_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query_view.setVisibility(View.VISIBLE);
                fab_show_path.hide();
                fab_current_location.show();
            }
        });
        fab_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                query_view.setVisibility(View.INVISIBLE);
                fab_current_location.hide();
                fab_show_path.show();
                getDeviceCurrentLocation(null);
            }
        });
    }

    /*
    Method for getting current location
     */
    private void getDeviceCurrentLocation(Long endTimestamp) {
        SharedPreferences sharedPreferences = getSharedPreferences(OurContract.SHARED_PREF, MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        progressDialog.show();
        apiService.getUserEvents("Bearer "+sharedPreferences.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "")
                , sharedPreferences.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "")
                , "sense", "0x00010100,0x00010200", 1, null, endTimestamp)
                .enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        if (response.code() == 200 && response.body().getEvents().size() > 0) {
                            // sIds String List is used to check if there are both longitude and latitude retrieved
                            List<String> sIds = new ArrayList<String>();
                            double lat = 0, lng = 0;
                            for(int i = 0; i<response.body().getEvents().get(0).getCause().getSenses().size();i++)
                            {
                                sIds.add(response.body().getEvents().get(0).getCause().getSenses().get(i).getSId());
                                //after the loop, sIds should contain at least 2 values "0x00010100" and "0x00010200"
                                if (response.body().getEvents().get(0).getCause().getSenses().get(i).getSId().equals("0x00010100")) {
                                    lat = response.body().getEvents().get(0).getCause().getSenses().get(i).getVal();
                                } else if (response.body().getEvents().get(0).getCause().getSenses().get(i).getSId().equals("0x00010200")) {
                                    lng = response.body().getEvents().get(0).getCause().getSenses().get(i).getVal();
                                }
                            }
                            if (sIds.contains("0x00010100")&&sIds.contains("0x00010200")) {
                                //if Sids satisfies the conditions, add a marker on the map
                                LatLng latLng = new LatLng(lat, lng);
                                mGoogleMap.addMarker(new MarkerOptions().position(latLng));
                                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                progressDialog.dismiss();
                            } else {
                                //if sIds doesn't contain both desired values, call a recursion
                                getDeviceCurrentLocation(response.body().getEvents().get(0).getTimestamp() - 1);
                            }
                        }else if (response.code() == 200 && response.body().getEvents().size() == 0){
                            //If there is no event contains the location, show a toast
                            Toast.makeText(LocationActivity.this, R.string.no_location, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Log.e("Giang loi events", t.toString());
                        Toast.makeText(LocationActivity.this,
                                getString(R.string.login_toast_login_failed_nointernet),
                                Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
    }
    private double calculateArcLengthBaseOnLatLng(LatLng start, LatLng end){
        double distance = 2*6371*Math.asin(Math.sqrt((1-Math.sin(Math.toRadians(start.latitude))*Math.sin(Math.toRadians(end.latitude))
                -Math.cos(Math.toRadians(start.latitude))*Math.cos(Math.toRadians(end.latitude))
                *Math.cos(Math.toRadians(start.longitude-end.longitude)))/2));
        return distance;
    }
}
