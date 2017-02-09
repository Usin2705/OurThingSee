package metro.ourthingsee.remote;

import metro.ourthingsee.POSTs.Authentication;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Query;

/**
 * Created by giang on 8.2.2017.
 */

public interface APIService {
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @FormUrlEncoded
    @POST("v2/accounts/login")
    Call<Authentication> postUserLogin(@Field("email") String email,
                                       @Field("password") String password);

    // For registerDevice
    @Headers({
            "Content-Type: application/x-www-form-urlencoded",
            "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0cyI6MTQ4NjY3MzI3OTQwMSwidXVpZCI6ImE5MDlmOWMwLWU4ODAtMTFlNi05OGY1LTRiMjg3NDkwYmUzNiIsInNjb3BlIjpbImFsbDphbGwiXSwiaWF0IjoxNDg2NjczMjc5LCJleHAiOjE0ODcyNzgwNzl9.mOyeHqit9pEwYM6sNJy7yBXqRiQEafeHi5tQp7L7VYQ"
    })
    @GET("v2/devices")
    Call<Authentication> registerDevice();

}
