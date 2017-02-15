package metro.ourthingsee;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

/**
 * Created by Usin on 15-Feb-17.
 */

public class MyHomeActivity extends AppCompatActivity {
    private TCCLoudRequestReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myhome);

        /*
        * Creates a new Intent to start the TCCloudRequestService
        * IntentService.
        */
        Intent serviceIntent = new Intent(this, TCCloudRequestService.class);
        startService(serviceIntent);

        // TODO CREATE ALARM MANAGER TO AUTOMATIC UPDATE

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

    public class TCCLoudRequestReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Double dbResponse = intent.getDoubleExtra(OurContract.BROADCAST_RESPONSE_VALUE, 0);
            Long longTimestamp = intent.getLongExtra(OurContract.BROADCAST_RESPONSE_TIMESTAMP, 0);

            Date eventDate = new Date(longTimestamp);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy kk:mm:ss");

            TextView myTextView1 = (TextView) findViewById(R.id.textView1);
            TextView myTextView2 = (TextView) findViewById(R.id.textView2);


            myTextView1.setText(dateFormat.format(eventDate));
            myTextView2.setText(String.valueOf(dbResponse));

        }
    }
}
