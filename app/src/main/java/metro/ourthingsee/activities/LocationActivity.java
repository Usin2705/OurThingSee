package metro.ourthingsee.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        addControls();
    }

    private void addControls() {
        MapFragment mapFragment = (MapFragment)
                getFragmentManager().findFragmentById(R.id.fragment_map);
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
                    }
                });
            }
        });
    }

    private void getDeviceCurrentLocation(Long endTimestamp) {
        String auth = "Bearer ";
        SharedPreferences sharedPreferences;
        sharedPreferences = getSharedPreferences(OurContract.SHARED_PREF, MODE_PRIVATE);
        auth += sharedPreferences.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, "");
//        auth += device.getToken();
        String senseIds = "0x00010100,0x00010200";
        APIService apiService = AppUtils.getAPIService();
        apiService.getUserEvents(auth, sharedPreferences.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, "")
                , "sense", senseIds, 1, null, endTimestamp)
                .enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        Log.e("Giang Event", response.code() + "");
                        if (response.code() == 200 && response.body().getEvents().size() ==1
                                &&response.body().getEvents().get(0).getCause().getSenses().size()==2) {
                            double lat = 0, lng = 0;
                            Log.e("Giang Event Id", response.body().getEvents().get(0).getCause().getSenses().size() + "");
                            for (int j = 0; j < response.body().getEvents().get(0).getCause().getSenses().size(); j++) {
                                Log.e("Giang value",response.body().getEvents().get(0).getCause().getSenses().get(j).getSId());
                                if (response.body().getEvents().get(0).getCause().getSenses().get(j).getSId().equals("0x00010100")) {
                                    lat = response.body().getEvents().get(0).getCause().getSenses().get(j).getVal();
                                    Log.e("Giang Lat", lat+"");
                                } else if (response.body().getEvents().get(0).getCause().getSenses().get(j).getSId().equals("0x00010200")) {
                                    lng = response.body().getEvents().get(0).getCause().getSenses().get(j).getVal();
                                    Log.e("Giang Lng", lng+"");
                                }
                            }
                            LatLng latLng = new LatLng(lat, lng);
                            Log.e("Giang",latLng.latitude+"");
                            mGoogleMap.addMarker(new MarkerOptions().position(latLng));
                            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                        }
                        else if (response.body().getEvents().get(0).getCause().getSenses().size()<2){
                            getDeviceCurrentLocation(response.body().getEvents().get(0).getTimestamp()-1);
                        }
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Log.e("Giang loi events", t.toString());
                        Toast.makeText(LocationActivity.this,
                                getString(R.string.login_toast_login_failed_nointernet),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
