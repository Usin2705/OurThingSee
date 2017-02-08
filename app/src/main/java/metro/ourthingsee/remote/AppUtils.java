package metro.ourthingsee.remote;

/**
 * Created by giang on 8.2.2017.
 */

public class AppUtils {
    private AppUtils() {}

    public static final String BASE_URL = "http://api.thingsee.com/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
