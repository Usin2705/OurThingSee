package metro.ourthingsee;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Usin on 15-Feb-17.
 */

public class TCCloudRequestService extends IntentService {
    public static final String LOG_TAG = TCCloudRequestService.class.getSimpleName();


    public TCCloudRequestService() {
        super("TCCloudRequestService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        apiService.getUserEvents(
                "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""),
                prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""),
                null, OurContract.SENSOR_ID_HUMIDITY, null, null, null).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        switch (response.code()) {
                            case 200:
                                if (response.body().getEvents().size() > 0) {
                                    Long longTimestamp = response.body().getEvents().
                                            get(0).getCause().getSenses().get(0).getTs();

                                    Double dbValue = response.body().getEvents().
                                            get(0).getCause().getSenses().get(0).getVal();

                                    /*
                                    * Creates a new Intent containing a Uri object
                                    * BROADCAST_ACTION is a custom Intent action
                                    */
                                    Intent broadcastIntent = new Intent(OurContract.BROADCAST_ACTION);
                                    // Puts the status into the Intent
                                    broadcastIntent.putExtra(OurContract.BROADCAST_RESPONSE_VALUE, dbValue);
                                    broadcastIntent.putExtra(OurContract.BROADCAST_RESPONSE_TIMESTAMP, longTimestamp);
                                    sendBroadcast(broadcastIntent);
                                }
                                break;
                            case 503:
                                break;
                        }
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Log.e(LOG_TAG, t.toString());
                        Toast.makeText(getApplicationContext(),
                                getString(R.string.fetch_toast_response_failed_general),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
