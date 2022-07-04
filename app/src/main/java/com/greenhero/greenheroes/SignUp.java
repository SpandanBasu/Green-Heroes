package com.greenhero.greenheroes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
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
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp extends AppCompatActivity {

    private String mFullName;
    public static int firstTime=0;
    private String mEmail;
    private String mPassword;
    private ImageView signUpBtn;
    private TextView logIn;
    public static FirebaseAuth mAuth;
    //public static FirebaseAuth.AuthStateListener firebaseAuthListener;
    private EditText name;
    private EditText email;
    private EditText password;
    private EditText re_password;
    private ProgressBar progressBar;
    private boolean pass_match= false;
    private boolean isConnected;
    public static String user_id;
    public static DatabaseReference current_user_db;
    public static User newUser;
    public static FirebaseUser user;
    static final int REQUEST_CODE= 123;
    public static boolean mainOpen= false;
    public static int location=0;
    private boolean flag= false;
    //private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN= 1234;
    //private LinearLayout GoogleButton,FacebookButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        firstTime = appGetFirstTimeRun();
        signUpBtn= (ImageView)findViewById(R.id.sign_up_btn);
        logIn =(TextView)findViewById(R.id.log_in) ;
        logIn.setPaintFlags(logIn.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        name= (EditText)findViewById(R.id.name) ;
        email=(EditText) findViewById(R.id.email_register);
        password=(EditText)findViewById(R.id.password_register);
        re_password=(EditText)findViewById(R.id.re_password_register);
        progressBar=(ProgressBar)findViewById(R.id.progress_bar) ;
       //GoogleButton = (LinearLayout)findViewById(R.id.google_sign_btn);
       //FacebookButton = (LinearLayout)findViewById(R.id.facebook_sign_btn);


        final ConnectivityManager cm= (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);


        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            location=1;
            turnOnLocation();

        }


        //createRequestGoogle();
//        GoogleButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                signInGoogle();
//            }
//        });

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View view) {

               mFullName = name.getText().toString().trim();
               mEmail= email.getText().toString().trim();
               mPassword=password.getText().toString();

                NetworkInfo net= cm.getActiveNetworkInfo();
                isConnected = net!= null && net.isConnectedOrConnecting();

               if(!isConnected)
               {
                   Toast.makeText(SignUp.this ,"No Internet Connection", Toast.LENGTH_SHORT).show();
                   return;
               }



                if(TextUtils.isEmpty(mFullName))
                {
                    name.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    name.setError(getText(R.string.name_required));
                    name.requestFocus();
                    return;
                }

               if(TextUtils.isEmpty(mEmail))
               {
                   name.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                   email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                   email.setError(getText(R.string.email_required));
                   email.requestFocus();
                   return;
               }



                if(TextUtils.isEmpty(mPassword))
                {
                    name.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                    email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                    password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                    password.setError(getText(R.string.pass_required));
                    password.requestFocus();
                    return;
                }

               if(password.getText().toString().length()<6)
               {
                   name.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                   email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                   password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                   password.setError(getString(R.string.six_char));
                   password.requestFocus();

               }
               else
               {
                   if(password.getText().toString().equals(re_password.getText().toString()))
                   {
                       pass_match=true;

                   }
                   else
                   {
                       name.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                       email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                       password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                       re_password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                       re_password.setError(getString(R.string.pass_dont_match));
                       re_password.requestFocus();
                       return;

                   }


               }
               if(pass_match==true){
                   signUpBtn.setImageResource(0);
                   progressBar.setVisibility(View.VISIBLE);
                   final long tStart = System.currentTimeMillis();
                    mAuth.createUserWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful())
                            {
                                Toast.makeText(SignUp.this ,"Successfully created the user", Toast.LENGTH_SHORT).show();
                                user_id = mAuth.getCurrentUser().getUid();
                                newUser = new User(user_id,mFullName,mEmail,mPassword);
                                current_user_db = FirebaseDatabase.getInstance().getReference("Users");
                                current_user_db.child(user_id).setValue(newUser);


                                Intent intent = new Intent(SignUp.this ,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                if(location==1)
                                {
                                    intent.putExtra("frgToLoad", 2);
                                }
                                else
                                {
                                    intent.putExtra("frgToLoad", 3);
                                }

                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                long tEnd = System.currentTimeMillis();
                                long tDelta = tEnd - tStart;
                                double elapsedSeconds = tDelta / 1000.0;
                                signUpBtn.setImageResource(R.drawable.ic_arrow_right);
                                progressBar.setVisibility(View.INVISIBLE);

                                if(elapsedSeconds>8)
                                {

                                    Toast.makeText(SignUp.this ,"Couldn't create your account check your Internet Connection", Toast.LENGTH_SHORT).show();
                                    return;

                                }

                                else
                                {
                                    try {
                                        throw task.getException();
                                    }  catch(FirebaseAuthInvalidCredentialsException e) {
                                        re_password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                                        email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                                        email.setError(getText(R.string.malformed_email));
                                        email.requestFocus();
                                    } catch(FirebaseAuthUserCollisionException e) {
                                         re_password.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.lightGray)));
                                         email.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.red)));
                                         email.setError(getText(R.string.email_in_use));
                                         email.requestFocus();
                                    } catch(Exception e) {
                                        Log.e("SignUpActivity", e.getMessage());
                                    }

                                }


                                Toast.makeText(SignUp.this ,"Error Couldn't Create the User recheck the data", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }


            }
        });

        logIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpBtn.setImageResource(0);
                progressBar.setVisibility(View.VISIBLE);
                startActivityForResult(new Intent(SignUp.this , LogIn.class),101);
                signUpBtn.setImageResource(R.drawable.ic_arrow_right);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

//    private void signInGoogle() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
//    }
//
//    private void createRequestGoogle() {
//        // Configure Google Sign In
//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//                .requestIdToken(getString(R.string.default_web_client_id))
//                .requestEmail()
//                .build();
//
//        // Build a GoogleSignInClient with the options specified by gso.
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
//
//    }
//
//    private void firebaseAuthWithGoogle(String idToken) {
//        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
//        mAuth.signInWithCredential(credential)
//                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if (task.isSuccessful()) {
//                            // Sign in success, update UI with the signed-in user's information
//                            Log.d("TAG", "signInWithCredential:success");
//                            FirebaseUser user = mAuth.getCurrentUser();
//                            Intent intent = new Intent(SignUp.this ,MainActivity.class);
//                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            intent.putExtra("frgToLoad", 2);
//                            startActivity(intent);
//                            finish();
//
//                        } else {
//                            // If sign in fails, display a message to the user.
//                            Log.w("TAG", "signInWithCredential:failure", task.getException());
//
//                        }
//
//                        // ...
//                    }
//                });
//    }

    @Override
    protected void onStart() {
        super.onStart();

       // mAuth.addAuthStateListener(firebaseAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
       // mAuth.removeAuthStateListener(firebaseAuthListener);
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
                        resolvableApiException.startResolutionForResult(SignUp.this, 1001);
                    } catch (IntentSender.SendIntentException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
//                firebaseAuthWithGoogle(account.getIdToken());
//
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//                Log.e("TAG", "Google sign in failed", e);
//                // ...
//            }
//        }
//    }



}