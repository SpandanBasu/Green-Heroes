package com.greenhero.greenheroes;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendsFragment extends Fragment {

    FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
    private String myUserId =myUser.getUid();
    private String otherUserId;
    private Dialog friendDialog;
    private Button messageBtn;
    private RelativeLayout aloneBox;
    CircleImageView userImageView;
    ImageView closeDialog;
    SwipeRefreshLayout refresh;
    List<User> mUser;
    RecyclerView recyclerView;
    FriendsAdapter friendsAdapter;
    GridLayoutManager gridLayoutManager;
    String otherProfilePic;
    EditText friendEmailInput;
    ImageButton searchBtn;
    TextView friendNameTextView,friendEmailTextView;
    AdView adViewTop,adViewBottom;
    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.navigationView.getMenu().getItem(5).setChecked(true);
        MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        final View rootView = inflater.inflate(R.layout.fragment_friends, container, false);
        setHasOptionsMenu(true);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                        adViewTop =rootView.findViewById(R.id.adView_allfriends_top);
                        adViewBottom =rootView.findViewById(R.id.adView_allfriends_bottom);

                        AdRequest adRequest = new AdRequest.Builder().build();
                        adViewTop.loadAd(adRequest);
                        adViewBottom.loadAd(adRequest);
                    }
                });

            }
        }, 500);

        recyclerView=(RecyclerView)rootView.findViewById(R.id.friend_recycler_list);
        friendEmailInput=(EditText)rootView.findViewById(R.id.search_mail);
        searchBtn=(ImageButton)rootView.findViewById(R.id.search_btn);
        aloneBox =(RelativeLayout)rootView.findViewById(R.id.alone_box);
        recyclerView.setHasFixedSize(true);
        mUser = new ArrayList<>();
        refresh = (SwipeRefreshLayout)rootView.findViewById(R.id.refresh_my_friends);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                dataQuery();
                refresh.setRefreshing(false);
            }
        });

        dataQuery();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String friendEmail = friendEmailInput.getText().toString().trim();
                Log.e("TAG", "my email is: " + myUser.getEmail());
                if (!friendEmail.equals(myUser.getEmail())) {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (final DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                if (dataSnapshot.child("emailId").getValue(String.class).equals(friendEmail)) {
                                    friendDialog = new Dialog(getContext());
                                    friendDialog.setContentView(R.layout.friend_dialog);
                                    closeDialog = (ImageView) friendDialog.findViewById(R.id.close_dialog_friend);
                                    friendNameTextView = (TextView) friendDialog.findViewById(R.id.user_name_friend_dialog);
                                    friendEmailTextView = (TextView) friendDialog.findViewById(R.id.user_email_friend_dialog);
                                    userImageView = (CircleImageView) friendDialog.findViewById(R.id.user_image_friend_dialog);
                                    messageBtn = (Button) friendDialog.findViewById(R.id.msg_btn_friend_dialog);



                                    friendNameTextView.setText(dataSnapshot.child("userName").getValue(String.class));
                                    friendEmailTextView.setText(dataSnapshot.child("emailId").getValue(String.class));
                                    if (dataSnapshot.hasChild("profilePic")) {
                                        Picasso.get()
                                                .load(dataSnapshot.child("profilePic").getValue(String.class))
                                                .placeholder(R.drawable.groot)
                                                .error(R.drawable.groot)
                                                .into(userImageView);
                                    }

                                    closeDialog.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            friendDialog.dismiss();
                                        }
                                    });

                                    messageBtn.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            friendDialog.dismiss();
                                            Intent messageIntent = new Intent(getActivity(), ChatMessageActivity.class);
                                            messageIntent.putExtra("otherUserId", dataSnapshot.child("userId").getValue(String.class));
                                            startActivity(messageIntent);
                                        }
                                    });

                                    friendDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                                    friendDialog.show();

                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });




        return rootView;
    }

    private void dataQuery() {
        mUser.clear();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users").child(myUserId).child("Friends");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUser.clear();
                if(!snapshot.hasChildren())
                {
                    aloneBox.setVisibility(View.VISIBLE);
                }
                else
                {
                    aloneBox.setVisibility(View.GONE);
                }
                for(DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    String otherId= dataSnapshot.child("mUserId").getValue(String.class);
                    String otherName= dataSnapshot.child("mUserName").getValue(String.class);
                    String otherMail= dataSnapshot.child("mEmailId").getValue(String.class);

                    if(dataSnapshot.hasChild("mprofilePic"))
                    {
                        otherProfilePic = dataSnapshot.child("mprofilePic").getValue(String.class);
                    }
                    else
                    {
                        otherProfilePic ="default";
                    }

                    User user = new User(otherId,otherName,otherMail,otherProfilePic,true);
                    mUser.add(user);
                }
                friendsAdapter = new FriendsAdapter(mUser,getContext());
                gridLayoutManager = new GridLayoutManager(getContext(),2,GridLayoutManager.VERTICAL,false);
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.setAdapter(friendsAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item=menu.findItem(R.id.refresh);
        if(item!=null)
            item.setVisible(false);
    }
}