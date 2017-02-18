package metro.ourthingsee.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    Calendar calendar = Calendar.getInstance(),
            calendarEnd = Calendar.getInstance();
    SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy");
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
    GoogleMap mGoogleMap;
    ProgressDialog progressDialog;
    View query_view;
    FloatingActionButton fab_show_path, fab_current_location;
    TextView tv_startDate, tv_startTime, tv_endDate, tv_endTime;
    Button btn_showPath;
    List<LatLng> listLatLng = new ArrayList<>();

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
        progressDialog.setMessage(getString(R.string.getting_current_location));
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
        tv_startTime.setText(sdfTime.format(calendar.getTime()));
        tv_endDate = (TextView) findViewById(R.id.tv_endDate);
        tv_endDate.setText(sdfDate.format(calendar.getTime()));
        tv_endTime = (TextView) findViewById(R.id.tv_endTime);
        tv_endTime.setText(sdfTime.format(calendar.getTime()));
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
                mGoogleMap.clear();
                query_view.setVisibility(View.VISIBLE);
                fab_show_path.hide();
                fab_current_location.show();
            }
        });
        fab_current_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mGoogleMap.clear();
                query_view.setVisibility(View.INVISIBLE);
                fab_current_location.hide();
                fab_show_path.show();
                getDeviceCurrentLocation(null);
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
                setUpTimePicker(tv_startTime, calendar);
            }
        });
        tv_endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUpTimePicker(tv_endTime, calendarEnd);
            }
        });
        btn_showPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                calendarEnd.set(Calendar.SECOND, 59);
                calendarEnd.set(Calendar.MILLISECOND, 999);
                listLatLng.clear();
                SharedPreferences sharedPreferences = getSharedPreferences(OurContract.SHARED_PREF, MODE_PRIVATE);
                String authen = "Bearer " + sharedPreferences.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "");
                String deviceAuthen = sharedPreferences.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "");
                APIService apiService = AppUtils.getAPIService();
                progressDialog.setMessage(getString(R.string.drawing_path));
                progressDialog.show();
                getPathInTimeInterval(calendar.getTimeInMillis(), calendarEnd.getTimeInMillis(), apiService, authen, deviceAuthen);

            }
        });
    }

    private void getPathInTimeInterval(final Long start, final Long end, final APIService apiService, final String authen, final String deviceAuthen) {
        apiService.getUserEvents(authen, deviceAuthen, "sense", "0x00010100,0x00010200", 50, start, end)
                .enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        switch (response.code()) {
                            case 200:
                                if (response.body().getEvents().size() > 0) {
                                    for (int i = 0; i < response.body().getEvents().size(); i++) {
                                        List<String> sIds = new ArrayList<String>();
                                        double lat = 0, lng = 0;
                                        for (int j = 0; j < response.body().getEvents().get(i).getCause().getSenses().size(); j++) {
                                            sIds.add(response.body().getEvents().get(i).getCause().getSenses().get(j).getSId());
                                            //after the loop, sIds should contain at least 2 values "0x00010100" and "0x00010200"
                                            if (response.body().getEvents().get(i).getCause().getSenses().get(j).getSId().equals("0x00010100")) {
                                                lat = response.body().getEvents().get(i).getCause().getSenses().get(j).getVal();
                                            } else if (response.body().getEvents().get(i).getCause().getSenses().get(j).getSId().equals("0x00010200")) {
                                                lng = response.body().getEvents().get(i).getCause().getSenses().get(j).getVal();
                                            }
                                        }
                                        if (sIds.contains("0x00010100") && sIds.contains("0x00010200")) {
                                            //if Sids satisfies the conditions, add a LatLng to the list
                                            LatLng latLng = new LatLng(lat, lng);
//                                            Log.e("Giang coor",latLng.latitude+","+latLng.longitude);
                                            listLatLng.add(latLng);
                                        }
                                    }
                                    if (response.body().getEvents().size() == 50) {
                                        getPathInTimeInterval(start, response.body().getEvents().get(49).getTimestamp() - 1
                                                , apiService, authen, deviceAuthen);
                                    } else {
                                        for(int i = 0;i<listLatLng.size();i++){
                                            Log.e("Giang latlng",listLatLng.get(i).latitude+","+listLatLng.get(i).longitude);
                                        }
                                        progressDialog.dismiss();
                                    }
                                } else if (response.body().getEvents().size() == 0) {
                                    if (listLatLng.isEmpty())
                                        Toast.makeText(LocationActivity.this, R.string.no_path, Toast.LENGTH_SHORT).show();
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

    private void setUpTimePicker(final TextView tv, final Calendar calendar) {
        TimePickerDialog.OnTimeSetListener callback = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                tv.setText(sdfTime.format(calendar.getTime()));
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(LocationActivity.this,
                callback,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.setCanceledOnTouchOutside(false);
        timePickerDialog.show();
    }

    /*
    Method for getting current location
     */
    private void getDeviceCurrentLocation(final Long endTimestamp) {
        SharedPreferences sharedPreferences = getSharedPreferences(OurContract.SHARED_PREF, MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        progressDialog.show();
        apiService.getUserEvents("Bearer " + sharedPreferences.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "")
                , sharedPreferences.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "")
                , "sense", "0x00010100,0x00010200", 1, null, endTimestamp)
                .enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        switch (response.code()) {
                            case 200:
                                if (response.body().getEvents().size() > 0) {
                                    // sIds String List is used to check if there are both longitude and latitude retrieved
                                    List<String> sIds = new ArrayList<String>();
                                    double lat = 0, lng = 0;
                                    for (int i = 0; i < response.body().getEvents().get(0).getCause().getSenses().size(); i++) {
                                        sIds.add(response.body().getEvents().get(0).getCause().getSenses().get(i).getSId());
                                        //after the loop, sIds should contain at least 2 values "0x00010100" and "0x00010200"
                                        if (response.body().getEvents().get(0).getCause().getSenses().get(i).getSId().equals("0x00010100")) {
                                            lat = response.body().getEvents().get(0).getCause().getSenses().get(i).getVal();
                                        } else if (response.body().getEvents().get(0).getCause().getSenses().get(i).getSId().equals("0x00010200")) {
                                            lng = response.body().getEvents().get(0).getCause().getSenses().get(i).getVal();
                                        }
                                    }
                                    if (sIds.contains("0x00010100") && sIds.contains("0x00010200")) {
                                        //if Sids satisfies the conditions, add a marker on the map
                                        LatLng latLng = new LatLng(lat, lng);
                                        mGoogleMap.addMarker(new MarkerOptions().position(latLng));
                                        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                        progressDialog.dismiss();
                                    } else {
                                        //if sIds doesn't contain both desired values, call a recursion
                                        getDeviceCurrentLocation(response.body().getEvents().get(0).getTimestamp() - 1);
                                    }
                                } else if (response.body().getEvents().size() == 0) {
                                    //If there is no event contains the location, show a toast
                                    Toast.makeText(LocationActivity.this, R.string.no_location, Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                                break;
                            case 503:
                                getDeviceCurrentLocation(endTimestamp);
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

    private double calculateArcLengthBaseOnLatLng(LatLng start, LatLng end) {
        double distance = 2 * 6371 * Math.asin(Math.sqrt((1 - Math.sin(Math.toRadians(start.latitude)) * Math.sin(Math.toRadians(end.latitude))
                - Math.cos(Math.toRadians(start.latitude)) * Math.cos(Math.toRadians(end.latitude))
                * Math.cos(Math.toRadians(start.longitude - end.longitude))) / 2));
        return distance;
    }
}
