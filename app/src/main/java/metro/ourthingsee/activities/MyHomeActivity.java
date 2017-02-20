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

/**
 * Created by Usin on 15-Feb-17.
 */

public class MyHomeActivity extends AppCompatActivity {
    private static final int MIN_VALUE = 0;
    private static final int DEFAULT_NOTIFICATION_INTERVAL_VALUE = 15;
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
    TCCLoudRequestReceiver receiver;
    AlarmManager alarmManager;
    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myhome);

        prefs = getSharedPreferences(OurContract.SHARED_PREF, Context.MODE_PRIVATE);

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
                Boolean bolSwtState = swtMyHome.isChecked() ? false : true;
                swtMyHome.setChecked(bolSwtState);
                handleSwtState(lnlMyHomeOpt, swtMyHome, bolSwtState);
            }
        });

        final TextView txtMyHomeNotfInterval = (TextView) findViewById(R.id.txtMyHomeNotfInterval);
        txtMyHomeNotfInterval.setText(
                String.valueOf(prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                        DEFAULT_NOTIFICATION_INTERVAL_VALUE)));
        txtMyHomeNotfInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpNumberPicker(txtMyHomeNotfInterval,
                        OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE);
            }
        });
        LinearLayout lnlMyHomeNotfInterval = (LinearLayout) findViewById(R.id.lnlMyHomeNotfInterval);
        lnlMyHomeNotfInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpNumberPicker(txtMyHomeNotfInterval,
                        OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE);
            }
        });

        /*
        * Creates a new Intent to start the TCCloudRequestService
        * IntentService.
        */
        Intent serviceIntent = new Intent(this, TCCloudRequestService.class);

        // Create a PendingIntent to send the service
        // Set the flag update current to update with new setting
        PendingIntent pendingIntent = PendingIntent.getService(this,
                OurContract.INTENT_REQUEST_CODE_MYHOMESERVICE, serviceIntent,
                0);


        // Get the alarmManager to set the repeating task
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (true) {
            // Wake up the device to fire the alarm in 15 minutes, and every 15 minutes after that:
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + prefs.getInt(
                            OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                            DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000,
                    prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                            DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000, pendingIntent);
        } else {
            // If user turn off notification, turn off all alarm.
            Log.e("AAAAA", "ALARM WAS CANCEL");
            alarmManager.cancel(pendingIntent);
        }

        IntentFilter filter = new IntentFilter(OurContract.BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new TCCLoudRequestReceiver();
        registerReceiver(receiver, filter);
    }

    /**
     * Handle the switch state of the switch button. As well as set prefs option based on the switch
     * and set notification
     *
     * @param lnlMyHomeOpt the layout contain the switch button
     * @param swtMyHome    the switch button
     * @param bolSwtState  the state of the switch
     */

    private void handleSwtState(LinearLayout lnlMyHomeOpt, Switch swtMyHome, boolean bolSwtState) {
        prefs.edit().putBoolean(
                OurContract.PREF_MYHOME_NOTIFICATION_OPTION, bolSwtState).apply();
        lnlMyHomeOpt.setVisibility(swtMyHome.isChecked() ? View.VISIBLE : View.GONE);
        setNotification(bolSwtState);
    }

    /**
     * Set up the number picker. Set the textview with the result and record to prefs
     *
     * @param textView the textview for display the return value of number picker
     * @param maxValue the max value for number picker. This varies from
     *                 {@link OurContract#MYHOME_MIN_HUMIDITY_LEVEL} and
     *                 {@link OurContract#MYHOME_NOTIFICATION_INTERVAL_MAXVALUE}
     */

    private void setUpNumberPicker(final TextView textView, int maxValue) {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.number_picker_dialog, null);
        dialog.setView(dialogView);

        if (maxValue == OurContract.MYHOME_NOTIFICATION_INTERVAL_MAXVALUE) {
            dialog.setTitle("Select your notification interval (min)");
        } else {
            dialog.setTitle("Select your minimum humidity level (%)");
        }

        final NumberPicker numberPicker = (NumberPicker) dialogView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(MIN_VALUE);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setValue(prefs.getInt(
                OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL, DEFAULT_NOTIFICATION_INTERVAL_VALUE));
        numberPicker.setWrapSelectorWheel(true);

        dialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                textView.setText(String.valueOf(numberPicker.getValue()));
                prefs.edit().putInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                        numberPicker.getValue()).apply();
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

        // Create a PendingIntent to send the service
        // Set the flag update current to update with new setting
        PendingIntent pendingIntent = PendingIntent.getService(this,
                OurContract.INTENT_REQUEST_CODE_MYHOMESERVICE, serviceIntent,
                0);


        // Get the alarmManager to set the repeating task
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Log.e("AAAAA","BOOLEAN STATE: " + String.valueOf(isOn));

        if (isOn) {
            // Wake up the device to fire the alarm in 15 minutes, and every 15 minutes after that:
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + prefs.getInt(
                            OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                            DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000,
                    prefs.getInt(OurContract.PREF_MYHOME_NOTIFICATION_INTERVAL,
                            DEFAULT_NOTIFICATION_INTERVAL_VALUE) * 60 * 1000, pendingIntent);
        } else {
            // If user turn off notification, turn off all alarm.
            Log.e("AAAAA", "ALARM WAS CANCEL");
            alarmManager.cancel(pendingIntent);
        }
    }


    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void sendNotification(String strValue) {
        Log.e("AAAAA", "NOTIFICATION SEND CAI BUM");
        //Get an instance of NotificationManager//
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.nature)
                        .setContentTitle("My notification")
                        .setContentText("ALO ALO: " + strValue);

        // Set the default value (vibrate, sound, light)
        // If  your phone is set on DO NOT DISTURB MODEL all sound and stuff won't work
        notification.setDefaults(Notification.DEFAULT_ALL);

        // Set background color to transparent (so the small icon look better)
        notification.setColor(ContextCompat.getColor(this, R.color.colorTransparent));

        // Gets an instance of the NotificationManager service//
        NotificationManager mNotificationManager =

                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        //When you issue multiple notifications about the same type of event, it’s best practice
        // for your app to try to update an existing notification with this new information, rather
        // than immediately creating a new notification. If you want to update this notification at
        // a later date, you need to assign it an ID. You can then use this ID whenever you issue a
        // subsequent notification. If the previous notification is still visible, the system will
        // update this existing notification, rather than create a new one.
        mNotificationManager.notify(OurContract.NOTIFICATION_ID_HUMIDITY, notification.build());
    }

    public class TCCLoudRequestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Double dbResponse = intent.getDoubleExtra(OurContract.BROADCAST_RESPONSE_VALUE, 0);
            Long longTimestamp = intent.getLongExtra(OurContract.BROADCAST_RESPONSE_TIMESTAMP, 0);

            Date eventDate = new Date(longTimestamp);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy kk:mm:ss");

            Log.e("AAAAA", "DA RECEIVE OnReceive run");

            TextView myTextView1 = (TextView) findViewById(R.id.txtHumidityTime);
            TextView myTextView2 = (TextView) findViewById(R.id.txtHumidityValue);


            myTextView1.setText(dateFormat.format(eventDate));
            myTextView2.setText(String.valueOf(dbResponse));
            sendNotification(String.valueOf(dateFormat.format(eventDate) + " " + dbResponse));
        }
    }

}
