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
     * User's auth token name to stored in shared pref
     */
    public static final String PREF_AUTH_TOKEN_NAME = "pref_auth_token";

    /**
     * The register url for register your account with ThingSee cloud
     */
    public static final String URL_REGISTER = "http://api.thingsee.com/v2/accounts/login";

    /**
     * The register ID for loader. Used when register new account with ThingSee cloud
     */
    public static final int LOADER_ID_REGISTER = 1;
}
