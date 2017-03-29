package com.example.sayantanchakraborty.blahblah.Adapter;

import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.example.sayantanchakraborty.blahblah.Model.Message;
import com.example.sayantanchakraborty.blahblah.Model.User;
import com.example.sayantanchakraborty.blahblah.R;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by sayantanchakraborty on 25/02/17.
 */

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Message> msgList;
    private Context context;
    private User user;

    public ChatMessagesAdapter(ArrayList<Message> msgList, Context context,User user) {
        this.msgList = msgList;
        this.context = context;
        this.user = user;
    }

    @Override
    public int getItemViewType(int position) {

        Message msg = msgList.get(position);
        int po;
        if(msg.SenderFrom.contentEquals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
        {
            if(TextUtils.isEmpty(msg.getImageUrl()))
                po = 0;
            else
                po = 2;
        }else {
            if(TextUtils.isEmpty(msg.getImageUrl()))
                po = 1;
            else
                po = 3;
        }
        return po;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder;
        Log.d("ChatMessagesAdapter", String.valueOf(viewType));
        switch (viewType){
            case 0:
                View view1 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_out,parent,false);
                return new ViewHolderOut(view1);

            case 1:
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
                return new ViewHolderIn(view);
            case 2:
                View view3 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item_out,parent,false);
                return new ViewHolderOutImage(view3);

            case 3:
                View view2 = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_item,parent,false);
                return new ViewHolderInImage(view2);

            default: return null;
        }
        //Message message = msgList.get()

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()){
            case 0:
                ViewHolderOut vW1 = (ViewHolderOut)holder;
                Message msg1 = msgList.get(position);
                vW1.messageTxt.setText(msg1.getText());
                vW1.dateText.setText(getDate(msg1.getTimestamp(),"hh:mm a"));
                break;
            case 1:

                ViewHolderIn vW0 = (ViewHolderIn)holder;
                Message msg = msgList.get(position);
                vW0.messageTxt.setText(msg.getText());
                vW0.dateText.setText(getDate(msg.getTimestamp(),"hh:mm a"));
                break;

            case 2:
                ViewHolderOutImage vW2 = (ViewHolderOutImage)holder;
                Message msg2 = msgList.get(position);
                RelativeLayout.LayoutParams params = new
                        RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params.height = (int)msg2.getImageHeight();
                params.width = (int)msg2.getImageWidth();
                ((ViewHolderOutImage) holder).imgMessage.setLayoutParams(params);
                Glide.with(context).load(msg2.getImageUrl()).asBitmap().centerCrop()
                        .into(new BitmapImageViewTarget(vW2.imgMessage));
                vW2.dateText.setText(getDate(msg2.getTimestamp(),"hh:mm a"));
                break;

            case 3:
                ViewHolderInImage vW3 = (ViewHolderInImage)holder;
                Message msg3 = msgList.get(position);
                RelativeLayout.LayoutParams params1 = new
                        RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
                params1.height = (int)msg3.getImageHeight();
                params1.width = (int)msg3.getImageWidth();
                ((ViewHolderInImage) holder).imgMessage.setLayoutParams(params1);
                Glide.with(context).load(msg3.getImageUrl()).asBitmap().centerCrop()
                        .into(new BitmapImageViewTarget(vW3.imgMessage));
                vW3.dateText.setText(getDate(msg3.getTimestamp(),"hh:mm a"));
                break;
        }


    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }



    @Override
    public int getItemCount() {
        return msgList.size();
    }

    public class ViewHolderIn extends RecyclerView.ViewHolder {
        public TextView messageTxt;
        public TextView dateText;
        public RelativeLayout chatBubbleLayout;
        public ViewHolderIn(View itemView) {
            super(itemView);
            //messageTxt = (TextView)itemView.findViewById(R.id.in_msg_txt);
            chatBubbleLayout = (RelativeLayout)itemView.findViewById(R.id.chat_bubble_layout);
            //chatBubbleLayout.setOrientation(LinearLayout.VERTICAL);
            chatBubbleLayout.setMinimumWidth(100);
            messageTxt = new TextView(context);
            messageTxt.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            messageTxt.setSingleLine(false);
            messageTxt.setId(1);
            messageTxt.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            messageTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.result_font));
            messageTxt.setPadding(4,4,8,4);
            messageTxt.setMaxWidth(800);

            dateText = new TextView(context);
            dateText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.result_font_time));
            dateText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.BELOW,messageTxt.getId());
            params1.addRule(RelativeLayout.END_OF,messageTxt.getId());
            dateText.setLayoutParams(params1);

            (chatBubbleLayout).addView(messageTxt);
            chatBubbleLayout.addView(dateText);

        }
    }

    public class ViewHolderInImage extends RecyclerView.ViewHolder {
        public ImageView imgMessage;
        public TextView dateText;
        public RelativeLayout chatBubbleLayout;
        public ViewHolderInImage(View itemView) {
            super(itemView);
            //imgMessage = (TextView)itemView.findViewById(R.id.in_msg_txt);
            chatBubbleLayout = (RelativeLayout)itemView.findViewById(R.id.chat_bubble_layout);
            chatBubbleLayout.setMinimumWidth(200);
            imgMessage = new ImageView(context);
            imgMessage.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imgMessage.setId(2);
            imgMessage.setPadding(4,4,8,4);
            imgMessage.setMaxWidth(800);

            dateText = new TextView(context);
            dateText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.result_font_time));
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.BELOW,imgMessage.getId());
            params1.addRule(RelativeLayout.ALIGN_RIGHT,imgMessage.getId());
            dateText.setLayoutParams(params1);
            ((RelativeLayout) chatBubbleLayout).addView(imgMessage);
            chatBubbleLayout.addView(dateText);
        }
    }

    public class ViewHolderOutImage extends RecyclerView.ViewHolder {
        public ImageView imgMessage;
        public TextView dateText;
        public RelativeLayout chatBubbleLayoutout;
        public ViewHolderOutImage(View itemView) {
            super(itemView);
            //imgMessage = (TextView)itemView.findViewById(R.id.in_msg_txt);
            chatBubbleLayoutout = (RelativeLayout)itemView.findViewById(R.id.chat_bubble_layout_out);
            chatBubbleLayoutout.setMinimumWidth(200);
            imgMessage = new ImageView(context);
            imgMessage.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            imgMessage.setId(3);
            imgMessage.setPadding(4,4,8,4);
            imgMessage.setMaxWidth(200);
            imgMessage.setMaxHeight(200);

            dateText = new TextView(context);
            dateText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.result_font_time));
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.BELOW,imgMessage.getId());
            params1.addRule(RelativeLayout.ALIGN_RIGHT,imgMessage.getId());
            dateText.setLayoutParams(params1);
            ((RelativeLayout) chatBubbleLayoutout).addView(imgMessage);
            chatBubbleLayoutout.addView(dateText);
        }
    }

    public class ViewHolderOut extends RecyclerView.ViewHolder {
        public TextView messageTxt;
        public TextView dateText;
        public RelativeLayout chatBubbleLayoutout;
        public ViewHolderOut(View itemView) {
            super(itemView);
            //messageTxt = (TextView)itemView.findViewById(R.id.out_msg_txt);
            chatBubbleLayoutout = (RelativeLayout)itemView.findViewById(R.id.chat_bubble_layout_out);
            chatBubbleLayoutout.setMinimumWidth(100);
            messageTxt = new TextView(context);
            messageTxt.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            messageTxt.setSingleLine(false);
            messageTxt.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
            messageTxt.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.result_font));
            messageTxt.setId(4);
            messageTxt.setPadding(4,4,8,4);
            messageTxt.setMaxWidth(800);

            dateText = new TextView(context);
            dateText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    context.getResources().getDimension(R.dimen.result_font_time));
            dateText.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            //RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams)dateText.getLayoutParams();
            //dateText.setPadding(700,0,0,0);
            params1.addRule(RelativeLayout.BELOW,messageTxt.getId());
            params1.addRule(RelativeLayout.END_OF,messageTxt.getId());
            dateText.setLayoutParams(params1);
            ((RelativeLayout) chatBubbleLayoutout).addView(messageTxt);
            chatBubbleLayoutout.addView(dateText);
        }
    }

    public static String getDate(long milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliSeconds);
        return formatter.format(calendar.getTime());
    }

}
