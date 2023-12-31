package com.technovateria.loveshayari;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.technovateria.loveshayari.Adapter.ShayariCategoryAdapter;
import com.technovateria.loveshayari.Model.ShayariCategoryModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import io.grpc.android.BuildConfig;

public class MainActivity extends AppCompatActivity {
    RecyclerView shayariCategory_recyclerView;
    ShayariCategoryAdapter shayariCategoryAdapter;
    FirebaseFirestore db;
    List<ShayariCategoryModel> list = new ArrayList<>();
    private static int REQUEST_CODE = 100;
    ProgressBar progressBar;
    private AdView adView;
    private FrameLayout adContainerView;
    ReviewManager manager;
    ReviewInfo reviewInfo = null;
    public static int UPDATE_CODE = 22;
    AppUpdateManager appUpdateManager;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
                Log.d("Banner", "Dashboard Banner ads is loaded");
                adContainerView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.d("Banner", "Loading dashboard banner ads is failed");
                adContainerView.setVisibility(View.GONE);
            }
        });

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                loadBanner();
            }
        });

        //review app or rate app
        initReviewInfo();
        //in-app update
        inAppUpdate();
        //All app permission
        grantAppPermission();


        //Open drawer code
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        ImageView btnMenu = findViewById(R.id.btn_menu);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
        //open drawer code end

        // menu item code
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                drawerLayout.closeDrawer(GravityCompat.START);
                switch (id) {
                    case R.id.home:
                        break;

                    case R.id.rate_us:
                        Uri uri = Uri.parse("market://details?id=" + getPackageName());
                        Intent myAppLinkToMarket = new Intent(Intent.ACTION_VIEW, uri);
                        try {
                            startActivity(myAppLinkToMarket);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(MainActivity.this, "unable to find market app", Toast.LENGTH_LONG).show();
                        }
                        break;

                    case R.id.share_app:
                        try {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Hindi Shayari");
                            String shareMessage = "\nDownload our Shayari app now and discover all types of Shayari & Status\n\n" +
                                    "शब्दों से ज्यादा कुछ नहीं कह सकते,😍\n" +
                                    "हमारी शायरियों से अब अपने दिल को आज़माएं।💖\n" +
                                    "बेहतरीन तस्वीरों के साथ जुड़ी हमारी एप,💕\n" +
                                    "अभी इंस्टॉल करें और दोस्तों के साथ शेयर कीजिए।🌹\n" +
                                    "👇👇👇👇👇👇👇👇👇👇👇👇\n\n";
                            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=com.technovateria.loveshayari\n\n";
                            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
                            startActivity(Intent.createChooser(shareIntent, "choose one"));
                        } catch (Exception e) {
                            //e.toString();
                        }
                        break;

                    case R.id.more_app:
                        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                        try {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                        } catch (ActivityNotFoundException anfe) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                        }
                        break;

                    case R.id.contact_us:
                        startActivity(new Intent(MainActivity.this, ContactUsActivity.class));
                        break;

                    case R.id.privacy_policy:
                        Uri urii = Uri.parse("https://doc-hosting.flycricket.io/hindi-shayari-privacy-policy/15901252-d2d2-47fb-9849-b5a496d09a64/privacy");
                        startActivity(new Intent(Intent.ACTION_VIEW, urii));
                        break;

                    default:
                        throw new IllegalStateException("Unexpected value: " + id);
                }
                return true;
            }
        });
        //menu item code end


        // Recycler view code

        //get instance of firebase cloud firestore
        db = FirebaseFirestore.getInstance();

        //set recycler view properties
//        shayariCategory_recyclerView.setHasFixedSize(true);
        shayariCategory_recyclerView = findViewById(R.id.recyclerview_shayari_category);
        shayariCategory_recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // load all shayari category from cloud firestore of firebase
        showAllShayariCategory();

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                showAllShayariCategory();
            }
        });

    }

    private void showAllShayariCategory() {
        progressBar = findViewById(R.id.mainProgressBar);
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Shayari")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        //callled when data is retrived
                        for (DocumentSnapshot document : task.getResult()) {
                            ShayariCategoryModel modelList = new ShayariCategoryModel(
                                    document.getString("id"),
                                    document.getString("name")
                            );
                            list.add(modelList);
                        }
                        //adapter
                        shayariCategoryAdapter = new ShayariCategoryAdapter(MainActivity.this, list);
                        //set adapter recycler view
                        shayariCategory_recyclerView.setAdapter(shayariCategoryAdapter);
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
                        Toast.makeText(MainActivity.this, "Failed to Load, Check Internet", Toast.LENGTH_LONG).show();
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

    @Override
    public void onBackPressed() {
        openReview();
        super.onBackPressed();
    }

    private void initReviewInfo(){
        manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener((task) -> {
           if(task.isSuccessful()){
               reviewInfo = task.getResult();
           }else {

           }
        });
    }

    private void openReview() {
        if (reviewInfo != null){
            Task<Void> flow = manager.launchReviewFlow(this, reviewInfo);
            flow.addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {

                }
            });
        }
    }

    private void inAppUpdate() {
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
            }{
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.IMMEDIATE,
                            this,
                            UPDATE_CODE);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    Log.d("UpdateError", "onSuccess" + e.toString());
                }
            }
        });
        appUpdateManager.registerListener(listener);
    }
    InstallStateUpdatedListener  listener = installState -> {
        if(installState.installStatus() == InstallStatus.DOWNLOADED){
            popUp();
        }
    };
    private void popUp() {
        Snackbar snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "App Update Almost Done.",
                Snackbar.LENGTH_INDEFINITE
                );
        snackbar.setAction("Reload", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                appUpdateManager.completeUpdate();
            }
        });

        snackbar.setTextColor(Color.parseColor("#FFEA2027"));
        snackbar.show();
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
                // finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void grantAppPermission(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED)
        {

        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                            Manifest.permission.READ_MEDIA_IMAGES
                    },
                    REQUEST_CODE
            );
        }
    }
}
