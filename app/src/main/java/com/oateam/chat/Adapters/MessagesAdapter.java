package com.oateam.chat.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.installations.Utils;
import com.oateam.chat.Activities.FullScreenActivity;
import com.oateam.chat.Models.Messages;
import com.oateam.chat.R;
import com.squareup.picasso.Picasso;

import java.net.InetAddress;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>
{
    private List<Messages> userMessagesList;
    private FirebaseAuth auth;
    private DatabaseReference usersRef;
    LayoutInflater inflater;





    public MessagesAdapter (List<Messages> userMessagesList)
    {
        this.userMessagesList = userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout,parent,false);
        auth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, @SuppressLint("RecyclerView") int position)
    {
        String messageSenderID = auth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(position);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        String fullTime = messages.getDate();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);


        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.linear_rec12.setVisibility(View.GONE);
        holder.fileLayout.setVisibility(View.GONE);
        holder.imgeRECLi.setVisibility(View.GONE);
        holder.imgeSENTLi.setVisibility(View.GONE);
        holder.filereLayoutre.setVisibility(View.GONE);
        holder.fileLayout.setVisibility(View.GONE);
        if (fromMessageType.equals("text"))
        {
            if (fromUserID.equals(messageSenderID))
            {

                holder.senderMessageText.setBackgroundResource(R.drawable.sender_message_lay);
                holder.senderMessageText.setTextColor(Color.BLACK);
                holder.senderMessageText.setText(messages.getMessage());


                SimpleDateFormat inputDate  = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                SimpleDateFormat outputDate = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                inputDate.setTimeZone(TimeZone.getTimeZone("UTC"));

                try {
                    Date date = inputDate.parse(fullTime);
                    assert date != null;
                    outputDate.setTimeZone(TimeZone.getDefault());
                    String finalTime = outputDate.format(date);


                    holder.senderTIME.setText(finalTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


//
//                if (position == userMessagesList.size()-1)
//               {

                       if (userMessagesList.get(position).isSeen())
                       {
                           holder.seenText.setImageResource(R.drawable.ic_baseline_done_all_24);

                       }
                       else
                       {
                           holder.seenText.setImageResource(R.drawable.ic_baseline_done_24);
                           holder.seenText.setColorFilter(holder.itemView.getResources().getColor(R.color.green_600));

                       }

//                   else
//                   {
//                       holder.seenImg.setImageResource(R.drawable.ic_baseline_done_24);
//                       holder.seenImg.setColorFilter(holder.itemView.getResources().getColor(R.color.green_600));
//                   }

//                }

            }
            else
            {
                holder.linear_sen12.setVisibility(View.INVISIBLE);
                holder.linear_rec12.setVisibility(View.VISIBLE);
                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_message_lay);
                holder.receiverMessageText.setTextColor(Color.BLACK);
                holder.receiverMessageText.setText(messages.getMessage());



                SimpleDateFormat inputDate  = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                SimpleDateFormat outputDate = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                inputDate.setTimeZone(TimeZone.getTimeZone("UTC"));

                try {
                    Date date = inputDate.parse(fullTime);
                    assert date != null;
                    outputDate.setTimeZone(TimeZone.getDefault());
                    String finalTime = outputDate.format(date);


                    holder.ReceiverTIME.setText(finalTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }

        else  if (fromMessageType.equals("image"))
        {
            if (fromUserID.equals(messageSenderID))
            {
                holder.linear_sen12.setVisibility(View.INVISIBLE);
                holder.linear_rec12.setVisibility(View.INVISIBLE);
                holder.imgeSENTLi.setVisibility(View.VISIBLE);
                holder.imgeRECLi.setVisibility(View.GONE);

                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_baseline_image_24).into(holder.messageSenderPicture);
                SimpleDateFormat inputDate  = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                SimpleDateFormat outputDate = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                inputDate.setTimeZone(TimeZone.getTimeZone("UTC"));

                try {
                    Date date = inputDate.parse(fullTime);
                    assert date != null;
                    outputDate.setTimeZone(TimeZone.getDefault());
                    String finalTime = outputDate.format(date);


                    holder.imgTimeSent.setText(finalTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(holder.itemView.getContext(), FullScreenActivity.class);
                        intent.putExtra("image_full",userMessagesList.get(position).getMessage());
                        intent.putExtra("username_full",userMessagesList.get(position).getName());
                        holder.itemView.getContext().startActivity(intent);
                    }
                });

//                if (position == userMessagesList.size()-1)
//                {
                    if (userMessagesList.get(position).isSeen())
                    {
                        holder.seenImg.setImageResource(R.drawable.ic_baseline_done_all_24);

                    }
                    else
                    {
                        holder.seenImg.setImageResource(R.drawable.ic_baseline_done_24);
                        holder.seenImg.setColorFilter(holder.itemView.getResources().getColor(R.color.green_600));

                    }
//                }
//                else
//                {
//                    holder.seenImg.setImageResource(R.drawable.ic_baseline_done_24);
//                    holder.seenImg.setColorFilter(holder.itemView.getResources().getColor(R.color.green_600));
//                }

            }
            else
            {
                holder.linear_sen12.setVisibility(View.INVISIBLE);
                holder.linear_rec12.setVisibility(View.INVISIBLE);
                holder.imgeSENTLi.setVisibility(View.GONE);
                holder.imgeRECLi.setVisibility(View.VISIBLE);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.ic_baseline_image_24).into(holder.messageReceiverPicture);

                SimpleDateFormat inputDate  = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                SimpleDateFormat outputDate = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
                inputDate.setTimeZone(TimeZone.getTimeZone("UTC"));

                try {
                    Date date = inputDate.parse(fullTime);
                    assert date != null;
                    outputDate.setTimeZone(TimeZone.getDefault());
                    String finalTime = outputDate.format(date);


                    holder.imgTimeREC.setText(finalTime);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

            }
        }

        else if (fromMessageType.equals("document"))
        {
           if (fromUserID.equals(messageSenderID))
           {
               holder.linear_sen12.setVisibility(View.INVISIBLE);
               holder.linear_rec12.setVisibility(View.INVISIBLE);
               holder.messageSenderPicture.setVisibility(View.INVISIBLE);
               holder.messageReceiverPicture.setVisibility(View.INVISIBLE);
               holder.fileLayout.setVisibility(View.VISIBLE);
               holder.filereLayoutre.setVisibility(View.GONE);
               holder.fileSize.setText(messages.getSize());
               holder.fileName.setText(messages.getName());

               SimpleDateFormat inputDate  = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
               SimpleDateFormat outputDate = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
               inputDate.setTimeZone(TimeZone.getTimeZone("UTC"));

               try {
                   Date date = inputDate.parse(fullTime);
                   assert date != null;
                   outputDate.setTimeZone(TimeZone.getDefault());
                   String finalTime = outputDate.format(date);


                   holder.fileTime.setText(finalTime);
               } catch (ParseException e) {
                   e.printStackTrace();
               }


               holder.fileExtension.setText(messages.getExtension());

               holder.itemView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {

                       Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                       holder.itemView.getContext().startActivity(intent);



//                       Tools.openInAppBrowser(, uri, false);
                   }
               });



//               if (position == userMessagesList.size()-1) {
                   if (userMessagesList.get(position).isSeen()) {
                       holder.seenFile.setImageResource(R.drawable.ic_baseline_done_all_24);

                   } else {
                       holder.seenFile.setImageResource(R.drawable.ic_baseline_done_24);
                       holder.seenFile.setColorFilter(holder.itemView.getResources().getColor(R.color.green_600));

                   }
//               }
//               else
//               {
//                   holder.seenImg.setImageResource(R.drawable.ic_baseline_done_24);
//                   holder.seenImg.setColorFilter(holder.itemView.getResources().getColor(R.color.green_600));
//               }

           }
           else
           {
               holder.linear_sen12.setVisibility(View.INVISIBLE);
               holder.linear_rec12.setVisibility(View.INVISIBLE);
               holder.messageSenderPicture.setVisibility(View.INVISIBLE);
               holder.messageReceiverPicture.setVisibility(View.INVISIBLE);
               holder.filereLayoutre.setVisibility(View.VISIBLE);
               holder.fileLayout.setVisibility(View.GONE);
               holder.fileSizere.setText(messages.getSize());
               holder.fileNamere.setText(messages.getName());
               SimpleDateFormat inputDate  = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
               SimpleDateFormat outputDate = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ENGLISH);
               inputDate.setTimeZone(TimeZone.getTimeZone("UTC"));

               try {
                   Date date = inputDate.parse(fullTime);
                   assert date != null;
                   outputDate.setTimeZone(TimeZone.getDefault());
                   String finalTime = outputDate.format(date);


                   holder.fileTimere.setText(finalTime);
               } catch (ParseException e) {
                   e.printStackTrace();
               }




               holder.fileExtensionre.setText(messages.getExtension());


               holder.itemView.setOnClickListener(new View.OnClickListener() {
                   @Override
                   public void onClick(View view) {
                       Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                       holder.itemView.getContext().startActivity(intent);

                   }
               });
           }
        }




        if (fromUserID.equals(messageSenderID))
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (userMessagesList.get(position).getType().equals("document"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        holder.itemView.getResources().getString(R.string.delete_everyone),
                                        holder.itemView.getResources().getString(R.string.delete_me),
                                        holder.itemView.getResources().getString(R.string.cancel)

                                };
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(holder.itemView.getContext());
                        alertDialog.setTitle("Delete message ?");
                        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteMessageForEveryone(position, holder);

                                }

                                else  if (i == 1)
                                {
                                    deleteSentMessage(position, holder);

                                }


                            }
                        });
                        alertDialog.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        holder.itemView.getResources().getString(R.string.delete_everyone),
                                        holder.itemView.getResources().getString(R.string.delete_me),
                                        holder.itemView.getResources().getString(R.string.cancel)
                                };
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(holder.itemView.getContext());
                        alertDialog.setTitle("Delete message ?");
                        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteMessageForEveryone(position, holder);


                                }
                                else  if (i == 1)
                                {
                                    deleteSentMessage(position, holder);

                                }
                            }
                        });
                        alertDialog.show();
                    }
                    if (userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {

                                        holder.itemView.getResources().getString(R.string.delete_everyone),
                                        holder.itemView.getResources().getString(R.string.delete_me),
                                        holder.itemView.getResources().getString(R.string.cancel)
                                };
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(holder.itemView.getContext());
                        alertDialog.setTitle(R.string.delete_msg_ques);
                        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                if (i == 0)
                                {
                                    deleteMessageForEveryone(position, holder);

                                }


                                else  if (i == 1)
                                {
                                    deleteSentMessage(position, holder);

                                }

                            }
                        });
                        alertDialog.show();
                    }
                    return false;
                }
            });
        }
        else
        {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (userMessagesList.get(position).getType().equals("document"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        holder.itemView.getResources().getString(R.string.delete_me),
                                        holder.itemView.getResources().getString(R.string.cancel)
                                };
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(holder.itemView.getContext());
                        alertDialog.setTitle("Delete message ?");
                        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {

                                if (i ==0)
                                {
                                    deleteReceivedMessage(position,holder);

                                }
                                else  if (i == 1)
                                {

                                }

                            }
                        });
                        alertDialog.show();
                    }

                    else if (userMessagesList.get(position).getType().equals("text"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        String.valueOf(R.string.delete_me),
                                        String.valueOf(R.string.cancel),
                                };
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(holder.itemView.getContext());
                        alertDialog.setTitle(R.string.delete_msg_ques);
                        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {

                                if (i == 0)
                                {
                                    deleteReceivedMessage(position,holder);

                                }
                            }
                        });
                        alertDialog.show();
                    }
                    if (userMessagesList.get(position).getType().equals("image"))
                    {
                        CharSequence options[] = new CharSequence[]
                                {
                                        holder.itemView.getResources().getString(R.string.delete_me),
                                        holder.itemView.getResources().getString(R.string.cancel)
                                };
                        AlertDialog.Builder alertDialog = new AlertDialog.Builder(holder.itemView.getContext());
                        alertDialog.setTitle("Delete message ?");
                        alertDialog.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {


                               if (i == 0)
                                {
                                    deleteReceivedMessage(position,holder);

                                }

                            }
                        });
                        alertDialog.show();
                    }
                    return false;
                }
            });
        }



    }

    @Override
    public int getItemCount()
    {
        return userMessagesList.size();
    }


    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    private void deleteSentMessage(final int position, final MessageViewHolder messageViewHolder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                      if (task.isSuccessful())
                      {
                          userMessagesList.remove(position);
                          notifyItemRemoved(position);
                          notifyItemRangeChanged(position,userMessagesList.size());
                          Toast toast = new Toast(messageViewHolder.itemView.getContext());
                          toast.setDuration(Toast.LENGTH_LONG);


                      }
                      else
                      {



                      }
                    }

                });
    }


    private void deleteReceivedMessage(final int position, final MessageViewHolder messageViewHolder)
    {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            userMessagesList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position,userMessagesList.size());
                            Toast toast = new Toast(messageViewHolder.itemView.getContext());
                            toast.setDuration(Toast.LENGTH_LONG);


                        }
                        else
                        {

                        }
                    }

                });
    }

//    private void setTimeTextVisibility(long ts1, long ts2, TextView timeText) {
//
//        if (ts2 == 0) {
//            timeText.setVisibility(View.VISIBLE);
//            timeText.setText(Utils.formatDayTimeHtml(ts1));
//        } else {
//            Calendar cal1 = Calendar.getInstance();
//            Calendar cal2 = Calendar.getInstance();
//            cal1.setTimeInMillis(ts1);
//            cal2.setTimeInMillis(ts2);
//
//            boolean sameMonth = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                    cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH);
//
//            if (sameMonth) {
//                timeText.setVisibility(View.GONE);
//                timeText.setText("");
//            } else {
//                timeText.setVisibility(View.VISIBLE);
//                timeText.setText(Utils.formatDayTimeHtml(ts2));
//            }
//
//        }
//    }

    private void deleteMessageForEveryone(final int position, final MessageViewHolder messageViewHolder)
    {
       final  DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            rootRef.child("Messages")
                                    .child(userMessagesList.get(position).getFrom())
                                    .child(userMessagesList.get(position).getTo())
                                    .child(userMessagesList.get(position).getMessageID())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                userMessagesList.remove(position);
                                                notifyItemRemoved(position);
                                                notifyItemRangeChanged(position,userMessagesList.size());

                                            }
                                        }});
                        }
                        else
                        {
                        }
                    }
                });

    }





    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText,textTime,receiverMessageText, senderTIME,ReceiverTIME,fileName,fileSize,fileExtension,fileTime,imgTimeREC,imgTimeSent,fileNamere,fileSizere,fileExtensionre,fileTimere;
        public CardView linear_rec12,linear_sen12;
        public ImageView messageSenderPicture,messageReceiverPicture,fileIcon,fileIconre,seenFile,seenImg,seenText;

        public LinearLayout imgeRECLi,fileLayout;
        public RelativeLayout imgeSENTLi,filereLayoutre;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            senderMessageText = (TextView)itemView.findViewById(R.id.message_content_chat);
            receiverMessageText = (TextView)itemView.findViewById(R.id.message_content_you_RE);
           senderTIME = (TextView)itemView.findViewById(R.id.text_time_chat_me);
            ReceiverTIME = (TextView)itemView.findViewById(R.id.text_time_chat_you);
            imgTimeREC = (TextView)itemView.findViewById(R.id.tv_time_image);
            imgTimeSent = (TextView)itemView.findViewById(R.id.tv_time_image_sent);
            linear_rec12 = (CardView) itemView.findViewById(R.id.linear_rec);
            linear_sen12 = (CardView) itemView.findViewById(R.id.linear_sen);
            imgeRECLi = (LinearLayout) itemView.findViewById(R.id.lay_image);
            imgeSENTLi = (RelativeLayout) itemView.findViewById(R.id.lay_image_sent);
            messageSenderPicture = (ImageView) itemView.findViewById(R.id.image_message_me);
            messageReceiverPicture = (ImageView) itemView.findViewById(R.id.image_message_you);

            fileName = (TextView)itemView.findViewById(R.id.tv_file_name);
            fileSize = (TextView)itemView.findViewById(R.id.tv_file_size);
            fileExtension = (TextView)itemView.findViewById(R.id.tv_file_extension);
            fileTime = (TextView)itemView.findViewById(R.id.tv_time_file);
            fileIcon = (ImageView) itemView.findViewById(R.id.file_icon);
            fileLayout = (LinearLayout) itemView.findViewById(R.id.file_lay);

            fileNamere = (TextView)itemView.findViewById(R.id.tv_file_name_rec);
            fileSizere = (TextView)itemView.findViewById(R.id.tv_file_size_rec);
            fileExtensionre = (TextView)itemView.findViewById(R.id.tv_file_extension_rec);
            fileTimere = (TextView)itemView.findViewById(R.id.tv_time_file_rec);
            fileIconre = (ImageView) itemView.findViewById(R.id.file_icon_rec);
            filereLayoutre = (RelativeLayout) itemView.findViewById(R.id.file_root_container_re);

            seenFile = (ImageView) itemView.findViewById(R.id.seenStatus_file);
            seenImg = (ImageView) itemView.findViewById(R.id.seenStatus_image);
            seenText = (ImageView) itemView.findViewById(R.id.seenStatus_text);
        }
    }


}
