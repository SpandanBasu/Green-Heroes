package com.greenhero.greenheroes;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {

    FirebaseAuth mAuth= FirebaseAuth.getInstance();
    private TextView username,emailId,bottomUsername,bottomEmailId,plant,level,levelInfo;
    private String user_id = mAuth.getCurrentUser().getUid();
    private User user;
    private Button addFriend;
    private Space centerSpace;
    private  DatabaseReference ref;
    private FirebaseDatabase database;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String name, email;
    private ImageView dp;
    private int levelId=1;
    private Button home;
    private LinearLayout Trees;
    private RelativeLayout mainContainer;
    private ProgressBar loading,dpLoading;
    private int GALLERY_PICK=1;
    private String dpDownloadUrl;
    public static Uri dpUri;
    private StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();
    private AdView adView;




    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainActivity.navigationView.getMenu().getItem(0).setChecked(true);
        MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        final View rootview = inflater.inflate(R.layout.profile_fragment,container,false);
        setHasOptionsMenu(true);

    Handler handler = new Handler();
    handler.postDelayed(new Runnable() {
        public void run() {
            try {
            MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
                @Override
                public void onInitializationComplete(InitializationStatus initializationStatus) {
                    adView = rootview.findViewById(R.id.adView);
                    AdRequest adRequest = new AdRequest.Builder().build();
                    adView.loadAd(adRequest);
                }
            });
                }catch(IllegalArgumentException e)
                {
                    e.printStackTrace();
                }
        }
    }, 1000);






        home=(Button)rootview.findViewById(R.id.home);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for(int i = 0; i < MainActivity.fragmentManager.getBackStackEntryCount(); ++i) {
                    MainActivity.fragmentManager.popBackStack();
                }
                MainActivity.fragmentManager.beginTransaction().replace(R.id.frame_container, new MapsFragment()).commit();
            }
        });

        addFriend=(Button)rootview.findViewById(R.id.add_friend);
        centerSpace=(Space)rootview.findViewById(R.id.center_space);
        loading= (ProgressBar)rootview.findViewById(R.id.profile_loading) ;
        mainContainer= (RelativeLayout)rootview.findViewById(R.id.main_content);
        dp=(CircleImageView)rootview.findViewById(R.id.prof_pic);
        username= (TextView)rootview.findViewById(R.id.username);
        emailId= (TextView)rootview.findViewById(R.id.email);
        bottomUsername= (TextView)rootview.findViewById(R.id.username_bottom);
        dpLoading=(ProgressBar)rootview.findViewById(R.id.dp_loading);
        bottomEmailId= (TextView)rootview.findViewById(R.id.email_bottom);
        plant=(TextView)rootview.findViewById(R.id.tree_num);
        level=(TextView)rootview.findViewById(R.id.level);
        levelInfo=(TextView)rootview.findViewById(R.id.level_info);
        swipeRefreshLayout = (SwipeRefreshLayout)rootview.findViewById(R.id.swipe);
        Trees= (LinearLayout)rootview.findViewById(R.id.trees);

        addFriend.setVisibility(View.GONE);
        centerSpace.setVisibility(View.GONE);
        Trees.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(MainActivity.treePlanted>0)
                {
                    MainActivity.fragmentManager.beginTransaction().replace(R.id.frame_container,new MyTreesFragment()).addToBackStack(null).commit();
                }

            }
        });



        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                dataQuery();

                swipeRefreshLayout.setRefreshing(false);
            }
        });


        database = FirebaseDatabase.getInstance();
        ref = database.getReference().child("Users").child(user_id);

        dataQuery();

        dp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_PICK);

            }
        });


        return rootview;
    }
    public void dataQuery()
    {
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                try {

                    if((name= dataSnapshot.child("userName").getValue(String.class))!=null)
                    {
                        username.setText(name);
                        bottomUsername.setText(name);
                    }

                    if((email= dataSnapshot.child("emailId").getValue(String.class))!=null)
                    {
                        emailId.setText(email);
                        bottomEmailId.setText(email);
                    }
                    if(dataSnapshot.hasChild("profilePic"))
                    {
                        if((dpDownloadUrl= dataSnapshot.child("profilePic").getValue(String.class))!=null)
                        {

                            Picasso.get()
                                    .load(dpDownloadUrl)
                                    .placeholder(R.color.black)
                                    .error(R.drawable.groot)
                                    .into(dp);
                            dpLoading.setVisibility(View.INVISIBLE);
                        }
                    }


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

        plant.setText(""+MainActivity.treePlanted);
        levelId = 1 + (MainActivity.treePlanted/5) ;
        level.setText(""+levelId);
        int treeToNextLvl = 5 - (MainActivity.treePlanted % 5);
        levelInfo.setText("Plant "+ treeToNextLvl +" trees to reach the Next Level!");
        loading.setVisibility(View.GONE);
        mainContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.refresh);
        if(item!=null)
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    dataQuery();
                    return true;
                }
            });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_PICK && data!= null)
        {
            Toast.makeText(getContext(), "Data received", Toast.LENGTH_SHORT).show();
            dpUri = data.getData();
            CropImage.activity(dpUri)
                    .setAspectRatio(1,1)
                    .setOutputCompressQuality(50)
                    .start(getContext(),this);
        }

        else if(requestCode==GALLERY_PICK && data== null)
        {
            Toast.makeText(getContext(), "Data Not received", Toast.LENGTH_SHORT).show();
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Toast.makeText(getContext(), "Cropped", Toast.LENGTH_SHORT).show();
                dpUri=result.getUri();
                uploadProfilePic();
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    public void uploadProfilePic()
    {
        dpLoading.setVisibility(View.VISIBLE);
        final StorageReference filePath = mStorageReference.child("Photos").child("ProfilePicture").child(user_id);
        if(dpUri!=null)
        {
            filePath.putFile(dpUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ref.child("profilePic").setValue(String.valueOf(uri)).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    dataQuery();

                                }
                            });

                        }
                    });
                }
            });
        }
        else
        {
            Toast.makeText(getContext(), "Image Not Found!!", Toast.LENGTH_SHORT).show();
        }
    }
}
