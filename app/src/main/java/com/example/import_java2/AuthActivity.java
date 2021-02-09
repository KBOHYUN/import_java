package com.example.import_java2;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class AuthActivity extends AppCompatActivity {

    private WebView webView;
    Activity activity= AuthActivity.this;

    private String API_URL = "https://api.iamport.kr"; //아임포트 url

    private String apiKey="";
    private String apiSecret="";

    String name=null;
    String phone=null;

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        webView=findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);//자바스크립트 허용
        webView.getSettings().getBuiltInZoomControls();
        webView.getSettings().getLoadWithOverviewMode();
        webView.getSettings().setDefaultTextEncodingName("UTF-8");
        webView.setWebChromeClient(new WebChromeClient());//웹뷰에 크롬 사용 허용//이 부분이 없으면 크롬에서 alert가 뜨지 않음
        webView.setWebViewClient(new WebViewClient());//새창열기 없이 웹뷰 내에서 다시 열기//페이지 이동 원활히 하기위해 사용

        //자바스크립트와 연결하기
        webView.addJavascriptInterface(new SmsAuthActivity(),"AndroidBridge");

        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                webView.loadUrl("file:///android_asset/auth.html");
            }
        }, 500);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {//뒤로가기 버튼 이벤트
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {//웹뷰에서 뒤로가기 버튼을 누르면 뒤로가짐
            webView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    class SmsAuthActivity extends Activity {

        SmsAuthActivity(){
        }

        @JavascriptInterface
        public void resultAuth(String message, String impKey){
            Intent intent=new Intent();
            if(message.equals("success")&&impKey!=null){
                intent.putExtra("result","success");
                intent.putExtra("impkey",impKey);
                Log.i(TAG, ">AuthActivity> resultAuth> impkey : "+impKey);

                Log.i(TAG, ">AuthActivity> resultAuth> apikey, apisecret : "+apiKey+" "+apiSecret);

                Call<IamportResponse<AccessToken>> callToken = MainActivity.iamport.token( new AuthData(apiKey, apiSecret) );
                Log.i(TAG, ">call token");
                callToken.enqueue(new Callback<IamportResponse<AccessToken>>() {
                    @Override
                    public void onResponse(Call<IamportResponse<AccessToken>> call, Response<IamportResponse<AccessToken>> responseToken) {
                        if( responseToken.isSuccessful()){
                            IamportResponse<AccessToken> tokenResponse = responseToken.body();
                            Log.i(TAG, ">MainActivity> onactivity> token : "+tokenResponse.getResponse().getToken());

                            //토큰을 획득한 경우 본인인증
                            AccessToken auth = tokenResponse.getResponse();
                            Call<IamportResponse<Certification>> callCertification = MainActivity.iamport.certification_by_imp_uid(auth.getToken(), impKey);
                            callCertification.enqueue(new Callback<IamportResponse<Certification>>() {
                                @Override
                                public void onResponse(Call<IamportResponse<Certification>> call, Response<IamportResponse<Certification>> response) {
                                    Log.i(TAG, ">MainActivity> onactivity> certification response: "+response.body().getResponse());
                                    IamportResponse<Certification> certificationResponse=response.body();
                                    name=certificationResponse.getResponse().getName();
                                    //phone=certificationResponse.getResponse().getPhone();
                                    Log.i(TAG, ">MainActivity> onactivity> name: "+name); //인증시 사용된 이름
                                }

                                @Override
                                public void onFailure(Call<IamportResponse<Certification>> call, Throwable t) {
                                    Log.i(TAG, "본인 인증 실패");
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<IamportResponse<AccessToken>> call, Throwable t) {
                       Log.i(TAG, "토큰 획득 실패");
                    }
                });

                activity.setResult(RESULT_OK,intent);
            }
            else{
                intent.putExtra("result","fail");
                System.out.println("resultAuth_실패");
                activity.setResult(RESULT_CANCELED,intent);
            }
            activity.finish();
        }

    }
}
