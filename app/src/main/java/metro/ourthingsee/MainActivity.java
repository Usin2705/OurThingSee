package metro.ourthingsee;

import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<String> {

    private static final String THINGSEE_URL = "http://api.thingsee.com/v2/accounts/login";
    private static final int NEWS_LOADER_ID = 1;
    RequestQueue mRequestQueue;


    TextView txtTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnButton = (Button) findViewById(R.id.btnButton);
        TextView txtTextView = (TextView) findViewById(R.id.txtTextView);

        btnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Instantiate the cache
                Cache cache = new DiskBasedCache(getCacheDir(), 1024 * 1024); // 1MB cap

                // Set up the network to use HttpURLConnection as the HTTP client.
                Network network = new BasicNetwork(new HurlStack());

                // Instantiate the RequestQueue with the cache and network.
                mRequestQueue = new RequestQueue(cache, network);

                // Start the queue
                mRequestQueue.start();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, THINGSEE_URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("accessToken:", response);
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.e("error:", volleyError.toString());

                            }
                        }) {


                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("email", "nhan.phan@metropolia.fi");
                        params.put("password", "metropolia2016");
                        return params;

                    }

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<String, String>();
                        headers.put("Content-Type", "application/x-www-form-urlencoded");
                        return headers;
                    }

                };

                // Add the request to the RequestQueue.
                mRequestQueue.add(stringRequest);


//                ConnectivityManager connMgr = (ConnectivityManager)
//                        getSystemService(Context.CONNECTIVITY_SERVICE);
//                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//
//                // If there is a network connection, fetch data
//                if (networkInfo != null && networkInfo.isConnected()) {
//                    // Get a reference to the LoaderManager, in order to interact with loaders.
//                    LoaderManager loaderManager = getLoaderManager();
//                    loaderManager.destroyLoader(NEWS_LOADER_ID);
//
//                    // Initialize the loader. Pass in the int ID constant defined above and pass
//                    // in null for the bundle. Pass in this activity for the LoaderCallbacks
//                    // parameter (which is valid because this activity implements the
//                    // LoaderCallbacks interface).
//                    loaderManager.initLoader(NEWS_LOADER_ID, null, MainActivity.this);
//                }
            }
        });
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        return new ThingSeeLoader(this, THINGSEE_URL);
    }

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null && !data.isEmpty()) {
            txtTextView.setText(data);
        }

    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
    }
}
