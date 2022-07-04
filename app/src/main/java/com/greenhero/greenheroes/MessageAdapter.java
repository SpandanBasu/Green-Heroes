package com.greenhero.greenheroes;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;

    int type=5;
    FirebaseUser myUser= FirebaseAuth.getInstance().getCurrentUser();
    String myUserIdHere = myUser.getUid();
    private Context mContext;
    private List<Chat> mChat;
    private String imageUrl;
    FirebaseUser fuser;

    public MessageAdapter(Context mContext, List<Chat> mChat,  String imageUrl) {
        this.mChat = mChat;
        this.mContext = mContext;
        this.imageUrl = imageUrl;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_RIGHT)
        {
            type=MSG_TYPE_RIGHT;
            View view= LayoutInflater.from(mContext).inflate(R.layout.msg_item_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
        else
        {
            type=MSG_TYPE_LEFT;
            View view= LayoutInflater.from(mContext).inflate(R.layout.msg_item_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull MessageAdapter.ViewHolder holder, int position) {

        final Chat chat= mChat.get(position);

        DateFormat df = new SimpleDateFormat("d MMM yyyy,HH:mm");
        String timeNow = df.format(Calendar.getInstance().getTime());
        String[] todayParts = timeNow.split(",");
        String todayDate= todayParts[0];
        String todayTime= todayParts[1];

        String[] timeParts= chat.getTime().split(",");
        String msgDate = timeParts[0];
        String msgTime= timeParts[1];

        if(msgDate.equals(todayDate))
        {
            holder.showTime.setText(msgTime);
        }
        else
        {
            holder.showTime.setText(msgDate);
        }

        holder.showMessage.setText(chat.getMessage());

        if(chat.isIsseen())
        {
            if(type==MSG_TYPE_RIGHT)
            {
                holder.seenView.setVisibility(View.VISIBLE);
            }

        }


        if(!imageUrl.equals("default"))
        {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.color.black)
                    .error(R.drawable.groot)
                    .into(holder.profileImage);
        }
        holder.chatMsg.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Delete Message")
                        .setMessage("Are you sure you want to delete the message ?")

                        // Specifying a listener allows you to take an action before dismissing the dialog.
                        // The dialog is automatically dismissed when a dialog button is clicked.
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Continue with delete operation
                                final DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Chats").child(chat.getChatKey());
                                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.hasChild("deleted"))
                                        {
                                            ref.removeValue();
                                        }
                                        else
                                        {
                                            HashMap<String, Object> hashMap = new HashMap<>();
                                            hashMap.put("deleted",myUserIdHere);
                                            ref.updateChildren(hashMap);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        })

                        // A null listener allows the button to dismiss the dialog and take no further action.
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.drawable.delete)
                        .show();


                return true;
            }
        });
    }


    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView showMessage,showTime;
        public CircleImageView profileImage;
        public LinearLayout chatMsg;
        public ImageView seenView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            showTime=itemView.findViewById(R.id.time_chat);
            showMessage=itemView.findViewById(R.id.show_msg);
            profileImage=itemView.findViewById(R.id.other_dp);
            chatMsg=itemView.findViewById(R.id.chat_msg);
            seenView=itemView.findViewById(R.id.eye);
        }
    }




    @Override
    public int getItemViewType(int position) {
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fuser.getUid()))
        {
            return MSG_TYPE_RIGHT;
        }
        else
        {
            return MSG_TYPE_LEFT;
        }
    }
}
