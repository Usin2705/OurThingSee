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
     * MyHome minimum humidity value name to stored in shared pref
     */
    public static final String PREF_MYHOME_MIN_HUMIDITY_VALUE = "pref_myhome_min_humidity_value";

    /**
     * prefs name for latest value for humidity
     */
    public static final String PREF_HUMID_LATEST_VALUE = "pref_humi_latest_value";

    /**
     * prefs name for latest time for humidity
     */
    public static final String PREF_HUMID_LATEST_TIME = "pref_humi_latest_time";

    /**
     * MyHome default minimum humidity value (%)
     */
    public static final int DEFAULT_MIN_HUMIDITY_VALUE = 30;

    /**
     * MyHome notification option name to stored in shared pref
     */
    public static final String PREF_MYHOME_NOTIFICATION_OPTION = "pref_myhome_notification_option";

    /**
     * MyHome notification interval name to stored in shared pref
     */
    public static final String PREF_MYHOME_NOTIFICATION_INTERVAL = "pref_myhome_notification_value";

    /**
     * MyHome default notification interval value (min)
     */
    public static final int DEFAULT_NOTIFICATION_INTERVAL_VALUE = 60;

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

    /**
     *Intent request code for my home service
     */
    public static final int INTENT_REQUEST_CODE_MYHOMESERVICE = 101;

    /**
     *Intent name for minimum humidity vale
     */
    public static final String INTENT_NAME_MIN_HUMIDITY_VALUE = "com.metro.ourthingsee.min_hud_val";

    /**
     * When you issue multiple notifications about the same type of event, it’s best practice
     * for your app to try to update an existing notification with this new information, rather
     * than immediately creating a new notification. If you want to update this notification at
     * a later date, you need to assign it an ID. You can then use this ID whenever you issue a
     * subsequent notification. If the previous notification is still visible, the system will
     * update this existing notification, rather than create a new one. In this example,
     * the notification’s ID is 001
     */
    public static final int NOTIFICATION_ID_HUMIDITY = 101;

    /**
     * Set the max value for notification interval. Used in umber picker in my home activity
     * Notice it should be different with {@link #MYHOME_MIN_HUMIDITY_MAXVALUE} so we can know
     * which number picker is for which. (those two share the same number picker)
     *
     */
    public static final int MYHOME_NOTIFICATION_INTERVAL_MAXVALUE = 120;

    /**
     * Set the max value for minimum humidity level. Used in umber picker in my home activity
     * Notice it should be different with {@link #MYHOME_NOTIFICATION_INTERVAL_MAXVALUE} so we can
     * know which number picker is for which. (those two share the same number picker)
     *
     */
    public static final int MYHOME_MIN_HUMIDITY_MAXVALUE = 100;


}
