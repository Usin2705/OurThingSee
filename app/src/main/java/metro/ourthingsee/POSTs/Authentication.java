package metro.ourthingsee.POSTs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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

    @Override
    public String toString() {
        return "Authentication{" +
                "timestamp=" + timestamp +
                ", accountAuthUuid='" + accountAuthUuid + '\'' +
                ", accountAuthToken='" + accountAuthToken + '\'' +
                '}';
    }
}
