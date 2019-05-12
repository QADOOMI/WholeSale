package fragments.java;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mostafa.e_commerce.BuyerMainActivity;
import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;
import com.example.mostafa.e_commerce.SellerMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import dialog.Dialog;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import user.structure.Seller;
import user.structure.Session;
import wholesale.callback.DataCallBack;

import static database.Authenticate.DEL_ACC_REQ;


public class ProfileFragment extends Fragment implements View.OnClickListener, DataCallBack {

    private final static String TAG = ProfileFragment.class.getSimpleName();
    private static final int IMAGE_FETCHED = 12;
    private Seller seller;
    private ImageView profileImg;

    public ProfileFragment() {
    }

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_fragment, container, false);

        ((SellerMainActivity) getActivity()).setPageTitle("Profile");

        try {
            seller = (Seller) Session.getUser(Seller.class);
            initViews(view);
        } catch (JSONException | ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initViews(View view) throws ExecutionException, InterruptedException {

        profileImg = view.findViewById(R.id.profile_image);
        Glide.with(profileImg.getContext())
                .asBitmap()
                .load(seller.getProfileImage())
                .listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        if (e != null) {
                            Log.e(TAG, "onLoadFailed Profile Image: ", e);
                            return true;
                        } else
                            return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(profileImg);

        ((TextView) view.findViewById(R.id.name))
                .setText(seller.getFirstName().concat(" ").concat(seller.getLastName()));

        ((TextView) view.findViewById(R.id.address))
                .setText(seller.getSellerInfo().getGovernance().concat(", ").concat(seller.getSellerInfo().getTown()));

        ((TextView) view.findViewById(R.id.brand_name))
                .setText(seller.getSellerInfo().getComapnyName());

        seller.fetchProdsCount(getActivity(), new DataCallBack() {
            @Override
            public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                try {
                    JSONObject prodsCount = new JSONObject(responseBody.string());
                    final String prodsText = "Products: ";
                    ((TextView) view.findViewById(R.id.num_of_published_prods))
                            .setText(prodsText.concat(String.valueOf(prodsCount.getJSONObject("prodsCount").getJSONArray("recordset").getJSONObject(0).getInt("prodsCount"))));
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onDataNotExtracted(Throwable error) {
                Log.e(TAG, error.getMessage(), error);
            }
        });

        view.findViewById(R.id.update_data).setOnClickListener(this);
        view.findViewById(R.id.delete_account).setOnClickListener(this);
        view.findViewById(R.id.sign_out).setOnClickListener(this);
        view.findViewById(R.id.update_pic).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.update_data: {
                Intent updateAccPage = new Intent(getActivity(), OtherActionsActivity.class);
                updateAccPage.putExtra(OtherActionsActivity.CURRENT_PAGE_ID, SellerSignUpFragment.newInstance(seller));
                getActivity().startActivity(updateAccPage);
                return;
            }
            case R.id.delete_account: {
                Dialog.criticalActionDialog(
                        getActivity()
                        , getResources().getString(R.string.delete_account_title)
                        , getResources().getString(R.string.delete_acc_dialog_msg)
                        , DEL_ACC_REQ
                        , seller
                        , new DataCallBack() {
                            @Override
                            public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                                try {
                                    JSONObject deleteState = new JSONObject(responseBody.string());
                                    if (deleteState.getBoolean("deleteState")) {
                                        Toast.makeText(getActivity(), "Deleted Successfully.", Toast.LENGTH_LONG).show();
                                        Session.signOut();
                                        getActivity().startActivity(new Intent(getActivity(), BuyerMainActivity.class));
                                    } else {
                                        Toast.makeText(getActivity(), "Deleted Failed.", Toast.LENGTH_LONG).show();
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
                );
                return;
            }
            case R.id.sign_out: {
                Dialog.criticalActionDialog(
                        getActivity()
                        , getResources().getString(R.string.sign_out_title)
                        , getResources().getString(R.string.sign_out_msg)
                        , Dialog.SIGN_OUT
                        , seller
                        , null
                );
                return;
            }
            case R.id.update_pic: {
                if (ActivityCompat.checkSelfPermission(getActivity()
                        , Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_FETCHED);
                } else {
                    ActivityCompat.requestPermissions(
                            getActivity()
                            , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                            , IMAGE_FETCHED
                    );
                }

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case IMAGE_FETCHED: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_FETCHED);
                }
                // other 'case' lines to check for other
                // permissions this app might request.
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IMAGE_FETCHED) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    seller.setProfileImage(data.getData().toString());
                    try {
                        Glide.with(profileImg.getContext())
                                .asBitmap()
                                .load(seller.getProfileImage())
                                .into(profileImg);

                        seller.updateUserData(getActivity(), this, -1);
                        Session.setUserData(-1, seller);
                    } catch (JSONException | ExecutionException | InterruptedException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                } else {
                    Log.e(TAG, "No Image Picked From Gallery.");
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getActivity(), "Canceled", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Bad Request.");
        }
    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject updateState = new JSONObject(responseBody.string());

            if (updateState.getBoolean("updateState")) {
                Toast.makeText(getActivity(), "Updated Successfully.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(), "Update Failed.", Toast.LENGTH_LONG).show();
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
