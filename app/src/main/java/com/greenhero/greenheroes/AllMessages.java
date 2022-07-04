package com.greenhero.greenheroes;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

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

import java.util.ArrayList;
import java.util.List;


public class AllMessages extends Fragment {


    FirebaseUser myUser = FirebaseAuth.getInstance().getCurrentUser();
    private String myUserId =myUser.getUid();
    private String otherUserId="qG2TvyPQs0MNfsWTDFgUKso80uz1";
    private RelativeLayout noChatBox;
    List<User> mUser;
    String otherUserNameString;
    String otherUserImageUrlString;
    private boolean chatFound=false;
    List<Chat> mChat;
    DatabaseReference refChat= FirebaseDatabase.getInstance().getReference("Chats");
    RecyclerView recyclerView;
    AllMessagesAdapter allMessagesAdapter;
    AdView adViewTop,adViewBottom;


    public AllMessages() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MainActivity.navigationView.getMenu().getItem(4).setChecked(true);
        MainActivity.toolbar.setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        MainActivity.navigationView.getHeaderView(0).findViewById(R.id.container).setBackground(new ColorDrawable(Color.parseColor("#008a00")));
        final View rootview  =  inflater.inflate(R.layout.fragment_all_messages, container, false);
        setHasOptionsMenu(true);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                MobileAds.initialize(getContext(), new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {
                        adViewTop =rootview.findViewById(R.id.adView_allmessages_top);
                        adViewBottom =rootview.findViewById(R.id.adView_allmessages_bottom);

                        AdRequest adRequest = new AdRequest.Builder().build();
                        adViewTop.loadAd(adRequest);
                        adViewBottom.loadAd(adRequest);
                    }
                });

            }
        }, 1000);

        recyclerView=(RecyclerView)rootview.findViewById(R.id.recycler_view_allmsg);
        noChatBox=(RelativeLayout)rootview.findViewById(R.id.no_message_box);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        mChat = new ArrayList<>();
        mUser= new ArrayList<>();


        dataQuery();



        return rootview;
    }

public void dataQuery()
{

    refChat.addValueEventListener(new ValueEventListener() {

        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            boolean chatFound=false;
            mChat.clear();
            List<String> userId= new ArrayList<>();
            List<Chat> chatId= new ArrayList<>();
            int pos=-1;

            for(DataSnapshot dataSnapshot: snapshot.getChildren())
            {
                pos++;

                Chat chat = dataSnapshot.getValue(Chat.class);
                if(chat.getReceiver().equals(myUserId) || chat.getSender().equals(myUserId))
                {

                    //Toast.makeText(getContext(), "chatAdded", Toast.LENGTH_SHORT).show();
                    if(chat.getReceiver().equals(myUserId))
                    {
                        otherUserId = chat.getSender();
                    }
                    else
                    {
                        otherUserId = chat.getReceiver();
                    }

                    if(dataSnapshot.hasChild("deleted"))
                    {
                        if(!dataSnapshot.child("deleted").getValue(String.class).equals(myUserId))
                        {
                            chatFound=true;
                            if(!userId.contains(otherUserId))
                            {
                                userId.add(otherUserId);
                                mChat.add(chat);
                            }
                            else
                            {
                                int index =  userId.indexOf(otherUserId);
                                mChat.remove(index);
                                userId.remove(index);
                                userId.add(otherUserId);
                                mChat.add(chat);
                            }
                        }
                    }
                    else
                    {
                        chatFound=true;
                        if(!userId.contains(otherUserId))
                        {
                            userId.add(otherUserId);
                            mChat.add(chat);
                        }
                        else
                        {
                            int index =  userId.indexOf(otherUserId);
                            mChat.remove(index);
                            userId.remove(index);
                            userId.add(otherUserId);
                            mChat.add(chat);
                        }
                    }


                }

            }
            if(chatFound)
            {
                Log.e("TAG", "Chat are present!!!!!!!!!!!!");
                noChatBox.setVisibility(View.GONE);
            }
            else
            {
                noChatBox.setVisibility(View.VISIBLE);
            }
            allMessagesAdapter = new AllMessagesAdapter(getContext(), mChat);
            recyclerView.setAdapter(allMessagesAdapter);
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
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    dataQuery();
                    return true;
                }
            });
    }



    //    public void createUser()
//    {
//
//        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Users");
//
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                for(DataSnapshot dataSnapshot: snapshot.getChildren())
//                {
//                    if(dataSnapshot.child("userId").getValue(String.class).equals(otherUserId))
//                    {
//                        Log.e("TAG", otherUserId+" Triggered!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!" );
//                        otherUserNameString = dataSnapshot.child("userName").getValue(String.class);
//                        if(dataSnapshot.hasChild("profilePic"))
//                        {
//                            otherUserImageUrlString=dataSnapshot.child("profilePic").getValue(String.class);
//                        }
//                        else
//                        {
//                           otherUserImageUrlString ="default";
//                        }
//
//                        User otherUser = new User(otherUserId,otherUserNameString,otherUserImageUrlString);
//                        mUser.add(otherUser);
//                    }
//
//                }
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//    }
}