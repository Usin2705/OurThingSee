package metro.ourthingsee.POSTs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Created by Usin on 09-Feb-17.
 */

public class GetDevices {
    @SerializedName("devices")
    @Expose
    private Arrays mDevices;
    @SerializedName("timestamp")
    @Expose
    private Long mTimestamp;

    public Arrays getDevices() {
        return mDevices;
    }

    public void setDevices(Arrays devices) {
        mDevices = devices;
    }

    public Long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(Long timestamp) {
        mTimestamp = timestamp;
    }
}
