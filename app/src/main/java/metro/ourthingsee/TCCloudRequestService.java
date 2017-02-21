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
    private static final int NO_OF_ITEM = 1;

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
        fetchDataFromThingSee(OurContract.SENSOR_ID_HUMIDITY, serviceIntent);
        fetchDataFromThingSee(OurContract.SENSOR_ID_TEMPERATURE, serviceIntent);
        fetchDataFromThingSee(OurContract.SENSOR_ID_LUMINANCE, serviceIntent);
    }

    /**
     * Fetch the data from ThingSee device by calling the {@link APIService} method's
     * {@link APIService#getUserEvents(String, String, String, String, Integer, Long, Long)}
     * The response is then handle in {@link #handleOnResponse(String, Response, Intent)}
     * <p>
     * <p>
     * If we call all data at the same time, ThingSee may return only one data, which may
     * make it complicate to get all require data. That why we need to fetch each data separately.
     *
     * @param sensorID      the SensorID of the data we need. Refer to
     *                      <a href="https://thingsee.zendesk.com/hc/en-us/articles/205133092-How-can-I-understand-the-info-displayed-in-senses-view-sensor-s-ID-">ThingSee documentation</a>
     * @param serviceIntent the intent used to pass data to this service
     */
    private void fetchDataFromThingSee(final String sensorID, final Intent serviceIntent) {
        SharedPreferences prefs = getSharedPreferences(OurContract.SHARED_PREF,
                Context.MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        apiService.getUserEvents(
                "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""),
                prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""),
                null, sensorID, NO_OF_ITEM, null, null).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        handleOnResponse(sensorID, response, serviceIntent);
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        handleFailure(t);
                    }
                });
    }

    /**
     * Handle the onResponse from apiService request
     * {@link APIService#getUserEvents(String, String, String, String, Integer, Long, Long)}
     *
     * @param sensorID      the sensorID used to send the request
     * @param response      the response return from the request. This is a success response.
     * @param serviceIntent the intent used to pass data to this service
     */
    private void handleOnResponse(String sensorID, Response<Events> response,
                                  Intent serviceIntent) {
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

                    // Puts the status into the Intent if they have value
                    // Just need to check for the timestamp, if the timestamp not null then the
                    // value also not null
                    broadcastIntent.putExtra(OurContract.BROADCAST_RESPONSE_TIMESTAMP, longTimestamp);
                    broadcastIntent.putExtra(OurContract.BROADCAST_RESPONSE_VALUE, dbValue);
                    broadcastIntent.putExtra(OurContract.BROADCAST_RESPONSE_SENSORID, sensorID);

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

    /**
     * Handle the failure from apiService request
     * {@link APIService#getUserEvents(String, String, String, String, Integer, Long, Long)}
     *
     * @param t Throwable t in the onFailure
     */
    private void handleFailure(Throwable t) {
        Log.e(LOG_TAG, t.toString());
        Toast.makeText(getApplicationContext(),
                getString(R.string.fetch_toast_response_failed_general),
                Toast.LENGTH_SHORT).show();
    }
}
