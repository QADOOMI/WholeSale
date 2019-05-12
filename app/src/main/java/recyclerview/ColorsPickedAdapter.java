package recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mostafa.e_commerce.R;

import java.util.ArrayList;

public class ColorsPickedAdapter extends RecyclerView.Adapter<ColorsPickedAdapter.ColorsPickedViewHolder> {

    private ArrayList<String> colors;
    private Context context;

    public ColorsPickedAdapter(ArrayList<String> colors, Context context) {
        this.colors = colors;
        this.context = context;
    }

    @NonNull
    @Override
    public ColorsPickedAdapter.ColorsPickedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.picked_colors_item, parent, false);


        return new ColorsPickedAdapter.ColorsPickedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorsPickedAdapter.ColorsPickedViewHolder holder, int position) {
        String color = colors.get(position);


        holder.setColoredView(Color.parseColor(color));
    }

    @Override
    public int getItemCount() {
        return colors.size();
    }

    public ArrayList<String> getColors() {
        return colors;
    }

    public class ColorsPickedViewHolder extends RecyclerView.ViewHolder {

        View coloredView;

        public ColorsPickedViewHolder(@NonNull View itemView) {
            super(itemView);
            coloredView = itemView.findViewById(R.id.colored_item);
        }

        public void setColoredView(final int color) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    coloredView.post(new Runnable() {
                        @Override
                        public void run() {
                            coloredView.setBackgroundColor(color);
                        }
                    });
                }
            }).start();

        }

    }
}
