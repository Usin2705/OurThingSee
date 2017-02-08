package metro.ourthingsee.remote;

import metro.ourthingsee.POSTs.Authentication;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by giang on 8.2.2017.
 */

public interface APIService {
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("v2/accounts/login")
    Call<Authentication> savePost(@Field("email") String email,
                                  @Field("password") String password);
}
