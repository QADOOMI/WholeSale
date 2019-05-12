package fragments.java;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostafa.e_commerce.R;

import java.util.ArrayList;

public class SignUpFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "RegisterFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.signup_fragment, container, false);

        initViews(view);

        return view;
    }

    private void initViews(final View view) {
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter pageAdapter = new SectionsPagerAdapter(getActivity().getSupportFragmentManager());
        pageAdapter.addFragment(new SellerSignUpFragment());
        pageAdapter.addFragment(BuyerSignUpFragment.newInstance(false));

        // Set up the ViewPager with the sections adapter.
        ViewPager viewPager = (ViewPager) view.findViewById(R.id.user_signup_types);
        viewPager.setAdapter(pageAdapter);

        TabLayout viewPagerTabs = (TabLayout) view.findViewById(R.id.signup_tabs);
        TabLayout.Tab tab = viewPagerTabs.newTab();

        viewPagerTabs.addTab(viewPagerTabs.newTab().setText("Sign Up As Seller"), 0);

        viewPagerTabs.addTab(viewPagerTabs.newTab().setText("Sign Up As Buyer"), 1);

        viewPagerTabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        viewPagerTabs.setupWithViewPager(viewPager, true);
    }

    @Override
    public void onClick(View view) {

    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private ArrayList<Fragment> fragments;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            fragments = new ArrayList<>(2);
        }

        void addFragment(Fragment fragment) {
            fragments.add(fragment);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Sign Up As Seller";
                case 1:
                    return "Sign Up As Buyer";
            }
            return null;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a SellerSignUpFragment (defined as a static inner class below).
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }
}
