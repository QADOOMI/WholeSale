package user.structure;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.mostafa.e_commerce.R;

import org.json.JSONException;
import org.json.JSONObject;

import database.JSONToObject;

public final class Session {

    private static SharedPreferences pref;
    private static Session session;
    public static final String TAG = Session.class.getSimpleName();

    private Session(String userData, Context context) {
        pref = context.getSharedPreferences(User.class.getSimpleName(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(User.class.getSimpleName(), userData);
        editor.apply();
    }

    public static Session createSession(String userData, Context context) {
        if (session == null) {
            session = new Session(userData, context);
        }
        return session;
    }

    public static User getUser(Class<?> user) throws JSONException {
        // convert the the saved user data(JSONObject) to real buyer
        if (isUserSignedIn()) {
            if (user == Seller.class)
                return (Seller) JSONToObject.convertToSeller(new JSONObject(pref.getString(User.class.getSimpleName(), null)));
            else
                return (Buyer) JSONToObject.convertToBuyer(pref.getString(User.class.getSimpleName(), null)
                );
        } else {
            return null;
        }
    }

    public static boolean isUserSignedIn() {
        try {
            return pref.getString(User.class.getSimpleName(), null) != null;
        } catch (NullPointerException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
    }

    public static void signOut() {
        if (session != null) {
            session = null;
            pref.edit().clear().apply();
        }
    }

    public static void setUserData(int updateType, User user) throws JSONException {
        SharedPreferences.Editor prefEditor = pref.edit();
        if (user instanceof Buyer) {
            String[] splitted = pref.getString(User.class.getSimpleName(), "User doesn't exists").split(" ");
            JSONObject userData = new JSONObject(splitted[0]);
            JSONObject userImages = new JSONObject(splitted[1]);
            prefEditor.remove(User.class.getSimpleName());
            if (updateType == R.id.update_image_go_icon) {
                userImages.put("profile_image", user.getProfileImage());
            } else if (updateType == R.id.update_name_go_icon) {
                userData.put("first_name", user.getFirstName());
                userData.put("last_name", user.getLastName());
            } else if (updateType == R.id.update_password_go_icon) {
                userData.put("password", user.getPassword());
            } else if (updateType == R.id.update_phone_number_go_icon) {
                userData.put("phone_number", user.getPhoneNumber());
            } else if (updateType == R.id.update_email_go_icon) {
                userData.put("buyer_email", user.getEmail());
            }
            prefEditor.putString(User.class.getSimpleName(), userData.toString() + " " + userImages);
            prefEditor.apply();
        } else if (user instanceof Seller) {
            JSONObject sellerData = new JSONObject(pref.getString(User.class.getSimpleName(), "Seller doesn't exists"));

            sellerData.put("brand_name", ((Seller) user).getSellerInfo().getComapnyName());
            sellerData.put("street#", ((Seller) user).getSellerInfo().getStreetNum());
            sellerData.put("building#", ((Seller) user).getSellerInfo().getBuildingNum());
            sellerData.put("PO_box", ((Seller) user).getSellerInfo().getPoBox());
            sellerData.put("apartment#", ((Seller) user).getSellerInfo().getApartmentNum());
            sellerData.put("town", ((Seller) user).getSellerInfo().getTown());
            sellerData.put("governance", ((Seller) user).getSellerInfo().getGovernance());
            sellerData.put("zip_code", ((Seller) user).getSellerInfo().getZipCode());
            sellerData.put("floor#", ((Seller) user).getSellerInfo().getFloorNum());
            sellerData.put("credit_info", ((Seller) user).getSellerInfo().getCreditInfo());
            sellerData.put("first_name", user.getFirstName());
            sellerData.put("last_name", user.getLastName());
            sellerData.put("seller_email", user.getEmail());
            sellerData.put("phone_number", user.getPhoneNumber());
            sellerData.put("password", user.getPassword());
            sellerData.put("profile_image", user.getProfileImage());

            prefEditor.putString(User.class.getSimpleName(), sellerData.toString());
            prefEditor.apply();
        }


    }
}
