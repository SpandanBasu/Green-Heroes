package com.greenhero.greenheroes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartingActivity extends AppCompatActivity {


    public static int location=0;
    FirebaseUser user;
    FirebaseAuth mAuth;
    public static int firstTime;
    static final int REQUEST_CODE= 123;
    Animation imgAnim, textAnim;
    ImageView imgLogo;
    TextView textLogo;
    private boolean isConnected;
    boolean isLoadedAlready =false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_starting);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firstTime = appGetFirstTimeRun();
        imgAnim= AnimationUtils.loadAnimation(StartingActivity.this,R.anim.img_splash);
        textAnim= AnimationUtils.loadAnimation(StartingActivity.this,R.anim.text_splash);
        imgLogo=(ImageView)findViewById(R.id.img_splash);
        textLogo=(TextView)findViewById(R.id.text_splash);
        imgLogo.setAnimation(imgAnim);
        textLogo.setAnimation(textAnim);



        if((ContextCompat.checkSelfPermission(StartingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)+ ContextCompat.checkSelfPermission(StartingActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED))
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(StartingActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(StartingActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {

                ActivityCompat.requestPermissions(StartingActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },REQUEST_CODE);
            }
            else
            {

                ActivityCompat.requestPermissions(StartingActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },REQUEST_CODE);
            }
        }
        else
        {

            if(firstTime!=0)
            {
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
                    location=1;
                    turnOnLocation();

                }
                else
                {
                    if (user != null)
                    {
                        isLoadedAlready = true;
                        Intent intent = new Intent(StartingActivity.this, MainActivity.class);
                        intent.putExtra("frgToLoad", 5);
                        startActivity(intent);
                        finish();
                    }
                }
            }

        }

        final ConnectivityManager cm= (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net= cm.getActiveNetworkInfo();
        isConnected = net!= null && net.isConnectedOrConnecting();

        if(!isConnected)
        {
            Toast.makeText(StartingActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(StartingActivity.this);
            builder1.setMessage("No Internet connection ");
            builder1.setCancelable(true);
            builder1.setPositiveButton(
                    "Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
            return;
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if(firstTime !=0 && !isLoadedAlready)
                {
                    if (user != null)
                    {
                        if(location==1)
                        {
                            Intent intent = new Intent(StartingActivity.this, MainActivity.class);
                            intent.putExtra("frgToLoad", 2);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            Intent intent = new Intent(StartingActivity.this, MainActivity.class);
                            intent.putExtra("frgToLoad", 3);
                            startActivity(intent);
                            finish();
                        }


                    }

                    else
                    {
                        Intent intent = new Intent(StartingActivity.this ,LogIn.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }


                }
                else
                {
                    if(!isLoadedAlready) {
                        Intent intent = new Intent(StartingActivity.this, SignUp.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }, 5000);


    }
    public int appGetFirstTimeRun() {

        /*0: If this is the first time.
        1: It has started ever.
        2: It has started once, but not that version , ie it is an update.*/

        //Check if App Start First Time
        SharedPreferences appPreferences = getSharedPreferences("MyAPP", 0);
        int appCurrentBuildVersion = BuildConfig.VERSION_CODE;
        int appLastBuildVersion = appPreferences.getInt("app_first_time", 0);

        //Log.d("appPreferences", "app_first_time = " + appLastBuildVersion);

        if (appLastBuildVersion == appCurrentBuildVersion ) {
            return 1; //yes it has started once before

        } else {
            appPreferences.edit().putInt("app_first_time",
                    appCurrentBuildVersion).apply();
            if (appLastBuildVersion == 0) {
                return 0; // Starting for first time
            } else {
                return 2; // It has started once, but not that version , ie it is an update.
            }
        }
    }

    public void turnOnLocation()
    {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {


                //startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(StartingActivity.this, 1002);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
                {
//                    location=1;
//                    turnOnLocation();
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
                }


            } else {
                Toast.makeText(this, "Error please grant the required permissions", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Couldn't load the app properly due to denial of permissions. Please accept them or continue without some features Try to restart the App");
                builder1.setCancelable(true);

                builder1.setPositiveButton(
                        "Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog alert11 = builder1.create();
                alert11.show();
            }
        }
    }
}