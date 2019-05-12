package fragments.java;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.Group;
import android.support.design.button.MaterialButton;
import android.support.design.card.MaterialCardView;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mostafa.e_commerce.BuyerMainActivity;
import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;
import com.example.mostafa.e_commerce.SellerMainActivity;

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
import user.structure.Buyer;
import user.structure.Seller;
import user.structure.Session;
import wholesale.callback.DataCallBack;

import static com.example.mostafa.e_commerce.OtherActionsActivity.CURRENT_PAGE_ID;

public final class SearchFragment extends Fragment {

    private SearchAdapter adapter;
    private ProgressBar searching;
    private EditText searchField;
    private static final String TAG = SearchFragment.class.getSimpleName();

    public SearchFragment() {
    }

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_fragment, container, false);

        ((BuyerMainActivity) getActivity()).setPageTitle(getResources().getString(R.string.search_page_title));

        initViews(view);

        searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                searching.setVisibility(View.VISIBLE);
                if (adapter.getItemCount() > 0)
                    adapter.clearAll();
                EditText search = ((EditText) v);
                if (!TextUtils.isEmpty(String.valueOf(search.getText()))) {
                    try {
                        new Buyer().search(getActivity()
                                , new DataCallBack() {
                                    @Override
                                    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                                        try {
                                            JSONObject searchedData = new JSONObject(responseBody.string());
                                            adapter.addManyProducts(JSONToObject.convertToSearched(searchedData, "searchState"));
                                        } catch (JSONException | IOException e) {
                                            e.printStackTrace();
                                        } finally {
                                            searching.setVisibility(View.GONE);
                                            if (adapter.getItemCount() == 0)
                                                Toast.makeText(
                                                        getActivity()
                                                        , getResources().getString(R.string.no_products_available_search_msg)
                                                        , Toast.LENGTH_LONG).show();
                                        }

                                    }

                                    @Override
                                    public void onDataNotExtracted(Throwable error) {
                                        Log.e(TAG, "onDataNotExtracted: ", error);
                                        Toast.makeText(
                                                getActivity()
                                                , getResources().getString(R.string.no_products_available_search_msg)
                                                , Toast.LENGTH_LONG).show();
                                    }
                                }
                                , String.valueOf(search.getText()));
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        return false;
                    }
                    return true;
                } else {
                    Toast.makeText(getActivity(), "Fill Search Field First.", Toast.LENGTH_LONG).show();
                }
            }
            return false;
        });
        return view;
    }

    private void initViews(View view) {
        View actionBar = view.findViewById(R.id.appBarLayout2);
        actionBar.setElevation(4);
        searchField = view.findViewById(R.id.add_pro_search_view);
        RecyclerView searchedList = view.findViewById(R.id.searched_list);
        searchedList.setHasFixedSize(true);
        adapter = new SearchAdapter(getActivity(), new ArrayList<>(), false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        searchedList.setLayoutManager(layoutManager);
        searchedList.setAdapter(adapter);
        searching = (ProgressBar) view.findViewById(R.id.search_progress_bar);
    }

    public static class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchViewHolder> {

        private Activity activity;
        private ArrayList<Product> products;
        private boolean sellerMode;
        private DataCallBack sellerActions = new DataCallBack() {
            @Override
            public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                try {
                    JSONObject sellerActionsState = new JSONObject(responseBody.string());
                    if (sellerActionsState.getString("action").equals("delete")) {
                        if (sellerActionsState.getBoolean("deleteState")) {
                            Toast.makeText(activity, "Deleted.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(activity, "Not deleted try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (JSONException | IOException e) {
                    Log.e(TAG, e.getMessage(), e);
                    Toast.makeText(activity, "Operation Failed.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onDataNotExtracted(Throwable error) {
                Log.e(TAG, error.getMessage(), error);
                Toast.makeText(activity, "Operation Failed.", Toast.LENGTH_LONG).show();
            }
        };


        SearchAdapter(Activity activity, ArrayList<Product> products, boolean sellerMode) {
            this.activity = activity;
            this.products = products;
            this.sellerMode = sellerMode;
        }

        void addManyProducts(ArrayList<Product> newProducts) {
            products.addAll(newProducts);
            notifyDataSetChanged();
        }

        void clearAll() {
            products.clear();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_item, parent, false);


            return new SearchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
            Product product = products.get(position);

            ScaleAnimation anim = new ScaleAnimation(
                    0.0f
                    , 1.0f
                    , 0.0f
                    , 1.0f
                    , Animation.RELATIVE_TO_SELF
                    , 0.5f
                    , Animation.RELATIVE_TO_SELF
                    , 0.5f
            );
            anim.setDuration(500);
            new Thread(() ->
                    holder.searchedLayout.post(() ->
                            holder.searchedLayout.startAnimation(anim))).start();

            View.OnClickListener sellerListener = view -> {
                Seller seller = null;
                try {
                    seller = (Seller) Session.getUser(Seller.class);
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }
                if (view.getId() == R.id.actual_product_card_view) {
                    Intent intent = new Intent(activity, OtherActionsActivity.class);
                    if (sellerMode) {
                        intent.putExtra(CURRENT_PAGE_ID, AddProductFragment.newInstance(product));
                    } else {
                        intent.putExtra(CURRENT_PAGE_ID, ProductDetailsFragment.newInstance(product));
                    }
                    activity.startActivity(intent);
                } else if (view.getId() == R.id.delete_prod_data_btn) {
                    Dialog.criticalActionDialog(
                            activity
                            , activity.getResources().getString(R.string.seller_delete_product_title)
                            , activity.getResources().getString(R.string.seller_delete_product_msg)
                            , Dialog.DELETE_PRODUCT
                            , seller
                            , product
                            , sellerActions);
                } else if (view.getId() == R.id.update_prod_data_btn) {
                    Intent updateIntent = new Intent(activity, OtherActionsActivity.class);
                    updateIntent.putExtra(CURRENT_PAGE_ID, AddProductFragment.newInstance(product));
                    ((SellerMainActivity) activity).startActivity(updateIntent);
                }
            };
            if (sellerMode) {
                holder.sellerGroup.setVisibility(View.VISIBLE);
                holder.updateProd.setOnClickListener(sellerListener);
                holder.deleteProd.setOnClickListener(sellerListener);
            } else {
                holder.updateProd.setVisibility(View.GONE);
                holder.deleteProd.setVisibility(View.GONE);
            }

            holder.prodName.setText(product.getProductName());
            holder.prodBrand.setText(product.getBrandName());
            Glide.with(holder.prodPic.getContext())
                    .asBitmap()
                    .load(product.getPics()[0])
                    .into(holder.prodPic);
        }

        @Override
        public int getItemCount() {
            return products.size();
        }


        class SearchViewHolder extends RecyclerView.ViewHolder {

            MaterialCardView searchedLayout;
            ImageView prodPic;
            TextView prodName;
            TextView prodBrand;
            Group sellerGroup;
            MaterialButton deleteProd;
            MaterialButton updateProd;


            SearchViewHolder(@NonNull View itemView) {
                super(itemView);

                deleteProd = itemView.findViewById(R.id.delete_prod_data_btn);
                updateProd = itemView.findViewById(R.id.update_prod_data_btn);
                sellerGroup = itemView.findViewById(R.id.seller_btn_group);
                searchedLayout = itemView.findViewById(R.id.actual_product_card_view);
                prodPic = itemView.findViewById(R.id.product_img_view);
                prodName = itemView.findViewById(R.id.product_name_view);
                prodBrand = itemView.findViewById(R.id.product_company_view);
            }
        }
    }
}
