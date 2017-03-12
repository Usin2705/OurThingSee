package metro.ourthingsee.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Calendar;
import java.util.TimeZone;

import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.RESTObjects.Events;
import metro.ourthingsee.TCCloudRequestService;
import metro.ourthingsee.Utils;
import metro.ourthingsee.activities.MainActivity;
import metro.ourthingsee.remote.APIService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


public class EnvironmentSensorFragment extends Fragment {
    private static final int MIN_VALUE = 1;
    private SharedPreferences prefs;
    private TextView txtTemperatureTime, txtTemperatureValue, txtHumidityTime, txtHumidityValue,
            txtLightTime, txtLightValue;
    Switch swtMyHome;
    View view;
    AlarmManager alarmManager;
    TextView txtMyHomeEndTime;

    public EnvironmentSensorFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_environment, container, false);
        // Inflate the layout for this fragment
        prefs = getContext().getSharedPreferences(OurContract.SHARED_PREF, MODE_PRIVATE);

        // Cast all the display texts
        castDisplayTV();


        ((MainActivity) getActivity()).progressDialog.dismiss();

        final LinearLayout lnlMyHomeOpt = (LinearLayout) view.findViewById(R.id.lnlMyHomeOption);

        //************************************ NOTIFICATION OPTION *********************************
        // Find the switch button, set it according to prefs, and set onCheckChangeListener
        swtMyHome = (Switch) view.findViewById(R.id.swtMyHome);
        swtMyHome.setChecked(prefs.getBoolean(OurContract.PREF_MYHOME_NOTIFICATION_OPTION, false));
        swtMyHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                handleSwtState(lnlMyHomeOpt, swtMyHome, swtMyHome.isChecked());
            }
        });

        // Display the layout option or not depend on the switch button state
        lnlMyHomeOpt.setVisibility(swtMyHome.isChecked() ? View.VISIBLE : View.GONE);

        // Find the layout of switch button, and set onClick
        LinearLayout lnlMyHomeSwt = (LinearLayout) view.findViewById(R.id.lnlMyHomeSwt);
        lnlMyHomeSwt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Reverse its state (turn it off) and handle the switch state
                Boolean isOn = !swtMyHome.isChecked();
                swtMyHome.setChecked(isOn);
                handleSwtState(lnlMyHomeOpt, swtMyHome, isOn);
            }
        });

        //************************************ END TIME ********************************************
        // Find and cast the onClick for time start going out

        //Set the default time
        final Calendar calendarEnd = Calendar.getInstance(TimeZone.getDefault());
        calendarEnd.set(Calendar.HOUR_OF_DAY, 3);
        calendarEnd.set(Calendar.MINUTE, 0);

        txtMyHomeEndTime = (TextView) view.findViewById(R.id.txtMyHomeEndTime);
        txtMyHomeEndTime.setText(
                (Utils.shortTimeFormat.format(prefs.getLong(OurContract.PREF_MYHOME_END_TIME,
                        calendarEnd.getTimeInMillis()))));
        txtMyHomeEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.setUpTimePicker(txtMyHomeEndTime, calendarEnd,
                        getContext(), Utils.TIMEPICKER_CODE_RECORD_END);
            }
        });
        LinearLayout lnlMyHomeEndTime = (LinearLayout) view.findViewById(R.id.lnlMyHomeEndTime);
        lnlMyHomeEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.setUpTimePicker(txtMyHomeEndTime, calendarEnd,
                        getContext(), Utils.TIMEPICKER_CODE_RECORD_END);
                prefs.edit().putLong(OurContract.PREF_MYHOME_END_TIME,
                        calendarEnd.getTimeInMillis()).apply();
            }
        });

        //************************************ LUMINANCE ********************************************
        // Find and cast the onClick for Humidity Level
        final TextView txtMyHomeLightLevel = (TextView) view.findViewById(R.id.txtMyHomeLightLevel);
        txtMyHomeLightLevel.setText(
                String.valueOf(prefs.getInt(OurContract.PREF_MYHOME_MIN_LIGHT_VALUE,
                        OurContract.DEFAULT_MIN_LIGHT_VALUE)));
        txtMyHomeLightLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupSpinnerPicker(txtMyHomeLightLevel);
            }
        });
        LinearLayout lnlMyHomeLightLevel = (LinearLayout) view.findViewById(R.id.lnlMyHomeLightLevel);
        lnlMyHomeLightLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupSpinnerPicker(txtMyHomeLightLevel);
            }
        });

        //************************************ HUMIDITY ********************************************
        // Find and cast the onClick for Humidity Level
        final TextView txtMyHomeHumidityLevel = (TextView) view.findViewById(R.id.txtMyHomeHumidityLevel);
        txtMyHomeHumidityLevel.setText(
                String.valueOf(prefs.getInt(OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE,
                        OurContract.DEFAULT_MIN_HUMIDITY_VALUE)));
        txtMyHomeHumidityLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupNumberPicker(txtMyHomeHumidityLevel,
                        OurContract.MYHOME_MIN_HUMIDITY_MAXVALUE);
            }
        });
        LinearLayout lnlMyHomeHumidityLevel = (LinearLayout) view.findViewById(R.id.lnlMyHomeHumidityLevel);
        lnlMyHomeHumidityLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupNumberPicker(txtMyHomeHumidityLevel,
                        OurContract.MYHOME_MIN_HUMIDITY_MAXVALUE);
            }
        });

        //************************************ UPDATE INTERVAL *************************************
        // Find and cast the onClick for Update interval
        final TextView txtMyHomeNotfInterval = (TextView) view.findViewById(R.id.txtMyHomeNotfInterval);
        txtMyHomeNotfInterval.setText(
                String.valueOf(prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                        OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE)));
        txtMyHomeNotfInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupNumberPicker(txtMyHomeNotfInterval,
                        OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE);
            }
        });
        LinearLayout lnlMyHomeNotfInterval = (LinearLayout) view.findViewById(R.id.lnlMyHomeNotfInterval);
        lnlMyHomeNotfInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupNumberPicker(txtMyHomeNotfInterval,
                        OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE);
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Call the updateDisplayTV() again in case of new data
        fetchDataFromThingSee(OurContract.SENSOR_ID_HUMIDITY, getContext());
        fetchDataFromThingSee(OurContract.SENSOR_ID_TEMPERATURE, getContext());
        fetchDataFromThingSee(OurContract.SENSOR_ID_LUMINANCE, getContext());
    }

    /**
     * Cast all the display text, including temp, humid, light.
     * Each sensor have both time update and value
     */
    private void castDisplayTV() {
        txtTemperatureTime = (TextView) view.findViewById(R.id.txtTemperatureTime);
        txtTemperatureValue = (TextView) view.findViewById(R.id.txtTemperatureValue);
        txtHumidityTime = (TextView) view.findViewById(R.id.txtHumidityTime);
        txtHumidityValue = (TextView) view.findViewById(R.id.txtHumidityValue);
        txtLightTime = (TextView) view.findViewById(R.id.txtLightTime);
        txtLightValue = (TextView) view.findViewById(R.id.txtLightValue);
    }

    /**
     * Update the textview with data fetched from thingsee cloud.
     * Only update if the context is not null (it still valid)
     */
    private void updateDisplayTV(Context context) {
        txtHumidityTime.setText(prefs.getString(OurContract.PREF_HUMID_LATEST_TIME,
                context.getString(R.string.myhome_default_novalue)));
        txtHumidityValue.setText(prefs.getString(OurContract.PREF_HUMID_LATEST_VALUE,
                context.getString(R.string.myhome_default_novalue)));
        txtTemperatureTime.setText(prefs.getString(OurContract.PREF_TEMP_LATEST_TIME,
                context.getString(R.string.myhome_default_novalue)));
        txtTemperatureValue.setText(prefs.getString(OurContract.PREF_TEMP_LATEST_VALUE,
                context.getString(R.string.myhome_default_novalue)));

        txtLightTime.setText(prefs.getString(OurContract.PREF_LIGHT_LATEST_TIME,
                context.getString(R.string.myhome_default_novalue)));
        txtLightValue.setText(prefs.getString(OurContract.PREF_LIGHT_LATEST_VALUE,
                context.getString(R.string.myhome_default_novalue)));
    }

    /**
     * Fetch the data from ThingSee device by calling the {@link APIService} method's
     * {@link APIService
     * <p>
     * <p>
     * If we call all data at the same time, ThingSee may return only one data, which may
     * make it complicate to get all require data. That why we need to fetch each data separately.
     *
     * @param sensorID the SensorID of the data we need. Refer to
     *                 <a href="https://thingsee.zendesk.com/hc/en-us/articles/205133092-How-can-I-understand-the-info-displayed-in-senses-view-sensor-s-ID-">ThingSee documentation</a>
     * @param prefs    the intent used to pass data to this service
     */
    private void fetchDataFromThingSee(final String sensorID, final Context context) {
        final SharedPreferences prefs = context.getSharedPreferences(OurContract.SHARED_PREF,
                Context.MODE_PRIVATE);
        APIService apiService = Utils.getAPIService();
        apiService.getUserEvents(
                "Bearer " + prefs.getString(OurContract.PREF_USER_AUTH_TOKEN_NAME, ""),
                prefs.getString(OurContract.PREF_DEVICE_AUTH_ID_NAME, ""),
                null, sensorID, OurContract.MIN_FETCH_ITEM_TC, null, null).
                enqueue(new Callback<Events>() {
                    @Override
                    public void onResponse(Call<Events> call, Response<Events> response) {
                        Utils.handleOnResponse(response, prefs);
                        updateDisplayTV(context);
                    }

                    @Override
                    public void onFailure(Call<Events> call, Throwable t) {
                        Utils.handleFailure(context);
                    }
                });
    }

    /**
     * Setup a spinner picker to pick the min light level.
     * Then display the value on the textview and save in prefs
     *
     * @param textView The textView to display the results
     */
    private void setupSpinnerPicker(final TextView textView) {
        final String[] lightLabels =
                getResources().getStringArray(R.array.recommend_luminance_level_labels);
        final String[] lightValues =
                getResources().getStringArray(R.array.recommend_luminance_level_values);

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle(getString(R.string.myhome_dialog_title_light));
        dialogBuilder.setIcon(R.drawable.light);

        View dialogView = View.inflate(getContext(), R.layout.listview_spinner_dialog, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        ListView listView = (ListView) dialogView.findViewById(R.id.myhome_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_selectable_list_item, lightLabels);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                textView.setText(lightValues[position]);
                prefs.edit().putInt(OurContract.PREF_MYHOME_MIN_LIGHT_VALUE,
                        Integer.parseInt(lightValues[position])).apply();
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }

    /**
     * Handle the switch state of the switch button. As well as set prefs option based on the switch
     * and set notification
     *
     * @param lnlMyHomeOpt the layout contain the switch button
     * @param swtMyHome    the switch button
     * @param isOn         the state of the switch, if true then it's on
     */

    private void handleSwtState(LinearLayout lnlMyHomeOpt, Switch swtMyHome, boolean isOn) {
        prefs.edit().putBoolean(
                OurContract.PREF_MYHOME_NOTIFICATION_OPTION, isOn).apply();
        lnlMyHomeOpt.setVisibility(swtMyHome.isChecked() ? View.VISIBLE : View.GONE);
        if (isOn)
            updateNotification();
        else
            cancelAlarm(getContext());
    }

    /**
     * Set up the number picker. Set the textview with the result and record to prefs
     *
     * @param textView the textview for display the return value of number picker
     * @param maxValue the max value for number picker. This varies from
     *                 {@link OurContract#MYHOME_MIN_HUMIDITY_MAXVALUE} and
     *                 {@link OurContract#MYHOME_NOTIFICATION_INTERVAL_MAXVALUE}
     */

    private void setupNumberPicker(final TextView textView, final int maxValue) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        View dialogView = View.inflate(getContext(), R.layout.number_picker_dialog, null);
        dialog.setView(dialogView);

        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(MIN_VALUE);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setWrapSelectorWheel(true);

        // Display the number picker correctly depend on the option:
        if (maxValue == OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE) {
            dialog.setTitle(getString(R.string.myhome_dialog_title_notfinterval));
            numberPicker.setValue(prefs.getInt(
                    OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                    OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE));
        } else {
            dialog.setTitle(getString(R.string.myhome_dialog_title_humidity));
            numberPicker.setValue(prefs.getInt(
                    OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE,
                    OurContract.DEFAULT_MIN_HUMIDITY_VALUE));
        }

        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                textView.setText(String.valueOf(numberPicker.getValue()));

                // If the max value is for notification interval,
                // then we know the setting is for notification interval,
                // then we need to record it properly in prefs.
                if (maxValue == OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE) {
                    prefs.edit().putInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                            numberPicker.getValue()).apply();
                } else {
                    prefs.edit().putInt(OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE,
                            numberPicker.getValue()).apply();
                }

                updateNotification();
            }
        });
        dialog.setNegativeButton(android.R.string.no, null);
        dialog.show();
    }

    /**
     * Set the repeating alarm based on the setting,
     * is the notification option is off then cancel all alarm
     */
    private void updateNotification() {
        cancelAlarm(getContext());
        /*
        * Creates a new Intent to start the TCCloudRequestService
        * IntentService.
        */
        Intent intent = new Intent(getContext().getApplicationContext(), TCCLoudRequestReceiver.class);

        // Create a PendingIntent to send the service
        // Set the flag update current to update with new setting
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(),
                OurContract.INTENT_REQUEST_CODE_MYHOMESERVICE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the alarmManager to set the repeating task
        alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                        + prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000,
                prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                        OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000, pendingIntent);
    }

    public static void cancelAlarm(Context context) {
        Log.e("Giang","canceled");
        Intent intent = new Intent(context.getApplicationContext(), TCCLoudRequestReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context,
                OurContract.INTENT_REQUEST_CODE_MYHOMESERVICE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    /**
     * Need to put it to static and put on AndroidManifest for it to run after app closed
     */
    public static class TCCLoudRequestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent i = new Intent(context, TCCloudRequestService.class);
            context.startService(i);
        }
    }
}
