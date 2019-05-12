package recyclerview;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mostafa.e_commerce.R;

import org.json.JSONException;

import java.util.ArrayList;

import user.structure.Session;
import user.structure.User;

public final class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessageViewHolder> {

    private ArrayList<Message> messages;
    private Activity activity;
    private Class<?> userType;
    private static final String TAG = MessagesAdapter.class.getSimpleName();

    public MessagesAdapter(ArrayList<Message> messages, Activity activity, Class<?> userType) {
        this.messages = messages;
        this.activity = activity;
        this.userType = userType;
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    public void addMessages(ArrayList<Message> newMessages) {
        messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);


        return new MessageViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        ScaleAnimation anim = new ScaleAnimation(
                0.0f
                , 1.0f
                , 0.0f
                , 1.0f
                , Animation.RELATIVE_TO_SELF
                , 0.5f
                , Animation.RELATIVE_TO_SELF
                , 0.5f
        );
        anim.setDuration(500);
        new Thread(() ->
                holder.messageLayout.post(() ->
                        holder.messageLayout.startAnimation(anim))).start();

        try {
            User user = Session.getUser(userType);
            if (user.getUserId().equals(message.getSenderId())) {
                holder.messageLayout.setGravity(Gravity.RIGHT);
                holder.textMessage.setTextColor(activity.getResources().getColor(R.color.white));
            } else if (user.getUserId().equals(message.getReceiverId())) {
                holder.messageLayout.setGravity(Gravity.LEFT);
                holder.textMessage.setTextColor(activity.getResources().getColor(R.color.secondaryColorPrimary));
            }
            holder.textMessage.setText(message.getTextMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        MaterialCardView messageHolder;
        TextView textMessage;
        RelativeLayout messageLayout;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            messageLayout = (RelativeLayout) itemView.findViewById(R.id.message_layout);
            messageHolder = itemView.findViewById(R.id.message_holder);
            textMessage = itemView.findViewById(R.id.message_text);
        }
    }

    public static final class Message {

        private String textMessage;
        private String senderId;
        private String receiverId;

        public Message(String textMessage, String senderId, String receiverId) {
            this.textMessage = textMessage;
            this.senderId = senderId;
            this.receiverId = receiverId;
        }

        String getSenderId() {
            return senderId;
        }

        String getReceiverId() {
            return receiverId;
        }

        String getTextMessage() {
            return textMessage;
        }

        @Override
        public String toString() {
            return "Message{" +
                    ", textMessage='" + textMessage + '\'' +
                    '}';
        }
    }
}
