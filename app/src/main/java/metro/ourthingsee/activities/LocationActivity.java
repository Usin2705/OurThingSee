package metro.ourthingsee.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.Utils;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LocationActivity extends AppCompatActivity {
    Calendar calendar = Calendar.getInstance(), calendarEnd = Calendar.getInstance();
    long startTime, endTime;
    boolean currentLocationState = true;
    List<Events.Event> eventList = new ArrayList<>();
    List<LatLng> listLatLng = new ArrayList<>();
    DecimalFormat df = new DecimalFormat("0.#");
    public static SimpleDateFormat sdfDate = new SimpleDateFormat("dd MMM yyyy");
    GoogleMap mGoogleMap;
    ProgressDialog progressDialog;
    View query_view;
    FloatingActionButton fab_show_path, fab_current_location;
    TextView tv_startDate, tv_startTime, tv_endDate, tv_endTime, tv_distance;
    Button btn_showPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        addControls();
    }

    private void addControls() {
        //get map fragment
        MapFragment mapFragment = (MapFragment)
                getFragmentManager().findFragmentById(R.id.fragment_map);
        //set up progress dialog showing when getting current location
        progressDialog = new ProgressDialog(LocationActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCanceledOnTouchOutside(false);
        //hide query view when first enter activity
        query_view = findViewById(R.id.query_view);
        query_view.setVisibility(View.INVISIBLE);
        //2 fabs show and hide base on each other's state
        fab_show_path = (FloatingActionButton) findViewById(R.id.fab_show_path);
        fab_current_location = (FloatingActionButton) findViewById(R.id.fab_current_location);
        fab_current_location.hide();
        //set up every views on query view
        tv_startDate = (TextView) findViewById(R.id.tv_startDate);
        tv_startDate.setText(sdfDate.format(calendar.getTime()));
        tv_startTime = (TextView) findViewById(R.id.tv_startTime);
        tv_startTime.setText(Utils.shortTimeFormat.format(calendar.getTime()));
        tv_endDate = (TextView) findViewById(R.id.tv_endDate);
        tv_endDate.setText(sdfDate.format(calendar.getTime()));
        tv_endTime = (TextView) findViewById(R.id.tv_endTime);
        tv_endTime.setText(Utils.shortTimeFormat.format(calendar.getTime()));
        tv_distance = (TextView) findViewById(R.id.tv_distance);
        btn_showPath = (Button) findViewById(R.id.btn_showPath);
        //load map when first enter
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        mGoogleMap.getUiSettings().setZoomGesturesEnabled(true);
                        //Simple way to custom info window of markers
                        mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                            @Override
                            public View getInfoWindow(Marker arg0) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {
                                LinearLayout info = new LinearLayout(LocationActivity.this);
                                info.setOrientation(LinearLayout.VERTICAL);
                                TextView title = new TextView(LocationActivity.this);
                                title.setTextColor(Color.BLACK);
                                title.setGravity(Gravity.CENTER);
                                title.setTypeface(null, Typeface.BOLD);
                                title.setText(marker.getTitle());
                                TextView snippet = new TextView(LocationActivity.this);
                                snippet.setTextColor(Color.GRAY);
                                snippet.setGravity(Gravity.CENTER);
                                snippet.setTextSize(10f);
                                snippet.setText(marker.getSnippet());
                                info.addView(title);
                                info.addView(snippet);
                                return info;
                            }
                        });
                        getDeviceCurrentLocation();
                        addEvents();
                    }
                });
            }
        });
    }

    private void addEvents() {
        mGoogleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                fab_current_location.hide();
                fab_show_path.hide();
                return false;
            }
        });
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (currentLocationState) {
                    fab_show_path.show();
                } else {
                    fab_current_location.show();
                }
            }
        });
        fab_show_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocationState = false;
                mGoogleMap.clear();
                query_view.setVisibility(View.VISIBLE);
                tv_distance.setText("0 m");
                fab_show_path.hide();
                fab_current_location.show();
            }
        });
        fab_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentLocationState = true;
                mGoogleMap.clear();
                query_view.setVisibility(View.INVISIBLE);
                fab_current_location.hide();
                fab_show_path.show();
                getDeviceCurrentLocation();
            }
        });
        tv_startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpDatePicker(tv_startDate, calendar);
            }
        });
        tv_endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpDatePicker(tv_endDate, calendarEnd);
            }
        });
        tv_startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setUpTimePicker(tv_startTime, calendar, LocationActivity.this,
                        Utils.TIMEPICKER_CODE_NO_RECORD);
            }
        });
        tv_endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.setUpTimePicker(tv_endTime, calendarEnd, LocationActivity.this,
                        Utils.TIMEPICKER_CODE_NO_RECORD);
            }
        });
        btn_showPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleMap.clear();
                eventList.clear();
                listLatLng.clear();
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendarEnd.set(Calendar.SECOND, 59);
                calendarEnd.set(Calendar.MILLISECOND, 999);
                SharedPreferences sharedPreferences = getSharedPreferences(OurContract.SHARED_PREF, MODE_PRIVATE);
                String authen = "Bearer " + sharedPreferences.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "");
                String deviceAuthen = sharedPreferences.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "");
                APIService apiService = AppUtils.getAPIService();
                progressDialog.setMessage(getString(R.string.drawing_path));
                progressDialog.show();
                getPathInTimeInterval(calendar.getTimeInMillis(),
                        calendarEnd.getTimeInMillis(), apiService, authen, deviceAuthen);

            }
        });
    }

    /*
        Method for getting path in a time interval
    */
    private void getPathInTimeInterval(final Long start, final Long end, final APIService apiService
            , final String authen, final String deviceAuthen) {

        apiService.getUserEvents(authen, deviceAuthen, "sense",
                OurContract.SENSOR_ID_LOCATION_LATITUDE +"," +
                OurContract.SENSOR_ID_LOCATION_LONGITUDE, 50, start, end)
                .enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        switch (response.code()) {
                            case 200:
                                    /*
                                    STEP 1: take out a list of events
                                    */
                                    eventList.addAll(response.body().getEvents());
                                    /*
                                    STEP 2: if the response has 50 events, maybe there are more
                                    than 50 events happened in the time interval, so check it
                                    by a recursion method
                                    */
                                    if (response.body().getEvents().size() == 50)
                                        getPathInTimeInterval(start, response.body().getEvents().get(49)
                                                .getTimestamp() - 1, apiService, authen, deviceAuthen);
                                    /*
                                    STEP 3: if the response has less than 50 events, draw the path
                                    */
                                    else {
                                        showingPathOnMap();
                                        progressDialog.dismiss();
                                    }
                                break;
                            case 503:
                                getPathInTimeInterval(start, end, apiService, authen, deviceAuthen);
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

    /*
    Method for getting current location
     */
    private void getDeviceCurrentLocation() {
        SharedPreferences sharedPreferences = getSharedPreferences(OurContract.SHARED_PREF, MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        progressDialog.setMessage(getString(R.string.getting_current_location));
        progressDialog.show();
        requestDeviceCurrentLocation(apiService, "Bearer " + sharedPreferences.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "")
                , sharedPreferences.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""), null);
    }

    private void requestDeviceCurrentLocation(final APIService apiService, final String s
            , final String string, final Long endTimestamp) {

        apiService.getUserEvents(s, string, "sense",
                OurContract.SENSOR_ID_LOCATION_LATITUDE +"," +
                OurContract.SENSOR_ID_LOCATION_LONGITUDE, 2, null, endTimestamp)
                .enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        switch (response.code()) {
                            case 200:
                                if (response.body().getEvents().size() > 0) {
                                    List<String> sIds = new ArrayList<>();
                                    double lat = 0, lng = 0;
                                    Long comparingTimestamp = response.body().getEvents().get(0).getTimestamp();
                                    for (int j = 0; j < response.body().getEvents().size(); j++) {
                                        List<Events.Sense> senses = response.body().getEvents().get(j)
                                                .getCause().getSenses();
                                        if (response.body().getEvents().get(j).getTimestamp() == comparingTimestamp) {
                                            for (int i = 0; i < senses.size(); i++) {
                                                sIds.add(senses.get(i).getSId());
                                                //after the loop, sIds should contain at least 2 values
                                                // "0x00010100" and "0x00010200"
                                                if (senses.get(i).getSId().equals(OurContract.SENSOR_ID_LOCATION_LATITUDE)) {
                                                    lat = senses.get(i).getVal();
                                                } else if (senses.get(i).getSId().equals(OurContract.SENSOR_ID_LOCATION_LONGITUDE)) {
                                                    lng = senses.get(i).getVal();
                                                }
                                            }
                                        }
                                    }
                                    if (sIds.contains(OurContract.SENSOR_ID_LOCATION_LATITUDE) && sIds.contains(OurContract.SENSOR_ID_LOCATION_LONGITUDE)) {
                                        //if Sids satisfies the conditions, add a marker on the map
                                        LatLng latLng = new LatLng(lat, lng);
                                        mGoogleMap.addMarker(new MarkerOptions().position(latLng)
                                                .title(getString(R.string.last_location))
                                                .snippet(sdfDate.format(response.body()
                                                        .getEvents().get(0).getTimestamp())
                                                        + "\n" + Utils.shortTimeFormat.format(response.body()
                                                        .getEvents().get(0).getTimestamp())));
                                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                        progressDialog.dismiss();
                                    } else {
                                        //if sIds doesn't contain both desired values, call a recursion
                                        if (response.body().getEvents().size() == 2)
                                            requestDeviceCurrentLocation(apiService, s, string, response.body()
                                                    .getEvents().get(response.body()
                                                            .getEvents().size() - 1).getTimestamp());
                                        else if (response.body().getEvents().size() == 1)
                                            requestDeviceCurrentLocation(apiService, s, string, response.body()
                                                    .getEvents().get(0).getTimestamp() - 1);
                                    }
                                } else if (response.body().getEvents().size() == 0) {
                                    //If there is no event contains the location, show a toast
                                    Toast.makeText(LocationActivity.this, R.string.no_location
                                            , Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                                break;
                            case 503:
                                requestDeviceCurrentLocation(apiService, s, string, endTimestamp);
                                break;
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


    private void showingPathOnMap() {
        for (int i = 0; i < eventList.size(); i++) {
            double lat = 0, lng = 0;
            //sIds is used to check if there are both lat lng value in one timestamp or not
            List<String> sIds = new ArrayList<>();
            List<Events.Sense> senses = new ArrayList<>();
            senses.addAll(eventList.get(i).getCause().getSenses());
            long comparingTimestamp = eventList.get(i).getTimestamp();
            for (int j = 0; j < senses.size(); j++) {
                //add all senses id in sIds
                sIds.add(senses.get(j).getSId());
                if (senses.get(j).getSId().equals(OurContract.SENSOR_ID_LOCATION_LATITUDE)) {
                    lat = senses.get(j).getVal();
                } else if (senses.get(j).getSId().equals(OurContract.SENSOR_ID_LOCATION_LONGITUDE)) {
                    lng = senses.get(j).getVal();
                }
            }
            //if there are both lat and lng then finish
            if (sIds.contains(OurContract.SENSOR_ID_LOCATION_LATITUDE) && sIds.contains(OurContract.SENSOR_ID_LOCATION_LONGITUDE)) {
                listLatLng.add(new LatLng(lat, lng));
                if(listLatLng.size()==1){
                    endTime=comparingTimestamp;
                }
                startTime=comparingTimestamp;
                //if there are one missing value then check the next event
            } else if ((sIds.contains(OurContract.SENSOR_ID_LOCATION_LATITUDE) && !sIds.contains(OurContract.SENSOR_ID_LOCATION_LONGITUDE)) ||
                    !sIds.contains(OurContract.SENSOR_ID_LOCATION_LATITUDE) && sIds.contains(OurContract.SENSOR_ID_LOCATION_LONGITUDE)) {
                //if the next event has the same timestamp with the previous
                //then check if it contains the missing value
                if (eventList.size()>(i+1) && eventList.get(i + 1).getTimestamp() == comparingTimestamp) {
                    senses.clear();
                    senses.addAll(eventList.get(i + 1).getCause().getSenses());
                    for (int j = 0; j < senses.size(); j++) {
                        sIds.add(senses.get(j).getSId());
                        if (senses.get(j).getSId().equals(OurContract.SENSOR_ID_LOCATION_LATITUDE)) {
                            lat = senses.get(j).getVal();
                        } else if (senses.get(j).getSId().equals(OurContract.SENSOR_ID_LOCATION_LONGITUDE)) {
                            lng = senses.get(j).getVal();
                        }
                    }
                    //if the next event has the missing value then finish and add 1 to i
                    //because we don't have to check it again
                    if (sIds.contains(OurContract.SENSOR_ID_LOCATION_LATITUDE) && sIds.contains(OurContract.SENSOR_ID_LOCATION_LONGITUDE)) {
                        listLatLng.add(new LatLng(lat, lng));
                        if(listLatLng.size()==1){
                            endTime=comparingTimestamp;
                        }
                        startTime=comparingTimestamp;
                        i++;
                    }
                }
            }
        }
        if(!listLatLng.isEmpty()) {
            if (listLatLng.size() > 1) {
                double distance = 0;
                PolylineOptions polylineOptions = new PolylineOptions();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (int i = 0; i < listLatLng.size() - 1; i++) {
                    distance += calculateArcLengthBaseOnLatLng(listLatLng.get(i), listLatLng.get(i + 1));
                    polylineOptions.add(listLatLng.get(i));
                    builder.include(listLatLng.get(i));
                    if (i == listLatLng.size() - 2) {
                        polylineOptions.add(listLatLng.get(i + 1));
                        builder.include(listLatLng.get(i + 1));
                    }
                }
                Polyline polyline = mGoogleMap.addPolyline(polylineOptions);
                polyline.setColor(0xFF2196F3);
                polyline.setWidth(15f);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 10));
                mGoogleMap.addMarker(new MarkerOptions().position(listLatLng.get(0))
                        .title(getString(R.string.destination)).snippet(sdfDate.format(endTime) + "\n"
                                + Utils.shortTimeFormat.format(endTime)));
                mGoogleMap.addMarker(new MarkerOptions().position(listLatLng.get(listLatLng.size() - 1))
                        .title(getString(R.string.origin)).snippet(sdfDate.format(startTime) + "\n"
                                + Utils.shortTimeFormat.format(startTime)));
                final Circle circleEnd = mGoogleMap.addCircle(new CircleOptions()
                        .center(listLatLng.get(0))
                        .fillColor(Color.GRAY)
                        .radius(calculateCircleRadiusMeterForMapCircle(
                                8, listLatLng.get(0).latitude, mGoogleMap.getCameraPosition().zoom))
                        .strokeWidth(3f)
                        .strokeColor(Color.DKGRAY)
                        .zIndex(3f));
                final Circle circleStart = mGoogleMap.addCircle(new CircleOptions()
                        .center(listLatLng.get(listLatLng.size() - 1))
                        .fillColor(Color.GRAY)
                        .radius(circleEnd.getRadius())
                        .strokeWidth(3f)
                        .strokeColor(Color.DKGRAY)
                        .zIndex(3f));
                mGoogleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        circleEnd.setRadius(calculateCircleRadiusMeterForMapCircle(
                                8, listLatLng.get(0).latitude, mGoogleMap.getCameraPosition().zoom));
                        circleStart.setRadius(circleEnd.getRadius());
                    }
                });
                if (distance * 6371000 < 1000)
                    tv_distance.setText(df.format(distance * 6371000) + " m");
                else
                    tv_distance.setText(df.format(distance * 6371) + " km");
            } else if (listLatLng.size() == 1) {
                Log.e("Giang time", endTime + "");
                tv_distance.setText("0 m");
                mGoogleMap.addMarker(new MarkerOptions().position(listLatLng.get(0))
                        .title(getString(R.string.only_location))
                        .snippet(sdfDate.format(endTime) + "\n"
                                + Utils.shortTimeFormat.format(endTime)));
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(listLatLng.get(0), 15));
            }
        } else if (listLatLng.isEmpty()) {
            Toast.makeText(this, R.string.no_path, Toast.LENGTH_SHORT).show();
            mGoogleMap.setOnCameraMoveListener(null);
            tv_distance.setText("0 m");
        }
    }

    private void setUpDatePicker(final TextView tv, final Calendar calendar) {
        DatePickerDialog.OnDateSetListener callback = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                tv.setText(sdfDate.format(calendar.getTime()));
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                LocationActivity.this,
                callback,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setCanceledOnTouchOutside(false);
        datePickerDialog.show();
    }

    private double calculateArcLengthBaseOnLatLng(LatLng start, LatLng end) {
        double arc = 2 * Math.asin(Math.sqrt((1 - Math.sin(Math.toRadians(start.latitude))
                * Math.sin(Math.toRadians(end.latitude))
                - Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude))
                * Math.cos(Math.toRadians(start.longitude - end.longitude))) / 2));
        if (Double.isNaN(arc)) {
            /*
            when the two points are too close, the value of arc is smaller than the smallest
            positive value that a double can represent, then arc is NaN (not a number)
            */
            return 0;
        }
        return arc;
    }

    public static double calculateCircleRadiusMeterForMapCircle(final int _targetRadiusDip
            , final double _circleCenterLatitude, final float _currentMapZoom) {
        //That base value seems to work for computing the meter length of a DIP
        final double arbitraryValueForDip = 156000D;
        final double oneDipDistance = Math.abs(Math.cos(Math.toRadians(_circleCenterLatitude)))
                * arbitraryValueForDip / Math.pow(2, _currentMapZoom);
        return oneDipDistance * (double) _targetRadiusDip;
    }
}
