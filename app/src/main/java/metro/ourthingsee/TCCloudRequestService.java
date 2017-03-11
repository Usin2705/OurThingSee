package metro.ourthingsee;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.activities.MainActivity;
import metro.ourthingsee.fragments.EnvironmentSensorFragment;
import metro.ourthingsee.remote.APIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TCCloudRequestService extends IntentService {
    /**
     * If the sensor timestamp is older than the current time with this amount, no notification
     * will be send.
     * Set at 2 hours
     */
    private static final int ELAPSE_TIME = 1000 * 60 * 60 * 2;
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
    protected void onHandleIntent(Intent serviceIntent) {
        fetchDataFromThingSee(OurContract.SENSOR_ID_HUMIDITY);
        fetchDataFromThingSee(OurContract.SENSOR_ID_TEMPERATURE);
        fetchDataFromThingSee(OurContract.SENSOR_ID_LUMINANCE);
    }

    /**
     * Fetch the data from ThingSee device by calling the {@link APIService} method's
     * {@link APIService#getUserEvents(String, String, String, String, Integer, Long, Long)}
     * The response is then handle in {@link #handleOnResponse(String, Response)}
     * <p>
     * <p>
     * If we call all data at the same time, ThingSee may return only one data, which may
     * make it complicate to get all require data. That why we need to fetch each data separately.
     *
     * @param sensorID the SensorID of the data we need. Refer to
     *                 <a href="https://thingsee.zendesk.com/hc/en-us/articles/205133092-How-can-I-understand-the-info-displayed-in-senses-view-sensor-s-ID-">ThingSee documentation</a>
     */
    private void fetchDataFromThingSee(final String sensorID) {
        SharedPreferences prefs = getSharedPreferences(OurContract.SHARED_PREF,
                Context.MODE_PRIVATE);
        APIService apiService = Utils.getAPIService();
        apiService.getUserEvents(
                "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""),
                prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""),
                null, sensorID, OurContract.MIN_FETCH_ITEM_TC, null, null).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        handleOnResponse(sensorID, response);
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Utils.handleFailure(getApplicationContext());
                    }
                });
    }

    /**
     * Handle the onResponse from apiService request
     * {@link APIService#getUserEvents(String, String, String, String, Integer, Long, Long)}
     *
     * @param sensorID the sensorID used to send the request
     * @param response the response return from the request. This is a success response.
     */
    private void handleOnResponse(String sensorID, Response<Events> response) {
        switch (response.code()) {
            case 200:
                if (response.body().getEvents().size() > 0) {
                    for(int i = 0; i<response.body().getEvents().get(0).getCause().getSenses().size();i++) {
                        Long longTimestamp = response.body().getEvents().
                                get(0).getCause().getSenses().get(i).getTs();

                        Double dbValue = response.body().getEvents().
                                get(0).getCause().getSenses().get(i).getVal();

                        dbValue = dbValue * 100;
                        dbValue = (double) Math.round(dbValue);
                        dbValue = dbValue / 100;

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

//                    sendBroadcast(broadcastIntent);

                        handleOnReceive(broadcastIntent, getApplicationContext(), OurContract.BROADCAST_RESPONSE_TIMESTAMP,
                                OurContract.BROADCAST_RESPONSE_VALUE);
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
    /**
     * Handle the onReceive of BroadcastReceiver here.
     * This will record the last timestamp and value to prefs.
     * Also it will handle the notification (if set) for each sensorID.
     *
     * @param intent  the intent we get from the Broadcast. We need to get sensorID from this
     *                intent to switch case based on the data we get.
     * @param context the context of the app, for sending notification
     * @param tsName  the Broadcast Response Name contain timestamp value in Long
     * @param vlName  the Broadcast Response Name contain sensor value in Double
     */
    private void handleOnReceive(Intent intent, Context context, String tsName, String vlName) {
        // If the intent have the extra value for tsName, then we take the value and process,
        // else we stop.
        Long longTimestamp = intent.getLongExtra(tsName, -100L);
        Double dbResponse = intent.getDoubleExtra(vlName, -100d);

        Date eventDate = new Date(longTimestamp);

        String sensorID = intent.getStringExtra(OurContract.BROADCAST_RESPONSE_SENSORID);

        // Named the prefs after the glory Giang
        SharedPreferences prefsGiang = context.getSharedPreferences
                (OurContract.SHARED_PREF, MODE_PRIVATE);

        // If prefsGiang is not null, and both timestamp and double value are not 0
        if (dbResponse != -100d && longTimestamp != -100L) {
            switch (sensorID) {
                case OurContract.SENSOR_ID_HUMIDITY:
                    prefsGiang.edit().putString(OurContract.PREF_HUMID_LATEST_TIME,
                            Utils.dateFormat.format(eventDate)).apply();
                    prefsGiang.edit().putString(OurContract.PREF_HUMID_LATEST_VALUE,
                            String.valueOf(dbResponse) + " %").apply();

                    // If the value is less than the min value, notify the user
                    // only notify if the notification option is turned on
                    if (dbResponse < prefsGiang.getInt(
                            OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE,
                            OurContract.DEFAULT_MIN_HUMIDITY_VALUE)) {

                        String strNotf = context.getString
                                (R.string.notification_humidity, dbResponse);
                        strNotf += "%. " + Utils.shortDateFormat.format(longTimestamp);
                        sendNotification(context, longTimestamp, strNotf,
                                OurContract.NOTIFICATION_ID_HUMIDITY);
                    }

                    break;

                case OurContract.SENSOR_ID_TEMPERATURE:
                    prefsGiang.edit().putString(OurContract.PREF_TEMP_LATEST_TIME,
                            Utils.dateFormat.format(eventDate)).apply();
                    prefsGiang.edit().putString(OurContract.PREF_TEMP_LATEST_VALUE,
                            String.valueOf(dbResponse) + " \u2103").apply();
                    break;

                case OurContract.SENSOR_ID_LUMINANCE:
                    prefsGiang.edit().putString(OurContract.PREF_LIGHT_LATEST_TIME,
                            Utils.dateFormat.format(eventDate)).apply();
                    prefsGiang.edit().putString(OurContract.PREF_LIGHT_LATEST_VALUE,
                            String.valueOf(dbResponse) + "lux").apply();

                    // If the value is less than the min value, notify the user
                    // only notify if the notification option is turned on
                    if (dbResponse < prefsGiang.getInt(
                            OurContract.PREF_MYHOME_MIN_LIGHT_VALUE,
                            OurContract.DEFAULT_MIN_LIGHT_VALUE)) {

                        String strNotf = context.getString
                                (R.string.notification_luminance, dbResponse);
                        strNotf += "lux. " + Utils.shortDateFormat.format(longTimestamp);
                        sendNotification(context, longTimestamp, strNotf,
                                OurContract.NOTIFICATION_ID_LUMINANCE);
                    }
                    break;
                default:
                    break;
            }
        }
    }
    /**
     * Send the notification to user. It need to be static so it can be called by our
     * static receiver {@link EnvironmentSensorFragment.TCCLoudRequestReceiver}.
     *
     * @param context       the context of the app, used to get resources
     * @param longTimestamp the Timestamp of the sensor value, in Long milliseconds
     * @param strContent    the content of the notification to be send
     * @param sensorType    the notification id
     */
    private void sendNotification(Context context, Long longTimestamp, String strContent,
                                         int sensorType) {
        Intent ntfIntent = new Intent(context, MainActivity.class);
        ntfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                ntfIntent, 0);

        //Get an instance of notification
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context)
                        .setSmallIcon((sensorType == OurContract.NOTIFICATION_ID_HUMIDITY)
                                ? R.drawable.nature
                                : R.drawable.light)
                        .setContentTitle(context.getString(R.string.myhome_option))
                        .setContentText(strContent)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true); // cancel when clicked

        // Set the default value (vibrate, sound, light)
        // If  your phone is set on DO NOT DISTURB MODEL all sound and stuff won't work
        notification.setDefaults(Notification.DEFAULT_ALL);

        // Set background color to transparent (so the small icon look better)
        notification.setColor(ContextCompat.getColor(context, R.color.colorTransparent));

        // Gets an instance of the NotificationManager service//
        NotificationManager mNotificationManager =

                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Only send notification if the time different between sensor time and current time less
        // than ELAPSE_TIME
        // Also check if the current time is not quite time
        if (((System.currentTimeMillis() - longTimestamp) < ELAPSE_TIME)
                && isNotQuietTime(context)) {
            //When you issue multiple notifications about the same type of event, it’s best practice
            // for your app to try to update an existing notification with this new information, rather
            // than immediately creating a new notification. If you want to update this notification at
            // a later date, you need to assign it an ID. You can then use this ID whenever you issue a
            // subsequent notification. If the previous notification is still visible, the system will
            // update this existing notification, rather than create a new one.
            if (sensorType == OurContract.NOTIFICATION_ID_HUMIDITY) {
                mNotificationManager.notify(OurContract.NOTIFICATION_ID_HUMIDITY,
                        notification.build());
            } else if (sensorType == OurContract.NOTIFICATION_ID_LUMINANCE) {
                mNotificationManager.notify(OurContract.NOTIFICATION_ID_LUMINANCE,
                        notification.build());
            }
        }
    }
    /**
     * Check if the current time is older than the quiet end time or earlier than quiet start time
     * only compare the time within a day, by convert any long timestamp to the time within day.
     * <p>
     * The method here is to divide any long timestamp by the milisecond * second * min * hour in a
     * day, and take the remainder. The integer quotient are the total days of that timestamp, and
     * the remainder is the "not full" day leftover, which can be easily compare within a day.
     * <p>
     * To compare time correctly, avoid timezone complication, get timezone default and compare
     * minutes within day
     *
     * @param context The context to get the shareprefs
     * @return Boolean value whether the current time is not quiet time period or not
     * @see <a href="http://stackoverflow.com/a/7676307/3623497">Stackoverflow Link</a>
     */
    private boolean isNotQuietTime(Context context) {
        Boolean isNotQuiet = false;

        // Named the prefs after the glory Giang
        SharedPreferences prefsGiang = context.getSharedPreferences
                (OurContract.SHARED_PREF, MODE_PRIVATE);

        Calendar currentCalendar = Calendar.getInstance(TimeZone.getDefault());
        Calendar endCalendar = Calendar.getInstance(TimeZone.getDefault());

        endCalendar.setTimeInMillis(prefsGiang.getLong(OurContract.PREF_MYHOME_END_TIME, 0));

        int intCurrentTime = currentCalendar.get(Calendar.HOUR_OF_DAY) * 60 +
                currentCalendar.get(Calendar.MINUTE);
        int intEndTime = endCalendar.get(Calendar.HOUR_OF_DAY) * 60 +
                endCalendar.get(Calendar.MINUTE);

        if (intCurrentTime >= intEndTime) {
            isNotQuiet = true;
        }
        return isNotQuiet;
    }
}
