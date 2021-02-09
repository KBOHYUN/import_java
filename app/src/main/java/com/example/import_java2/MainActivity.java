package com.example.import_java2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private int SMS_AUTH_REQ_CODE = 202;

    private String API_URL = "https://api.iamport.kr"; //아임포트 url
    public static Iamport iamport = null;

    String name=null;
    String phone=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .addInterceptor(new Interceptor() { //Tier Header
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();

                        return chain.proceed(request);
                    }
                })
                .build();

        //retrofit 구성
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        Log.i(TAG, ">Iamport create > retrofit");
        iamport=retrofit.create(Iamport.class);


        Button button=findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, AuthActivity.class), SMS_AUTH_REQ_CODE);
            }
        });
    }

    //본인인증 결과
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode==SMS_AUTH_REQ_CODE){
            if(resultCode==RESULT_OK){
                Toast.makeText(MainActivity.this, "본인인증이 성공하였습니다", Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "본인인증이 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}