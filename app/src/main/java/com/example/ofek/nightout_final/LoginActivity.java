package com.example.ofek.nightout_final;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import static Entries.AppEntries.PREF_SETTINGS_TAG;
import static com.example.ofek.nightout_final.LoginActivity.Entries.PREF_IS_CONNECTED_TAG;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {
    CallbackManager manager;
    SharedPreferences preferences;
    public static AccessToken userToken;
    private AccessTokenTracker accessTokenTracker;
    GoogleApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setGoogleApiClient();
        preferences=getSharedPreferences(PREF_SETTINGS_TAG,MODE_PRIVATE);
        LoginButton btn= (LoginButton) findViewById(R.id.loginBtn);
        manager=CallbackManager.Factory.create();
        btn.registerCallback(manager, callback);
        accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                updateWithToken(newAccessToken);
            }
        };
        updateWithToken(AccessToken.getCurrentAccessToken());
    }

    private void setGoogleApiClient() {
        apiClient=new GoogleApiClient.Builder(this)
                .enableAutoManage(this,this).
                        addApi(Places.GEO_DATA_API).
                        addApi(Places.PLACE_DETECTION_API)
                .build();
        if (!apiClient.isConnecting()&&!apiClient.isConnected())
            apiClient.connect();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        manager.onActivityResult(requestCode,resultCode,data);
    }


    private FacebookCallback<LoginResult> callback=new FacebookCallback<LoginResult>() {
        @Override
        public void onSuccess(LoginResult loginResult) {
            userToken=loginResult.getAccessToken();
            String userId=userToken.getUserId();
            SharedPreferences.Editor editor=preferences.edit();
            editor.putString(Entries.USER_ID_TAG,userId).commit();
            editor.putInt(PREF_IS_CONNECTED_TAG,1).commit();

        }

        @Override
        public void onCancel() {
            Toast.makeText(LoginActivity.this, "Please Login in order to proceed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onError(FacebookException error) {
            Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
        }
    };

    private void updateWithToken(AccessToken currentAccessToken) {

        if (currentAccessToken != null) {
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }, 3);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("Google Api Result Error",connectionResult.getErrorMessage());
    }


    //-------------Entries--------------------------------
    class Entries{

        public static final String USER_ID_TAG ="userID" ;
        public static final String PREF_IS_CONNECTED_TAG ="connected";
    }
}
