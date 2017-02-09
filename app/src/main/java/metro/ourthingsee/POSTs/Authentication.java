package metro.ourthingsee.POSTs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

/**
 * Created by giang on 8.2.2017.
 */

public class Authentication {
    @SerializedName("timestamp")
    @Expose
    private Long timestamp;
    @SerializedName("accountAuthUuid")
    @Expose
    private String accountAuthUuid;
    @SerializedName("accountAuthToken")
    @Expose
    private String accountAuthToken;
    @SerializedName("devices")
    @Expose
    private List<Device> devices = null;

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAccountAuthUuid() {
        return accountAuthUuid;
    }

    public void setAccountAuthUuid(String accountAuthUuid) {
        this.accountAuthUuid = accountAuthUuid;
    }

    public String getAccountAuthToken() {
        return accountAuthToken;
    }

    public void setAccountAuthToken(String accountAuthToken) {
        this.accountAuthToken = accountAuthToken;
    }

//    @Override
//    public String toString() {
//        return "Authentication{" +
//                "timestamp=" + timestamp +
//                ", accountAuthUuid='" + accountAuthUuid + '\'' +
//                ", accountAuthToken='" + accountAuthToken + '\'' +
//                '}';
//    }

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
    public class Device {
        @SerializedName("uuid")
        @Expose
        private String uuid;
        @SerializedName("token")
        @Expose
        private String token;

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

//        @SerializedName("state")
//        @Expose
//        private State state;
//        public State getState() {
//            return state;
//        }
//        public void setState(State state) {
//            this.state = state;
//        }
    }
}
