package networking;


import org.json.JSONArray;
import org.json.JSONObject;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface INodeJs {

    @POST("buyerSignUp")
    @FormUrlEncoded
    Observable<ResponseBody> buyerSignUp(
            @Field("email") String email,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("password") String password,
            @Field("phoneNumber") String phoneNumber
    );

    @POST("sellerSignUp")
    @FormUrlEncoded
    Observable<ResponseBody> sellerSignUp(
            @Field("email") String email,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("comName") String companyName,
            @Field("password") String password,
            @Field("category") String category,
            @Field("phoneNumber") String phoneNumber,
            @Field("poBox") String poBox,
            @Field("floorNum") String floorNum,
            @Field("apartmentNum") String apartmentNum,
            @Field("buildingNum") String buildingNum,
            @Field("streetNum") String streetNum,
            @Field("creditInfo") String creditInfo,
            @Field("zipCode") Integer zipCode,
            @Field("town") String town,
            @Field("governance") String governance
    );

    @POST("signIn")
    @FormUrlEncoded
    Observable<ResponseBody> userSignIn(
            @Field("userEmail") String email,
            @Field("password") String password,
            @Field("type") String type
    );

    @POST("deleteAcc")
    @FormUrlEncoded
    Observable<ResponseBody> deleteAccount(
            @Field("userId") String userId
            , @Field("userType") String userType);

    @POST("fetchMainPageData")
    @FormUrlEncoded
    Observable<ResponseBody> fetchMainPageData(@Field("type") String productType);

    @POST("fetchProductsByType")
    @FormUrlEncoded
    Observable<ResponseBody> fetchProductsByType(
            @Field("buyerId") String buyerId,
            @Field("type") String productType
    );

    @POST("wishOrUnwishProduct")
    @FormUrlEncoded
    Observable<ResponseBody> wishOrUnwish(
            @Field("wishIt") boolean wishIt,
            @Field("buyerId") String buyerId,
            @Field("productId") String productId
    );

    @POST("modifyUserData")
    @FormUrlEncoded
    Observable<ResponseBody> updateUserEmail(
            @Field("updateType") String updateType,
            @Field("userType") String userType,
            @Field("email") String email,
            @Field("buyerId") String buyerId
    );

    @POST("modifyUserData")
    @FormUrlEncoded
    Observable<ResponseBody> updateUserName(
            @Field("updateType") String updateType,
            @Field("userType") String userType,
            @Field("buyerId") String buyerId,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName
    );

    @POST("modifyUserData")
    @FormUrlEncoded
    Observable<ResponseBody> updateUserPassword(
            @Field("updateType") String updateType,
            @Field("userType") String userType,
            @Field("buyerId") String buyerId,
            @Field("password") String password
    );

    @POST("modifyUserData")
    @FormUrlEncoded
    Observable<ResponseBody> updateUserImage(
            @Field("updateType") String updateType,
            @Field("userType") String userType,
            @Field("buyerId") String buyerId,
            @Field("profileImage") String profileImage
    );


    @POST("modifyUserData")
    @FormUrlEncoded
    Observable<ResponseBody> updateUserPhoneNumber(
            @Field("updateType") String updateType,
            @Field("userType") String userType,
            @Field("buyerId") String buyerId,
            @Field("phoneNumber") String phoneNumber
    );

    @PUT("deleteWishedProducts")
    @FormUrlEncoded
    Observable<ResponseBody> deleteWishedProducts(
            @Field("buyerId") String buyerId
    );

    @POST("fetchAllWished")
    @FormUrlEncoded
    Observable<ResponseBody> fetchAllWished(@Field("buyerId") String buyerId);

    @POST("fetchAProduct")
    @FormUrlEncoded
    Observable<ResponseBody> fetchAProduct(@Field("productId") String productId, @Field("buyerId") String buyerId, @Field("type") String type);

    @POST("productReviews")
    @FormUrlEncoded
    Observable<ResponseBody> addReview(
            @Field("productId") String productId
            , @Field("buyerId") String buyerId
            , @Field("reviewText") String reviewText
            , @Field("starsNum") String starsNum
            , @Field("fetchReviews") Boolean fetchReviews);

    @POST("productReviews")
    @FormUrlEncoded
    Observable<ResponseBody> fetchReview(
            @Field("productId") String productId
            , @Field("fetchReviews") Boolean fetchReviews);

    @POST("fetchSellerProducts")
    @FormUrlEncoded
    Observable<ResponseBody> fetchSellerProducts(
            @Field("sellerId") String sellerId
            , @Field("fetchType") String fetchType
    );

    @POST("search")
    @FormUrlEncoded
    Observable<ResponseBody> search(@Field("keyValue") String keyValue);

    @POST("sellerActions")
    @FormUrlEncoded
    Observable<ResponseBody> updateProduct(
             @Field("minOrder") Integer minOrder
            , @Field("minPrice") Double minPrice
            , @Field("name") String productName
            , @Field("description") String description
            , @Field("pics") JSONArray pics
            , @Field("priceRanges") JSONArray priceRanges
            , @Field("productId") String productId
            , @Field("type") String type
            , @Field("updateType") String updateType
            , @Field("isDelete") boolean isDelete);

    @POST("sellerActions")
    @FormUrlEncoded
    Observable<ResponseBody> deleteProduct(
            @Field("productId") String productId
            , @Field("type") String type
            , @Field("isDelete") boolean isDelete);

    @POST("fetchSellerProducts")
    @FormUrlEncoded
    Observable<ResponseBody> fetchProductRanges(
            @Field("productId") String productId
            , @Field("fetchType") String fetchType
    );

    @POST("addProduct")
    @FormUrlEncoded
    Observable<ResponseBody> addProduct(
            @Field("name") String productName
            , @Field("description") String description
            , @Field("type") String type
            , @Field("pics") JSONArray pics
            , @Field("priceRanges") JSONArray priceRanges
            , @Field("brand") String brandName
            , @Field("minOrder") Integer minOrder
            , @Field("minPrice") Double minPrice
            , @Field("sellerId") String sellerId);

    @POST("modifyUserData")
    @FormUrlEncoded
    Observable<ResponseBody> updateSeller(
            @Field("userType") String userType,
            @Field("email") String email,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("comName") String companyName,
            @Field("password") String password,
            @Field("phoneNumber") String phoneNumber,
            @Field("poBox") String poBox,
            @Field("floorNum") String floorNum,
            @Field("apartmentNum") String apartmentNum,
            @Field("buildingNum") String buildingNum,
            @Field("streetNum") String streetNum,
            @Field("creditInfo") String creditInfo,
            @Field("governance") String governance,
            @Field("zipCode") Integer zipCode,
            @Field("town") String town,
            @Field("sellerId") String sellerId,
            @Field("profileImage") String profileImg);

    @POST("prodsCount")
    @FormUrlEncoded
    Observable<ResponseBody> fetchPublishedCount(@Field("sellerId") String sellerId);

    @POST("fetchContacts")
    @FormUrlEncoded
    Observable<ResponseBody> fetchContacts(@Field("usersId") JSONObject usersId);

    @POST("fetchMessages")
    @FormUrlEncoded
    Observable<ResponseBody> fetchMessages(@Field("usersId") JSONObject usersId);


}
