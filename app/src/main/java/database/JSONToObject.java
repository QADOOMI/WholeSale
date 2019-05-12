package database;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import product.structure.AutomotiveParts;
import product.structure.Baby;
import product.structure.Clothing;
import product.structure.Food;
import product.structure.IProductBuilder;
import product.structure.PriceRange;
import product.structure.Product;
import product.structure.Review;
import recyclerview.MessagesAdapter;
import user.structure.Buyer;
import user.structure.Seller;
import user.structure.Session;
import user.structure.User;

public final class JSONToObject {

    private final static String TAG = JSONToObject.class.getSimpleName();

    public static Seller convertToSeller(JSONObject sellerData) throws JSONException {
        Seller.SellerAddiInfo sellerAddiInfo = new Seller.SellerAddiInfo(
                sellerData.getString("brand_name")
                , sellerData.getString("street#")
                , sellerData.getString("building#")
                , sellerData.getString("PO_box")
                , sellerData.getString("apartment#")
                , sellerData.getString("town")
                , sellerData.getString("governance")
                , sellerData.getInt("zip_code")
                , sellerData.getString("floor#")
                , sellerData.getString("credit_info"));

        return new Seller(sellerAddiInfo
                , sellerData.getString("seller_id")
                , sellerData.getString("first_name")
                , sellerData.getString("last_name")
                , sellerData.getString("seller_email")
                , sellerData.getString("phone_number")
                , sellerData.getString("password")
                , sellerData.getString("profile_image"));

    }

    public static Buyer convertToBuyer(String buyerData) throws JSONException {
        Log.d(TAG, "user data: " + buyerData + " signedIn: " + Session.isUserSignedIn());
        if (Session.isUserSignedIn()) {
            String[] splittedData = buyerData.split(" ");
            if (splittedData.length > 1) {
                JSONObject data = new JSONObject(splittedData[0].trim());
                JSONObject image = new JSONObject(splittedData[1].trim());
                return new Buyer(data.getString("buyer_id")
                        , data.getString("first_name")
                        , data.getString("last_name")
                        , data.getString("buyer_email")
                        , data.getString("phone_number")
                        , data.getString("password")
                        , image.getString("profile_image"));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static ArrayList<Product> convertToProduct(JSONObject productData, String type, boolean isWished) throws JSONException {
        ArrayList<Product> products = new ArrayList<>();
        JSONArray productsData = productData.getJSONArray("productsData");
        JSONArray productsImgs = productData.getJSONArray("productsImgs");
        if (productData.getBoolean("isFetched")) {
            if (!isWished) {
                JSONArray wishListed = productData.getJSONArray("wishListed");
                for (int i = 0; i < productsData.length(); i++) {
                    if (type.equals(Food.class.getSimpleName())) {
                        products.add(buildProduct(productsData, productsImgs, wishListed, type, "food_id", i));
                    } else if (type.equals(AutomotiveParts.class.getSimpleName())) {
                        products.add(buildProduct(productsData, productsImgs, wishListed, type, "auto_id", i));
                    } else if (type.equals(Clothing.class.getSimpleName())) {
                        products.add(buildProduct(productsData, productsImgs, wishListed, type, "clothing_id", i));
                    } else if (type.equals(Baby.class.getSimpleName())) {
                        products.add(buildProduct(productsData, productsImgs, wishListed, type, "baby_id", i));
                    }
                }
            } else {
                for (int i = 0; i < productsData.length(); i++) {
                    Product product = null;
                    if (!productsData.getJSONObject(i).isNull("foodId")) {
                        product = buildWishedProduct(productsData, Food.class.getSimpleName(), "foodId", i, productsImgs);
                        if (product != null)
                            products.add(product);
                    }
                    if (!productsData.getJSONObject(i).isNull("autoId")) {
                        product = buildWishedProduct(productsData, AutomotiveParts.class.getSimpleName(), "autoId", i, productsImgs);
                        if (product != null)
                            products.add(product);
                    }
                    if (!productsData.getJSONObject(i).isNull("clothingId")) {
                        product = buildWishedProduct(productsData, Clothing.class.getSimpleName(), "clothingId", i, productsImgs);
                        if (product != null)
                            products.add(product);
                    }
                    if (!productsData.getJSONObject(i).isNull("babyId")) {
                        product = buildWishedProduct(productsData, Baby.class.getSimpleName(), "babyId", i, productsImgs);
                        if (product != null)
                            products.add(product);
                    }
                }
            }
        } else {
            Log.e(TAG, "NO Data Fetched " + productData.getJSONObject("err"));
        }
        return products;
    }

    //build a product for all products list(product by type)
    private static Product buildProduct(JSONArray productsData, JSONArray productsImgs, JSONArray wishListed
            , String type, String productId, int index) throws JSONException {
        return new IProductBuilder.ProductBuilder(type)
                .setProductName(productsData.getJSONObject(index).getString("product_name"))
                .setPics(new Uri[]{searchForImages(productsData.getJSONObject(index).getString(productId), productsImgs)})
                .setPrice(productsData.getJSONObject(index).getDouble("price"))
                .setProductId(productsData.getJSONObject(index).getString(productId))
                .setToFav(searchForWished(
                        productsData.getJSONObject(index).getString(productId)
                        , wishListed))
                .setBrand(productsData.getJSONObject(index).getString("brand_name"))
                .build();
    }

    //build a product for wish list of products
    private static Product buildWishedProduct(JSONArray productsData, String type, String productId, int index, JSONArray productImages)
            throws JSONException {
        IProductBuilder.ProductBuilder builder = new IProductBuilder.ProductBuilder(type);

        // specify the id name for each product

        String nameKey = null;
        String priceKey = null;
        String brandKey = null;
        if (Food.class.getSimpleName().equals(type)) {
            nameKey = "foodName";
            priceKey = "foodPrice";
            brandKey = "foodBrand";
        } else if (Clothing.class.getSimpleName().equals(type)) {
            nameKey = "clothingName";
            priceKey = "clothingPrice";
            brandKey = "clothingBrand";
        } else if (Baby.class.getSimpleName().equals(type)) {
            nameKey = "babyName";
            priceKey = "babyPrice";
            brandKey = "babyBrand";
        } else if (AutomotiveParts.class.getSimpleName().equals(type)) {
            nameKey = "autoName";
            priceKey = "autoPrice";
            brandKey = "autoBrand";
        }

        builder = builder.setProductName(productsData.getJSONObject(index).getString(nameKey))
                .setPrice(productsData.getJSONObject(index).getDouble(priceKey))
                .setProductId(productsData.getJSONObject(index).getString(productId))
                .setToFav(true)
                .setBrand(productsData.getJSONObject(index).getString(brandKey));


        Uri[] imgs = new Uri[productImages.length()];
        for (int i = 0; i < productImages.length(); i++) {
            if (productsData.getJSONObject(index).getString(productId)
                    .equals(productImages.getJSONObject(i).getString("prod_id")))
                imgs[i] = Uri.parse(productImages.getJSONObject(i).getString("prod_image"));
        }
        builder = builder.setPics(imgs);

        return builder.build();
    }


    // searches for products that the current buyer wished for
    private static boolean searchForWished(String prodId, JSONArray wishedPords) throws JSONException {
        Log.d(TAG, "searchForWished: " + wishedPords.isNull(0));

        if (wishedPords.length() == 0) {
            return false;
        }
        ArrayList<String> ids = new ArrayList<>();
        for (int i = 0; i < wishedPords.length(); i++) {
            if (prodId.equals(wishedPords.getJSONObject(i).getString("product_id")))
                return true;
        }

        return false;
    }

    // combine the product with the right image
    private static Uri searchForImages(String prodId, JSONArray images) throws JSONException {
        ArrayList<String> prodsIds = new ArrayList<>();
        for (int i = 0; i < images.length(); i++) {
            prodsIds.add(images.getJSONObject(i).getString("prod_id"));

            Set<String> values = new HashSet<>(prodsIds);
            if (values.contains(prodId)) {
                return Uri.parse(images.getJSONObject(i).getString("prod_image"));
            }
        }

        return null;

    }

    public static ArrayList<Product> convertToMainProduct(JSONObject productData) throws JSONException {
        ArrayList<Product> products = new ArrayList<>();
        JSONArray productsData = productData.getJSONArray("productsData");
        JSONArray productsImgs = productData.getJSONArray("productsImgs");
        String type = productData.getString("type");

        if (productData.getBoolean("isFetched")) {
            for (int i = 0; i < productData.length(); i++) {
                String idName = null;
                if (type.equals(Food.class.getSimpleName())) {
                    idName = "food_id";
                } else if (type.equals(AutomotiveParts.class.getSimpleName())) {
                    idName = "auto_id";
                } else if (type.equals(Clothing.class.getSimpleName())) {
                    idName = "clothing_id";
                } else if (type.equals(Baby.class.getSimpleName())) {
                    idName = "baby_id";
                }

                products.add(new IProductBuilder.ProductBuilder(type)
                        .setProductName(productsData.getJSONObject(i).getString("product_name"))
                        .setPics(new Uri[]{searchForImages(productsData.getJSONObject(i).getString(idName), productsImgs)})
                        .setBrand(productsData.getJSONObject(i).getString("brand_name"))
                        .setProductId(productsData.getJSONObject(i).getString(idName))
                        .build());
            }
        } else {
            Log.e(TAG, "NO data fetched check the log");
        }
        return products;
    }

    public static Product convertToProductDetails(JSONObject details) throws JSONException {
        JSONArray productData = details.getJSONArray("productData");
        JSONArray priceRange = details.getJSONArray("priceRange");
        JSONArray reviewsData = details.getJSONArray("reviewData");
        JSONArray productImages = details.getJSONArray("images");

        ArrayList<PriceRange> priceRanges = new ArrayList<>(priceRange.length());
        ArrayList<Uri> pics = new ArrayList<>(productData.length());

        IProductBuilder.ProductBuilder productBuilder = new IProductBuilder.ProductBuilder(details.getString("productType"))
                .setProductId(productData.getJSONObject(0).getString("productID"))
                .setBrand(productData.getJSONObject(0).getString("brand_name"))
                .setProductName(productData.getJSONObject(0).getString("product_name"))
                .setDescription(productData.getJSONObject(0).getString("description"))
                .setPrice(productData.getJSONObject(0).getInt("price"));

        if (Session.getUser(Buyer.class) != null) {
            productBuilder = productBuilder.setProductBy(productData.getJSONObject(0).getString("seller_id"));
        }

        if (!productData.getJSONObject(0).isNull("wishedID")) {
            productBuilder = productBuilder.setToFav(productData.getJSONObject(0).getString("productID")
                    .equals(productData.getJSONObject(0).getString("wishedID")));

        }


        if (!reviewsData.getJSONObject(0).isNull("starsTotal")
                && !reviewsData.getJSONObject(0).isNull("starsSum")) {
            Review[] reviews = new Review[reviewsData.getJSONObject(0).getInt("starsTotal")];
            reviews[0] = new Review(Integer.parseInt(reviewsData.getJSONObject(0).getString("starsSum")));
            productBuilder = productBuilder.setReviews(reviews);
        }

        if (productImages != null) {
            for (int i = 0; i < productImages.length(); i++) {
                pics.add(Uri.parse(productImages.getJSONObject(i).getString("prod_image")));
            }
        } else {
            Log.e(TAG, "No images for this product");
        }

        for (int i = 0; i < priceRange.length(); i++) {
            if (!priceRange.getJSONObject(i).isNull("range_price")) {
                priceRanges.add(
                        new PriceRange(
                                priceRange.getJSONObject(i).getInt("min_units")
                                , priceRange.getJSONObject(i).getInt("max_units")
                                , priceRange.getJSONObject(i).getInt("range_price"))
                );
            }
        }

        productBuilder = productBuilder.setPics(pics.toArray(new Uri[pics.size()]))
                .setPriceRanges(priceRanges.toArray(new PriceRange[priceRanges.size()]));

        return productBuilder.build();
    }

    public static ArrayList<Review> convertToReviews(JSONArray reviewData) throws JSONException {
        ArrayList<Review> reviews = new ArrayList<>();
        for (int i = 0; i < reviewData.length(); i++) {
            JSONObject reviewIndex = reviewData.getJSONObject(i);
            reviews.add(new Review(reviewIndex.getInt("stars#")
                    , reviewIndex.getString("review_text")
                    , reviewIndex.getString("first_name").concat(" ").concat(reviewIndex.getString("last_name"))));
        }
        return reviews;
    }

    public static ArrayList<Product> convertToSearched(JSONObject searchedData, String accessType)
            throws JSONException, NullPointerException {
        ArrayList<Product> products = new ArrayList<>();
        if (searchedData.getBoolean(accessType)) {
            JSONArray data = searchedData.getJSONArray("productsData");
            JSONArray searchedImages = searchedData.getJSONArray("imagesData");
            for (int i = 0; i < data.length(); i++) {
                if (!data.getJSONObject(i).isNull("foodId")) {
                    products.add(new IProductBuilder.ProductBuilder(Food.class.getSimpleName())
                            .setProductName(data.getJSONObject(i).getString("foodName"))
                            .setBrand(data.getJSONObject(i).getString("foodBrand"))
                            .setProductId(data.getJSONObject(i).getString("foodId"))
                            .setDescription(!data.getJSONObject(i).isNull("foodDesc")
                                    ? data.getJSONObject(i).getString("foodDesc")
                                    : null)
                            .setPics(new Uri[]{searchForImages(data.getJSONObject(i).getString("foodId"), searchedImages)})
                            .build());
                }
                if (!data.getJSONObject(i).isNull("autoId")) {
                    products.add(new IProductBuilder.ProductBuilder(AutomotiveParts.class.getSimpleName())
                            .setProductName(data.getJSONObject(i).getString("autoName"))
                            .setBrand(data.getJSONObject(i).getString("autoBrand"))
                            .setProductId(data.getJSONObject(i).getString("autoId"))
                            .setDescription(!data.getJSONObject(i).isNull("autoDesc")
                                    ? data.getJSONObject(i).getString("autoDesc")
                                    : null)
                            .setPics(new Uri[]{searchForImages(data.getJSONObject(i).getString("autoId"), searchedImages)})
                            .build());
                }
                if (!data.getJSONObject(i).isNull("clothingId")) {
                    products.add(new IProductBuilder.ProductBuilder(Clothing.class.getSimpleName())
                            .setProductName(data.getJSONObject(i).getString("clothingName"))
                            .setBrand(data.getJSONObject(i).getString("clothingBrand"))
                            .setProductId(data.getJSONObject(i).getString("clothingId"))
                            .setDescription(!data.getJSONObject(i).isNull("clothingDesc")
                                    ? data.getJSONObject(i).getString("clothingDesc")
                                    : null)
                            .setPics(new Uri[]{searchForImages(data.getJSONObject(i).getString("clothingId"), searchedImages)})
                            .build());
                }
                if (!data.getJSONObject(i).isNull("babyId")) {
                    products.add(new IProductBuilder.ProductBuilder(Baby.class.getSimpleName())
                            .setProductName(data.getJSONObject(i).getString("babyName"))
                            .setBrand(data.getJSONObject(i).getString("babyBrand"))
                            .setProductId(data.getJSONObject(i).getString("babyId"))
                            .setDescription(!data.getJSONObject(i).isNull("babyDesc")
                                    ? data.getJSONObject(i).getString("babyDesc")
                                    : null)
                            .setPics(new Uri[]{searchForImages(data.getJSONObject(i).getString("babyId"), searchedImages)})
                            .build());
                }
            }
            return products;
        } else {
            return null;
        }
    }

    public static JSONObject messagesToJSON(String senderId, String receiverId, String message) throws JSONException {
        JSONObject messageJson = new JSONObject();

        messageJson.put("senderId", senderId);
        messageJson.put("receiverId", receiverId);
        messageJson.put("message", message);

        return messageJson;
    }

    public static ArrayList<MessagesAdapter.Message> convertToMessages(JSONArray messages) throws JSONException {
        ArrayList<MessagesAdapter.Message> newMessages = new ArrayList<>(messages.length());
        for (int i = 0; i < messages.length(); i++) {
            newMessages.add(new MessagesAdapter.Message(
                    messages.getJSONObject(i).getString("message_text")
                    , messages.getJSONObject(i).getString("sender_id")
                    , messages.getJSONObject(i).getString("receiver_id")));
        }

        return newMessages;
    }

    public static ArrayList<User> convertToContacts(JSONArray contacts, Class<?> userType) throws JSONException {
        ArrayList<User> users = new ArrayList<>(contacts.length());

        for (int i = 0; i < contacts.length(); i++) {
            if (userType == Seller.class) {
                users.add(new Buyer(contacts.getJSONObject(i).getString("buyer_id")
                        , contacts.getJSONObject(i).getString("first_name")
                        , contacts.getJSONObject(i).getString("last_name")
                        , contacts.getJSONObject(i).getString("buyer_email")
                        , Uri.parse(contacts.getJSONObject(i).getString("profile_image"))));
            } else if (userType == Buyer.class) {
                users.add(new Seller(contacts.getJSONObject(i).getString("seller_id")
                        , contacts.getJSONObject(i).getString("first_name")
                        , contacts.getJSONObject(i).getString("last_name")
                        , contacts.getJSONObject(i).getString("seller_email")
                        , Uri.parse(contacts.getJSONObject(i).getString("profile_image"))));
            }

        }

        return users;
    }
}
