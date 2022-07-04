package com.greenhero.greenheroes;


import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.greenhero.greenheroes.MapsFragment.otherUserId;

public class OtherUserProfile extends Fragment {


    FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
    private String myUserId =myUser.getUid();

    private TextView username,emailId,bottomUsername,bottomEmailId,plant,level,levelInfo;
    private DatabaseReference ref;
    private FirebaseDatabase database;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String name, email;
    private Button addFriend;
    private Space centerSpace;
    private ImageView dp;
    private int totalTreePlanted;
    private int levelId=1;
    private Button home;
    private LinearLayout Trees,InfoBox;
    private RelativeLayout mainContainer;
    private ProgressBar loading,dpLoading;
    private int GALLERY_PICK=1;
    private String dpDownloadUrl;
    public static Uri dpUri;
    private StorageReference mStorageReference = FirebaseStorage.getInstance().getReference();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        setHasOptionsMenu(true);
        View rootview = inflater.inflate(R.layout.profile_fragment,container,false);

        addFriend=(Button)rootview.findViewById(R.id.add_friend);
        centerSpace=(Space)rootview.findViewById(R.id.center_space);
        home=(Button)rootview.findViewById(R.id.home);
        home.setText("Message");
        home.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_chat, 0, 0, 0);
        home.setCompoundDrawablePadding(8);
        home.setPadding(28,0,28,0);
        InfoBox=(LinearLayout)rootview.findViewById(R.id.info_box) ;
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

        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference("Users").child(myUserId).child("Friends");
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    if(dataSnapshot.child("mUserId").getValue(String.class).equals(otherUserId))
                    {
                        addFriend.setVisibility(View.GONE);
                        centerSpace.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference otherRef = FirebaseDatabase.getInstance().getReference("Users").child(otherUserId);
                otherRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String otherName= snapshot.child("userName").getValue(String.class);
                        String otherMail= snapshot.child("emailId").getValue(String.class);
                        String otherProfilePic;
                        if(snapshot.hasChild("profilePic"))
                        {
                            otherProfilePic = snapshot.child("profilePic").getValue(String.class);
                        }
                        else
                        {
                            otherProfilePic ="default";
                        }
                        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Users").child(myUserId);
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("mUserId",otherUserId);
                        hashMap.put("mUserName",otherName);
                        hashMap.put("mEmailId", otherMail);
                        hashMap.put("mprofilePic",otherProfilePic);
                        hashMap.put("bool",true);
                        myRef.child("Friends").push().setValue(hashMap);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                addFriend.setVisibility(View.GONE);
                centerSpace.setVisibility(View.GONE);

            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent messageIntent= new Intent(getActivity(),ChatMessageActivity.class);
                messageIntent.putExtra("otherUserId",otherUserId);
                startActivity(messageIntent);
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
        ref = database.getReference().child("Users").child(otherUserId);
        dataQuery();


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
                    if(dataSnapshot.hasChild("totalTrees"))
                    {
                        totalTreePlanted = dataSnapshot.child("totalTrees").getValue(Integer.class);
                        plant.setText(""+totalTreePlanted);
                        levelId = 1 + (totalTreePlanted/5) ;
                        level.setText(""+levelId);
                        Trees.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(totalTreePlanted>0)
                                {
                                    MainActivity.fragmentManager.beginTransaction().replace(R.id.frame_container,new OtherUserTrees()).addToBackStack(null).commit();
                                }

                            }
                        });
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

        InfoBox.setPadding(0,16,0,0);
        levelInfo.setVisibility(View.GONE);
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
}
