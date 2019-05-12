package product.structure;

import android.app.Activity;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import wholesale.callback.CallBack;
import wholesale.callback.DataCallBack;

public abstract class Product implements Serializable {

    protected String id;
    protected String brandName;
    protected String prodBy;
    protected String description;
    protected Uri pics[];
    protected double price;
    protected PriceRange[] priceRanges;
    protected String productName;
    protected long timePublished;
    protected String[] colors;
    protected boolean isAddedToFav;
    protected Review[] review;

    Product() {
    }


    public String getProdBy() {
        return prodBy;
    }

    public void setProdBy(String prodBy) {
        this.prodBy = prodBy;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Review[] getReview() {
        return review;
    }

    public void setReview(Review[] review) {
        this.review = review;
    }

    public String getDescription() {
        return description;
    }

    public Uri[] getPics() {
        return pics;
    }

    public String getProductName() {
        return productName;
    }

    public long getTimePublished() {
        return timePublished;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPics(Uri[] pics) {
        this.pics = pics;
    }

    public PriceRange[] getPriceRanges() {
        return priceRanges;
    }

    public void setPriceRanges(PriceRange[] priceRanges) {
        this.priceRanges = priceRanges;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setTimePublished(long timePublished) {
        this.timePublished = timePublished;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandName() {
        return brandName;
    }

    public boolean isFav() {
        return isAddedToFav;
    }

    public void setToFav(boolean addedToFav) {
        isAddedToFav = addedToFav;
    }

    public abstract String[] getColors();

    public abstract void setColors(String[] colors);

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public abstract String getType();

    public JSONArray picsToJSONArray(Uri[] pics) throws JSONException {
        JSONArray jsonPics = new JSONArray();
        int index = 0;
        for (Uri pic : pics) {
            JSONObject jsonPic = new JSONObject();
            jsonPic.put("prod_image", pic.toString());
            jsonPics.put(index, jsonPic);
            index++;
        }
        return jsonPics;
    }

    public abstract void getAllProdData(Activity activity, DataCallBack dataCallBack, String buyerId)
            throws ExecutionException, InterruptedException;

    public abstract void publishProduct(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException;

    public abstract void getMainPageProducts(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException;

    public abstract void wishListIt(Activity activity, CallBack callBack, boolean wishIt)
            throws ExecutionException, InterruptedException, JSONException;

    public abstract void fetchProductDetails(Activity activity, CallBack callBack, String buyerId)
            throws ExecutionException, InterruptedException, JSONException;

    public abstract void fetchReviews(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException;

    public abstract void updateData(Activity activity
            , CallBack callBack
            , Product oldProduct
            , int sellerAction) throws ExecutionException, InterruptedException;

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", brandName='" + brandName + '\'' +
                ", prodBy='" + prodBy + '\'' +
                ", description='" + description + '\'' +
                ", pics=" + Arrays.toString(pics) +
                ", price=" + price +
                ", priceRanges=" + Arrays.toString(priceRanges) +
                ", productName='" + productName + '\'' +
                ", timePublished=" + timePublished +
                ", colors=" + Arrays.toString(colors) +
                ", isAddedToFav=" + isAddedToFav +
                ", review=" + Arrays.toString(review) +
                '}';
    }
}
