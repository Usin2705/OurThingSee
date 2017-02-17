package metro.ourthingsee.activities;

/**
 * Created by Jingxuaw on 17.2.2017.
 */

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import metro.ourthingsee.R;

public class AboutUs extends AppCompatActivity implements View.OnClickListener {
    Button btnBack = (Button) findViewById(R.id.back);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        btnBack.setOnClickListener(this);
    }

    @Override

    // If click "back" , it will turn to "MainActivity" layout.
    public void onClick(View v) {
        if (btnBack.isClickable()) {
            Intent intentAboutUs = new Intent(AboutUs.this, MainActivity.class);
            startActivityForResult(intentAboutUs, 1);
            finish();

        }


    }
}
