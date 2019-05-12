package fragments.java;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mostafa.e_commerce.BuyerMainActivity;
import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import dialog.Dialog;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import product.structure.Product;
import recyclerview.AllProductAdapter;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.DataCallBack;

public final class WishListFragment extends Fragment
        implements DataCallBack, View.OnClickListener {

    private static final String TAG = WishListFragment.class.getSimpleName();
    private AllProductAdapter adapter;
    private ArrayList<Product> products;
    private Buyer buyer;
    private TextView pageStateMsg;
    private Group unavailableGroup;
    private ImageView unavailableImage;
    private FloatingActionButton deleteAllWished;
    private Handler handler = new Handler(Looper.getMainLooper());

    public WishListFragment() {
        products = new ArrayList<>();
    }

    public static WishListFragment newInstance() {
        return new WishListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wish_list_fragment, container, false);

        ((BuyerMainActivity) getActivity()).setPageTitle(getResources().getString(R.string.wishlist_title));

        initViews(view);

        try {
            buyer = (Buyer) Session.getUser(Buyer.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (buyer != null) {
            new Thread(() ->
                    handler.post(() ->
                            deleteAllWished.setVisibility(View.VISIBLE))).start();
            try {
                Log.e(TAG, "buyer " + buyer.getUserId());
                buyer.fetchAllWished(getActivity(), this);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            new Thread(() ->
                    handler.post(() -> {
                        unavailableImage.setImageDrawable(getActivity().getDrawable(R.drawable.ic_info_outline_black_50dp));
                        pageStateMsg.setText(getResources().getString(R.string.msgs_page_notsigned_in));
                        unavailableGroup.setVisibility(View.VISIBLE);
                    })).start();
        }

        deleteAllWished.setOnClickListener(this);

        return view;
    }

    private void initViews(View view) {
        assert getActivity() != null;
        pageStateMsg = view.findViewById(R.id.page_state_text);
        unavailableImage = view.findViewById(R.id.unava_messages_img);
        unavailableGroup = view.findViewById(R.id.uava_group);
        deleteAllWished = view.findViewById(R.id.delete_all_wished);

        RecyclerView wishedProducts = view.findViewById(R.id.products_wish_list);
        wishedProducts.setHasFixedSize(true);
        wishedProducts.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        adapter = new AllProductAdapter(getActivity(), products, true);
        wishedProducts.setAdapter(adapter);
    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject productsData = new JSONObject(responseBody.string());
            if (productsData.getBoolean("isFetched")) {
                adapter.addManyProducts(new ArrayList<>(
                                JSONToObject.convertToProduct(
                                        productsData
                                        , null
                                        , true)
                        )
                );
            } else {
                new Thread(() ->
                        handler.post(() -> {
                            unavailableImage.setImageDrawable(getActivity().getDrawable(R.drawable.messages_unavailable));
                            unavailableGroup.setVisibility(View.VISIBLE);
                            pageStateMsg.setText(getResources().getString(R.string.no_avaliable_wished_products));
                        })).start();
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
    public void onClick(View view) {
        if (view.getId() == R.id.delete_all_wished) {
            if (adapter.getItemCount() > 0) {
                Dialog.criticalActionDialog(getActivity()
                        , getString(R.string.delete_all_wished_dialog_title)
                        , getString(R.string.delete_all_wished_dialog_msg)
                        , Dialog.DEL_ALL_WISHED
                        , buyer
                        , new DataCallBack() {
                            @Override
                            public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                                try {
                                    JSONObject deleteState = new JSONObject(responseBody.string());
                                    if (deleteState.getBoolean("deleteState")) {
                                        adapter.removeAllProducts();
                                        Toast.makeText(getActivity(), R.string.toast_delete_wished_msg_success, Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(getActivity(), R.string.toast_delete_wished_msg_failed, Toast.LENGTH_LONG).show();
                                    }
                                } catch (JSONException | IOException e) {
                                    Log.e(TAG, "Delete All Wished: " + e.getMessage(), e);
                                }
                            }

                            @Override
                            public void onDataNotExtracted(Throwable error) {
                                Log.e(TAG, error.getMessage(), error);
                            }
                        });
            } else {
                Toast.makeText(getActivity(), "No Wished Products To delete.", Toast.LENGTH_LONG).show();
            }
        }
    }
}
