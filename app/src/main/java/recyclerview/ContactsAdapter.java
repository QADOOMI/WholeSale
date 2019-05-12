package recyclerview;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;

import java.util.ArrayList;

import fragments.java.MessagesFragment;
import user.structure.User;

import static com.example.mostafa.e_commerce.OtherActionsActivity.CURRENT_PAGE_ID;

public final class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactViewHolder> {

    private ArrayList<User> users;
    private Activity activity;
    private final static String TAG = ContactsAdapter.class.getSimpleName();

    public ContactsAdapter(ArrayList<User> users, Activity activity) {
        this.users = users;
        this.activity = activity;
    }

    public void addContacts(ArrayList<User> newUsers) {
        users.addAll(newUsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_item, parent, false);


        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User user = users.get(position);

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
                holder.holder.post(() ->
                        holder.holder.startAnimation(anim))).start();

        holder.contacterEmail.setText(user.getEmail());
        holder.contacterName.setText(user.getFirstName().concat(" ").concat(user.getLastName()));
        Glide.with(holder.contacterImage.getContext())
                .asBitmap()
                .load(Uri.parse(user.getProfileImage()))
                .into(holder.contacterImage);

        holder.holder.setOnClickListener(view -> {
            Intent goToChatRoom = new Intent(activity, OtherActionsActivity.class);
            Log.e(TAG, "sent data: " + user.getUserId() + "\n" + user.bringType());
            goToChatRoom.putExtra(CURRENT_PAGE_ID, MessagesFragment.newInstance(user.getUserId(), user.bringType()));
            activity.startActivity(goToChatRoom);
        });
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {

        AppCompatImageView contacterImage;
        TextView contacterName;
        TextView contacterEmail;
        View holder;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            holder = itemView.findViewById(R.id.contacter_holder);
            contacterImage = itemView.findViewById(R.id.contacter_image);
            contacterName = itemView.findViewById(R.id.contacter_name);
            contacterEmail = itemView.findViewById(R.id.contacter_email);
        }
    }
}
