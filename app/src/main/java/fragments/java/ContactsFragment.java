package fragments.java;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mostafa.e_commerce.BuyerMainActivity;
import com.example.mostafa.e_commerce.R;
import com.example.mostafa.e_commerce.SellerMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import database.MessagesFunctions;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import recyclerview.ContactsAdapter;
import user.structure.Buyer;
import user.structure.Seller;
import user.structure.Session;
import user.structure.User;
import wholesale.callback.DataCallBack;

public class ContactsFragment extends Fragment implements DataCallBack {

    private static final String TAG = ContactsFragment.class.getSimpleName();
    private Handler handler = new Handler(Looper.getMainLooper());
    private static final String USER_TYPE = "userType";
    private ContactsAdapter adapter;
    private User currentUser;
    private TextView pageStateMsg;
    private Group unavailableGroup;
    private ScaleAnimation anim;

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance(Class<?> userType) {
        ContactsFragment contactsFragment = new ContactsFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(USER_TYPE, userType);
        contactsFragment.setArguments(bundle);

        return contactsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.messages_contacts_fragment, container, false);

        anim = new ScaleAnimation(
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

        if (getActivity() instanceof BuyerMainActivity) {
            ((BuyerMainActivity) getActivity()).setPageTitle(getResources().getString(R.string.contact_title));
        } else {
            ((SellerMainActivity) getActivity()).setPageTitle(getResources().getString(R.string.contact_title));
        }

        try {
            if (getArguments() != null) {
                currentUser = ((Class<?>) getArguments().getSerializable(USER_TYPE)) == Seller.class
                        ? (Seller) Session.getUser(Seller.class)
                        : (Buyer) Session.getUser(Buyer.class);
                initViews(view);
            } else {
                Toast.makeText(getActivity(), "No Contacts Yet.", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initViews(final View view) throws ExecutionException, InterruptedException {

        pageStateMsg = view.findViewById(R.id.page_state_text);
        unavailableGroup = view.findViewById(R.id.uava_group);

        RecyclerView contactsList = view.findViewById(R.id.contacts_list);
        if (currentUser != null) {
            currentUser.messagesFunctions(
                    getActivity()
                    , this
                    , currentUser.getUserId()
                    , null
                    , MessagesFunctions.FETCH_CONTACTS
            );
            new Thread(() ->
                    handler.post(() -> {
                                contactsList.setHasFixedSize(true);
                                contactsList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                                adapter = new ContactsAdapter(new ArrayList<>(), getActivity());
                                contactsList.setAdapter(adapter);
                                contactsList.setVisibility(View.VISIBLE);
                            }
                    )).start();
        } else {
            new Thread(() ->
                    handler.post(() -> {
                        unavailableGroup.startAnimation(anim);
                        unavailableGroup.setVisibility(View.VISIBLE);
                        pageStateMsg.setText(R.string.msgs_page_notsigned_in);
                    })).start();
        }
        if (getActivity() instanceof SellerMainActivity)
            ((SellerMainActivity) getActivity()).setPageTitle(getString(R.string.messages_page_title));
        else if (getActivity() instanceof BuyerMainActivity)
            ((BuyerMainActivity) getActivity()).setPageTitle(getString(R.string.messages_page_title));

    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject contacts = new JSONObject(responseBody.string());

            if (!contacts.isNull("contacts")) {
                adapter.addContacts(JSONToObject.convertToContacts(contacts.getJSONArray("contacts"), currentUser.bringType()));
            } else {
                new Thread(() ->
                        handler.post(() -> {
                            unavailableGroup.setVisibility(View.VISIBLE);
                            unavailableGroup.setAnimation(anim);
                            pageStateMsg.setText(R.string.msgs_page_no_contacts);
                        })).start();
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
    }
}
