package recyclerview;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.design.button.MaterialButton;
import android.support.design.card.MaterialCardView;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.example.mostafa.e_commerce.R;

import org.json.JSONException;

import java.util.List;

import fragments.java.AllCategoriesFragment;
import fragments.java.ProductDetailsFragment;
import product.structure.Product;
import user.structure.Buyer;
import user.structure.Session;

public class OuterListAdapter extends RecyclerView.Adapter<OuterListAdapter.OuterListViewHolder> {

    private Activity activity;
    private List<OuterItem> outerItems;
    private RecyclerView.RecycledViewPool viewPool;
    private final static String TAG = "OuterListAdapter";
    private OnCatoClickListener clickListener;

    public OuterListAdapter(Activity activity, List<OuterItem> outerItems, OnCatoClickListener clickListener) {
        this.activity = activity;
        this.outerItems = outerItems;
        Log.d(TAG, outerItems.toString());
        this.clickListener = clickListener;
        viewPool = new RecyclerView.RecycledViewPool();
    }

    public void addList(OuterItem outerItem) {
        outerItems.add(outerItem);
        notifyItemInserted(outerItems.size() - 1);
    }

    @NonNull
    @Override
    public OuterListViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.outer_list_item, viewGroup, false);

        OuterListViewHolder holder = new OuterListViewHolder(view);
        holder.innerList.setRecycledViewPool(viewPool);

        return holder;
    }

    @Override
    public void onBindViewHolder(final @NonNull OuterListViewHolder holder, final int position) {
        final OuterItem item = outerItems.get(position);

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
                holder.viewsHolder.post(() ->
                        holder.viewsHolder.startAnimation(anim))).start();

        if (position == 0) {
            holder.productsSliderLayout.setVisibility(View.VISIBLE);
            holder.setProdTypeList();
            try {
                if (Session.getUser(Buyer.class) == null) {
                    holder.signInGroup.setVisibility(View.VISIBLE);
                    holder.signIn.setOnClickListener(view ->
                            clickListener.onCatoClicked(view, null));
                } else {
                    holder.signInGroup.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else
            holder.signInGroup.setVisibility(View.GONE);

        holder.catogoryTitle.setText(item.getOuterListTitle());
        holder.setInnerList(item);
        holder.catogoryPage.setOnClickListener(view ->
                clickListener.onCatoClicked(view, outerItems.get(position).getInnerProducts().get(position)));

    }

    @Override
    public int getItemCount() {
        return outerItems.size();
    }

    class OuterListViewHolder extends RecyclerView.ViewHolder {
        private final RecyclerView prodTypeList;
        private final ViewPager productsSlider;
        RecyclerView innerList;
        Group signInGroup;
        MaterialButton signIn;
        MaterialButton catogoryPage;
        View viewsHolder;
        TextView catogoryTitle;
        MaterialCardView productsSliderLayout;

        OuterListViewHolder(@NonNull View itemView) {
            super(itemView);

            viewsHolder = itemView.findViewById(R.id.outer_list_layout);
            productsSliderLayout = itemView.findViewById(R.id.materialCardView5);
            productsSlider = (ViewPager) itemView.findViewById(R.id.products_type_pager);
            prodTypeList = itemView.findViewById(R.id.prod_type_list);
            innerList = itemView.findViewById(R.id.inner_pro_item_list);
            signInGroup = itemView.findViewById(R.id.sign_in_group);
            signIn = itemView.findViewById(R.id.request_signin_btn);
            catogoryPage = itemView.findViewById(R.id.show_all_products_btn);
            catogoryTitle = itemView.findViewById(R.id.outer_pro_item_title_btn);
        }

        void setInnerList(final OuterItem item) {
            final InnerListAdapter innerAdapter = new InnerListAdapter(activity, item.getInnerProducts());
            innerList.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
            innerList.setHasFixedSize(true);
            new Thread(() -> {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                innerList.post(() ->
                        OuterListViewHolder.this.innerList.setAdapter(innerAdapter));
            }).start();
        }

        void setProdTypeList() {
          // TODO: create timer to slide images automatically

            ProductDetailsFragment.ImagesSliderAdapter productsSliderAdapter
                    = new ProductDetailsFragment.ImagesSliderAdapter(activity, AllCategoriesFragment.productDrawables);

            new Thread(() ->
                    productsSlider.post(() -> productsSlider.setAdapter(productsSliderAdapter))
            ).start();

            ProductTypeAdapter productTypeAdapter
                    = new ProductTypeAdapter(activity.getResources().getStringArray(R.array.products_type_titles), activity);
            prodTypeList.setHasFixedSize(true);
            prodTypeList.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
            new Thread(() -> {
                prodTypeList.post(() -> prodTypeList.setAdapter(productTypeAdapter));
            }).start();
        }
    }

    public interface OnCatoClickListener {
        void onCatoClicked(View view, final Product product);
    }

    public static final class OuterItem {

        private int color = Color.BLUE;
        private String outerListTitle;
        private List<Product> innerProducts;

        public OuterItem(int color, String outerListTitle, List<Product> innerProducts) {
            this.color = color;
            this.outerListTitle = outerListTitle;
            this.innerProducts = innerProducts;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        String getOuterListTitle() {
            return outerListTitle;
        }

        public List<Product> getInnerProducts() {
            return innerProducts;
        }

        public void setInnerItems(List<Product> innerProducts) {
            this.innerProducts = innerProducts;
        }
    }
}
