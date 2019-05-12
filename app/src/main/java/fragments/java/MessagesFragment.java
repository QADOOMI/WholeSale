package fragments.java;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import database.MessagesFunctions;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import recyclerview.MessagesAdapter;
import user.structure.Buyer;
import user.structure.Seller;
import user.structure.Session;
import user.structure.User;
import wholesale.callback.DataCallBack;

public class MessagesFragment extends Fragment implements Serializable, DataCallBack {

    private User currentUser;
    private Socket socket;
    private MessagesAdapter adapter;
    private static String receiverId;
    private static Class<?> userType;
    private RecyclerView messagesList;
    private static final String TAG = MessagesFragment.class.getSimpleName();
    private static final String RECEIVER_USER = "RECUSER";
    private static final String USER_TYPE = "userType";

    public MessagesFragment() {
    }

    public static MessagesFragment newInstance(String newReceiverId, Class<?> newUserType) {
        MessagesFragment fragment = new MessagesFragment();

        Bundle bundle = new Bundle();
        bundle.putString(RECEIVER_USER, newReceiverId);
        bundle.putSerializable(USER_TYPE, newUserType);

        receiverId = newReceiverId;
        userType = newUserType;

        fragment.setArguments(bundle);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.message_fragment, container, false);
        OtherActionsActivity currentActivity = ((OtherActionsActivity) getActivity());
        Toolbar toolbar = view.findViewById(R.id.messages_toolbar);
        currentActivity.setSupportActionBar(toolbar);

        try {
            String receiverId = null;
            Class<?> userType = null;

            if (getArguments() != null) {
                userType = ((Class<?>) getArguments().getSerializable(USER_TYPE));
                Log.e(TAG, "onCreatingView: **1** arguments not null");
                currentUser = (userType == Seller.class)
                        ? Session.getUser(Buyer.class)
                        : Session.getUser(Seller.class);

                receiverId = getArguments().getString(RECEIVER_USER);

            } else {
                Log.e(TAG, "onCreateView: arguments is null");
                receiverId = MessagesFragment.receiverId;
                userType = MessagesFragment.userType;

                currentUser = (userType == Seller.class)
                        ? Session.getUser(Buyer.class)
                        : Session.getUser(Seller.class);


            }
            currentUser.messagesFunctions(
                    getActivity()
                    , this
                    , currentUser.getUserId()
                    , receiverId
                    , MessagesFunctions.FETCH_MESSAGES);

            socket = IO.socket("http://10.0.3.2:3000/");
            socket.connect();
            socket.emit("join", currentUser.getUserId());

            initViews(view);

        } catch (JSONException | URISyntaxException | InterruptedException | ExecutionException e) {
            Log.e(TAG, e.getMessage(), e);
        }


        return view;
    }

    private void initViews(View view) {
        messagesList = (RecyclerView) view.findViewById(R.id.messages_list);
        messagesList.setHasFixedSize(true);
        messagesList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter = new MessagesAdapter(new ArrayList<>(), getActivity(), currentUser.bringType());
        messagesList.setAdapter(adapter);

        TextInputEditText messageHolder = view.findViewById(R.id.message_editor);
        FloatingActionButton sendMessage = view.findViewById(R.id.send_msg_btn);

        sendMessage.setOnClickListener(view1 -> {
            if (TextUtils.isEmpty(String.valueOf(messageHolder.getText()))) {
                messageHolder.setError(
                        "Can't be empty"
                        , getResources().getDrawable(R.drawable.ic_info_outline_black_24dp, null)
                );
                return;
            }

            try {
                socket.emit("messagedetection", JSONToObject.messagesToJSON(
                        currentUser.getUserId()
                        , getArguments() != null ? getArguments().getString(RECEIVER_USER) : receiverId
                        , String.valueOf(messageHolder.getText()))
                );
            } catch (JSONException e) {
                e.printStackTrace();
            }
            messageHolder.setText(null);
        });

        socket.on("usersjoinedthechat", args -> {
            Log.d(TAG, (String) args[0]);
        });

        socket.on("usersdisconnect", args -> {
            Log.d(TAG, (String) args[0]);
        });

        socket.on("message", args -> {
            new Thread(() -> {
                messagesList.post(() -> {
                    JSONObject message = (JSONObject) args[0];
                    try {
                        adapter.addMessage(new MessagesAdapter.Message(
                                message.getString("message")
                                , message.getString("senderId")
                                , message.getString("receiverId")
                        ));
                        messagesList.scrollToPosition(adapter.getItemCount() - 1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }).start();
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socket.disconnect();
    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject messages = new JSONObject(responseBody.string());

            if (messages.getJSONArray("messages") != null) {
                adapter.addMessages(JSONToObject.convertToMessages(messages.getJSONArray("messages")));
                messagesList.scrollToPosition(adapter.getItemCount() - 1);
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
