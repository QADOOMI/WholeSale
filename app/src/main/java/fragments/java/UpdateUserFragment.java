package fragments.java;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import dialog.UpdateDialog;
import io.reactivex.disposables.CompositeDisposable;
import networking.InternetChecker;
import okhttp3.ResponseBody;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.DataCallBack;

import static android.app.Activity.RESULT_OK;

public class UpdateUserFragment extends Fragment
        implements View.OnClickListener, Serializable, DataCallBack {

    private ImageButton updateName;
    private ImageButton updatePhone;
    private ImageButton updateImage;
    private ImageButton updatePassword;
    private ImageButton updateEmail;
    private Buyer buyer;
    private static final int RESULT_IMAGE = 2;
    private static final String TAG = UpdateUserFragment.class.getSimpleName();

    public UpdateUserFragment() {
    }

    public static UpdateUserFragment newInstance() {
        UpdateUserFragment userFragment = new UpdateUserFragment();

        return userFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.update_user_data_fragment, container, false);


        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Update Your Data");


        initViews(view);

        updateEmail.setOnClickListener(this);
        updatePhone.setOnClickListener(this);
        updateImage.setOnClickListener(this);
        updateName.setOnClickListener(this);
        updatePassword.setOnClickListener(this);

        return view;
    }

    private void initViews(View view) {
        updateName = view.findViewById(R.id.update_name_go_icon);
        updatePassword = view.findViewById(R.id.update_password_go_icon);
        updatePhone = view.findViewById(R.id.update_phone_number_go_icon);
        updateImage = view.findViewById(R.id.update_image_go_icon);
        updateEmail = view.findViewById(R.id.update_email_go_icon);
    }

    @Override
    public void onClick(View view) {
        assert getFragmentManager() != null;
        try {
            if (new InternetChecker(getContext()).execute().get()) {
                if (view.getId() == R.id.update_image_go_icon) {
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED) {
                        Intent gallaryIntent = new Intent();
                        gallaryIntent.setType("image/*");
                        gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(gallaryIntent, "Select Profile Image"), RESULT_IMAGE);
                    } else {
                        ActivityCompat.requestPermissions(getActivity()
                                , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                                , RESULT_IMAGE);
                    }
                } else {
                    Toast.makeText(getActivity(), R.string.connection_state_msg, Toast.LENGTH_LONG).show();
                }
                return;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        UpdateDialog newFragment = UpdateDialog.getInstance(view.getId());
        newFragment.show(getFragmentManager(), newFragment.getTag());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == RESULT_IMAGE) {
                if (data != null) {
                    try {
                        String uri = String.valueOf((Uri) data.getData());
                        buyer = (Buyer) Session.getUser(Buyer.class);
                        buyer.setProfileImage(uri);
                        buyer.updateUserData(getActivity(), this, R.id.update_image_go_icon);
                    } catch (JSONException | ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(), "No Image Picked.", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "You invoked another(diff from IMAGE request) data request");
            }
        } else {
            Log.e(TAG, "Somthing went wrong: resultCode -> " + resultCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RESULT_IMAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent gallaryIntent = new Intent();
                gallaryIntent.setType("image/*");
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(gallaryIntent, "Select Profile Image"), RESULT_IMAGE);
            } else {
                Log.e(TAG, "PERMISSION NOT GRANTED");
            }
        }

    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject updateObject = new JSONObject(responseBody.string());
            if (updateObject.getBoolean("updateState")) {
                Toast.makeText(getActivity(), "Updated Successfully", Toast.LENGTH_LONG).show();
                Session.setUserData(R.id.update_image_go_icon, buyer);
            } else {
                Toast.makeText(getActivity(), "Update Failed", Toast.LENGTH_LONG).show();
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
