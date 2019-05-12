package fragments.java;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import database.JSONToObject;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import product.structure.PriceRange;
import product.structure.Product;
import user.structure.Buyer;
import user.structure.Session;
import wholesale.callback.DataCallBack;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;
import static fragments.java.ProductDetailsFragment.ViewsKeys.CONTACT;
import static fragments.java.ProductDetailsFragment.ViewsKeys.IMAGES_LIST;
import static fragments.java.ProductDetailsFragment.ViewsKeys.PRICE_LIST;
import static fragments.java.ProductDetailsFragment.ViewsKeys.REVIEW;
import static fragments.java.ProductDetailsFragment.ViewsKeys.SHOW_ACTIONS;

public class ProductDetailsFragment extends Fragment implements View.OnClickListener, DataCallBack, Serializable {

    private transient SparseArray<FloatingActionButton> actions;
    private RatingBar ratingBar;
    private ImageButton wishBtn;
    private transient ImagesSliderAdapter imagesSliderAdapter;
    private ProductDetailsAdapter imagesAdapter;
    private ProductDetailsAdapter priceAdapter;
    private static Product product;
    private CoordinatorLayout mainContainer;
    private CollapsingToolbarLayout collapseContainer;
    private TextView prodTypeText;
    private TextView prodNameText;
    private TextView prodBrandText;
    private TextView prodReviewsNumText;
    private TextView prodDescText;
    private TextView prodMinOrderText;
    private boolean isFabVisible;
    private ProgressBar progressBar;
    private transient Buyer currentBuyer;
    private ViewPager imagesSlider;

    public ProductDetailsFragment() {
    }

    public static ProductDetailsFragment newInstance(Product newProduct) {
        ProductDetailsFragment productDetailsFragment = new ProductDetailsFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable("product", newProduct);
        productDetailsFragment.setArguments(bundle);

        product = newProduct;

        return productDetailsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.product_detail_fragment, container, false);

        try {
            currentBuyer = (Buyer) Session.getUser(Buyer.class);
        } catch (JSONException e) {
            e.printStackTrace();
            currentBuyer = null;
        }

        initViews(view);

        try {
            product.fetchProductDetails(getActivity()
                    , this
                    , currentBuyer != null ? currentBuyer.getUserId() : null
            );
        } catch (InterruptedException | ExecutionException | JSONException e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initViews(View view) {
        HashMap<String, RecyclerView> detailsList = new HashMap<>();
        actions = new SparseArray<>();

        mainContainer = view.findViewById(R.id.main_container);
        collapseContainer = view.findViewById(R.id.collapsing_toolbar_layout);

        progressBar = view.findViewById(R.id.progressBar);

        view.findViewById(R.id.buyer_actions_group).setVisibility(View.VISIBLE);

        // buyer actions on products
        actions.append(SHOW_ACTIONS, (FloatingActionButton) view.findViewById(R.id.action_provider_btn));
        actions.append(REVIEW, (FloatingActionButton) view.findViewById(R.id.product_details_review));
        actions.append(CONTACT, (FloatingActionButton) view.findViewById(R.id.product_details_contact));

        actions.get(REVIEW).setOnClickListener(this);
        actions.get(SHOW_ACTIONS).setOnClickListener(this);
        actions.get(CONTACT).setOnClickListener(this);

        ratingBar = view.findViewById(R.id.product_details_rating_bar);
        wishBtn = view.findViewById(R.id.add_to_fav_btn);

        prodTypeText = view.findViewById(R.id.product_type_details);
        prodNameText = view.findViewById(R.id.product_name_details);
        prodBrandText = view.findViewById(R.id.product_details_brand_name);
        prodReviewsNumText = view.findViewById(R.id.product_reviews_num_details);
        prodDescText = view.findViewById(R.id.product_details_description);
        prodMinOrderText = view.findViewById(R.id.product_details_min_order);

        imagesAdapter = new ProductDetailsAdapter(getActivity(), new ArrayList<Uri>());
        detailsList.put(IMAGES_LIST, view.findViewById(R.id.product_images_details_list));
        detailsList.get(IMAGES_LIST).setHasFixedSize(true);
        detailsList.get(IMAGES_LIST).setLayoutManager(
                new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false)
        );
        detailsList.get(IMAGES_LIST).setAdapter(imagesAdapter);

        priceAdapter = new ProductDetailsAdapter(getActivity(), new ArrayList<PriceRange>(), null);
        detailsList.put(PRICE_LIST, view.findViewById(R.id.price_ranges_list));
        detailsList.get(PRICE_LIST).setHasFixedSize(true);
        detailsList.get(PRICE_LIST).setLayoutManager(new GridLayoutManager(getActivity(), 2));
        detailsList.get(PRICE_LIST).setAdapter(priceAdapter);

        // building slider of prducts images
        imagesSliderAdapter = new ImagesSliderAdapter(getActivity(), new ArrayList<>());
        imagesSlider = (ViewPager) view.findViewById(R.id.images_slider);
        imagesSlider.setAdapter(imagesSliderAdapter);
    }

    @Override
    public void onClick(View view) {
        if (currentBuyer != null) {
            if (view.getId() == R.id.action_provider_btn) {
                if (isFabVisible) {
                    closeBuyerFab();
                } else {
                    showBuyerFab();
                }
            } else if (view.getId() == R.id.product_details_review) {
                ((NavigationHost) getActivity()).navigateTo(ReviewsFragment.newInstance(product), true);
            } else if (view.getId() == R.id.product_details_contact) {
                ((NavigationHost) getActivity()).navigateTo(MessagesFragment.newInstance(product.getProdBy(), currentBuyer.bringType()), true);
            }
        } else {
            if (view.getId() == R.id.action_provider_btn) {
                Toast.makeText(getActivity(), "Sign In First.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {

            JSONObject productDetails = new JSONObject(responseBody.string());
            product = JSONToObject.convertToProductDetails(productDetails);

            prodTypeText.setText(product.getType());
            prodNameText.setText(product.getProductName());
            prodBrandText.setText(product.getBrandName());
            prodDescText.setText(product.getDescription());


            if (product.getReview() != null) {
                prodReviewsNumText.setText(String.valueOf(product.getReview().length));
                ratingBar.setRating(product.getReview().length > 0 ?
                        product.getReview()[0].getReviewNum() / product.getReview().length : 0);
            } else {
                ratingBar.setRating(0);
                prodReviewsNumText.setText("0");
            }

            prodMinOrderText.setText(String.valueOf("Minimum Order: " + PriceRange.getMinOrder(product.getPriceRanges())));


            priceAdapter.addPriceRange(product.getPriceRanges(), product.getPrice());
            imagesAdapter.addManyImages(product.getPics());
            imagesSliderAdapter.addManyUriImages(product.getPics());

            // get bitmap dominante color
            try {
                Glide.with(getActivity()).asBitmap().load(product.getPics()[0]).into(new SimpleTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        Palette.from(resource).generate(palette -> {
                            assert palette != null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                Log.e(TAG, "onResourceReady: " + resource);
                                int dominateColor = palette.getDominantColor(getResources().getColor(R.color.colorAccent, null));
                                mainContainer.setBackgroundColor(dominateColor);
                                collapseContainer.setBackgroundColor(dominateColor);
                            }
                        });
                    }
                });
            } catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, e.getMessage(), e);
            }


            // wish data callback
            DataCallBack wishCallBack = new DataCallBack() {
                @Override
                public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                    try {
                        JSONObject wishMsg = new JSONObject(responseBody.string());
                        if (wishMsg.getBoolean("wishState")) {
                            Toast.makeText(getActivity(), wishMsg.getString("wishMsg"), Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getActivity(), wishMsg.getString("wishMsg"), Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException | JSONException error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                }

                @Override
                public void onDataNotExtracted(Throwable error) {
                    Log.e(TAG, error.getMessage(), error);
                }
            };

            if (currentBuyer != null) {
                wishBtn.setVisibility(View.VISIBLE);
                if (product.isFav()) {
                    wishBtn.setImageResource(R.drawable.ic_favorite_black_24dp);
                } else {
                    wishBtn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                }
                wishBtn.setOnClickListener(view -> {
                    if (!product.isFav()) {
                        try {
                            product.wishListIt(getActivity(), wishCallBack, true);
                        } catch (ExecutionException | InterruptedException | JSONException e) {
                            e.printStackTrace();
                        }
                        product.setToFav(true);
                        wishBtn.setImageResource(R.drawable.ic_favorite_black_24dp);
                    } else {
                        try {
                            product.wishListIt(getActivity(), wishCallBack, false);
                        } catch (ExecutionException | InterruptedException | JSONException e) {
                            e.printStackTrace();
                        }
                        product.setToFav(false);
                        wishBtn.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                    }
                });
            }

        } catch (JSONException | IOException e) {
            e.printStackTrace();
            progressBar.setVisibility(View.GONE);
        }
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
    }

    public static final class ImagesSliderAdapter extends PagerAdapter {

        private Context context;
        private ArrayList<Uri> imagesUri;
        private int[] imagesDrawables;

        ImagesSliderAdapter(Context context, ArrayList<Uri> imagesUri) {
            this.context = context;
            this.imagesUri = imagesUri;
        }

        public ImagesSliderAdapter(Context context, int[] imagesDrawables) {
            this.context = context;
            this.imagesDrawables = imagesDrawables;
        }

        void addManyUriImages(Uri[] images) {
            imagesUri.addAll(Arrays.asList(images));
            notifyDataSetChanged();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            AppCompatImageView productImage = new AppCompatImageView(context);
            Glide.with(context)
                    .load(imagesUri != null ? imagesUri.get(position) : context.getResources().getDrawable(imagesDrawables[position], null))
                    .into(productImage);
            container.addView(productImage);
            return productImage;
        }

        @Override
        public int getCount() {
            return imagesUri != null ? imagesUri.size() : imagesDrawables.length;
        }
    }

    private final class ProductDetailsAdapter extends RecyclerView.Adapter<ProductDetailsAdapter.ProductDetailsViewHolder> {

        private Context context;
        private List<Uri> images;
        private ArrayList<PriceRange> priceRanges;

        // for products images list
        ProductDetailsAdapter(Context context, ArrayList<Uri> data) {
            this.context = context;
            this.images = data;
        }

        ProductDetailsAdapter(Context context, ArrayList<PriceRange> priceRanges, String string) {
            this.context = context;
            this.priceRanges = priceRanges;
        }

        void addPriceRange(PriceRange[] newPriceRange, double price) {
            if (newPriceRange.length > 0) {
                priceRanges.addAll(Arrays.asList(newPriceRange));
                notifyDataSetChanged();
            } else {
                priceRanges.add(new PriceRange(10, 10, price));
                notifyDataSetChanged();
            }
        }

        void addManyImages(Uri[] newImages) {
            if (newImages.length > 0) {
                images.addAll(Arrays.asList(newImages));
                notifyDataSetChanged();
            }
        }

        @NonNull
        @Override
        public ProductDetailsAdapter.ProductDetailsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = null;
            if (priceRanges != null) {
                view = inflater.inflate(R.layout.price_ranges_layout, parent, false);
            } else {
                view = inflater.inflate(R.layout.images_products_layout, parent, false);
            }
            return new ProductDetailsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ProductDetailsViewHolder holder, int position) {
            if (priceRanges != null) {
                PriceRange priceRange = priceRanges.get(position);
                holder.unitPrice.setText(String.valueOf(priceRange.getPrice() + " JOD"));
                holder.unitsRange.setText(String.valueOf(priceRange.getMinQuantity()).concat("-").concat(String.valueOf(priceRange.getMaxQuantity())));
            } else if (images != null) {
                Uri imageUri = images.get(position);

                holder.productImage.setOnClickListener(view ->
                        new Thread(() ->
                                imagesSlider.post(() ->
                                            imagesSlider.setCurrentItem(position, true))).start());

                Glide.with(holder.productImage.getContext())
                        .asBitmap()
                        .load(imageUri)
                        .into(holder.productImage);
            }
        }

        @Override
        public int getItemCount() {
            return (priceRanges != null) ? priceRanges.size() : images.size();
        }

        class ProductDetailsViewHolder extends RecyclerView.ViewHolder {
            TextView unitPrice;
            TextView unitsRange;
            ImageView productImage;

            ProductDetailsViewHolder(@NonNull View view) {
                super(view);
                if (priceRanges != null) {
                    unitPrice = view.findViewById(R.id.unit_price);
                    unitsRange = view.findViewById(R.id.range_ofunits_price);
                } else {
                    productImage = view.findViewById(R.id.product_details_image_view);
                }
            }
        }
    }

    private void showBuyerFab() {
        isFabVisible = true;
        new Thread(() -> {
            getActivity().runOnUiThread(() -> {
                ((FloatingActionButton) actions.get(CONTACT)).animate().translationY(-getResources().getDimension(R.dimen.standard_55));
                ((FloatingActionButton) actions.get(REVIEW)).animate().translationY(-getResources().getDimension(R.dimen.standard_105));
            });
        }).start();

    }

    private void closeBuyerFab() {
        isFabVisible = false;
        new Thread(() -> {
            getActivity().runOnUiThread(() -> {
                ((FloatingActionButton) actions.get(CONTACT)).animate().translationY(0);
                ((FloatingActionButton) actions.get(REVIEW)).animate().translationY(0);
            });
        }).start();
    }

    protected interface ViewsKeys {
        int SHOW_ACTIONS = 1;
        int REVIEW = 4;
        int CONTACT = 5;
        String IMAGES_LIST = "images_list";
        String PRICE_LIST = "price_list";
    }
}
