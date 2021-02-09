package com.example.import_java2;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface Iamport {
    @POST("/users/getToken")
    Call<IamportResponse<AccessToken>> token(@Body AuthData auth);

    /* 본인인증 결과 (certification result) */
    @GET("/certifications/{imp_uid}")
    Call<IamportResponse<Certification>> certification_by_imp_uid(@Header("Authorization") String token, @Path("imp_uid") String imp_uid);
}
