package dialog;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.DataCallBack;

public final class UpdateDialog extends DialogFragment implements View.OnClickListener, DataCallBack {

    private static final String TAG = UpdateDialog.class.getSimpleName();
    private TextInputLayout[] layouts = new TextInputLayout[6];
    private TextInputEditText[] inputs = new TextInputEditText[6];
    private MaterialButton submitBtn;
    private MaterialButton cancelBtn;
    private Group nameGroup;
    private Group passwordGroup;
    private Buyer buyer;
    private Handler handler = new Handler(Looper.getMainLooper());
    private final static String ACTION_ID = "actionid";

    public UpdateDialog() {
    }

    public static UpdateDialog getInstance(int actionId) {
        UpdateDialog choiceFragment = new UpdateDialog();

        Bundle bundle = new Bundle();
        bundle.putInt(ACTION_ID, actionId);

        choiceFragment.setArguments(bundle);
        return choiceFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.update_data_dialog, container, false);

        initViews(view);

        try {
            buyer = (Buyer) Session.getUser(Buyer.class);
            if (getArguments().getInt(ACTION_ID) == R.id.update_name_go_icon) {
                nameGroup.setVisibility(View.VISIBLE);
                inputs[0].setText(buyer.getFirstName());
                inputs[1].setText(buyer.getLastName());
            } else if (getArguments().getInt(ACTION_ID) == R.id.update_password_go_icon) {
                passwordGroup.setVisibility(View.VISIBLE);
            } else if (getArguments().getInt(ACTION_ID) == R.id.update_phone_number_go_icon) {
                layouts[2].setVisibility(View.VISIBLE);
                inputs[2].setText(buyer.getPhoneNumber());
            } else if (getArguments().getInt(ACTION_ID) == R.id.update_email_go_icon) {
                layouts[3].setVisibility(View.VISIBLE);
                inputs[3].setText(buyer.getEmail());
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        submitBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);

        return view;
    }

    private void initViews(View view) {
        inputs[0] = view.findViewById(R.id.first_name_field);
        inputs[1] = view.findViewById(R.id.last_name_field);
        inputs[2] = view.findViewById(R.id.phone_number_field);
        inputs[3] = view.findViewById(R.id.email_field);
        inputs[4] = view.findViewById(R.id.pass_field);
        inputs[5] = view.findViewById(R.id.con_pass_field);

        layouts[0] = view.findViewById(R.id.first_name_layout);
        layouts[1] = view.findViewById(R.id.last_name_layout);
        layouts[2] = view.findViewById(R.id.phone_number_layout);
        layouts[3] = view.findViewById(R.id.email_layout);
        layouts[4] = view.findViewById(R.id.pass_layout);
        layouts[5] = view.findViewById(R.id.con_pass_layout);

        submitBtn = view.findViewById(R.id.cancel_btn);
        cancelBtn = view.findViewById(R.id.submit_btn);

        nameGroup = view.findViewById(R.id.update_name_group);
        passwordGroup = view.findViewById(R.id.update_password_group);
    }


    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.cancel_btn) {
            dismissAllowingStateLoss();
        } else if (view.getId() == R.id.submit_btn) {
            try {
                if (passwordGroup.getVisibility() == View.VISIBLE) {
                    // password update
                    if (isPassValid()) {
                        Log.d(TAG, "password: " + String.valueOf((inputs[4].getText())));
                        buyer.setPassword(String.valueOf((inputs[4].getText())));
                        buyer.updateUserData(getActivity(), this, R.id.update_password_go_icon);
                    } else {
                        new Thread(() -> {
                            handler.post(() -> {
                                layouts[4].setBoxStrokeColor(getResources().getColor(R.color.errorRed));
                                layouts[4].setError("Password is the same as the old or password doesn't match");
                            });
                        }).start();
                        inputs[4].addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                            }

                            @Override
                            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                            }

                            @Override
                            public void afterTextChanged(Editable editable) {
                                layouts[4].setBoxStrokeColor(getResources().getColor(R.color.colorAccent));
                                layouts[4].setError(null);
                            }
                        });
                    }
                } else {
                    // update based on user request
                    // by action id(button clicked id)
                    assert getArguments() != null;
                    if (getArguments().getInt(ACTION_ID) == R.id.update_name_go_icon) {
                        buyer.setFirstName(String.valueOf(inputs[0].getText()));
                        buyer.setLastName(String.valueOf(inputs[1].getText()));
                    } else if (getArguments().getInt(ACTION_ID) == R.id.update_phone_number_go_icon) {
                        buyer.setPhoneNumber(String.valueOf(inputs[2].getText()));
                    } else if (getArguments().getInt(ACTION_ID) == R.id.update_email_go_icon) {
                        buyer.setEmail(String.valueOf(inputs[3].getText()));
                    }
                    buyer.updateUserData(getActivity(), this, getArguments().getInt(ACTION_ID));
                }
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }


    private boolean isPassValid() {
        return (!String.valueOf(inputs[4].getText()).equals(buyer.getPassword()))
                && String.valueOf(inputs[5].getText()).equals(String.valueOf(inputs[4].getText()));
    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject updateObject = new JSONObject(responseBody.string());
            if (updateObject.getBoolean("updateState")) {
                Toast.makeText(getActivity(), "Updated Successfully", Toast.LENGTH_LONG).show();
                Session.setUserData(getArguments().getInt(ACTION_ID), buyer);
                dismissAllowingStateLoss();
            } else {
                Toast.makeText(getActivity(), "Updated Failed", Toast.LENGTH_LONG).show();
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
