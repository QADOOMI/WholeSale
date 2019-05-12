package fragments.java;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mostafa.e_commerce.BuyerMainActivity;
import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import user.structure.Buyer;
import user.structure.Session;
import user.structure.User;
import wholesale.callback.AuthListener;

public class BuyerSignUpFragment extends Fragment
        implements View.OnClickListener, AuthListener, Serializable {

    private static final String TAG = BuyerSignUpFragment.class.getSimpleName();
    private TextInputEditText[] textInputs = new TextInputEditText[6];
    private TextInputLayout[] layoutInputs = new TextInputLayout[6];
    private MaterialButton signUpBtn;
    private TextView backToSignin;
    private User buyer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private final ArrayList<CompositeDisposable> compositeDisposable;
    private static final String IS_UPDATE = "update";

    public BuyerSignUpFragment() {
        compositeDisposable = new ArrayList<>();
    }

    public static BuyerSignUpFragment newInstance(boolean isUpdate) {
        BuyerSignUpFragment fragment = new BuyerSignUpFragment();
        Bundle args = new Bundle();
        args.putBoolean(IS_UPDATE, isUpdate);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.buyer_signup_fragement, container, false);

        initViews(view);

        if (getArguments().getBoolean(IS_UPDATE)) {
            try {
                buyer = Session.getUser(Buyer.class);
                textInputs[0].setText(buyer.getFirstName());
                textInputs[1].setText(buyer.getLastName());
                textInputs[2].setText(buyer.getEmail());
                textInputs[3].setText(buyer.getPassword());
                textInputs[4].setText(buyer.getPhoneNumber());
                layoutInputs[5].setVisibility(View.GONE);
                signUpBtn.setText(R.string.buyer_changes_submit_text);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        signUpBtn.setOnClickListener(this);
        backToSignin.setOnClickListener(this);

        return view;
    }

    private void initViews(View view) {
        textInputs[0] = view.findViewById(R.id.first_name_field);
        textInputs[1] = view.findViewById(R.id.last_name_field);
        textInputs[2] = view.findViewById(R.id.phone_number_field);
        textInputs[3] = view.findViewById(R.id.email_field);
        textInputs[4] = view.findViewById(R.id.pass_field);
        textInputs[5] = view.findViewById(R.id.con_pass_field);

        layoutInputs[0] = view.findViewById(R.id.first_name_layout);
        layoutInputs[1] = view.findViewById(R.id.last_name_layout);
        layoutInputs[2] = view.findViewById(R.id.phone_number_layout);
        layoutInputs[3] = view.findViewById(R.id.email_layout);
        layoutInputs[4] = view.findViewById(R.id.pass_layout);
        layoutInputs[5] = view.findViewById(R.id.con_pass_layout);

        signUpBtn = view.findViewById(R.id.sign_up_btn);
        backToSignin = view.findViewById(R.id.signin_account_qus);
    }

    @Override
    public void onClick(View view) {
        ArrayList<TextInputLayout> layout = getEmptyFields(textInputs);
        if (view.getId() == R.id.signin_account_qus) {
            ((NavigationHost) getActivity()).navigateTo(new SignInUserTypeFragment(), false);
        } else if (view.getId() == R.id.sign_up_btn) {
            if (!getArguments().getBoolean(IS_UPDATE)) {
                if (isPasswordMatch()) {
                    if (layout.size() == 0) {
                        try {
                            Log.d(TAG, "before buyer creation");
                            buyer = new Buyer(String.valueOf(textInputs[0].getText()),
                                    String.valueOf(textInputs[1].getText()),
                                    String.valueOf(textInputs[3].getText()),
                                    String.valueOf(textInputs[2].getText()),
                                    String.valueOf(textInputs[4].getText()));
                            buyer.signUp(getActivity(), this);

                        } catch (IllegalStateException | NullPointerException e) {
                            ((BuyerMainActivity) getActivity()).toast(e.getMessage());
                        } catch (Exception e) {
                            Log.e(TAG, e.getMessage(), e);
                        }
                    } else {
                        for (int i = 0; i < layout.size(); i++) {
                            setLayoutHelperText(layout.get(i), getResources().getString(R.string.empty_fields_required_msg), R.color.errorRed);
                        }
                    }
                } else {
                    setInputLayoutAttrs(4, getResources().getString(R.string.diff_pass_msg), R.color.errorRed);
                }
            } else {

            }
        }
    }

    private void setLayoutHelperText(final TextInputLayout layout, final String text, final int COLOR) {
        new Thread(() -> handler.post(() -> {
            layout.setError(text);
            layout.setBoxStrokeColor(getResources().getColor(COLOR));
        })).start();

        for (int i = 0; i < 6; i++) {
            trackTextChange(i, 16);
            // removeErrorText(textInputs[i], layoutInputs[i]);
        }

    }

    private void setInputLayoutAttrs(final int index, final String text, final int color) {
        new Thread(() ->
                handler.post(() -> {
                    layoutInputs[index].setError(text);
                    layoutInputs[index].setBoxStrokeColor(getResources().getColor(color));
                })).start();
    }

    private ArrayList<TextInputLayout> getEmptyFields(TextInputEditText[] field) {
        ArrayList<TextInputLayout> layouts = new ArrayList<>();

        for (int i = 0; i < field.length; i++) {
            if (TextUtils.isEmpty(String.valueOf(field[i].getText()))
                    && (i != 1 || i != 2)) {
                layouts.add(layoutInputs[i]);
            }
        }
        return layouts;
    }

    private void trackTextChange(final int index, final int charAllowed) {

        textInputs[index].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (textInputs[index] == textInputs[4]
                        || textInputs[index] == textInputs[5]) {
                    if (charSequence.length() < 8) {
                        setBoxColorInThread(index, getResources().getString(R.string.pass_in_range_msg), R.color.errorRed);
                    }
                } else {
                    changeFieldsToDefault(layoutInputs[index]);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setBoxColorInThread(index, null, R.color.colorAccent);
            }

            private void setBoxColorInThread(final int index, final String errorMsg, final int color) {
                new Thread(() -> handler.post(() -> {
                    layoutInputs[index].setBoxStrokeColor(getResources().getColor(color));
                    layoutInputs[index].setError(errorMsg);
                })).start();
            }
        });
    }

    private void changeFieldsToDefault(final TextInputLayout layout) {
        new Thread(() ->
                layout.post(() -> {
                    layout.setError(null);
                    layout.setBoxStrokeColor(getResources().getColor(R.color.colorAccent));
                })).start();
    }

    public boolean isPasswordMatch() {
        return String.valueOf(textInputs[4].getText())
                .equals(String.valueOf(textInputs[5].getText()));
    }

    @Override
    public void onSignIn(ResponseBody userCategory, CompositeDisposable compositeDisposable) {

    }

    @Override
    public void onSignUp(ResponseBody buyerState, CompositeDisposable compositeDisposable) {
        this.compositeDisposable.add(compositeDisposable);
        try {
            JSONObject signUp = new JSONObject(buyerState.string());
            Toast.makeText(getActivity(), signUp.getString("msg"), Toast.LENGTH_LONG).show();

            if (signUp.getString("msg").equals("Account Created as Buyer."))
                getActivity().getSupportFragmentManager().popBackStack();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onErrorOccured(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
    }

    @Override
    public void onDestroyView() {
        if (!compositeDisposable.isEmpty())
            if (compositeDisposable.get(0) != null)
                compositeDisposable.get(0).dispose();
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        if (!compositeDisposable.isEmpty())
            if (compositeDisposable.get(0) != null)
                compositeDisposable.get(0).dispose();
        super.onStop();
    }
}
