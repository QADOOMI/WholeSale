package com.example.mostafa.e_commerce;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import fragments.java.AllCategoriesFragment;
import fragments.java.AllProductsFragment;
import fragments.java.ProductDetailsFragment;

public class OtherActionsActivity extends AppCompatActivity
        implements NavigationHost {

    private static final String TAG = OtherActionsActivity.class.getSimpleName();
    public static final String CURRENT_PAGE_ID = "currentpage";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_actions);

        View actionBarLayout = findViewById(R.id.appBarLayout2);
        Toolbar toolbar = findViewById(R.id.other_action_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(6);
        ActionBar actionBar = getSupportActionBar();
        actionBar.show();

        if (getIntent().getExtras() != null) {
            Fragment currentPageFragment = (Fragment) getIntent().getExtras().getSerializable(CURRENT_PAGE_ID);
            if (currentPageFragment instanceof ProductDetailsFragment) {
                toolbar.setVisibility(View.GONE);
                actionBarLayout.setVisibility(View.GONE);
                actionBar.hide();
            }


            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.other_actions_container, currentPageFragment);

            if (savedInstanceState == null) {
                if (currentPageFragment instanceof AllProductsFragment
                        || currentPageFragment instanceof AllCategoriesFragment)
                    transaction.addToBackStack(currentPageFragment.getTag())
                            .commitAllowingStateLoss();
                else
                    transaction.commitAllowingStateLoss();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.other_actions_container, fragment);

        if (addToBackstack) {
            transaction.addToBackStack(null);
        }

        transaction.commitAllowingStateLoss();
    }

    @Override
    public void navigateExistedFragment(Fragment fragment) {
        Fragment fragment1 = getSupportFragmentManager().findFragmentByTag(fragment.getTag());
        if (fragment1 == null)
            navigateTo(fragment, true);
        else
            navigateTo(fragment, false);
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    public void setPageTitle(String pageTitle) {
        new Thread(() ->
                runOnUiThread(() ->
                        getSupportActionBar().setTitle(pageTitle))).start();

    }
}
