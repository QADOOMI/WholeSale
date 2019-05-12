package fragments.java;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.R;

import java.io.Serializable;

import product.structure.IProductBuilder;

public final class AllCategoriesFragment extends Fragment
        implements View.OnClickListener, Serializable {

    private transient SparseArray<AppCompatImageView> categoriesImages;
    private String[] titles;
    public static final int[] productDrawables =
            {R.drawable.auto_image, R.drawable.clothing_image, R.drawable.food_image, R.drawable.baby_image};

    public AllCategoriesFragment() {
    }

    public static Fragment newInstance() {
        return new AllCategoriesFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.all_categories_fragment, container, false);
        categoriesImages = new SparseArray<>();
        titles = getResources().getStringArray(R.array.products_type_titles);
        initViews(view);

        return view;
    }

    private void initViews(View view) {

        categoriesImages.append(0, view.findViewById(R.id.product_image_auto));
        categoriesImages.append(1, view.findViewById(R.id.product_image_clothing));
        categoriesImages.append(2, view.findViewById(R.id.product_image_food));
        categoriesImages.append(3, view.findViewById(R.id.product_image_baby));
        categoriesImages.append(4, view.findViewById(R.id.product_image_home));

        categoriesImages.get(0).setOnClickListener(this);
        categoriesImages.get(1).setOnClickListener(this);
        categoriesImages.get(2).setOnClickListener(this);
        categoriesImages.get(3).setOnClickListener(this);
        categoriesImages.get(4).setOnClickListener(this);

        new Thread(() -> getActivity().runOnUiThread(() -> {
            Glide.with(getActivity())
                    .asDrawable()
                    .load(getResources().getDrawable(R.drawable.auto_image, null))
                    .into(categoriesImages.get(0));

            Glide.with(getActivity())
                    .asDrawable()
                    .load(getResources().getDrawable(R.drawable.clothing_image, null))
                    .into(categoriesImages.get(1));

            Glide.with(getActivity())
                    .asDrawable()
                    .load(getResources().getDrawable(R.drawable.food_image, null))
                    .into(categoriesImages.get(2));

            Glide.with(getActivity())
                    .asDrawable()
                    .load(getResources().getDrawable(R.drawable.baby_image, null))
                    .into(categoriesImages.get(3));

            Glide.with(getActivity())
                    .asDrawable()
                    .load(getResources().getDrawable(R.drawable.logo1, null))
                    .into(categoriesImages.get(4));
        })).start();

        (view.findViewById(R.id.go_to_auto)).setOnClickListener(this);
        (view.findViewById(R.id.go_to_clothing)).setOnClickListener(this);
        (view.findViewById(R.id.go_to_food)).setOnClickListener(this);
        (view.findViewById(R.id.go_to_baby)).setOnClickListener(this);
        (view.findViewById(R.id.go_to_home)).setOnClickListener(this);


    }

    @Override
    public void onClick(View view) {
        String productType = null;
        if (view.getId() == R.id.product_image_auto || view.getId() == R.id.go_to_auto) {
            productType = titles[0];
        } else if (view.getId() == R.id.product_image_clothing || view.getId() == R.id.go_to_clothing) {
            productType = titles[1];
        } else if (view.getId() == R.id.product_image_food || view.getId() == R.id.go_to_food) {
            productType = titles[3];
        } else if (view.getId() == R.id.product_image_baby || view.getId() == R.id.go_to_baby) {
            productType = titles[2];
        } else if (view.getId() == R.id.product_image_home || view.getId() == R.id.go_to_home) {
            getActivity().finish();
            return;
        }
        ((NavigationHost) getActivity()).navigateTo(
                AllProductsFragment.getInstance(new IProductBuilder.ProductBuilder(productType).build())
                , true
        );
    }
}
