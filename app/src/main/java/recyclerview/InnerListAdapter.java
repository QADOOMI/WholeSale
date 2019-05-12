package recyclerview;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.card.MaterialCardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;

import java.util.List;

import fragments.java.ProductDetailsFragment;
import product.structure.Product;

import static com.example.mostafa.e_commerce.OtherActionsActivity.CURRENT_PAGE_ID;

class InnerListAdapter extends RecyclerView.Adapter<InnerListAdapter.InnerListViewHolder> {

    private Activity activity;
    private List<Product> innerProductList;
    private final static String TAG = InnerListAdapter.class.getSimpleName();

    InnerListAdapter(Activity activity, List<Product> innerProductList) {
        this.activity = activity;
        this.innerProductList = innerProductList;
    }

    @NonNull
    @Override
    public InnerListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.inner_list_item, viewGroup, false);


        return new InnerListViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull InnerListViewHolder viewHolder, int i) {
        Product product = innerProductList.get(i);
        viewHolder.setImageBitmap(product.getPics()[0]);
        viewHolder.proTitle.setText(product.getProductName());
        viewHolder.proBrand.setText(product.getBrandName());
        viewHolder.productCard.setOnClickListener(view -> {
            Intent intent = new Intent(activity, OtherActionsActivity.class);
            intent.putExtra(CURRENT_PAGE_ID, ProductDetailsFragment.newInstance(product));
            activity.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return innerProductList.size();
    }

    class InnerListViewHolder extends RecyclerView.ViewHolder {
        ImageView proImage;
        TextView proTitle;
        TextView proBrand;
        MaterialCardView productCard;
        ProgressBar loadImgBar;

        InnerListViewHolder(@NonNull View itemView) {
            super(itemView);

            productCard = itemView.findViewById(R.id.inner_pro_item_layout);
            proImage = itemView.findViewById(R.id.inner_pro_item_img);
            proTitle = itemView.findViewById(R.id.inner_pro_item_name);
            proBrand = itemView.findViewById(R.id.inner_pro_item_brand);
            loadImgBar = itemView.findViewById(R.id.load_img_progress);
        }

        void setImageBitmap(final Uri url) {
            loadImgBar.setVisibility(View.VISIBLE);

            Glide.with(activity)
                    .asBitmap()
                    .load(url)
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            loadImgBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            loadImgBar.setVisibility(View.GONE);
                            return false;
                        }
                    }).into(proImage);
        }
    }
}