package com.example.mostafa.e_commerce;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import dialog.Dialog;
import fragments.java.ContactsFragment;
import fragments.java.HomeFragment;
import fragments.java.SearchFragment;
import fragments.java.UpdateUserFragment;
import fragments.java.WishListFragment;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.DataCallBack;

import static com.example.mostafa.e_commerce.OtherActionsActivity.CURRENT_PAGE_ID;
import static database.Authenticate.DEL_ACC_REQ;

public class BuyerMainActivity extends AppCompatActivity
        implements NavigationHost, View.OnClickListener, NavigationView.OnNavigationItemSelectedListener, DataCallBack {

    private static final String TAG = BuyerMainActivity.class.getSimpleName();
    private BottomNavigationView navigation;
    private Handler handler = new Handler(Looper.getMainLooper());


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                navigateTo(HomeFragment.newInstance(), false);
                return true;
            case R.id.navigation_wishlist:
                navigateTo(WishListFragment.newInstance(), false);
                return true;
            case R.id.navigation_messages:
                navigateTo(ContactsFragment.newInstance(Buyer.class), false);
                return true;
            case R.id.navigation_search:
                navigateTo(SearchFragment.newInstance(), false);
                return true;
            default:
                return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.other_action_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setElevation(4);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        try {
            if (Session.getUser(Buyer.class) != null) {
                Buyer buyer = (Buyer) Session.getUser(Buyer.class);
                View headerView = navigationView.getHeaderView(0);
                if (buyer != null) {
                    ((TextView) headerView.findViewById(R.id.user_name))
                            .setText(buyer.getFirstName().concat(" ").concat(buyer.getLastName()));

                    ((TextView) headerView.findViewById(R.id.user_email))
                            .setText(buyer.getEmail());

                    Glide.with(this)
                            .asBitmap()
                            .load(buyer.getProfileImage())
                            .into(((ImageView) headerView.findViewById(R.id.profile_image)));
                } else {
                    Log.e(TAG, "no one signed in.");
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        navigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.main_page_section, HomeFragment.newInstance(), HomeFragment.newInstance().getClass().getName())
                    .commitAllowingStateLoss();

        }

        navigation = findViewById(R.id.buyer_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

    }

    @Override
    public void navigateTo(Fragment fragment, boolean addToBackstack) {
        FragmentTransaction transaction =
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.main_page_section, fragment, fragment.getClass().getName());

        if (addToBackstack) {
            transaction.addToBackStack(fragment.getTag());
        }
        transaction.commitNowAllowingStateLoss();
    }

    public void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.sign_in_btn) {
            startActivity(new Intent(this, AuthActivity.class));
        }
    }

    public void setPageTitle(final String title) {
        new Thread(() ->
                handler.post(() ->
                        getSupportActionBar().setTitle(title))).start();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent intent = new Intent();

        if (id == R.id.nav_home) {
            navigation.getMenu().findItem(R.id.navigation_home).setChecked(true);
            navigateTo(HomeFragment.newInstance(), false);
        } else if (id == R.id.nav_wishlist) {
            navigation.getMenu().findItem(R.id.navigation_wishlist).setChecked(true);
            navigateTo(WishListFragment.newInstance(), false);
        } else if (id == R.id.nav_messages) {
            navigation.getMenu().findItem(R.id.navigation_messages).setChecked(true);
            navigateTo(new ContactsFragment(), false);
        } else if (id == R.id.nav_search) {
            navigation.getMenu().findItem(R.id.navigation_search).setChecked(true);
            navigateTo(new SearchFragment(), false);
        } else if (id == R.id.nav_share) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getResources().getString(R.string.share_msg_body));
            startActivity(Intent.createChooser(sharingIntent, "Share via"));
        } else if (id == R.id.nav_go_to_website) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com"));
            startActivity(browserIntent);
        } else if (id == R.id.nav_update_acc) {
            if (Session.isUserSignedIn()) {
                Intent updateAcc = new Intent(this, OtherActionsActivity.class);
                updateAcc.putExtra(CURRENT_PAGE_ID, UpdateUserFragment.newInstance());
                startActivity(updateAcc);
            } else {
                toast(getResources().getString(R.string.sign_in_first_msg));
            }
        } else if (id == R.id.nav_logout) {
            if (Session.isUserSignedIn()) {
                Dialog.criticalActionDialog(
                        this
                        , getString(R.string.signout_acc_dialog_title)
                        , getString(R.string.signout_acc_dialog_msg)
                        , Dialog.SIGN_OUT
                        , null
                        , null);
            } else {
                toast(getResources().getString(R.string.sign_in_first_msg));
            }
        } else if (id == R.id.nav_delete_acc) {
            if (Session.isUserSignedIn()) {
                try {
                    Dialog.criticalActionDialog(
                            this
                            , getString(R.string.delete_acc_dialog_title)
                            , getString(R.string.delete_acc_dialog_msg)
                            , DEL_ACC_REQ
                            , (Buyer) Session.getUser(Buyer.class)
                            , this);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                toast(getResources().getString(R.string.sign_in_first_msg));
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    private Fragment getCurrentFragment() {
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        // get the the current fragment tag
//        int fragmentId = fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 1).getId();
//        // return the fragment based on it's tag
//        return fragmentManager.findFragmentById(fragmentId);
//    }


    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject deleteState = new JSONObject(responseBody.string());
            if (deleteState.getBoolean("deleteState")) {
                Toast.makeText(this, "Deleted Successfully.", Toast.LENGTH_LONG).show();
                Session.signOut();
                this.recreate();
            } else {
                Toast.makeText(this, "Deleted Failed.", Toast.LENGTH_LONG).show();
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
    }

    @Override
    public void navigateExistedFragment(Fragment fragment) {
        boolean isFragmentExists = getSupportFragmentManager().popBackStackImmediate(fragment.getClass().getName(), 0);
        if (!isFragmentExists)
            navigateTo(fragment, true);
        else {
            navigateTo(fragment, false);
        }
    }

}
