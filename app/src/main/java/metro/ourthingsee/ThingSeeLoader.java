package metro.ourthingsee;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by Usin on 01-Feb-17.
 */
public class ThingSeeLoader extends AsyncTaskLoader<String> {
    private static final String LOG_TAG = ThingSeeLoader.class.getSimpleName();

    /**
     * Query URL
     */
    private String mUrl;

    /**
     * ID for the loader, to differentiate it with other loaders
     */
    private int mLoaderID;

    /**
     * Email of the user, used for login
     */
    private String mEmail;

    /**
     * Password of the user, used for login
     */
    private String mPassword;

    /**
     * Constructs a new {@link ThingSeeLoader}.
     *
     * @param context  of the activity
     * @param url      to load data from
     * @param loaderID the id to differentiate the task
     */
    public ThingSeeLoader(Context context, String url, int loaderID) {
        super(context);
        mUrl = url;
        mLoaderID = loaderID;
    }

    /**
     * Constructs a new {@link ThingSeeLoader}.
     *
     * @param context  of the activity
     * @param url      to load data from
     * @param loaderID the id to differentiate the task
     * @param email    Email of the user, used for login
     * @param password Password of the user, used for login
     */
    public ThingSeeLoader(Context context, String url, int loaderID,
                          String email, String password) {
        super(context);
        mUrl = url;
        mLoaderID = loaderID;
        mEmail = email;
        mPassword = password;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        // Perform the network request, parse the response, and extract data base on the
        // id of the request.
        switch (mLoaderID) {
            // First cate is register.
            case OurContract.LOADER_ID_REGISTER:
                String data = QueryUtils.fetchThingSeeData(mUrl, mLoaderID, mEmail, mPassword);
                return data;

            case OurContract.LOADER_ID_DATALOADER:
                // Perform the network request, parse the response, and extract a list of News
                return null;

            default:
                return null;

        }
    }
}
