package metro.ourthingsee.RESTObjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by giang on 2/12/17.
 */

public class Events {
    @SerializedName("events")
    @Expose
    private List<Event> events = null;

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public class Cause {

        @SerializedName("engine")
        @Expose
        private Engine engine;
        @SerializedName("senses")
        @Expose
        private List<Sense> senses = null;

        public Engine getEngine() {
            return engine;
        }

        public void setEngine(Engine engine) {
            this.engine = engine;
        }

        public List<Sense> getSenses() {
            return senses;
        }

        public void setSenses(List<Sense> senses) {
            this.senses = senses;
        }

    }
    public class Engine {

        @SerializedName("pId")
        @Expose
        private String pId;
        @SerializedName("puId")
        @Expose
        private long puId;
        @SerializedName("stId")
        @Expose
        private long stId;
        @SerializedName("evId")
        @Expose
        private long evId;
        @SerializedName("ts")
        @Expose
        private long ts;

        public String getPId() {
            return pId;
        }

        public void setPId(String pId) {
            this.pId = pId;
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

        public long getEvId() {
            return evId;
        }

        public void setEvId(long evId) {
            this.evId = evId;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }

    }
    public class Event {

        @SerializedName("timestamp")
        @Expose
        private long timestamp;
        @SerializedName("type")
        @Expose
        private String type;
        @SerializedName("deviceAuthUuid")
        @Expose
        private String deviceAuthUuid;
        @SerializedName("cause")
        @Expose
        private Cause cause;
        @SerializedName("event")
        @Expose
        private String event;
        @SerializedName("newValue")
        @Expose
        private long newValue;
        @SerializedName("oldValue")
        @Expose
        private long oldValue;

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDeviceAuthUuid() {
            return deviceAuthUuid;
        }

        public void setDeviceAuthUuid(String deviceAuthUuid) {
            this.deviceAuthUuid = deviceAuthUuid;
        }

        public Cause getCause() {
            return cause;
        }

        public void setCause(Cause cause) {
            this.cause = cause;
        }

        public String getEvent() {
            return event;
        }

        public void setEvent(String event) {
            this.event = event;
        }

        public long getNewValue() {
            return newValue;
        }

        public void setNewValue(long newValue) {
            this.newValue = newValue;
        }

        public long getOldValue() {
            return oldValue;
        }

        public void setOldValue(long oldValue) {
            this.oldValue = oldValue;
        }

    }
    public class Sense {

        @SerializedName("sId")
        @Expose
        private String sId;
        @SerializedName("val")
        @Expose
        private double val;
        @SerializedName("ts")
        @Expose
        private long ts;

        public String getSId() {
            return sId;
        }

        public void setSId(String sId) {
            this.sId = sId;
        }

        public double getVal() {
            return val;
        }

        public void setVal(double val) {
            this.val = val;
        }

        public long getTs() {
            return ts;
        }

        public void setTs(long ts) {
            this.ts = ts;
        }

    }
}
