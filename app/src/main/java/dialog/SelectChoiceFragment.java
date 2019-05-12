package dialog;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.R;
import com.example.mostafa.e_commerce.SellerMainActivity;

import fragments.java.AddProductFragment;
import fragments.java.SellerHomeFragment;

public class SelectChoiceFragment extends DialogFragment {

    private static final String ACTIVITY = "act";
    private static int title;
    private static int list;
    private static boolean isColorsList;
    public static final String TITLE = "TITLE";
    public static final String IS_COLORS_LIST = "ISCOLORS";
    public static final String LIST = "LIST";

    public SelectChoiceFragment() {
    }

    public static SelectChoiceFragment getInstance(int semiTitle, int semiList, boolean semiIsColorsList, Activity newActivity) {
        SelectChoiceFragment choiceFragment = new SelectChoiceFragment();

        Bundle bundle = new Bundle();
        bundle.putInt(TITLE, semiTitle);
        bundle.putInt(LIST, semiList);
        bundle.putBoolean(IS_COLORS_LIST, semiIsColorsList);
        bundle.putSerializable(ACTIVITY, ((SellerMainActivity) newActivity));

        title = semiTitle;
        list = semiList;
        isColorsList = semiIsColorsList;

        choiceFragment.setArguments(bundle);
        return choiceFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        // checkedItems = new ArrayList<>(Objects.requireNonNull(getActivity()).getResources().getStringArray(list).length);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder = builder.setTitle(title);
        if (isColorsList) {
//            builder = builder.setMultiChoiceItems(list, null, (dialogInterface, position, isChecked) -> {
//                if (isChecked) {
//                    checkedItems.add(getActivity()
//                            .getResources()
//                            .getStringArray(R.array.colors_in_hex)[position]);
//                }
//            });
//
//            builder = builder.setPositiveButton(R.string.confirm, (dialog, id) ->
//                    colorsSender.onDataSent(checkedItems., isColorsList)).setNegativeButton(R.string.cancel, (dialog, id) ->
//                    getDialog().dismiss());
        } else {
            builder = builder.setItems(list, (dialogInterface, i) -> ((SellerMainActivity) getActivity())
                    .navigateTo(AddProductFragment.newInstance(getActivity()
                            .getResources()
                            .getStringArray(list)[i], false), false))
                    .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                        getDialog().dismiss();
                        if (getArguments() != null) {
                            if (getArguments().getSerializable(ACTIVITY) != null) {
                                SellerMainActivity mainActivity = (SellerMainActivity) getArguments().getSerializable(ACTIVITY);
                                mainActivity.getNavigation()
                                        .getMenu()
                                        .findItem(R.id.navigation_home)
                                        .setChecked(true);
                                ((NavigationHost) mainActivity).navigateTo(new SellerHomeFragment(), false);
                            }
                        }
                    });
        }

        // Create the AlertDialog object and return it
        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);

        return dialog;
    }

    public interface DialogSender {
        void onDataSent(String data);
    }
}
