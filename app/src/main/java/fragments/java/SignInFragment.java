package fragments.java;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mostafa.e_commerce.AuthActivity;
import com.example.mostafa.e_commerce.BuyerMainActivity;
import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.R;
import com.example.mostafa.e_commerce.SellerMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import io.reactivex.disposables.CompositeDisposable;
import networking.InternetChecker;
import okhttp3.ResponseBody;
import user.structure.Buyer;
import user.structure.Seller;
import user.structure.Session;
import user.structure.User;
import wholesale.callback.AuthListener;

public class SignInFragment extends Fragment
        implements View.OnClickListener, AuthListener {

    private MaterialButton signInBtn;
    private TextInputEditText email, password;
    private TextInputLayout passwordLayout;
    private TextView register;
    private static String userType;
    private User user;
    private static final String TAG = SignInFragment.class.getSimpleName();
    private final ArrayList<CompositeDisposable> compositeDisposable;

    public SignInFragment() {
        compositeDisposable = new ArrayList<>();
    }

    public SignInFragment getSignInFragment(String userType) {
        SignInFragment.userType = userType;

        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.login_fragment, container, false);

        initViews(view);

        signInBtn.setOnClickListener(this);
        register.setOnClickListener(this);

        return view;
    }

    private void initViews(final View view) {
        signInBtn = view.findViewById(R.id.sign_in_btn);
        register = view.findViewById(R.id.create_account_qus);

        email = view.findViewById(R.id.login_email_field);
        password = view.findViewById(R.id.login_password_field);

        TextInputLayout emailLayout = view.findViewById(R.id.email_layout);
        passwordLayout = view.findViewById(R.id.password_layout);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onClick(View view) {
        try {
            if (new InternetChecker(getActivity()).execute().get()) {
                if (view.getId() == R.id.sign_in_btn) {
                    if (!TextUtils.isEmpty(email.getText())) {
                        if (!TextUtils.isEmpty(password.getText())) {
                            user = userType.equals(Buyer.class.getSimpleName()) ?
                                    new Buyer(String.valueOf(email.getText()), String.valueOf(password.getText())) :
                                    new Seller(String.valueOf(email.getText()), String.valueOf(password.getText()));
                            user.signIn(getActivity(), this);
                        } else {
                            passwordLayout.setError("Insert your password");
                            passwordLayout.setBoxStrokeColor(getResources().getColor(R.color.errorRed, null));
                        }
                    } else {
                        ((AuthActivity) getActivity()).toast("Insert your email");
                    }
                } else if (view.getId() == R.id.create_account_qus) {
                    ((NavigationHost) getActivity()).navigateTo(new SignUpFragment(), true);
                }
            } else {
                ((AuthActivity) getActivity()).toast("Connect to the internet first");
            }
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSignIn(ResponseBody signInState, CompositeDisposable compositeDisposable) {
        this.compositeDisposable.add(compositeDisposable);
        // user
        try {
            // get the data from response body
            JSONObject res = new JSONObject(signInState.string());
            if (res.getBoolean("signInState")) {
                Toast.makeText(getActivity(), "Sign in successful.", Toast.LENGTH_LONG).show();

                if (userType.equals(Seller.class.getSimpleName())) {

                    // get the user that just signed in
                    JSONObject userData = res.getJSONArray("signInData").getJSONObject(0);

                    user = JSONToObject.convertToSeller(userData);
                    Session.createSession(userData.toString(), getActivity());

                } else if (userType.equals(Buyer.class.getSimpleName())) {
                    // get the user that just signed in
                    JSONObject userData = res.getJSONObject("result")
                            .getJSONArray("recordset").getJSONObject(0);

                    JSONObject userImage = res.getJSONObject("imageResult")
                            .getJSONArray("recordset").getJSONObject(0);

                    user = JSONToObject.convertToBuyer(userData.toString() + " " + userImage);
                    Session.createSession(userData.toString() + " " + userImage.toString(), getActivity());
                }
                navigateToActivity();
            } else {
                Toast.makeText(getActivity(), "Sign in failed, check your data.", Toast.LENGTH_LONG).show();
                Log.e(TAG, res.getString("error"));
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToActivity() {
        assert getActivity() != null;
        if (userType.equals(Seller.class.getSimpleName())) {
            Intent i = new Intent(getActivity(), SellerMainActivity.class);
            getActivity().startActivity(i);
        } else if (userType.equals(Buyer.class.getSimpleName())) {
            Intent i = new Intent(getActivity(), BuyerMainActivity.class);
            getActivity().startActivity(i);
        }
        getActivity().finish();
    }

    @Override
    public void onSignUp(ResponseBody userCategory, CompositeDisposable compositeDisposable) {

    }

    @Override
    public void onErrorOccured(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (!compositeDisposable.isEmpty())
            if (compositeDisposable.get(0) != null)
                compositeDisposable.get(0).dispose();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (!compositeDisposable.isEmpty())
            if (compositeDisposable.get(0) != null)
                compositeDisposable.get(0).dispose();
    }
}
