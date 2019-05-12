package fragments.java;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.mostafa.e_commerce.AuthActivity;
import com.example.mostafa.e_commerce.BuyerMainActivity;
import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import product.structure.AutomotiveParts;
import product.structure.Baby;
import product.structure.Clothing;
import product.structure.Food;
import product.structure.Product;
import recyclerview.OuterListAdapter;
import wholesale.callback.DataCallBack;

import static com.example.mostafa.e_commerce.OtherActionsActivity.CURRENT_PAGE_ID;

@SuppressLint("ValidFragment")
public class HomeFragment extends Fragment implements DataCallBack {

    private final static List<OuterListAdapter.OuterItem> outerItems = new ArrayList<>();
    private ProgressBar progressBar;
    private OuterListAdapter outerListAdapter;
    private final static String TAG = HomeFragment.class.getSimpleName();

    public HomeFragment() {

    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);
        outerItems.clear();

        ((BuyerMainActivity) getActivity()).setPageTitle(getResources().getString(R.string.app_name));

        initViews(view);

        try {
            new Food().getMainPageProducts(getActivity(), this);
            new AutomotiveParts().getMainPageProducts(getActivity(), this);
            new Clothing().getMainPageProducts(getActivity(), this);
            new Baby().getMainPageProducts(getActivity(), this);
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.progressBar);

        RecyclerView outerList = view.findViewById(R.id.outer_product_list);
        outerList.setHasFixedSize(true);
        outerList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        outerListAdapter = new OuterListAdapter(getActivity(), outerItems, (view1, product) -> {
            assert getActivity() != null;
            if (product != null) {
                Intent i = new Intent(getActivity(), OtherActionsActivity.class);
                i.putExtra(CURRENT_PAGE_ID, AllProductsFragment.getInstance(product));
                getActivity().startActivity(i);
            } else {
                getActivity().finish();
                Intent i = new Intent(getActivity(), AuthActivity.class);
                getActivity().startActivity(i);
            }
        });
        outerList.setAdapter(outerListAdapter);
    }

    @Override
    public void onDataExtracted(ResponseBody mainPageData, CompositeDisposable disposable) {
        try {
            ArrayList<Product> products = JSONToObject.convertToMainProduct(new JSONObject(mainPageData.string()));
            outerListAdapter.addList(new OuterListAdapter.OuterItem(R.color.blue, products.get(0).getType(), products));
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }

        if (outerListAdapter.getItemCount() == 4)
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.d(TAG, error.getMessage());
    }

}
