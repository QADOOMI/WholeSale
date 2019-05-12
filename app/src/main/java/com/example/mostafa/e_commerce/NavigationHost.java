package com.example.mostafa.e_commerce;

import android.support.v4.app.Fragment;

/**
 * A host (typically an {@code Activity}} that can display fragments and knows how to respond to
 * seller_navigation events.
 */
public interface NavigationHost {
    /**
     * Trigger a seller_navigation to the specified fragment, optionally adding a transaction to the back
     * stack to make this seller_navigation reversible.
     */
    void navigateTo(Fragment fragment, boolean addToBackstack);

    void navigateExistedFragment(Fragment fragment);
}
