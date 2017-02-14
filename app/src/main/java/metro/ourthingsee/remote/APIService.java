package metro.ourthingsee.remote;

import metro.ourthingsee.RESTObjects.Authentication;
import metro.ourthingsee.RESTObjects.Devices;
import metro.ourthingsee.RESTObjects.Events;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by giang on 8.2.2017.
 */

public interface APIService {
    // For user login
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("v2/accounts/login")
    Call<Authentication> savePostAuth(@Field("email") String email,
                                      @Field("password") String password);

    // For get User's devices
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @GET("v2/devices")
    Call<Devices> getUserDevices(@Header("Authorization") String auth);// For get User's events base on Device's id
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @GET("v2/events/{deviceAuthUuid}")
    Call<Events> getUserEvents(@Header("Authorization") String auth,
                               @Path("deviceAuthUuid") String deviceAuthUuid,
                               @Query("type") String type,
                               @Query("senses") String senses,
                               @Query("limit") Integer limit,
                               @Query("start") Long start,
                               @Query("end") Long end);
}
