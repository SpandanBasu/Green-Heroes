package com.greenhero.greenheroes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.res.ColorStateList;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LogIn extends AppCompatActivity {

    private ImageView logInBtn;
    private TextView signUp;
    private TextView forgot;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private EditText email;
    private EditText password;
    private String mEmail;
    private String mPassword;
    private boolean isConnected;
    private int location=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        logInBtn= (ImageView)findViewById(R.id.login_btn);
        signUp =(TextView)findViewById(R.id.sign_up) ;
        forgot=(TextView)findViewById(R.id.forgot) ;
        email=(EditText) findViewById(R.id.email_login);
        password=(EditText)findViewById(R.id.password_login);
        progressBar=(ProgressBar)findViewById(R.id.progress_bar_log) ;
        mAuth= FirebaseAuth.getInstance();

        final ConnectivityManager cm= (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            location=1;
            turnOnLocation();

        }


        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        logInBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {

                mEmail= email.getText().toString().trim();
                mPassword=password.getText().toString();


                NetworkInfo net= cm.getActiveNetworkInfo();
                isConnected = net!= null && net.isConnectedOrConnecting();

                if(!isConnected)
                {
                    Toast.makeText(LogIn.this ,"No Internet Connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(mEmail))
                {
                    email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    email.setError(getText(R.string.email_required));
                    return;
                }

                if(TextUtils.isEmpty(mPassword))
                {
                    password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    password.setError(getText(R.string.pass_required));
                    return;
                }
                logInBtn.setImageResource(0);
                progressBar.setVisibility(View.VISIBLE);
                final long tStart = System.currentTimeMillis();
                mAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(LogIn.this ,"Successfully LoggedIn the user", Toast.LENGTH_SHORT).show();
                            //Checking if password is changed or not
                            FirebaseUser user= mAuth.getCurrentUser();

                            if(user!= null)
                            {
                                try {
                                    String userId = user.getUid();
                                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String passwordSaved = snapshot.child("password").getValue(String.class);
                                            if (!passwordSaved.equals(mPassword)) {
                                                snapshot.child("password").getRef().setValue(mPassword);
                                            }

                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }catch(DatabaseException e)
                                {
                                    e.printStackTrace();
                                }
                            }













                            Intent intent = new Intent(LogIn.this ,MainActivity.class);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            if(StartingActivity.firstTime==0)
                            {
                                    intent.putExtra("frgToLoad", 2);
                            }
                            else
                            {
                                if(StartingActivity.location==1)
                                {
                                    intent.putExtra("frgToLoad", 2);
                                }
                                else if(SignUp.location==1)
                                {
                                    intent.putExtra("frgToLoad", 2);
                                }
                                else if(location==1)
                                {
                                    intent.putExtra("frgToLoad", 2);
                                }
                                else
                                {
                                    intent.putExtra("frgToLoad", 3);
                                }

                            }


                            startActivity(intent);
                            finishActivity(101);

                            finish();
                        }
                        else
                        {
                            long tEnd = System.currentTimeMillis();
                            long tDelta = tEnd - tStart;
                            double elapsedSeconds = tDelta / 1000.0;
                            logInBtn.setImageResource(R.drawable.ic_arrow_right);
                            progressBar.setVisibility(View.INVISIBLE);



                            if(elapsedSeconds>8)
                            {

                                Toast.makeText(LogIn.this ,"Couldn't Log you in try to check your Internet Connection", Toast.LENGTH_SHORT).show();
                                return;

                            }

                            else
                            {
                                email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                                password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                                password.setText("");
                                password.setError("email_id or password is wrong please check again");

                            }




                        }
                    }
                });


            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logInBtn.setImageResource(0);
                progressBar.setVisibility(View.VISIBLE);
                Intent intent = new Intent(LogIn.this,SignUp.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });

        forgot.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {
                logInBtn.setImageResource(0);
                progressBar.setVisibility(View.VISIBLE);
                mEmail= email.getText().toString().trim();
                mPassword=password.getText().toString();


                NetworkInfo net= cm.getActiveNetworkInfo();
                isConnected = net!= null && net.isConnectedOrConnecting();

                if(!isConnected)
                {
                    logInBtn.setImageResource(R.drawable.ic_arrow_right);
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(LogIn.this ,"No Internet Connection", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(mEmail.equals(""))
                {
                    logInBtn.setImageResource(R.drawable.ic_arrow_right);
                    progressBar.setVisibility(View.INVISIBLE);
                    email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                    email.setError("Please enter your Registered Email_ID");
                    password.setText("");
                    return;
                }

                password.setText("");

                mAuth.sendPasswordResetEmail(mEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            logInBtn.setImageResource(R.drawable.ic_arrow_right);
                            progressBar.setVisibility(View.INVISIBLE);
                            email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorDeepGreen)));

                            AlertDialog.Builder builder1 = new AlertDialog.Builder(LogIn.this);
                            builder1.setMessage("A mail with Reset-Password-link is sent to your Registered Email");
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
                        else
                        {
                            logInBtn.setImageResource(R.drawable.ic_arrow_right);
                            progressBar.setVisibility(View.INVISIBLE);
                            email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                            password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                            email.setError("Please enter your Registered Email_ID");
                            password.setText("");
                            AlertDialog.Builder builder1 = new AlertDialog.Builder(LogIn.this);
                            builder1.setMessage("We couldn't find your email in our database, Please try to SignUp");
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
                });


            }
        });

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
                        resolvableApiException.startResolutionForResult(LogIn.this, 1003);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }
}