package fragments.java;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.constraint.Group;
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
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import database.SellerAction;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import user.structure.Seller;
import user.structure.Session;
import wholesale.callback.AuthListener;
import wholesale.callback.DataCallBack;

public class SellerSignUpFragment extends Fragment
        implements View.OnClickListener, AuthListener, Serializable {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String TAG = SellerSignUpFragment.class.getSimpleName();
    private TextInputEditText[] firstTextInputs = new TextInputEditText[7];
    private TextInputEditText[] secondTextInputs = new TextInputEditText[8];
    private TextInputLayout[] firstLayoutInputs = new TextInputLayout[7];
    private TextInputLayout[] secondLayoutInputs = new TextInputLayout[8];
    private transient Spinner governancesPicker;
    private MaterialButton signUpBtn;
    private MaterialButton nextInfoEntry;
    private TextView backToSignIn;
    private transient Group firstInfoGroup;
    private transient Group secondInfoGroup;
    private transient Handler handler = new Handler(Looper.getMainLooper());
    private transient final ArrayList<CompositeDisposable> compositeDisposable;
    private static Seller seller;

    public SellerSignUpFragment() {
        compositeDisposable = new ArrayList<>();
    }

    public static SellerSignUpFragment newInstance(int sectionNumber) {
        SellerSignUpFragment fragment = new SellerSignUpFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static SellerSignUpFragment newInstance(Seller newSeller) {
        SellerSignUpFragment fragment = new SellerSignUpFragment();

        seller = newSeller;

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_signup_fragment, container, false);

        initViews(view);

        nextInfoEntry.setOnClickListener(this);
        signUpBtn.setOnClickListener(this);
        backToSignIn.setOnClickListener(this);
        (view.findViewById(R.id.seller_signin_account_qus)).setOnClickListener(this);
        (view.findViewById(R.id.seller_signin_qus)).setOnClickListener(this);

        return view;
    }

    private void initViews(View view) {
        firstTextInputs[0] = view.findViewById(R.id.company_name_field);
        firstTextInputs[1] = view.findViewById(R.id.phone_number_field);
        firstTextInputs[2] = view.findViewById(R.id.seller_fname_field);
        firstTextInputs[3] = view.findViewById(R.id.seller_lname_field);
        firstTextInputs[4] = view.findViewById(R.id.email_field);
        firstTextInputs[5] = view.findViewById(R.id.pass_field);
        firstTextInputs[6] = view.findViewById(R.id.con_pass_field);

        firstLayoutInputs[0] = view.findViewById(R.id.company_name_layout);
        firstLayoutInputs[1] = view.findViewById(R.id.phone_number_layout);
        firstLayoutInputs[2] = view.findViewById(R.id.seller_fname_layout);
        firstLayoutInputs[3] = view.findViewById(R.id.seller_lname_layout);
        firstLayoutInputs[4] = view.findViewById(R.id.email_layout);
        firstLayoutInputs[5] = view.findViewById(R.id.pass_layout);
        firstLayoutInputs[6] = view.findViewById(R.id.con_pass_layout);

        secondTextInputs[0] = view.findViewById(R.id.seller_town_field);
        secondTextInputs[1] = view.findViewById(R.id.seller_street_field);
        secondTextInputs[2] = view.findViewById(R.id.seller_building_field);
        secondTextInputs[3] = view.findViewById(R.id.seller_apartment_field);
        secondTextInputs[4] = view.findViewById(R.id.seller_floor_field);
        secondTextInputs[5] = view.findViewById(R.id.seller_pobox_field);
        secondTextInputs[6] = (TextInputEditText) view.findViewById(R.id.seller_zip_code_field);
        secondTextInputs[7] = view.findViewById(R.id.seller_creditinfo_field);

        secondLayoutInputs[0] = view.findViewById(R.id.seller_town_layout);
        secondLayoutInputs[1] = view.findViewById(R.id.seller_street_layout);
        secondLayoutInputs[2] = view.findViewById(R.id.seller_building_layout);
        secondLayoutInputs[3] = view.findViewById(R.id.seller_apartment_layout);
        secondLayoutInputs[4] = view.findViewById(R.id.seller_floor_layout);
        secondLayoutInputs[5] = view.findViewById(R.id.seller_pobox_layout);
        secondLayoutInputs[6] = view.findViewById(R.id.seller_zipcode_layout);
        secondLayoutInputs[7] = view.findViewById(R.id.seller_creditinfo_layout);

        firstInfoGroup = view.findViewById(R.id.first_seller_info);
        secondInfoGroup = view.findViewById(R.id.second_seller_info);
        nextInfoEntry = view.findViewById(R.id.seller_next_btn);
        signUpBtn = view.findViewById(R.id.seller_signup_btn);
        backToSignIn = view.findViewById(R.id.seller_signin_qus);

        if (seller != null) {
            firstTextInputs[0].setText(seller.getSellerInfo().getComapnyName());
            firstTextInputs[1].setText(seller.getPhoneNumber());
            firstTextInputs[2].setText(seller.getFirstName());
            firstTextInputs[3].setText(seller.getLastName());
            firstTextInputs[4].setText(seller.getEmail());
            firstTextInputs[5].setText(seller.getPassword());
            firstTextInputs[6].setText(seller.getPassword());

            signUpBtn.setText(R.string.submit_data_update_btn_text);
        }

        governancesPicker = view.findViewById(R.id.seller_governance_field);
        ArrayAdapter goverAdapter = ArrayAdapter
                .createFromResource(getActivity(), R.array.governances, android.R.layout.simple_spinner_item);
        goverAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        new Thread(() ->
                getActivity().runOnUiThread(() ->
                        governancesPicker.setAdapter(goverAdapter))).start();


    }


    @Override
    public void onClick(View view) {
        // get all empty fields
        ArrayList<TextInputLayout> firstFormEmptyFields = getEmptyFields(firstTextInputs, firstLayoutInputs);
        ArrayList<TextInputLayout> secondFormEmptyFields = getEmptyFields(secondTextInputs, secondLayoutInputs);
        if (view.getId() == R.id.seller_signin_qus || view.getId() == R.id.seller_signin_account_qus) {
            ((NavigationHost) getActivity()).navigateTo(new SignInUserTypeFragment(), false);
        } else if (view.getId() == R.id.seller_next_btn) {
            if (firstFormEmptyFields.size() == 0) {
                if (isPasswordMatch()) {
                    new Thread(() -> {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Objects.requireNonNull(getActivity(), "getActivity is null SellerSignUpFragment")
                                .runOnUiThread(() -> {
                                    firstInfoGroup.setVisibility(View.GONE);
                                    secondInfoGroup.setVisibility(View.VISIBLE);

                                    if (seller != null) {
                                        secondTextInputs[0].setText(seller.getSellerInfo().getTown());
                                        secondTextInputs[1].setText(seller.getSellerInfo().getStreetNum());
                                        secondTextInputs[2].setText(seller.getSellerInfo().getBuildingNum());
                                        secondTextInputs[3].setText(seller.getSellerInfo().getApartmentNum());
                                        secondTextInputs[4].setText(seller.getSellerInfo().getFloorNum());
                                        secondTextInputs[5].setText(seller.getSellerInfo().getPoBox());
                                        secondTextInputs[6].setText(String.valueOf(seller.getSellerInfo().getZipCode()));
                                        secondTextInputs[7].setText(seller.getSellerInfo().getCreditInfo());
                                    }
                                });
                    }).start();
                } else {
                    setInputLayoutAttrs(4, getResources().getString(R.string.diff_pass_msg), R.color.errorRed);
                }
            } else {
                for (int i = 0; i < firstFormEmptyFields.size(); i++) {
                    setLayoutHelperText(firstFormEmptyFields.get(i), getResources().getString(R.string.empty_fields_required_msg), R.color.errorRed);
                }
            }
        } else if (view.getId() == R.id.seller_signup_btn) {
            if (secondFormEmptyFields.size() == 0) {
                Seller.SellerAddiInfo moreSellerInfo = new Seller.SellerAddiInfo(
                        String.valueOf(firstTextInputs[0].getText())
                        , String.valueOf(secondTextInputs[1].getText())
                        , String.valueOf(secondTextInputs[2].getText())
                        , String.valueOf(secondTextInputs[5].getText())
                        , String.valueOf(secondTextInputs[3].getText())
                        , String.valueOf(secondTextInputs[0].getText())
                        , governancesPicker.getSelectedItem() != null ?
                        governancesPicker.getSelectedItem().toString()
                        : getActivity().getResources().getStringArray(R.array.governances)[0]
                        , Integer.valueOf(String.valueOf(secondTextInputs[6].getText()))
                        , String.valueOf(secondTextInputs[4].getText())
                        , String.valueOf(secondTextInputs[7].getText()));

                Seller seller = new Seller(
                        moreSellerInfo
                        , String.valueOf(firstTextInputs[2].getText())
                        , String.valueOf(firstTextInputs[3].getText())
                        , String.valueOf(firstTextInputs[4].getText())
                        , String.valueOf(firstTextInputs[1].getText())
                        , String.valueOf(firstTextInputs[5].getText()));

                if (String.valueOf(signUpBtn.getText()).equals(getResources().getString(R.string.submit_data_update_btn_text))) {
                    try {
                        seller.setUserId(Session.getUser(Seller.class).getUserId());
                        seller.setProfileImage(Session.getUser(Seller.class).getProfileImage());
                        seller.updateUserData(getActivity(), new DataCallBack() {
                            @Override
                            public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                                try {
                                    JSONObject updateState = new JSONObject(responseBody.string());
                                    if (updateState.getBoolean("updateState")) {
                                        Session.setUserData(-1, seller);
                                        getActivity().finish();
                                        Toast.makeText(getActivity(), "Data Updated.", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getActivity(), "No Data Changed Try Again.", Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException | IOException e) {
                                    Log.e(TAG, e.getMessage(), e);
                                }
                            }

                            @Override
                            public void onDataNotExtracted(Throwable error) {
                                Log.e(TAG, error.getMessage(), error);
                            }
                        }, SellerAction.UPDATE);
                    } catch (ExecutionException | InterruptedException | JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        seller.signUp(getActivity(), this);
                    } catch (IllegalStateException | NullPointerException e) {
                        ((BuyerMainActivity) getActivity()).toast(e.getMessage());
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                for (int i = 0; i < secondFormEmptyFields.size(); i++) {
                    setLayoutHelperText(secondFormEmptyFields.get(i), getResources().getString(R.string.empty_fields_required_msg), R.color.errorRed);
                }
            }
        }
    }


    private void setLayoutHelperText(final TextInputLayout layout, final String text, final int COLOR) {
        new Thread(() ->
                handler.post(() -> {
                    layout.setError(text);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    layout.setBoxStrokeColor(getResources().getColor(COLOR));
                })).start();
        for (int i = 0; i < 8; i++) {
            if (i != 7) {
                trackFirstFormTextChange(i);
            }
            trackSecondFormTextChange(i);
        }
    }

    private void setInputLayoutAttrs(final int index, final String text, final int color) {
        new Thread(() ->
                handler.post(() -> {
                    firstLayoutInputs[index].setError(text);
                    firstLayoutInputs[index].setBoxStrokeColor(getResources().getColor(color));
                })).start();
    }

    private ArrayList<TextInputLayout> getEmptyFields(TextInputEditText[] field, TextInputLayout[] layouts) {
        ArrayList<TextInputLayout> emptyLayouts = new ArrayList<>();

        for (int i = 0; i < field.length; i++) {
            if (TextUtils.isEmpty(String.valueOf(field[i].getText()))
                    && (i != 1 || i != 2)) {
                emptyLayouts.add(layouts[i]);
            }
        }
        return emptyLayouts;
    }

    private void changeFieldsToDefault(final TextInputLayout layout) {
        new Thread(() ->
                handler.post(() -> {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    layout.setError(null);
                    layout.setBoxStrokeColor(getResources().getColor(R.color.colorAccent));
                })).start();
    }

    private void trackFirstFormTextChange(final int index) {

        firstTextInputs[index].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (firstTextInputs[index] == firstTextInputs[5]
                        || firstTextInputs[index] == firstTextInputs[6]) {
                    if (charSequence.length() < 8) {
                        setBoxColorInThread(index, getResources().getString(R.string.pass_in_range_msg), R.color.errorRed);
                    }
                } else {
                    changeFieldsToDefault(firstLayoutInputs[index]);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setBoxColorInThread(index, null, R.color.colorAccent);
            }

            private void setBoxColorInThread(final int index, final String errorMsg, final int color) {
                new Thread(() ->
                        handler.post(() -> {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            firstLayoutInputs[index].setBoxStrokeColor(getResources().getColor(color));
                            firstLayoutInputs[index].setError(errorMsg);
                        })).start();
            }
        });
    }

    private void trackSecondFormTextChange(final int index) {

        secondTextInputs[index].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                changeFieldsToDefault(secondLayoutInputs[index]);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setBoxColorInThread(index, null, R.color.colorAccent);
            }

            private void setBoxColorInThread(final int index, final String errorMsg, final int color) {
                new Thread(() ->
                        handler.post(() -> {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            secondLayoutInputs[index].setBoxStrokeColor(getResources().getColor(color));
                            secondLayoutInputs[index].setError(errorMsg);
                        })).start();
            }
        });
    }


    public boolean isPasswordMatch() {
        return String.valueOf(firstTextInputs[5].getText())
                .equals(String.valueOf(firstTextInputs[6].getText()));
    }


    @Override
    public void onSignIn(ResponseBody userCategory, CompositeDisposable compositeDisposable) {

    }

    @Override
    public void onSignUp(ResponseBody sellerState, CompositeDisposable compositeDisposable) {
        this.compositeDisposable.add(compositeDisposable);
        try {
            JSONObject signUpState = new JSONObject(sellerState.string());
            Toast.makeText(getActivity(), signUpState.getString("msg"), Toast.LENGTH_LONG).show();
            if (signUpState.getString("msg").equals("Account Created as Seller."))
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
        if (compositeDisposable != null)
            if (!compositeDisposable.isEmpty())
                if (compositeDisposable.get(0) != null)
                    compositeDisposable.get(0).dispose();
        super.onDestroyView();
    }

    @Override
    public void onStop() {
        if (compositeDisposable != null)
            if (!compositeDisposable.isEmpty())
                if (compositeDisposable.get(0) != null)
                    compositeDisposable.get(0).dispose();
        super.onStop();
    }


}