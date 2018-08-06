package com.proger.ruslan.advancedact;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.startapp.android.publish.adsCommon.StartAppAd;
import com.startapp.android.publish.adsCommon.StartAppSDK;

import java.util.Timer;
import java.util.TimerTask;

public class Intro extends AppCompatActivity {

    private final int REQUEST_CODE_PERMISSION_LOCATION = 10295;
    private StartAppAd startAppAd = new StartAppAd(this);

    private boolean adIsLoaded = false, startAdStarted = false;

    String devID = "180306043"; //ID разработчика
    String appID = "207047312"; //ID приложения

    private InterstitialAd mInterstitialAd;

    Timer interstitialAsLoad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StartAppSDK.init(this, appID, true);
        StartAppAd.disableSplash();
        StartAppAd.init(this, devID, appID);

        mInterstitialAd = new InterstitialAd(Intro.this);
            mInterstitialAd.setAdUnitId("ca-app-pub-1683287051972127/3063798140");
            AdRequest adRequestInter = new AdRequest.Builder().build();

            mInterstitialAd.loadAd(adRequestInter);

            mInterstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
//                    if (!adIsLoaded) {
//                        adIsLoaded = true;
//
////                        startMenuActivity();
//
////                        if (interstitialAsLoad != null)
////                            interstitialAsLoad.cancel();
////
////                        SplashScreenActivity.this.finish();
//                    }
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);

//                    if (!startAdStarted && !adIsLoaded) {
//                        startAdStarted = true;
//                        adIsLoaded = true;
//                        showStartAppStartingAds();
//                    }

                    if (!startAppAd.isReady())
                        startAppAd.loadAd();
                }
            });

        setContentView(R.layout.splash);

        getSupportActionBar().hide();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    ActivityCompat.requestPermissions(Intro.this,
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_PERMISSION_LOCATION);
            }
        }, 500);

        accessAllowed();
    }

    //Load adMob
    private void waitInterstitialAsLoad() {

        interstitialAsLoad = new Timer();
        final int[] timer = {0};

//        if (!startAdStarted) {
//            startAdStarted = true;
//            showStartAppStartingAds();
//        }

        interstitialAsLoad.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if ((mInterstitialAd!=null && mInterstitialAd.isLoaded())) {
                            if (!adIsLoaded) {
                                adIsLoaded = true;

                                startMenuActivity();

                                if (mInterstitialAd != null && mInterstitialAd.isLoaded())
                                    mInterstitialAd.show();

                                interstitialAsLoad.cancel();

                                Intro.this.finish();
                            }
                        } else if (timer[0] >= 16) {
                            startMenuActivity();

//                            if (!startAdStarted) {
//                                startAdStarted = true;
//                                showStartAppStartingAds();
//                            }

                            if (!adIsLoaded && !startAdStarted) {
                                adIsLoaded = true;
                                startAdStarted = true;
                                startAppAd.showAd();
                                startAppAd.loadAd();
                            }

                            interstitialAsLoad.cancel();

                            Intro.this.finish();
                        }
                        timer[0]++;
                    }
                });
            }
        }, 0, 500);
    }

    public void startMenuActivity () {
        if (getSharedPreferences(getApplicationContext().getString(R.string.APP_PREFERENCES),
                Context.MODE_PRIVATE).getBoolean("isInit", true)) {
            startActivity(new Intent(getApplicationContext(), Introducing.class));
        } else
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        if (!adIsLoaded)
            StartAppAd.showAd(Intro.this);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_LOCATION:
                if (grantResults.length > 0 && (grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.must_have_permission_enabled), Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }

        accessAllowed();
    }

    private void accessAllowed () {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            waitInterstitialAsLoad();
        }
    }
}
