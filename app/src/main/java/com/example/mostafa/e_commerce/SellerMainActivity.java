package com.example.mostafa.e_commerce;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import java.io.Serializable;

import fragments.java.AddProductFragment;
import fragments.java.ContactsFragment;
import fragments.java.ProfileFragment;
import fragments.java.SellerHomeFragment;
import user.structure.Seller;

public class SellerMainActivity extends AppCompatActivity
        implements NavigationHost, Serializable {

    private Handler handler = new Handler(Looper.getMainLooper());
    private BottomNavigationView navigation;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                navigateTo(SellerHomeFragment.newInstance(), false);
                return true;
            case R.id.navigation_add_product:
                navigateTo(AddProductFragment.newInstance(null, true), false);
                return true;
            case R.id.navigation_messages:
                navigateTo(ContactsFragment.newInstance(Seller.class), false);
                return true;
            case R.id.navigation_profile:
                navigateTo(ProfileFragment.newInstance(), false);
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_main);
        assert getSupportActionBar() != null;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(6);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.seller_layout_container, new SellerHomeFragment())
                    .commitAllowingStateLoss();
        }

        navigation = findViewById(R.id.seller_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    public BottomNavigationView getNavigation() {
        return navigation;
    }

    public void setPageTitle(final String title) {
        new Thread(() ->
                handler.post(() ->
                        getSupportActionBar().setTitle(title))).start();
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.seller_layout_container, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commitAllowingStateLoss();
    }

    @Override
    public void navigateExistedFragment(Fragment fragment) {
        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(fragment.getClass().getName());
        if (fragment1 == null)
            navigateTo(fragment, true);
        else
            navigateTo(fragment, false);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
