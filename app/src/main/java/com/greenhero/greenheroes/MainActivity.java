package com.greenhero.greenheroes;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.greenhero.greenheroes.PlantTreeSlidesFragment.imgUri;
import static com.greenhero.greenheroes.PlantTreeSlidesFragment.successAnimation;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    boolean doubleBackToExitPressedOnce = false;
    Animation bubbleAnim;
    private boolean netWasOff =false;
    double elapsedSeconds=0;
    public static String addressLine;
    public static Toolbar toolbar;
    private String user_id = FirebaseAuth.getInstance().getUid();
    public static Double latitude = 0.00;
    public static Double longitude = 0.00;
    private FrameLayout frameLayout;
    private boolean isConnected;
    private DrawerLayout drawer;
    private FusedLocationProviderClient fusedLocationProviderClient;
    public static FragmentManager fragmentManager;
    private FragmentManager fm;
    public static int treePlanted=0;
    public static FragmentTransaction ft;
    public static NavigationView navigationView;
    final static int REQUEST_CODE=123;
    private String dpDownloadUrl;
    Dialog plantTreeDialog,noTreeDialog,noTreePlantedDialog;
    ImageView closeDialog,closeNoTreeDialog,noTreeBubble;
    CircleImageView plantImage,userPic;
    EditText plantName;
    Button completeBtn,okBtnNoTreeDialog,okNoTreePlantedBtn;
    FirebaseAuth mAuth;
    FirebaseUser user;
    private boolean treeFound =false;
    InputImage imageInputML;

    LocationRequest locationRequest;
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                Log.e("TAG", "onLocationResult: LocationResult is null!!!!! ");
                return;
            }
            for (Location location : locationResult.getLocations()) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                try {
                    Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

                    addressLine = addresses.get(0).getAddressLine(0);
                   try {
                       if (addressLine.contains(addresses.get(0).getSubLocality())) {
                           String[] parts = addressLine.split(", " + addresses.get(0).getSubLocality());
                           addressLine = parts[0];
                       }
                   }
                   catch (NullPointerException e){
                       e.printStackTrace();
                   }


                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    };


    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        SignUp.mainOpen= true;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        countTotalTrees();
        if(user == null)
        {
            Intent intent = new Intent(MainActivity.this, LogIn.class);
            startActivity(intent);
            finish();
        }



        drawer = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(4000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        frameLayout = (FrameLayout) findViewById(R.id.frame_container);
        fragmentManager = getSupportFragmentManager();
        navigationView = findViewById(R.id.nav_view);
        userPic = (CircleImageView) navigationView.getHeaderView(0).findViewById(R.id.user_pic);
        final TextView userName=(TextView)  navigationView.getHeaderView(0).findViewById(R.id.user_name);
        final TextView emailNav=(TextView)  navigationView.getHeaderView(0).findViewById(R.id.email);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference().child("Users").child(user_id);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                try {
//                  String totalTreesNumString = snapshot.getValue().toString;
                    int treePlantedByMe = snapshot.child("totalTrees").getValue(Integer.class);
                    if(treePlantedByMe==0)
                    {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                        public void run() {

                        noTreePlantedDialog= new Dialog(MainActivity.this);
                        noTreePlantedDialog.setContentView(R.layout.no_trees_dialog);
                        okNoTreePlantedBtn = (Button)noTreePlantedDialog.findViewById(R.id.no_tree_btn);
                        noTreeBubble = (ImageView)noTreePlantedDialog.findViewById(R.id.confused_bubble);
                        bubbleAnim= AnimationUtils.loadAnimation(MainActivity.this,R.anim.confused_text_animation);
                        noTreeBubble.setAnimation(bubbleAnim);
                        okNoTreePlantedBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                noTreePlantedDialog.dismiss();
                            }
                        });
                        noTreePlantedDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        noTreePlantedDialog.show();
                        }
                        }, 4000);
                    }
                    String name= snapshot.child("userName").getValue(String.class);
                    String email= snapshot.child("emailId").getValue(String.class);
                    if(snapshot.hasChild("profilePic"))
                    {
                        if((dpDownloadUrl= snapshot.child("profilePic").getValue(String.class))!=null)
                        {

                            Picasso.get()
                                    .load(dpDownloadUrl)
                                    .placeholder(R.drawable.groot)
                                    .error(R.drawable.groot)
                                    .into(userPic);
                        }
                    }

                    userName.setText(""+name);
                    emailNav.setText(""+email);

                }catch (NullPointerException e)
                {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Upload Trees", "Errorrrrrrrrrrrrrrrrrr !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  ");
            }
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close);


        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(2).setChecked(true);
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        userPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ft = fragmentManager.beginTransaction();
                ft.replace(R.id.frame_container, new ProfileFragment()).addToBackStack(null).commit();
                if (drawer.isDrawerOpen(GravityCompat.START)) {
                    drawer.closeDrawer(GravityCompat.START);
                }
            }
        });


        NetworkInfo net = cm.getActiveNetworkInfo();
        isConnected = net != null && net.isConnectedOrConnecting();
        if (!isConnected) {
            netWasOff=true;
            Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
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

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if(ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_FINE_LOCATION)+ ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE))
            {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },REQUEST_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },REQUEST_CODE);
            }
        }
        else
        {
            int intentFragment = getIntent().getExtras().getInt("frgToLoad");
            switch (intentFragment){
                case 1:
                    loadMap();
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.frame_container, new ScanFragment()).commit();
                    break;
                case 2:

                        loadMap();
                        ft = fragmentManager.beginTransaction();
                        ft.replace(R.id.frame_container, new ProfileFragment()).commit();


                    break;
                case 3:
                    loadMap();
//                    ft = fragmentManager.beginTransaction();
//                    ft.replace(R.id.frame_container, new MapsFragment()).commit();
                    break;
                case 4:
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.frame_container, new PlantTreeSlidesFragment()).commit();
                    break;

                case 5:
                    showsplash();
                    loadMap();
                    break;


            }








//            getCurrentLocation();
//            checkSettingsAndStartLocationUpdates();


//                loadMap();
//                Toast.makeText(this, "Map First time", Toast.LENGTH_SHORT).show();

//                final long tStart = System.currentTimeMillis();
//                do
//                {
//                    long tEnd = System.currentTimeMillis();
//                    long tDelta = tEnd - tStart;
//                    elapsedSeconds = tDelta / 1000.0;
//                }while(elapsedSeconds<=4);


//            while(getSupportFragmentManager().findFragmentById(R.id.map) == null)
//            {
//                getCurrentLocation();
//                checkSettingsAndStartLocationUpdates();
//            }


        }

    }



    private void checkSettingsAndStartLocationUpdates() {

        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();

        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask = client.checkLocationSettings(request);
        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                if (e instanceof ResolvableApiException) {
                    ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                    try {
                        resolvableApiException.startResolutionForResult(MainActivity.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {

        try {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    private void stopLocationUpdates() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    private void getCurrentLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        try {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();


                        ft = fragmentManager.beginTransaction();
                        ft.replace(R.id.frame_container, new MapsFragment()).commit();


                        //getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new MapsFragment()).commit();

                    }

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("TAG", "onFailure: Can not load location!! ");
                }
            });
        }catch(NullPointerException e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {

            case R.id.log_out_menu:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LogIn.class);
                startActivity(intent);
                finish();
                return true;


            case R.id.refresh:
                loadMap();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if ((grantResults.length > 0) && (grantResults[0]== PackageManager.PERMISSION_GRANTED || grantResults[1]== PackageManager.PERMISSION_GRANTED)) {

                   loadMap();

            } else
                {
                Toast.makeText(this, "Error please grant the required permissions", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Couldn't load the app properly due to denial of permissions. Please restart and accept them or continue without some features");
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
        else if(requestCode == 1)
        {
            if ((grantResults.length > 0) && (grantResults[0]== PackageManager.PERMISSION_GRANTED))
            {

                Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 5);

//                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
//                builder1.setMessage("Storage permission granted successfully, Now you can capture the image");
//                builder1.setCancelable(true);
//
//                builder1.setPositiveButton(
//                        "Ok",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//                                dialog.cancel();
//                            }
//                        });
//
//                AlertDialog alert11 = builder1.create();
//                alert11.show();
            }
            else
            {
                Toast.makeText(this, "Error couldn't access the storage", Toast.LENGTH_LONG).show();
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
                builder1.setMessage("Couldn't access the storage, Please give the storage permission Or Continue without some feature");
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

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }

        else {

            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else if (!doubleBackToExitPressedOnce) {
                this.doubleBackToExitPressedOnce = true;
                Toast.makeText(this,"Please click BACK again to exit.", Toast.LENGTH_SHORT).show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            } else {
                super.onBackPressed();
                return;
            }
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                if(!navigationView.getMenu().getItem(0).isChecked())
                {
                    fragmentManager.popBackStack();
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.frame_container, new ProfileFragment());
                    ft.addToBackStack(null);
                    ft.commit();

                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        });

                    }
                }
                break;

            case R.id.scan:
                if(!navigationView.getMenu().getItem(1).isChecked())
                {
                    fragmentManager.popBackStack();
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.frame_container, new ScanFragment());
                    ft.addToBackStack(null);
                    ft.commit();
                    // getSupportFragmentManager().beginTransaction().replace(R.id.frame_container,new ScanFragment()).commit();
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        });
                    }
                }
                break;

            case R.id.map:

                    for(int i = 0; i < MainActivity.fragmentManager.getBackStackEntryCount(); ++i) {
                        MainActivity.fragmentManager.popBackStack();
                    }
                    MainActivity.fragmentManager.beginTransaction().replace(R.id.frame_container, new MapsFragment()).commit();
                    // getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new MapsFragment()).commit();

                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        });
                    }
                
//                ft = fragmentManager.beginTransaction();
//                ft.replace(R.id.frame_container, new MapsFragment());
//                ft.addToBackStack(null);
//                ft.commit();

                break;

            case R.id.plant_tree:
                if(!navigationView.getMenu().getItem(3).isChecked()) {

                    fragmentManager.popBackStack();
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.frame_container, new PlantTreeSlidesFragment());
                    ft.addToBackStack(null);
                    ft.commit();
                    //getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new PlantTreeFragment()).commit();
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        });
                    }
                }
                break;

            case R.id.messages_menu:
                if(!navigationView.getMenu().getItem(4).isChecked()) {
                    fragmentManager.popBackStack();
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.frame_container, new AllMessages());
                    ft.addToBackStack(null);
                    ft.commit();
                    //getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new ReportFragment()).commit();
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        });
                    }
                }
                break;

            case R.id.friends_menu:
                if(!navigationView.getMenu().getItem(5).isChecked()) {

                    fragmentManager.popBackStack();
                    ft = fragmentManager.beginTransaction();
                    ft.replace(R.id.frame_container, new FriendsFragment());
                    ft.addToBackStack(null);
                    ft.commit();
                    //getSupportFragmentManager().beginTransaction().replace(R.id.frame_container, new ReportFragment()).commit();
                    if (drawer.isDrawerOpen(GravityCompat.START)) {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                drawer.closeDrawer(GravityCompat.START);
                            }
                        });
                    }
                }
                break;

            case R.id.share:
                Toast.makeText(this, "Share Our App", Toast.LENGTH_SHORT).show();
                ShareCompat.IntentBuilder.from(MainActivity.this)
                        .setType("text/plain")
                        .setChooserTitle("App")
                        .setText("Want to save the Planet? Become a green Hero today! Plant as many trees as you can !! and motivate others too! \n"+"http://play.google.com/store/apps/details?id=" + MainActivity.this.getPackageName())
                        .startChooser();
                break;

            case R.id.rate_us:

                Toast.makeText(this, "Rate Us!", Toast.LENGTH_SHORT).show();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + this.getPackageName())));
                } catch (android.content.ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                //Toast.makeText(this, "Rate Us in Google Playstore", Toast.LENGTH_SHORT).show();
                break;

            case R.id.log_out:
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(MainActivity.this, LogIn.class);
                startActivity(intent);
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode==5 && resultCode== RESULT_OK)
        {

            if(data != null) {

                final Bitmap image = (Bitmap) data.getExtras().get("data");
                imgUri = getImageUri(MainActivity.this, image);


                try {
                    imageInputML = InputImage.fromFilePath(MainActivity.this, imgUri);
                    ImageLabeler labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS);
                    labeler.process(imageInputML)
                            .addOnSuccessListener(new OnSuccessListener<List<ImageLabel>>() {
                                @Override
                                public void onSuccess(List<ImageLabel> labels) {
                                    // Task completed successfully
                                    for (ImageLabel label : labels) {

                                        String text = label.getText();

                                        float confidence = label.getConfidence();
                                        int index = label.getIndex();

                                        if(text.equals("Plants") || text.equals("Plant") || text.equals("Tree") || text.equals("Soil")|| text.equals("Green") || text.equals("Vegetable") || text.equals("Vegetables") || text.equals("Flower") || text.equals("Fruit") || text.equals("Leaf") || text.equals("Trunk"))
                                        {
                                            treeFound=true;
                                            if(imgUri!=null)
                                            {
                                                Log.v("uri of image", "uri of image is here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" + imgUri.toString());
                                                plantTreeDialog = new Dialog(MainActivity.this);
                                                plantTreeDialog.setContentView(R.layout.popup_plant_tree);
                                                closeDialog = (ImageView) plantTreeDialog.findViewById(R.id.close_dialog_plant_tree);
                                                plantImage = (CircleImageView) plantTreeDialog.findViewById(R.id.plant_image);
                                                plantName = (EditText) plantTreeDialog.findViewById(R.id.plant_name);
                                                completeBtn = (Button) plantTreeDialog.findViewById(R.id.complete_plant_tree_btn);

                                                //plantImage.setImageURI(imgUri);

                                                plantImage.setImageBitmap(image);
                                                closeDialog.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        plantTreeDialog.dismiss();
                                                    }
                                                });

                                                completeBtn.setOnClickListener(new View.OnClickListener() {
                                                    @SuppressLint("NewApi")
                                                    @Override
                                                    public void onClick(View view) {
                                                        if (TextUtils.isEmpty(plantName.getText().toString().trim())) {
                                                            plantName.setError("Name is required");
                                                            plantName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                                                            plantName.requestFocus();
                                                            return;
                                                        }
                                                        successAnimation.setVisibility(View.VISIBLE);
                                                        successAnimation.playAnimation();
                                                        plantTreeDialog.dismiss();
                                                        CreateNewTree newTree = new CreateNewTree(latitude, longitude, plantName.getText().toString().trim());
                                                        UploadTree newUploadTree = new UploadTree(newTree,MainActivity.this);
                                                        newUploadTree.uploadTree();
                                                        Toast.makeText(MainActivity.this, "The data uploaded to server successfully going for verification...", Toast.LENGTH_LONG).show();
                                                        Log.e("TAG", "Data uploaded successfully for tree!!!!!");
                                                    }
                                                });
                                                plantTreeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                                plantTreeDialog.show();
                                            }
                                            else
                                            {
                                                Toast.makeText(MainActivity.this, "Could not find image uri!!!", Toast.LENGTH_SHORT).show();
                                            }
                                            break;

                                        }
                                        Log.e("TAG", "Labels are!!!!!!!!!! "+text+"     " );
                                    }
                                    if(!treeFound)
                                    {

                                        noTreeDialog = new Dialog(MainActivity.this);
                                        noTreeDialog.setContentView(R.layout.no_tree_found_dialog);
                                        closeNoTreeDialog = (ImageView) noTreeDialog.findViewById(R.id.close_dialog_no_tree);
                                        okBtnNoTreeDialog = (Button) noTreeDialog.findViewById(R.id.no_tree_found_ok_btn);
                                        closeNoTreeDialog.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                noTreeDialog.dismiss();
                                            }
                                        });
                                        okBtnNoTreeDialog.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                noTreeDialog.dismiss();

                                            }
                                        });
                                        noTreeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                        noTreeDialog.show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                    // Task failed with an exception
                                    // ...
                                }
                            });
                } catch (IOException e) {
                    e.printStackTrace();
                }







            }
            else
            {
                Toast.makeText(MainActivity.this, "Image Not Found!!", Toast.LENGTH_SHORT).show();
                plantTreeDialog = new Dialog(MainActivity.this);
                plantTreeDialog.setContentView(R.layout.popup_plant_tree);
                closeDialog = (ImageView) plantTreeDialog.findViewById(R.id.close_dialog_plant_tree);
                plantImage = (CircleImageView) plantTreeDialog.findViewById(R.id.plant_image);
                plantName = (EditText) plantTreeDialog.findViewById(R.id.plant_name);
                completeBtn = (Button) plantTreeDialog.findViewById(R.id.complete_plant_tree_btn);


                //plantImage.setImageURI(imgUri);
                imgUri = getImageUri(MainActivity.this, BitmapFactory.decodeResource(getResources(), R.drawable.tree));
                plantImage.setImageResource(R.drawable.tree);
                closeDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        plantTreeDialog.dismiss();

                    }
                });

                completeBtn.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View view) {
                        if (TextUtils.isEmpty(plantName.getText().toString().trim())) {
                            plantName.setError("Name is required");
                            plantName.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                            plantName.requestFocus();
                            return;
                        }
                        plantTreeDialog.dismiss();

                        CreateNewTree newTree = new CreateNewTree(latitude, longitude, plantName.getText().toString().trim());
                        UploadTree newUploadTree = new UploadTree(newTree,MainActivity.this);
                        newUploadTree.uploadTree();
                        Toast.makeText(MainActivity.this, "The data uploaded to server successfully going for verification...", Toast.LENGTH_LONG).show();
                    }
                });
                plantTreeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                plantTreeDialog.show();

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public void loadMap()
    {
        final ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net= cm.getActiveNetworkInfo();
        boolean isConnected = net!= null && net.isConnectedOrConnecting();

        if(isConnected)
        {

            if (getSupportFragmentManager().findFragmentById(R.id.map) == null)
            {

                    getCurrentLocation();
                    checkSettingsAndStartLocationUpdates();


                if(netWasOff)
                {
                    Log.e("TAG", "Net was off!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  " );
                    for(int i = 0; i < MainActivity.fragmentManager.getBackStackEntryCount(); ++i) {
                        MainActivity.fragmentManager.popBackStack();
                    }
                    MainActivity.fragmentManager.beginTransaction().replace(R.id.frame_container, new MapsFragment()).commit();
                    netWasOff=false;
                }
                else {
                    Log.e("TAG", "Internet present !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
                }
            }
            else {
                Log.e("TAG", "loadMap: here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!  fragment map is not null!!!!!!!!!!!!! " );
            }

        }
        else
        {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(MainActivity.this);
            builder1.setMessage("No internet connection ! can not load map !");
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

    public void countTotalTrees()
    {
    try {
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user_id).child("Trees");
    ref.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            treePlanted = 0;
            if (snapshot.hasChildren()) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    treePlanted++;
                }
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {

        }
    });
    }catch(NullPointerException e)
    {
        e.printStackTrace();
    }

    }


    public void showsplash() {

        final Dialog dialog = new Dialog(MainActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_starting);
        ImageView tree= (ImageView) dialog.findViewById(R.id.img_splash);
        TextView text= (TextView) dialog.findViewById(R.id.text_splash);
        //Animation imgAnim= AnimationUtils.loadAnimation(MainActivity.this,R.anim.splash_img_small);
        //Animation textAnim= AnimationUtils.loadAnimation(MainActivity.this,R.anim.text_splash);
        //tree.setAnimation(imgAnim);
        //text.setAnimation(textAnim);
        dialog.setCancelable(true);
        dialog.show();

        final Handler handler  = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                {
                    dialog.dismiss();
                }
            }
        };
        handler.postDelayed(runnable, 4000);
    }



}