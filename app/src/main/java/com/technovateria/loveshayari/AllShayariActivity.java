package com.technovateria.loveshayari;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.technovateria.loveshayari.Adapter.AllShayariAdapter;
import com.technovateria.loveshayari.Model.AllShayariModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AllShayariActivity extends AppCompatActivity {

    RecyclerView allShayari_recyclerView;
    AllShayariAdapter allShayariAdapter;
    FirebaseFirestore db;
    List<AllShayariModel> list = new ArrayList<>();
    String id;
    ProgressBar progressBar;

    private AdView adView;
    private FrameLayout adContainerView;
    private InterstitialAd mInterstitialAd;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_shayari);

        // Check internet and Wi-Fi connectivity
        if (!isConnectedToInternet() && !isConnectedToWifi()) {
            showEnableInternetDialog();
        }

        // Banner ads section of code
        adContainerView = findViewById(R.id.ad_view_container);
        adContainerView.setVisibility(View.GONE);
        adView = new AdView(this);
        adView.setAdUnitId(getString(R.string.adaptive_banner_ad_unit_id_main_activity));
        adView.setAdSize(getAdSize());
        adContainerView.addView(adView);

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                Log.d("Banner", "Banner ads is loaded");
                adContainerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("Banner", "Loading banner ads is failed");
                adContainerView.setVisibility(View.GONE);
            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadBanner();
            }
        });


        //InterstitialAd
        loadInterstitialAd();


        //Go back to Home Page of Home screen
        MaterialToolbar btnBack  = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AllShayariActivity.this, MainActivity.class));
                finish();
            }
        });



        //get data id and name from ShayariCategoryAdapter class
        // set the tile of page based on shayari category

        String name = getIntent().getStringExtra("name");
        id = getIntent().getStringExtra("id");
        btnBack.setTitle(name);


        //set recyclerview properties
        allShayari_recyclerView = findViewById(R.id.recyclerview_all_shayari);
        allShayari_recyclerView.setLayoutManager(new LinearLayoutManager(this));


        //load all shayari from clous firestore of firebase based on the collection id
        showAllShari(id);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showAllShari(id);
            }
        });

    }

    private void showAllShari(String id) {

        progressBar = findViewById(R.id.mainProgressBar);
        progressBar.setVisibility(View.VISIBLE);

        db = FirebaseFirestore.getInstance();

        db.collection("Shayari").document(id).collection("all")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>(){
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //callled when data is retrived
                        for (DocumentSnapshot document: task.getResult()){
                            AllShayariModel modelList = new AllShayariModel(
                                    document.getString("id"),
                                    document.getString("data")
                            );
                            list.add(modelList);
                        }
                        //adapter
                        allShayariAdapter = new AllShayariAdapter(AllShayariActivity.this, list);
                        //set adapter recycler view
                        allShayari_recyclerView.setAdapter(allShayariAdapter);
                        swipeRefreshLayout.setRefreshing(false);
                        progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // called when failed to load data
                        progressBar.setVisibility(View.GONE);
                        swipeRefreshLayout.setRefreshing(false);
                        Toast.makeText(AllShayariActivity.this, "Failed to Load, Check Internet", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadBanner() {
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        float widthPixels = getResources().getDisplayMetrics().widthPixels;
        float density = getResources().getDisplayMetrics().density;
        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth);
    }

    private void loadInterstitialAd(){
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(this,getString(R.string.Interstitial_backpress_ad_unit), adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        mInterstitialAd = interstitialAd;
                        Log.i("InterstitialAd", "onAdLoaded");
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        Log.d("InterstitialAd", loadAdError.toString());
                        mInterstitialAd = null;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(AllShayariActivity.this);

            mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    super.onAdDismissedFullScreenContent();
                    mInterstitialAd = null;
                    AllShayariActivity.super.onBackPressed();
                }
            });
        } else {
            super.onBackPressed();
            Log.d("InterstitialAd", "The interstitial ad wasn't ready yet.");
        }
    }

    private void showEnableInternetDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Internet Connection");
        builder.setMessage("Please turn on your internet connection.");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Open settings to enable internet
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Handle cancel button click
                //finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }
    private boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private boolean isConnectedToWifi() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                // Connected to Wi-Fi
                return true;
            }
        }
        return false;
    }
}