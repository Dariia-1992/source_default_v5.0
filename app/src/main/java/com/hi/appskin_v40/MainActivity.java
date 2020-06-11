package com.hi.appskin_v40;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.hi.appskin_v40.fragment.IRewardAdded;
import com.hi.appskin_v40.utils.AdHelper;
import com.hi.appskin_v40.utils.LocalStorage;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class MainActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private InterstitialAd interstitialAd;
    private RewardedVideoAd videoAd;
    private IRewardAdded fragmentCallback;
    private IRewardAdded calledOnResume;
    private NavController navController;

    // Banner Ad
    private FrameLayout adContainer;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // Banner ad
        adContainer = findViewById(R.id.ad_container);
        adView = new AdView(this);
        adContainer.post(() -> AdHelper.loadBanner(this, adView, adContainer));

        // Interstitial ad
        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getResources().getString(R.string.id_ads_interstitial));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });

        // Video ad
        videoAd = MobileAds.getRewardedVideoAdInstance(this);
        videoAd.setRewardedVideoAdListener(this);
        loadRewardedVideoAd();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
    }

    @Override
    protected void onResume() {
        videoAd.resume(this);
        super.onResume();

        if (adView != null) {
            adView.resume();
        }

        if (calledOnResume != null) {
            calledOnResume.onAddReward();
            calledOnResume = null;
        }
    }

    @Override
    protected void onPause() {
        videoAd.pause(this);

        if (adView != null) {
            adView.pause();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        videoAd.destroy(this);

        if (adView != null) {
            adView.destroy();
        }

        super.onDestroy();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    private void loadRewardedVideoAd() {
        videoAd.loadAd(getResources().getString(R.string.id_ads_video), new AdRequest.Builder().build());
    }

    public void showInterstitialAd() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            LocalStorage.setOpensWithoutAd(this, 0);
        }
    }

    public void showVideoAd(@NonNull IRewardAdded callback) {
        if (videoAd != null && videoAd.isLoaded()) {
            fragmentCallback = callback;
            videoAd.show();
        } else {
            showInterstitialAd();
            callback.onAddReward();
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {

    }
    @Override
    public void onRewardedVideoAdOpened() {

    }
    @Override
    public void onRewardedVideoStarted() {

    }
    @Override
    public void onRewardedVideoAdClosed() {

    }
    @Override
    public void onRewarded(RewardItem rewardItem) {

    }
    @Override
    public void onRewardedVideoAdLeftApplication() {

    }
    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {

    }
    @Override
    public void onRewardedVideoCompleted() {

    }
}
