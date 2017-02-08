package metro.ourthingsee;

/**
 * ThingSee class to store ThingSee data information.
 * Including timestamp, sensorID and sensor value
 */

public class ThingSee {
    private int mTimestamp;
    private String mSensorID;
    private float mSensorValue;

    /**
     * Constructs a new {@link ThingSee } object.
     *
     * @param timeStamp         is the timestamp of the data
     * @param sensorID          the id of the data
     * @param sensorValue       the value of the data
     */
    public ThingSee(int timeStamp, String sensorID, float sensorValue) {
        mTimestamp = timeStamp;
        mSensorID = sensorID;
        mSensorValue = sensorValue;
    }

    public int getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(int mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public String getmSensorID() {
        return mSensorID;
    }

    public void setSensorID(String mSensorID) {
        this.mSensorID = mSensorID;
    }

    public float getSensorValue() {
        return mSensorValue;
    }

    public void setSensorValue(float mSensorValue) {
        this.mSensorValue = mSensorValue;
    }
}
