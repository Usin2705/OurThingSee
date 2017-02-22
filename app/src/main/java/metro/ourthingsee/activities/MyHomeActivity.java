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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;

import java.sql.Date;
import java.text.SimpleDateFormat;

import metro.ourthingsee.OurContract;
import metro.ourthingsee.R;
import metro.ourthingsee.TCCloudRequestService;
import metro.ourthingsee.Utils;

/**
 * Created by Usin on 15-Feb-17.
 */

public class MyHomeActivity extends AppCompatActivity {
    private static final int MIN_VALUE = 0;
    static SharedPreferences prefs;
    static TextView txtTemperatureTime, txtTemperatureValue, txtHumidityTime, txtHumidityValue,
            txtLightTime, txtLightValue;
    private static Context staticContext;
    TCCLoudRequestReceiver receiver;
    AlarmManager alarmManager;

    /**
     * Send the notification to user. It need to be static so it can be called by our
     * static receiver {@link TCCLoudRequestReceiver}.
     *
     * @param context  the context of the app, used to get resources
     * @param strValue the value to display
     */
    public static void sendNotification(Context context, String strValue) {
        Intent ntfIntent = new Intent(context, MyHomeActivity.class);
        ntfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                ntfIntent, 0);

        //Get an instance of notification
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.nature)
                        .setContentTitle("My notification")
                        .setContentText("ALO ALO: " + strValue)
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

        //When you issue multiple notifications about the same type of event, it’s best practice
        // for your app to try to update an existing notification with this new information, rather
        // than immediately creating a new notification. If you want to update this notification at
        // a later date, you need to assign it an ID. You can then use this ID whenever you issue a
        // subsequent notification. If the previous notification is still visible, the system will
        // update this existing notification, rather than create a new one.
        mNotificationManager.notify(OurContract.NOTIFICATION_ID_HUMIDITY, notification.build());
    }

    private static void updateDisplayTV() {
        txtHumidityTime.setText(prefs.getString(OurContract.PREF_HUMID_LATEST_TIME,
                staticContext.getString(R.string.myhome_default_novalue)));
        txtHumidityValue.setText(prefs.getString(OurContract.PREF_HUMID_LATEST_VALUE,
                staticContext.getString(R.string.myhome_default_novalue)));

        txtTemperatureTime.setText(prefs.getString(OurContract.PREF_TEMP_LATEST_TIME,
                staticContext.getString(R.string.myhome_default_novalue)));
        txtTemperatureValue.setText(prefs.getString(OurContract.PREF_TEMP_LATEST_VALUE,
                staticContext.getString(R.string.myhome_default_novalue)));

        txtLightTime.setText(prefs.getString(OurContract.PREF_LIGHT_LATEST_TIME,
                staticContext.getString(R.string.myhome_default_novalue)));
        txtLightValue.setText(prefs.getString(OurContract.PREF_LIGHT_LATEST_VALUE,
                staticContext.getString(R.string.myhome_default_novalue)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myhome);
        staticContext = this;

        // Setup the toolbar
        setupToolBar();

        // Cast all the display texts
        castDisplayTV();

        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);

        // Update all the display texts with latest value. Call after prefs since we will update
        // from prefs.
        updateDisplayTV();

        final LinearLayout lnlMyHomeOpt = (LinearLayout) findViewById(R.id.lnlMyHomeOption);

        // Find the switch button, set it according to prefs, and set onCheckChangeListener
        final Switch swtMyHome = (Switch) findViewById(R.id.swtMyHome);
        swtMyHome.setChecked(prefs.getBoolean(OurContract.PREF_MYHOME_NOTIFICATION_OPTION, false));
        swtMyHome.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean bolSwtState) {
                handleSwtState(lnlMyHomeOpt, swtMyHome, bolSwtState);
            }
        });

        // Display the layout option or not depend on the switch button state
        lnlMyHomeOpt.setVisibility(swtMyHome.isChecked() ? View.VISIBLE : View.GONE);

        // Find the layout of switch button, set it according to prefs, and set onClick
        LinearLayout lnlMyHomeSwt = (LinearLayout) findViewById(R.id.lnlMyHomeSwt);
        lnlMyHomeSwt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean isOn = swtMyHome.isChecked() ? false : true;
                swtMyHome.setChecked(isOn);
                handleSwtState(lnlMyHomeOpt, swtMyHome, isOn);
            }
        });

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


        // Find and cast the onClick for Notification interval
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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

        // Call the updateDisplayTV() again in case of new data
        updateDisplayTV();

        // Create the receiver again, since we unregister it in onPause
        //IntentFilter filter = new IntentFilter(OurContract.BROADCAST_ACTION);
        //filter.addCategory(Intent.CATEGORY_DEFAULT);
        //receiver = new TCCLoudRequestReceiver();
        //registerReceiver(receiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        //this.unregisterReceiver(receiver);
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
        setNotification(isOn);
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
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);
        dialog.setView(dialogView);

        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(MIN_VALUE);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setWrapSelectorWheel(true);

        // Display the number picker correctly depend on the option:
        if (maxValue == OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE) {
            dialog.setTitle("Select your notification interval (min)");
            numberPicker.setValue(prefs.getInt(
                    OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                    OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE));
        } else {
            dialog.setTitle("Select your minimum humidity level (%)");
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

                setNotification(true);
            }
        });
        dialog.setNegativeButton(android.R.string.no, null);
        dialog.show();
    }

    /**
     * Set the repeating alarm based on the setting,
     * is the notification option is off then cancal all alarm
     *
     * @param isOn the state of the notification, is it on
     */
    private void setNotification(Boolean isOn) {
        /*
        * Creates a new Intent to start the TCCloudRequestService
        * IntentService.
        */
        Intent serviceIntent = new Intent(this, TCCloudRequestService.class);
        serviceIntent.putExtra(OurContract.INTENT_NAME_MIN_HUMIDITY_VALUE,
                prefs.getInt(OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE,
                        OurContract.DEFAULT_MIN_HUMIDITY_VALUE));

        int minHumid = prefs.getInt(OurContract.PREF_MYHOME_MIN_HUMIDITY_VALUE, 30);
        serviceIntent.putExtra(OurContract.INTENT_NAME_MIN_HUMIDITY_VALUE, minHumid);
        Log.e("AAAAA", "put extra in intent: " + String.valueOf(minHumid));

        // Create a PendingIntent to send the service
        // Set the flag update current to update with new setting
        PendingIntent pendingIntent = PendingIntent.getService(this,
                OurContract.INTENT_REQUEST_CODE_MYHOMESERVICE, serviceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        // Get the alarmManager to set the repeating task
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            // Wake up the device to fire the alarm in 15 minutes, and every 15 minutes after that:
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() +
                            prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                                    OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000,
                    prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                            OurContract.DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000,
                    pendingIntent);
        } else {
            // If user turn off notification, turn off all alarm.
            alarmManager.cancel(pendingIntent);
        }

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
            if (staticContext != null) {
                updateDisplayTV();
            }
        }

        /**
         * Handle the onReceive of BroadcastReceiver here.
         * This will record the last timestamp and value to prefs.
         * Also it will handle the notification (if set) for each sensorID.
         *
         * @param intent the intent we get from the Broadcast. We need to get sensorID from this
         *               intent to switch case based on the data we get.
         * @param context the context of the app, for sending notification
         * @param tsName the Broadcast Response Name contain timestamp value in Long
         * @param vlName the Broadcast Response Name contain sensor value in Double
         */
        private void handleOnReceive(Intent intent, Context context, String tsName, String vlName) {
            // If the intent have the extra value for tsName, then we take the value and process,
            // else we stop.
            Long longTimestamp = intent.getLongExtra(tsName, 0);
            Double dbResponse = intent.getDoubleExtra(vlName, 0d);

            Date eventDate = new Date(longTimestamp);

            String sensorID = intent.getStringExtra(OurContract.BROADCAST_RESPONSE_SENSORID);

            // If prefs is not null, and both timestamp and double value are not 0
            if (prefs != null && dbResponse != 0d && longTimestamp != 0) {
                switch (sensorID) {
                    case OurContract.SENSOR_ID_HUMIDITY:
                        prefs.edit().putString(OurContract.PREF_HUMID_LATEST_TIME,
                                String.valueOf(Utils.dateFormat.format(eventDate))).apply();
                        prefs.edit().putString(OurContract.PREF_HUMID_LATEST_VALUE,
                                String.valueOf(dbResponse) + " %").apply();
                        // If the value is less than the min value, notify the user
                        if (dbResponse < intent.getIntExtra(
                                OurContract.INTENT_NAME_MIN_HUMIDITY_VALUE,
                                OurContract.DEFAULT_MIN_HUMIDITY_VALUE)) {
                            sendNotification(context, String.valueOf(Utils.dateFormat.format(eventDate) + " " + dbResponse));
                        }

                        break;

                    case OurContract.SENSOR_ID_TEMPERATURE:
                        prefs.edit().putString(OurContract.PREF_TEMP_LATEST_TIME,
                                String.valueOf(Utils.dateFormat.format(eventDate))).apply();
                        prefs.edit().putString(OurContract.PREF_TEMP_LATEST_VALUE,
                                String.valueOf(dbResponse) + " \u2103").apply();
                        break;

                    case OurContract.SENSOR_ID_LUMINANCE:
                        prefs.edit().putString(OurContract.PREF_LIGHT_LATEST_TIME,
                                String.valueOf(Utils.dateFormat.format(eventDate))).apply();
                        prefs.edit().putString(OurContract.PREF_LIGHT_LATEST_VALUE,
                                String.valueOf(dbResponse) + " lux").apply();
                        break;

                    default:
                        break;
                }
            }
        }
    }
}
