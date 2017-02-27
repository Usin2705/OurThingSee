package metro.ourthingsee.activities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import java.sql.Date;
import java.util.Calendar;
import java.util.TimeZone;

import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.TCCloudRequestService;
import metro.ourthingsee.Utils;

/**
 * Activity to display sensor data (temp, humidity and light).
 * As well as make notification settings
 */
public class MyHomeActivity extends AppCompatActivity {
    private static final int MIN_VALUE = 1;

    /**
     * If the sensor timestamp is older than the current time with this amount, no notification
     * will be send.
     * Set at 2 hours
     */
    private static final int ELAPSE_TIME = 1000 * 60 * 60 * 2;

    static SharedPreferences prefs;
    static TextView txtTemperatureTime, txtTemperatureValue, txtHumidityTime, txtHumidityValue,
            txtLightTime, txtLightValue;
    TCCLoudRequestReceiver receiver;
    AlarmManager alarmManager;

    /**
     * Send the notification to user. It need to be static so it can be called by our
     * static receiver {@link TCCLoudRequestReceiver}.
     *
     * @param context       the context of the app, used to get resources
     * @param longTimestamp the Timestamp of the sensor value, in Long milliseconds
     * @param strContent    the content of the notification to be send
     * @param sensorType    the notification id
     */
    public static void sendNotification(Context context, Long longTimestamp, String strContent,
                                        int sensorType) {
        Intent ntfIntent = new Intent(context, MyHomeActivity.class);
        ntfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                ntfIntent, 0);

        //Get an instance of notification
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context)
                        .setSmallIcon((sensorType == OurContract.NOTIFICATION_ID_HUMIDITY)
                                ? R.drawable.nature
                                : R.drawable.ic_lightbulb_outline_24dp)
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
    private static boolean isNotQuietTime(Context context) {
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

    /**
     * Update the textview with data fetched from thingsee cloud.
     * Only update if the context is not null (it still valid)
     *
     * @param context the context of the activity
     */
    private static void updateDisplayTV(Context context) {
        if (context != null && txtHumidityTime != null) {
            try {

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

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myhome);

        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);

        // Setup the toolbar
        setupToolBar();

        // Cast all the display texts
        castDisplayTV();

        // Update all the display texts with latest value. Call after prefs since we will update
        // from prefs.
        updateDisplayTV(MyHomeActivity.this);

        final LinearLayout lnlMyHomeOpt = (LinearLayout) findViewById(R.id.lnlMyHomeOption);

        //************************************ NOTIFICATION OPTION *********************************
        // Find the switch button, set it according to prefs, and set onCheckChangeListener
        final Switch swtMyHome = (Switch) findViewById(R.id.swtMyHome);
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
        LinearLayout lnlMyHomeSwt = (LinearLayout) findViewById(R.id.lnlMyHomeSwt);
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
        calendarEnd.set(Calendar.HOUR_OF_DAY, 19);
        calendarEnd.set(Calendar.MINUTE, 0);

        final TextView txtMyHomeEndTime = (TextView) findViewById(R.id.txtMyHomeEndTime);
        txtMyHomeEndTime.setText(
                (Utils.shortTimeFormat.format(prefs.getLong(OurContract.PREF_MYHOME_END_TIME,
                        calendarEnd.getTimeInMillis()))));
        txtMyHomeEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.setUpTimePicker(txtMyHomeEndTime, calendarEnd,
                        MyHomeActivity.this, Utils.TIMEPICKER_CODE_RECORD_END);
            }
        });
        LinearLayout lnlMyHomeEndTime = (LinearLayout) findViewById(R.id.lnlMyHomeEndTime);
        lnlMyHomeEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utils.setUpTimePicker(txtMyHomeEndTime, calendarEnd,
                        MyHomeActivity.this, Utils.TIMEPICKER_CODE_RECORD_END);
                prefs.edit().putLong(OurContract.PREF_MYHOME_END_TIME,
                        calendarEnd.getTimeInMillis()).apply();
            }
        });

        //************************************ LUMINANCE ********************************************
        // Find and cast the onClick for Humidity Level
        final TextView txtMyHomeLightLevel = (TextView) findViewById(R.id.txtMyHomeLightLevel);
        txtMyHomeLightLevel.setText(
                String.valueOf(prefs.getInt(OurContract.PREF_MYHOME_MIN_LIGHT_VALUE,
                        OurContract.DEFAULT_MIN_LIGHT_VALUE)));
        txtMyHomeLightLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupSpinnerPicker(txtMyHomeLightLevel);
            }
        });
        LinearLayout lnlMyHomeLightLevel = (LinearLayout) findViewById(R.id.lnlMyHomeLightLevel);
        lnlMyHomeLightLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupSpinnerPicker(txtMyHomeLightLevel);
            }
        });

        //************************************ HUMIDITY ********************************************
        // Find and cast the onClick for Humidity Level
        final TextView txtMyHomeHumidityLevel = (TextView) findViewById(R.id.txtMyHomeHumidityLevel);
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
        LinearLayout lnlMyHomeHumidityLevel = (LinearLayout) findViewById(R.id.lnlMyHomeHumidityLevel);
        lnlMyHomeHumidityLevel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupNumberPicker(txtMyHomeHumidityLevel,
                        OurContract.MYHOME_MIN_HUMIDITY_MAXVALUE);
            }
        });

        //************************************ UPDATE INTERVAL *************************************
        // Find and cast the onClick for Update interval
        final TextView txtMyHomeNotfInterval = (TextView) findViewById(R.id.txtMyHomeNotfInterval);
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
        LinearLayout lnlMyHomeNotfInterval = (LinearLayout) findViewById(R.id.lnlMyHomeNotfInterval);
        lnlMyHomeNotfInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setupNumberPicker(txtMyHomeNotfInterval,
                        OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE);
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

        final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.myhome_dialog_title_light));
        dialogBuilder.setIcon(R.drawable.ic_lightbulb_outline_24dp);

        View dialogView = View.inflate(this, R.layout.listview_spinner_dialog, null);
        dialogBuilder.setView(dialogView);
        final AlertDialog alertDialog = dialogBuilder.create();

        ListView listView = (ListView) dialogView.findViewById(R.id.myhome_listview);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(MyHomeActivity.this,
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
     * Cast all the display text, including temp, humid, light.
     * Each sensor have both time update and value
     */
    private void castDisplayTV() {
        txtTemperatureTime = (TextView) this.findViewById(R.id.txtTemperatureTime);
        txtTemperatureValue = (TextView) this.findViewById(R.id.txtTemperatureValue);
        txtHumidityTime = (TextView) this.findViewById(R.id.txtHumidityTime);
        txtHumidityValue = (TextView) this.findViewById(R.id.txtHumidityValue);
        txtLightTime = (TextView) this.findViewById(R.id.txtLightTime);
        txtLightValue = (TextView) this.findViewById(R.id.txtLightValue);
    }

    /**
     * Set up the toolbar, with a Navigation button which return to previous activity
     * (similar to onBackPress)
     */
    private void setupToolBar() {
        Toolbar toolbar = (Toolbar) this.findViewById(R.id.tb_main);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        toolbar.setTitle(prefs.getString(OurContract.PREF_DEVICE_NAME,
                getString(R.string.unknown_device)));

    }

    @Override
    public void onResume() {
        super.onResume();

        // Call the updateDisplayTV() again in case of new data
        updateDisplayTV(MyHomeActivity.this);

        // Create the receiver again, since we unregister it in onPause
//        IntentFilter filter = new IntentFilter(OurContract.BROADCAST_ACTION);
//        filter.addCategory(Intent.CATEGORY_DEFAULT);
//        receiver = new TCCLoudRequestReceiver();
//        registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
//        this.unregisterReceiver(receiver);
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
        updateNotification();
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
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        View dialogView = View.inflate(this, R.layout.number_picker_dialog, null);
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
        /*
        * Creates a new Intent to start the TCCloudRequestService
        * IntentService.
        */
        Intent serviceIntent = new Intent(getBaseContext(), TCCloudRequestService.class);

        // Create a PendingIntent to send the service
        // Set the flag update current to update with new setting
        PendingIntent pendingIntent = PendingIntent.getService(this,
                OurContract.INTENT_REQUEST_CODE_MYHOMESERVICE, serviceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the alarmManager to set the repeating task
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // Wake up the device to fire the alarm in 15 minutes, and every 15 minutes after that:
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                                OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000,
                prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                        OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000,
                pendingIntent);

        IntentFilter filter = new IntentFilter(OurContract.BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new TCCLoudRequestReceiver();
        registerReceiver(receiver, filter);
    }

    /**
     * Need to put it to static and put on AndroidManifest for it to run after app closed
     */
    public static class TCCLoudRequestReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            handleOnReceive(intent, context, OurContract.BROADCAST_RESPONSE_TIMESTAMP,
                    OurContract.BROADCAST_RESPONSE_VALUE);

            // Call the updateDisplayTV() again in case of new data
            updateDisplayTV(context);
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

            Log.e("AAAAA", "Get value" + String.valueOf(dbResponse));
            Log.e("AAAAA", "Get timestamp" + String.valueOf(longTimestamp));

            // If prefsGiang is not null, and both timestamp and double value are not 0
            if (dbResponse != -100d && longTimestamp != -100L) {
                switch (sensorID) {
                    case OurContract.SENSOR_ID_HUMIDITY:
                        prefsGiang.edit().putString(OurContract.PREF_HUMID_LATEST_TIME,
                                String.valueOf(Utils.dateFormat.format(eventDate))).apply();
                        prefsGiang.edit().putString(OurContract.PREF_HUMID_LATEST_VALUE,
                                String.valueOf(dbResponse) + " %").apply();

                        // If the value is less than the min value, notify the user
                        // only notify if the notification option is turned on
                        if ((dbResponse < prefsGiang.getInt(
                                OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE,
                                OurContract.DEFAULT_MIN_HUMIDITY_VALUE)) && prefsGiang.getBoolean(
                                OurContract.PREF_MYHOME_NOTIFICATION_OPTION,
                                OurContract.DEFAULT_NOTIFICATION_OPTION)) {

                            Log.e("AAAAA", "Get value to send notification" + String.valueOf(dbResponse));
                            Log.e("AAAAA", "Get timestamp" + String.valueOf(longTimestamp));
                            String strNotf = context.getString
                                    (R.string.notification_humidity, dbResponse);
                            strNotf += "%. " + Utils.shortDateFormat.format(longTimestamp);
                            sendNotification(context, longTimestamp, strNotf,
                                    OurContract.NOTIFICATION_ID_HUMIDITY);
                        }

                        break;

                    case OurContract.SENSOR_ID_TEMPERATURE:
                        prefsGiang.edit().putString(OurContract.PREF_TEMP_LATEST_TIME,
                                String.valueOf(Utils.dateFormat.format(eventDate))).apply();
                        prefsGiang.edit().putString(OurContract.PREF_TEMP_LATEST_VALUE,
                                String.valueOf(dbResponse) + " \u2103").apply();
                        break;

                    case OurContract.SENSOR_ID_LUMINANCE:
                        prefsGiang.edit().putString(OurContract.PREF_LIGHT_LATEST_TIME,
                                String.valueOf(Utils.dateFormat.format(eventDate))).apply();
                        prefsGiang.edit().putString(OurContract.PREF_LIGHT_LATEST_VALUE,
                                String.valueOf(dbResponse) + "lux").apply();

                        // If the value is less than the min value, notify the user
                        // only notify if the notification option is turned on
                        if ((dbResponse < prefsGiang.getInt(
                                OurContract.PREF_MYHOME_MIN_LIGHT_VALUE,
                                OurContract.DEFAULT_MIN_LIGHT_VALUE)) && prefsGiang.getBoolean(
                                OurContract.PREF_MYHOME_NOTIFICATION_OPTION,
                                OurContract.DEFAULT_NOTIFICATION_OPTION)) {
                            Log.e("AAAAA", "Get value to send notification light" + String.valueOf(dbResponse));
                            Log.e("AAAAA", "Get timestamp light" + String.valueOf(longTimestamp));

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
    }
}
