package metro.ourthingsee.activities;

/**
 * Created by Jingxuaw on 17.2.2017.
 */

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import metro.ourthingsee.R;

;

public class AboutUs extends AppCompatActivity implements View.OnClickListener {
    Button btn_Back;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us);
        btn_Back = (Button) findViewById(R.id.btnBack);
        btn_Back.setOnClickListener(this);

        Glide.with(this).load(R.drawable.about).asBitmap().diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).animate(android.R.anim.fade_in).approximate()
                .into((ImageView)findViewById(R.id.imgAboutUs));
    }

    @Override

    // If click "back" , it will turn to "MainActivity" layout.
    public void onClick(View v) {
        if (btn_Back.isClickable()) {
            Intent intentAboutUs = new Intent(AboutUs.this, MainActivity.class);
            startActivity(intentAboutUs);
            finish();
        }
    }
}
