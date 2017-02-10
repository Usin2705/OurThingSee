package metro.ourthingsee.RESTObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by giang on 10.2.2017.
 */
/**
 * Class to store the List of devices taken from devices
 * The most important one is "uuid", which store the device's unique id. We must use this
 * id to get the event from devices
 *
 * <p>NOTE: there're also a "state" but we did not use it (since it require creating another
 * class called State
 *
 * Most of the class is random generated at this website:
 * @see <a href="http://www.jsonschema2pojo.org/">Sexy ladies</a>
 *
 *
 *
 */
public class Devices {
    @SerializedName("devices")
    @Expose
    private List<Device> devices = null;
    @SerializedName("timestamp")
    @Expose
    private long timestamp;

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public class Device {
        @SerializedName("uuid")
        @Expose
        private String uuid;
        @SerializedName("token")
        @Expose
        private String token;
        @SerializedName("state")
        @Expose
        private State state;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public State getState() {
            return state;
        }

        public void setState(State state) {
            this.state = state;
        }
    }
    public class State {

        @SerializedName("ts")
        @Expose
        private long ts;
        @SerializedName("puId")
        @Expose
        private long puId;
        @SerializedName("stId")
        @Expose
        private long stId;

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }

        public long getPuId() {
            return puId;
        }

        public void setPuId(long puId) {
            this.puId = puId;
        }

        public long getStId() {
            return stId;
        }

        public void setStId(long stId) {
            this.stId = stId;
        }
    }
}


