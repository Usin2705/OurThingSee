package metro.ourthingsee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;

import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Usin on 21-Feb-17.
 */

public class Utils {
    /**
     * Tag for the log messages
     */
    public static final String LOG_TAG = Utils.class.getSimpleName();

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy kk:mm:ss");
    public static SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd-MMM kk:mm");

    /**
     * Handle the failure from apiService request
     * {@link APIService
     *
     * @param context Context of the app, used for showing toast
     * @param t       Throwable t in the onFailure
     */
    public static void handleFailure(Context context, Throwable t) {
        Log.e(LOG_TAG, t.toString());
        Toast.makeText(context,
                context.getString(R.string.fetch_toast_response_failed_general),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Fetch the data from ThingSee device by calling the {@link APIService} method's
     * {@link APIService
     * The response is then handle in {@link #handleOnResponse(String, Response, SharedPreferences)}
     * <p>
     * <p>
     * If we call all data at the same time, ThingSee may return only one data, which may
     * make it complicate to get all require data. That why we need to fetch each data separately.
     *
     * @param sensorID the SensorID of the data we need. Refer to
     *                 <a href="https://thingsee.zendesk.com/hc/en-us/articles/205133092-How-can-I-understand-the-info-displayed-in-senses-view-sensor-s-ID-">ThingSee documentation</a>
     * @param prefs    the intent used to pass data to this service
     */
    public static void fetchDataFromThingSee(final String sensorID, final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(OurContract.SHARED_PREF,
                Context.MODE_PRIVATE);
        APIService apiService = AppUtils.getAPIService();
        apiService.getUserEvents(
                "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""),
                prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""),
                null, sensorID, OurContract.MIN_FETCH_ITEM_TC, null, null).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        handleOnResponse(sensorID, response, prefs);
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        handleFailure(context, t);
                    }
                });
    }

    /**
     * Handle the onResponse from apiService request
     * {@link APIService#getUserEvents(String, String, String, String, Integer, Long, Long)}
     *
     * @param sensorID the sensorID used to send the request
     * @param response the response return from the request. This is a success response.
     * @param prefs    the sharedprefs used to record data
     */
    public static void handleOnResponse(String sensorID, Response<Events> response,
                                        SharedPreferences prefs) {
        switch (response.code()) {
            case 200:
                if (response.body().getEvents().size() > 0) {
                    Long longTimestamp = response.body().getEvents().
                            get(0).getCause().getSenses().get(0).getTs();

                    Double dbValue = response.body().getEvents().
                            get(0).getCause().getSenses().get(0).getVal();

                    Date eventDate = new Date(longTimestamp);

                    switch (sensorID) {
                        case OurContract.SENSOR_ID_HUMIDITY:
                            prefs.edit().putString(OurContract.PREF_HUMID_LATEST_TIME,
                                    String.valueOf(dateFormat.format(eventDate))).apply();
                            prefs.edit().putString(OurContract.PREF_HUMID_LATEST_VALUE,
                                    String.valueOf(dbValue) + " %").apply();
                            break;

                        case OurContract.SENSOR_ID_TEMPERATURE:
                            prefs.edit().putString(OurContract.PREF_TEMP_LATEST_TIME,
                                    String.valueOf(dateFormat.format(eventDate))).apply();
                            prefs.edit().putString(OurContract.PREF_TEMP_LATEST_VALUE,
                                    String.valueOf(dbValue) + " \u2103").apply();
                            break;

                        case OurContract.SENSOR_ID_LUMINANCE:
                            prefs.edit().putString(OurContract.PREF_LIGHT_LATEST_TIME,
                                    String.valueOf(dateFormat.format(eventDate))).apply();
                            prefs.edit().putString(OurContract.PREF_LIGHT_LATEST_VALUE,
                                    String.valueOf(dbValue) + " lux").apply();
                            break;

                        default:
                            break;
                    }
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
}
