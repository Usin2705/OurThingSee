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

    /**
     * For the service to work in manifest, need to create the constructor this way
     */
    public TCCloudRequestService() {
        super("TCCloudRequestService");
    }

    /**
     * Handle the service request, with data passed in intent
     *
     * @param serviceIntent the intent used to pass data to this service
     */
    @Override
    protected void onHandleIntent(final Intent serviceIntent) {
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

                                    // Put the minimum humidity value back, so receiver can check
                                    // to notify. Notice that you can get the extra if the flag
                                    // of the pendingIntent start the service intent is UPDATE
                                    // CURRENT and not 0
                                    broadcastIntent.putExtra(
                                            OurContract.INTENT_NAME_MIN_HUMIDITY_VALUE,
                                            serviceIntent.getIntExtra(
                                                    OurContract.INTENT_NAME_MIN_HUMIDITY_VALUE,
                                                    OurContract.DEFAULT_MIN_HUMIDITY_VALUE));
                                    sendBroadcast(broadcastIntent);
                                }
                                break;
                            // To handle error 503  - Service unavailable. Which mean sometime
                            // it can't connect to the cloud. If we don't handle it, it still count
                            // as response, and response.body() get call but since it's null it'll
                            // throw an error.
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
