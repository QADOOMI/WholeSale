package recyclerview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostafa.e_commerce.R;

import java.util.List;

import product.structure.Product;

public final class ProfileProdAdapter extends RecyclerView.Adapter<ProfileProdAdapter.ProfileProdViewHolder> {

    private List<Product> productList;
    private Context context;

    public ProfileProdAdapter(List<Product> productList, Context context) {
        this.productList = productList;
        this.context = context;
    }

    @NonNull
    @Override
    public ProfileProdViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.profile_prod_list, parent, false);


        return new ProfileProdViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileProdViewHolder holder, int position) {
        Product product = productList.get(position);


    }

    @Override
    public int getItemCount() {
        return 0;
    }

    class ProfileProdViewHolder extends RecyclerView.ViewHolder {



        public ProfileProdViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
