package metro.ourthingsee.remote;

import metro.ourthingsee.POSTs.Authentication;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

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
    Call<Authentication> getUserDevices(@Header("Authorization") String auth);

}
