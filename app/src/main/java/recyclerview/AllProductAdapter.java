package recyclerview;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import fragments.java.ProductDetailsFragment;
import fragments.java.ReviewsFragment;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import product.structure.Product;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.DataCallBack;

public class AllProductAdapter extends RecyclerView.Adapter<AllProductAdapter.AllProductsViewHolder>
        implements DataCallBack {

    private List<Product> productsList;
    private Activity activity;
    private boolean isWishList;
    private int clickedIndex;
    private final static String PRODUCT_IMAGE = "prodImg";
    private final static String PRODUCT_NAME = "prodName";
    private final static String PRODUCT_PRICE = "prodPrice";
    private final static String PRODUCT_COM = "prodCompany";
    private final static String TAG = AllProductAdapter.class.getSimpleName();

    public AllProductAdapter(Activity activity, List<Product> productsList, boolean isWishList) {
        this.activity = activity;
        this.productsList = productsList;
        this.isWishList = isWishList;
    }

    public void removeAllProducts() {
        productsList.clear();
        notifyDataSetChanged();
    }

    private void removeProductAt(int index) {
        productsList.remove(index);
        notifyItemRemoved(index);
    }

    public void addManyProducts(ArrayList<Product> products) {
        productsList.addAll(products);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AllProductAdapter.AllProductsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = (!isWishList) ? LayoutInflater.from(parent.getContext())
                .inflate(R.layout.acutual_product_item, parent, false) :
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.wish_list_item, parent, false);

        return new AllProductAdapter.AllProductsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AllProductAdapter.AllProductsViewHolder holder, final int position) {
        final Product productsItem = productsList.get(position);

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
                holder.productCard.post(() ->
                        holder.productCard.startAnimation(anim))).start();


        setProductView(holder, productsItem);
        if (isWishList) {
            holder.removeFromFav.setOnClickListener(view -> {
                try {
                    productsItem.wishListIt(activity, this, false);
                    clickedIndex = position;
                } catch (ExecutionException | InterruptedException | JSONException e) {
                    e.printStackTrace();
                }
            });

        } else {
            try {
                if (((Buyer) Session.getUser(Buyer.class)) != null) {
                    if (productsItem.isFav()) {
                        holder.addToFav.setImageResource(R.drawable.ic_favorite_black_24dp);
                    } else {
                        holder.addToFav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    }
                    holder.reviewIt.setOnClickListener(view ->
                            ((NavigationHost) activity).navigateTo(ReviewsFragment.newInstance(productsItem), true)
                    );
                    holder.addToFav.setOnClickListener(view -> {
                        if (!productsItem.isFav()) {
                            try {
                                productsItem.wishListIt(activity, this, true);
                            } catch (ExecutionException | InterruptedException | JSONException e) {
                                e.printStackTrace();
                            }
                            productsItem.setToFav(true);
                            holder.addToFav.setImageResource(R.drawable.ic_favorite_black_24dp);
                        } else {
                            try {
                                productsItem.wishListIt(activity, this, false);
                            } catch (ExecutionException | InterruptedException | JSONException e) {
                                e.printStackTrace();
                            }
                            productsItem.setToFav(false);
                            holder.addToFav.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                        }
                    });
                } else {
                    holder.reviewIt.setVisibility(View.GONE);
                    holder.addToFav.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        holder.productCard.setOnClickListener(view -> {
                    if (!isWishList) {
                        ((NavigationHost) activity).navigateTo(
                                ProductDetailsFragment.newInstance(productsItem)
                                , true);

                    } else {
                        Intent intent = new Intent(activity, OtherActionsActivity.class);
                        intent.putExtra(OtherActionsActivity.CURRENT_PAGE_ID, ProductDetailsFragment.newInstance(productsItem));
                        activity.startActivity(intent);
                    }
                }
        );
    }

    private void setProductView(@NonNull AllProductsViewHolder holder, @NonNull Product productsItem) {
        holder.setProductImg(productsItem.getPics()[0]);
        ((TextView) holder.textViews.get(PRODUCT_NAME))
                .setText(productsItem.getProductName());
        ((TextView) holder.textViews.get(PRODUCT_PRICE))
                .setText(String.valueOf(productsItem.getPrice()).concat(" ").concat("JOD"));
        ((TextView) holder.textViews.get(PRODUCT_COM))
                .setText(productsItem.getBrandName());
    }

    @Override
    public int getItemCount() {
        return productsList.size();
    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject wishMsg = new JSONObject(responseBody.string());
            if (wishMsg.getBoolean("wishState")) {
                if (isWishList)
                    removeProductAt(clickedIndex);
                Toast.makeText(activity, wishMsg.getString("wishMsg"), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(activity, wishMsg.getString("wishMsg"), Toast.LENGTH_LONG).show();
            }
        } catch (IOException | JSONException error) {
            Log.e(TAG, error.getMessage(), error);
        }
    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
    }

    class AllProductsViewHolder extends RecyclerView.ViewHolder {

        HashMap<String, View> textViews;
        //  ProgressBar loadImgBar;
        ImageButton addToFav;
        ImageButton removeFromFav;
        ImageButton reviewIt;
        MaterialCardView productCard;

        AllProductsViewHolder(@NonNull View itemView) {
            super(itemView);

            textViews = new HashMap<>();
            initViews(itemView);
        }

        private void initViews(View view) {
            textViews.put(PRODUCT_IMAGE, view.findViewById(R.id.product_img_view));
            textViews.put(PRODUCT_NAME, view.findViewById(R.id.product_name_view));
            textViews.put(PRODUCT_PRICE, view.findViewById(R.id.product_price_view));
            textViews.put(PRODUCT_COM, view.findViewById(R.id.product_company_view));

            productCard = view.findViewById(R.id.actual_product_card_view);
            if (isWishList)
                removeFromFav = view.findViewById(R.id.remove_from_fav_btn);
            else {
                reviewIt = view.findViewById(R.id.review_product);
                addToFav = view.findViewById(R.id.add_to_fav_btn);
            }
            // loadImgBar = view.findViewById(R.id.load_img_bar);
        }

        void setProductImg(Uri uri) {
            //  loadImgBar.setVisibility(View.VISIBLE);

            Glide.with(textViews.get(PRODUCT_IMAGE).getContext())
                    .asBitmap()
                    .load(uri)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            //   loadImgBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            //   loadImgBar.setVisibility(View.GONE);
                            return false;
                        }
                    }).into((ImageView) textViews.get(PRODUCT_IMAGE));
        }
    }
}
