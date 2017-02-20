package metro.ourthingsee.activities;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
    private TCCLoudRequestReceiver receiver;
    private AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myhome);

        // Get the alarmManager to set the repeating task
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);


        /*
        * Creates a new Intent to start the TCCloudRequestService
        * IntentService.
        */
        Intent serviceIntent = new Intent(this, TCCloudRequestService.class);

        // Create a PendingIntent to send the service
        PendingIntent pendingIntent = PendingIntent.getService(this,
                OurContract.INTENT_REQUEST_CODE_MYHOMESERVICE, serviceIntent, 0);

        // Wake up the device to fire the alarm in 15 minutes, and every 15 minutes after that:
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + 60000,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);

        IntentFilter filter = new IntentFilter(OurContract.BROADCAST_ACTION);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new TCCLoudRequestReceiver();
        registerReceiver(receiver, filter);

    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(receiver);
        super.onDestroy();
    }

    public void sendNotification(String strValue) {
        //Get an instance of NotificationManager//
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.nature)
                        .setContentTitle("My notification")
                        .setContentText("ALO ALO: " + strValue);

        // Set the default value (vibrate, sound, light, color)
        // If  your phone is set on DO NOT DISTURB MODEL all sound and stuff won't work
        notification.setDefaults(Notification.DEFAULT_ALL);

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

            Log.e("AAAAA", "OnReceive run");

            TextView myTextView1 = (TextView) findViewById(R.id.textView1);
            TextView myTextView2 = (TextView) findViewById(R.id.textView2);


            myTextView1.setText(dateFormat.format(eventDate));
            myTextView2.setText(String.valueOf(dbResponse));
            sendNotification(String.valueOf(dateFormat.format(eventDate) + " " + dbResponse));
        }
    }

}
