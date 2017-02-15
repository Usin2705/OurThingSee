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
     * The index option for location
     */
    public static final int INDEX_OPTION_LOCATION = 0;

    /**
     * The index option for Temperature
     */
    public static final int INDEX_OPTION_TEMPERATURE = 1;

    /**
     * The index option for location
     */
    public static final int INDEX_OPTION_HUMIDITY = 2;

    /**
     * The index option for My Thingsee
     */
    public static final int INDEX_OPTION_MYTHINGSEE = 3;

    /**
     * Defines a custom Intent action. This will be used for filter the broadcast.
     */
    public static final String BROADCAST_ACTION = "com.metro.ourthingsee.BROADCAST";

    /**
     * Response name for the Intent Service Extra of sensor value
     */
    public static final String BROADCAST_RESPONSE_VALUE = "com.metro.ourthingsee.BROADCAST_RESPONSE_VALUE";

    /**
     * Response name for the Intent Service Extra of sensor timestamp
     */
    public static final String BROADCAST_RESPONSE_TIMESTAMP = "com.metro.ourthingsee.BROADCAST_RESPONSE_TIMESTAMP";

    /**
     * The sensor id for Thingsee device. Temperature unit is degress
     *
     */
    public static final String SENSOR_ID_TEMPERATURE = "0x00060100";

    /**
     * The sensor id for Thingsee device. Humidity unit is %
     */
    public static final String SENSOR_ID_HUMIDITY = "0x00060200";

    /**
     * The sensor id for Thingsee device. Luminance unit is lux
     */
    public static final String SENSOR_ID_LUMINANCE = "0x00060300";

    /**
     * The sensor id for Thingsee device. Pressure unit is hPa
     */
    public static final String SENSOR_ID_PRESSURE = "0x00060400";

    /**
     * The sensor id for Thingsee device. Acceleration unit is g
     */
    public static final String SENSOR_ID_ACCELERATION_X = "0x00050100";

    /**
     * The sensor id for Thingsee device. Acceleration unit is g
     */
    public static final String SENSOR_ID_ACCELERATION_Y = "0x00050100";

    /**
     * The sensor id for Thingsee device. Acceleration unit is g
     */
    public static final String SENSOR_ID_ACCELERATION_Z = "0x00050100";

    /**
     * The sensor id for Thingsee device. Latidude unit is degrees
     */
    public static final String SENSOR_ID_LOCATION_LATIDUDE = "0x00010100";

    /**
     * The sensor id for Thingsee device. Longitude unit is degrees
     */
    public static final String SENSOR_ID_LOCATION_LONGITUDE = "0x00010200";

    /**
     * The sensor id for Thingsee device. Altidude unit is meters
     */
    public static final String SENSOR_ID_LOCATION_ALTIDUDE = "0x00010300";

    /**
     * The sensor id for Thingsee device. Accuracy unit is meters
     */
    public static final String SENSOR_ID_LOCATION_ACCURACY = "0x00010400";


}
