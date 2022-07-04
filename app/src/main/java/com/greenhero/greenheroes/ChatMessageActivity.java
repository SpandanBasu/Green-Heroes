package com.greenhero.greenheroes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatMessageActivity extends AppCompatActivity {


    public static boolean otherUserProfileChatIsVisiible=false;
    private TextView UserNameTop;
    private CircleImageView userImgViewTop;
    private Toolbar toolbar;
    public static String otherUserId;
    public static FragmentManager fragmentManagerChatActivity;
    public static FragmentTransaction fragmentTransaction;
    private FrameLayout frame;
    DatabaseReference referenceSeen;
    ValueEventListener seenListener;
    ImageButton buttonSend;
    EditText messageText;
    Intent intent;
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;
    String otherUserName;
    String otherUserImageUrl="default";
    FirebaseUser mUser= FirebaseAuth.getInstance().getCurrentUser();
    String myUserId= mUser.getUid();
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_message);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        buttonSend=(ImageButton)findViewById(R.id.btn_send);
        messageText=(EditText) findViewById(R.id.msg_text);
        frame= (FrameLayout)findViewById(R.id.frame_chat_profile);
        UserNameTop=(TextView)findViewById(R.id.user_top);
        userImgViewTop=(CircleImageView)findViewById(R.id.img_top);
        recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        setSupportActionBar(toolbar);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(this.getResources().getColor(R.color.black));
        }
        intent=getIntent();
        otherUserId= intent.getStringExtra("otherUserId");

        ref.child(otherUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                otherUserName= snapshot.child("userName").getValue(String.class);
                UserNameTop.setText(otherUserName);
                UserNameTop.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                            fragmentManagerChatActivity = getSupportFragmentManager();
                            fragmentManagerChatActivity.popBackStack();
                            fragmentTransaction = fragmentManagerChatActivity.beginTransaction();
                            fragmentTransaction.replace(R.id.frame_chat_profile, new OtherUserProfileChat()).addToBackStack(null).commit();
                            frame.setVisibility(View.VISIBLE);
//                        Intent intent = new Intent(ChatMessageActivity.this ,MainActivity.class);
//                        intent.putExtra("frgToLoad", 5);
//                        startActivity(intent);

                    }
                });



                if(snapshot.hasChild("profilePic"))
                {
                    otherUserImageUrl=snapshot.child("profilePic").getValue(String.class);
                    Picasso.get()
                            .load(otherUserImageUrl)
                            .placeholder(R.color.black)
                            .error(R.drawable.groot)
                            .into(userImgViewTop);
                }
                readMessages(myUserId,otherUserId,otherUserImageUrl);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        buttonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg= messageText.getText().toString();
                if(!msg.equals(""))
                {
                    sendMessage(myUserId,otherUserId,msg);
                }
                messageText.setText("");
            }
        });

        seenMessage(otherUserId);
    }

    private void sendMessage(String sender, String receiver, String message)
    {
        DateFormat df = new SimpleDateFormat("d MMM yyyy,HH:mm");
        String timeNow = df.format(Calendar.getInstance().getTime());
        DatabaseReference rootRef= FirebaseDatabase.getInstance().getReference();

        String chatKey = rootRef.child("Chats").push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message",message);
        hashMap.put("time",timeNow);
        hashMap.put("chatKey",chatKey);
        hashMap.put("isseen",false);

        rootRef.child("Chats").child(chatKey).setValue(hashMap);
    }
    private void readMessages(final String myUserId, final String otherUserId, final String otherUserImageUrl)
    {
        mChat= new ArrayList<>();
        DatabaseReference ChatReference= FirebaseDatabase.getInstance().getReference("Chats");
        ChatReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                mChat.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    Chat chat= dataSnapshot.getValue(Chat.class);
                    if((chat.getReceiver().equals(myUserId) && chat.getSender().equals(otherUserId)) || (chat.getReceiver().equals(otherUserId) && chat.getSender().equals(myUserId)))
                    {
                        if(dataSnapshot.hasChild("deleted"))
                        {
                            if(!dataSnapshot.child("deleted").getValue(String.class).equals(myUserId))
                            {
                                mChat.add(chat);
                            }
                        }
                        else
                        {
                            mChat.add(chat);
                        }

                    }
                    messageAdapter = new MessageAdapter(ChatMessageActivity.this, mChat,otherUserImageUrl);
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage(final String otherUserId)
    {
        referenceSeen= FirebaseDatabase.getInstance().getReference("Chats");
        seenListener= referenceSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    Chat chat= dataSnapshot.getValue(Chat.class);
                    if(chat.getReceiver().equals(myUserId) && chat.getSender().equals(otherUserId))
                    {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen",true);
                        dataSnapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onPause() {
        referenceSeen.removeEventListener(seenListener);
        super.onPause();
    }
}