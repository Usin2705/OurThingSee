package metro.ourthingsee;

/**
 * Created by Usin on 06-Feb-17.
 */

public class OurContract {
    /**
     * The app's shared pref name
     */
    public static final String SHARED_PREF = "metro.ourthingsee.sharedpref";

    /**
     * User's email address
     */
    public static final String PREF_AUTH_EMAIL = "pref_auth_email";

    /**
     * User's email address
     */
    public static final String PREF_AUTH_PASSWORD = "pref_auth_password";

    /**
     * Device's auth id name to stored in shared pref
     */
    public static final String PREF_DEVICE_AUTH_ID_NAME = "pref_device_auth_ID";
    /**
     * Device's token to stored in shared pref
     */
    public static final String PREF_DEVICE_TOKEN = "pref_device_token";

    /**
     * User's auth token name to stored in shared pref
     */
    public static final String PREF_USER_AUTH_TOKEN_NAME = "pref_auth_token";

    /**
     * The register url for register your account with ThingSee cloud
     */
    public static final String URL_REGISTER = "http://api.thingsee.com/v2/accounts/login";

    /**
     * The register url for register your account with ThingSee cloud
     */
    public static final String URL_LOAD_DATA = "http://api.thingsee.com/v2//events/";

    /**
     * The register ID for loader. Used when register new account with ThingSee cloud
     */
    public static final int LOADER_ID_REGISTER = 1;

    /**
     * The Load dât ID for loader. Load data from cloud to database to screen. Used when resisted
     */
    public static final int LOADER_ID_DATALOADER = 2;
}
