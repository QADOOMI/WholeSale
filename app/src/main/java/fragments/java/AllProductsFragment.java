package fragments.java;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import product.structure.Product;
import recyclerview.AllProductAdapter;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.DataCallBack;

public class AllProductsFragment extends Fragment
        implements DataCallBack, View.OnClickListener, Serializable {

    private static final String PRODUCT = "product11";
    private static final String TAG = AllProductsFragment.class.getSimpleName();
    private RecyclerView allProductsList;
    private AllProductAdapter adapter;
    private static Product product;
    private final ArrayList<Product> productsItems = new ArrayList<>();
    public static final int requestCode = 1;

    public AllProductsFragment() {

    }

    public static AllProductsFragment getInstance(Product newProduct) {
        AllProductsFragment choiceFragment = new AllProductsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PRODUCT, newProduct);
        product = newProduct;

        choiceFragment.setArguments(bundle);
        return choiceFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_products, container, false);
        initViews(view);

        try {
            product.getAllProdData(getActivity()
                    , this
                    , Session.getUser(Buyer.class) != null ? Session.getUser(Buyer.class).getUserId() : null);

        } catch (ExecutionException | InterruptedException | JSONException e) {
            e.printStackTrace();
        }

        ((OtherActionsActivity)getActivity()).setPageTitle(product.getType());


        return view;
    }

    private void initViews(View view) {
        assert getActivity() != null;
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.other_action_toolbar);
        ((OtherActionsActivity) getActivity()).setSupportActionBar(toolbar);
        ((OtherActionsActivity)getActivity()).setPageTitle(product.getType());

        allProductsList = view.findViewById(R.id.all_products_list);
        allProductsList.setHasFixedSize(true);
        allProductsList.setLayoutManager(new GridLayoutManager(
                getActivity()
                , 2
                , GridLayoutManager.VERTICAL
                , false));
        adapter = new AllProductAdapter(getActivity(), productsItems, false);
        new Thread(() ->
                allProductsList.post(() ->
                        allProductsList.setAdapter(adapter))).start();
    }

    @Override
    public void onDataExtracted(ResponseBody productsData, CompositeDisposable compositeDisposable) {
        try {
            ArrayList<Product> products = JSONToObject.convertToProduct(
                    new JSONObject(productsData.string())
                    , product.getType()
                    , false
            );

            adapter.addManyProducts(products);

        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.e(TAG, error.getMessage());
    }

    @Override
    public void onClick(View view) {
//                    if (user != null) {
//                        SelectChoiceFragment dialogFragment = SelectChoiceFragment
//                                .newInstance(R.string.products_type_title, R.array.products_types
//                                        , false, null);
//                        getFragmentManager().beginTransaction()
//                                .add(dialogFragment, SelectChoiceFragment.class.getSimpleName())
//                                .commitAllowingStateLoss();
//                    } else {
//                        startActivity(new Intent(getActivity(), AuthActivity.class));
//                        Toast.makeText(getActivity(), "Sign In first.", Toast.LENGTH_LONG).show();
//                    }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.buyer_menu_actions, menu);


        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.refresh_icon:
                if (!Session.isUserSignedIn()) {
                    Toast.makeText(getActivity(), "Sign In First.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), "Messages show.", Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return false;
        }

    }
}
