package recyclerview;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;

import fragments.java.AllCategoriesFragment;
import fragments.java.AllProductsFragment;
import product.structure.IProductBuilder;

import static com.example.mostafa.e_commerce.OtherActionsActivity.CURRENT_PAGE_ID;

public class ProductTypeAdapter extends RecyclerView.Adapter<ProductTypeAdapter.CatoViewHolder> {

    private String[] titles;
    private Activity activity;
    public static final int[] drawables = {
            R.drawable.ic_directions_car_black_24dp
            , R.drawable.ic_accessibility_black_24dp
            , R.drawable.ic_child_friendly_black_24dp
            , R.drawable.ic_restaurant_menu_black_24dp
            , R.drawable.ic_menu_black_24dp
    };

    public ProductTypeAdapter(String[] titles, Activity activity) {
        this.titles = titles;
        this.activity = activity;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CatoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cato_item, parent, false);


        return new CatoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CatoViewHolder holder, int position) {
        final String title = titles[position];
        final int drawable = drawables[position];

        holder.catoTitle.setText(title);
        holder.catoImage.setBackgroundDrawable(activity.getDrawable(drawable));

        holder.mainView.setOnClickListener(view -> {
            Intent i = new Intent(activity, OtherActionsActivity.class);
            if (position != 4) {
                i.putExtra(
                        CURRENT_PAGE_ID
                        , (AllProductsFragment) AllProductsFragment.getInstance(new IProductBuilder.ProductBuilder(title).build())
                );
            } else {
                i.putExtra(
                        CURRENT_PAGE_ID
                        , (AllCategoriesFragment) AllCategoriesFragment.newInstance()
                );
            }
            activity.startActivity(i);
        });
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }

    class CatoViewHolder extends RecyclerView.ViewHolder {

        TextView catoTitle;
        AppCompatImageView catoImage;
        private ConstraintLayout mainView;

        CatoViewHolder(@NonNull View itemView) {
            super(itemView);

            mainView = itemView.findViewById(R.id.main_layout);
            catoTitle = itemView.findViewById(R.id.product_type_title);
            catoImage = itemView.findViewById(R.id.product_type_image);
        }
    }
}
