package metro.ourthingsee;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import metro.ourthingsee.POSTs.Authentication;
import metro.ourthingsee.remote.APIService;
import metro.ourthingsee.remote.AppUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRetrofit2Activity extends AppCompatActivity {
    private static final String TAG = "Retrofit2";
    EditText et_email;
    EditText et_password;
    Button btn_login;
    TextView tv_info;
    APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        addControls();
        addEvents();
    }

    private void addControls() {
        et_email = (EditText) findViewById(R.id.et_email);
        et_email.setText("");
        et_password = (EditText) findViewById(R.id.et_pw);
        et_password.setText("");
        btn_login = (Button) findViewById(R.id.btn_login);
        tv_info = (TextView) findViewById(R.id.tv_info);
        apiService = AppUtils.getAPIService();
    }

    private void addEvents() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = et_email.getText().toString();
                String password = et_password.getText().toString();
                sendPost(email, password);
            }
        });
    }

    public void sendPost(final String email, String password) {
        apiService.savePost(email, password).enqueue(new Callback<Authentication>() {
            @Override
            public void onResponse(Call<Authentication> call, Response<Authentication> response) {
                Log.i(TAG, response.code() + "");
                if(response.isSuccessful()){
                    showResponse(response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<Authentication> call, Throwable t) {

            }
        });
    }

    public void showResponse(String response) {
        tv_info.setText(response);
    }
}
