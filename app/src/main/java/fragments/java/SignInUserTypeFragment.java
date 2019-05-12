package fragments.java;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.R;

import user.structure.Buyer;
import user.structure.Seller;

public class SignInUserTypeFragment extends Fragment
        implements View.OnClickListener {


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.signin_user_type, container, false);

        view.findViewById(R.id.signin_seller_btn).setOnClickListener(this);
        view.findViewById(R.id.create_account_qus).setOnClickListener(this);
        view.findViewById(R.id.signin_buyer_btn).setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.signin_seller_btn) {
            ((NavigationHost) getActivity()).navigateTo(
                    new SignInFragment().getSignInFragment(Seller.class.getSimpleName())
                    , false);
        } else if (view.getId() == R.id.signin_buyer_btn) {
            ((NavigationHost) getActivity()).navigateTo(
                    new SignInFragment().getSignInFragment(Buyer.class.getSimpleName())
                    , false);
        } else if (view.getId() == R.id.create_account_qus) {
            ((NavigationHost) getActivity()).navigateTo(new SignUpFragment(), true);
        }
    }
}
