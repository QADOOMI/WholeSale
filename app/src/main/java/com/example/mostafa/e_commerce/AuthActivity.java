package com.example.mostafa.e_commerce;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import fragments.java.SignInUserTypeFragment;

public class AuthActivity extends AppCompatActivity implements NavigationHost {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.auth_container, new SignInUserTypeFragment())
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.auth_container, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(fragment.getTag());
        }

        transaction.commit();
    }

    //navigate to to fragment that already existed in backstack
    @Override
    public void navigateExistedFragment(Fragment fragment) {
        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(fragment.getTag());
        if (fragment1 == null)
            navigateTo(fragment, true);
        else
            navigateTo(fragment, false);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, BuyerMainActivity.class);
        startActivity(intent);
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
