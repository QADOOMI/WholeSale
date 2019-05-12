package fragments.java;

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
import android.widget.Toast;

import com.example.mostafa.e_commerce.R;
import com.example.mostafa.e_commerce.SellerMainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import user.structure.Seller;
import user.structure.Session;
import wholesale.callback.DataCallBack;

public class SellerHomeFragment extends Fragment implements DataCallBack {

    private SearchFragment.SearchAdapter adapter;
    private ProgressBar progressBar;
    private static final String TAG = SellerHomeFragment.class.getSimpleName();

    public SellerHomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.seller_home_fragment, container, false);

        initViews(view);
        ((SellerMainActivity) Objects.requireNonNull(getActivity(), "Activity object is null.")).setPageTitle("Published Products");
        try {
            progressBar.setVisibility(View.VISIBLE);
            ((Seller) Objects.requireNonNull(Session.getUser(Seller.class), "Seller not signed in."))
                    .fetchPublishedPorducts(getActivity(), this);
        } catch (JSONException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);

        }

        return view;
    }

    private void initViews(View view) {
        progressBar = view.findViewById(R.id.seller_prods_progress_bar);
        RecyclerView publishedProducts = view.findViewById(R.id.seller_products_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        publishedProducts.setHasFixedSize(true);
        adapter = new SearchFragment.SearchAdapter(getActivity(), new ArrayList<>(), true);
        publishedProducts.setLayoutManager(linearLayoutManager);
        publishedProducts.setAdapter(adapter);
    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
            try {
                JSONObject productsData = new JSONObject(responseBody.string());
                adapter.addManyProducts(JSONToObject.convertToSearched(productsData, "fetchState"));
                if (adapter.getItemCount() == 0)
                    Toast.makeText(getActivity(), "You didn't publish any products yet.", Toast.LENGTH_LONG).show();
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
        progressBar.setVisibility(View.GONE);
    }

    public static SellerHomeFragment newInstance() {
        return new SellerHomeFragment();
    }
}
