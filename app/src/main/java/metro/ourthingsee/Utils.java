package metro.ourthingsee;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.fragments.EnvironmentSensorFragment;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static metro.ourthingsee.fragments.LocationFragment.sdfDate;

/**
 * Utils class for handle some shared tasks of activities
 */

public class Utils {

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss",
            Locale.getDefault());
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy",
            Locale.getDefault());
    public static SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd MMM HH:mm",
            Locale.getDefault());
    public static SimpleDateFormat shortTimeFormat = new SimpleDateFormat("HH:mm",
            Locale.getDefault());
    public static final int TIMEPICKER_CODE_NO_RECORD = 0;
    public static final int TIMEPICKER_CODE_RECORD_END = 1;

    /**
     * Handle the failure from apiService request
     * {@link APIService
     *
     * @param context Context of the app, used for showing toast
     * @param t       Throwable t in the onFailure
     */
    public static void handleFailure(Context context, Throwable t) {
        Toast.makeText(context,
                context.getString(R.string.fetch_toast_response_failed_general),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Fetch the data from ThingSee device by calling the {@link APIService} method's
     * {@link APIService
     * The response is then handle in {@link #handleOnResponse(Response, SharedPreferences)}
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
                        handleOnResponse(response, prefs);
                        EnvironmentSensorFragment.updateDisplayTV(context);
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

     * @param response the response return from the request. This is a success response.
     * @param prefs    the sharedprefs used to record data
     */
    public static void handleOnResponse(Response<Events> response,
                                        SharedPreferences prefs) {
        switch (response.code()) {
            case 200:
                if (response.body().getEvents().size() > 0) {
                    for(int i = 0; i < response.body().getEvents().size();i++) {
                        for(int j=0;j<response.body().getEvents().get(i).getCause().getSenses().size();j++) {
                            String sId = response.body().getEvents().get(i).getCause().getSenses().get(j).getSId();
                            Long longTimestamp = response.body().getEvents().
                                    get(i).getCause().getSenses().get(j).getTs();

                            Double dbValue = response.body().getEvents().
                                    get(i).getCause().getSenses().get(j).getVal();

                            dbValue = dbValue * 100;
                            dbValue = (double) Math.round(dbValue);
                            dbValue = dbValue / 100;

                            Date eventDate = new Date(longTimestamp);
                            if((prefs.getLong(OurContract.UPDATE_TIME,-1l)==-1l)||
                                    (longTimestamp>prefs.getLong(OurContract.UPDATE_TIME,-1l))){
                                prefs.edit().putLong(OurContract.UPDATE_TIME,longTimestamp).apply();
                            }
                            switch (sId) {
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
     * Open the time picker dialog, and promt the user to pick the time.
     * Then set the calendar time to that time.
     * After that, display the time with HH:mm format in the textView.
     *
     * @param textView    The textview to display the results
     * @param calendar    The calendar the get the time results
     * @param context     The context of the activity
     * @param requestCode The request code for the time picker, to know if we need to record in
     *                    prefs or not
     */
    public static void setUpTimePicker(final TextView textView, final Calendar calendar, final Context context, final int requestCode) {
        final TimePickerDialog.OnTimeSetListener callback = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                final SharedPreferences prefs = context.getSharedPreferences(OurContract.SHARED_PREF,
                        Context.MODE_PRIVATE);

                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                textView.setText(shortTimeFormat.format(calendar.getTime()));
                switch (requestCode) {

                    case TIMEPICKER_CODE_NO_RECORD:
                        break;

                    case TIMEPICKER_CODE_RECORD_END:
                        prefs.edit().putLong(OurContract.PREF_MYHOME_END_TIME,
                                calendar.getTimeInMillis()).apply();
                        break;
                    default:
                        break;
                }
            }
        };
        TimePickerDialog timePickerDialog = new TimePickerDialog(context,
                callback,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true);
        timePickerDialog.setCanceledOnTouchOutside(false);
        timePickerDialog.show();
    }

    public static void setUpDatePicker(final TextView tv, final Calendar calendar, final Context context) {
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
                context,
                callback,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setCanceledOnTouchOutside(false);
        datePickerDialog.show();
    }
}
