package metro.ourthingsee;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Usin on 10-Feb-17.
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


}
