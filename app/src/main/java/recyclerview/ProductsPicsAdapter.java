package recyclerview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.mostafa.e_commerce.R;

import java.util.ArrayList;

public class ProductsPicsAdapter extends RecyclerView.Adapter<ProductsPicsAdapter.PicViewHolder> {

    private ArrayList<Bitmap> pics;
    private ArrayList<Uri> uriPics;
    private Context context;
    private View.OnClickListener clickListener;

    public ProductsPicsAdapter(ArrayList<Bitmap> pics, Context context, View.OnClickListener clickListener, ArrayList<Uri> uriPics) {
        this.pics = pics;
        this.context = context;
        this.uriPics = uriPics;
        this.clickListener = clickListener;
    }

    public void clearPics() {
        pics.clear();
        notifyDataSetChanged();
    }

    public void addPics(ArrayList<Bitmap> pics) {
        this.pics = pics;
        notifyDataSetChanged();
    }

    public void addUriPics(ArrayList<Uri> uriPics) {
        this.uriPics = uriPics;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.prod_pic_list_item, parent, false);

        return new PicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PicViewHolder holder, int position) {
        if (pics != null) {
            Bitmap pic = pics.get(position);
            holder.prodImage.setOnClickListener(clickListener);

            if (position == 0) {
                if (!convertDrawToBitmap(context.getResources()
                        .getDrawable(R.drawable.ic_add_white_24dp, null))
                        .sameAs(pic)) {
                    holder.setProdImage(pic);
                }
            } else {
                holder.setProdImage(pic);
            }
        } else {
            Uri pic = uriPics.get(position);
            holder.prodImage.setOnClickListener(clickListener);

            Glide.with(holder.prodImage.getContext())
                    .asBitmap()
                    .load(pic)
                    .into(holder.prodImage);
        }

    }

    private Bitmap convertDrawToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public int getItemCount() {
        return pics == null ? uriPics.size() : pics.size();
    }

    class PicViewHolder extends RecyclerView.ViewHolder {
        ImageView prodImage;

        PicViewHolder(@NonNull View itemView) {
            super(itemView);

            prodImage = (ImageView) itemView.findViewById(R.id.pic_selected_view);
        }

        void setProdImage(final Bitmap pic) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    prodImage.post(new Runnable() {
                        @Override
                        public void run() {
                            Glide.with(prodImage)
                                    .load(pic)
                                    .into(prodImage);
                        }
                    });
                }
            }).start();
        }
    }
}
