package metro.ourthingsee;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * Created by Usin on 01-Feb-17.
 */
public class ThingSeeLoader extends AsyncTaskLoader<String> {
    /**
     * Query URL
     */
    private String mUrl;

    /**
     * Constructs a new {@link ThingSeeLoader}.
     *
     * @param context of the activity
     * @param url     to load data from
     */
    public ThingSeeLoader(Context context, String url) {
        super(context);
        mUrl = url;
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
        // Perform the network request, parse the response, and extract a list of News.
        String data = QueryUtils.fetchThingSeeData(mUrl);
        return data;
    }
}
