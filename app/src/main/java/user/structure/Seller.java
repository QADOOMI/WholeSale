package user.structure;

import android.app.Activity;
import android.net.Uri;

import java.util.concurrent.ExecutionException;

import database.ActionExec;
import database.Authenticate;
import database.DatabaseActionExec;
import database.FetchSellerProducts;
import database.SellerAction;
import database.UpdateUserData;
import wholesale.callback.CallBack;

public class Seller extends User {

    private SellerAddiInfo sellerInfo;

    public Seller() {
    }

    public Seller(String email, String password) {
        super(email, password);
    }

    public Seller(String userId,String firstName, String lastName, String email, Uri profileImage) {
        super(userId, firstName, lastName, email,profileImage);
    }

    public Seller(String userId, String firstName, String lastName, String email, String phoneNumber, String password, String profileImage) {
        super(userId, firstName, lastName, email, phoneNumber, password, profileImage);
    }

    public Seller(SellerAddiInfo sellerInfo, String firstName, String lastName, String email, String phoneNumber, String password) {
        super(firstName, lastName, email, phoneNumber, password);
        this.sellerInfo = sellerInfo;
    }

    public Seller(SellerAddiInfo sellerInfo, String userId, String firstName, String lastName, String email, String phoneNumber, String password, String profileImage) {
        super(userId, firstName, lastName, email, phoneNumber, password, profileImage);
        this.sellerInfo = sellerInfo;
    }

    public SellerAddiInfo getSellerInfo() {
        return sellerInfo;
    }

    public void setSellerInfo(SellerAddiInfo sellerInfo) {
        this.sellerInfo = sellerInfo;
    }

    @Override
    public void signIn(final Activity activity, CallBack callBack) throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new Authenticate(activity, this, Authenticate.SIGN_IN_REQ));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void signUp(final Activity activity, CallBack callBack) throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new Authenticate(activity, this, Authenticate.SIGN_UP_REQ));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void updateUserData(Activity activity, CallBack callBack, int updateType)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new UpdateUserData(activity, this, updateType));
        actionExec.beginExecution(callBack);
    }

    @Override
    public void deleteAccount(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new Authenticate(activity, this, Authenticate.DEL_ACC_REQ));
        actionExec.beginExecution(callBack);
    }

    public void fetchPublishedPorducts(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new FetchSellerProducts(activity, this, FetchSellerProducts.FETCH_PUBLISHED));
        actionExec.beginExecution(callBack);
    }

    public void fetchProdsCount(Activity activity, CallBack callBack)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec = new ActionExec(new SellerAction(activity, this, SellerAction.PUBLISHED_PRODS));
        actionExec.beginExecution(callBack);
    }


    @Override
    public Class<?> bringType() {
        return this.getClass();
    }

    public static class SellerAddiInfo {
        private String comapnyName;
        private String streetNum;
        private String buildingNum;
        private String poBox;
        private String apartmentNum;
        private String town;
        private String governance;
        private Integer zipCode;
        private String floorNum;
        private String creditInfo;

        public SellerAddiInfo(String comapnyName, String streetNum, String buildingNum, String poBox, String apartmentNum, String town, String governance, Integer zipCode, String floorNum, String creditInfo) {
            this.streetNum = streetNum;
            this.buildingNum = buildingNum;
            this.poBox = poBox;
            this.apartmentNum = apartmentNum;
            this.town = town;
            this.governance = governance;
            this.zipCode = zipCode;
            this.floorNum = floorNum;
            this.creditInfo = creditInfo;
            this.comapnyName = comapnyName;
        }

        public String getComapnyName() {
            return comapnyName;
        }

        public void setComapnyName(String comapnyName) {
            this.comapnyName = comapnyName;
        }

        public String getStreetNum() {
            return streetNum;
        }

        public void setStreetNum(String streetNum) {
            this.streetNum = streetNum;
        }

        public String getBuildingNum() {
            return buildingNum;
        }

        public void setBuildingNum(String buildingNum) {
            this.buildingNum = buildingNum;
        }

        public String getPoBox() {
            return poBox;
        }

        public void setPoBox(String poBox) {
            this.poBox = poBox;
        }

        public String getApartmentNum() {
            return apartmentNum;
        }

        public void setApartmentNum(String apartmentNum) {
            this.apartmentNum = apartmentNum;
        }

        public String getTown() {
            return town;
        }

        public void setTown(String town) {
            this.town = town;
        }

        public String getGovernance() {
            return governance;
        }

        public void setGovernance(String governance) {
            this.governance = governance;
        }

        public Integer getZipCode() {
            return zipCode;
        }

        public void setZipCode(Integer zipCode) {
            this.zipCode = zipCode;
        }

        public String getFloorNum() {
            return floorNum;
        }

        public void setFloorNum(String floorNum) {
            this.floorNum = floorNum;
        }

        public String getCreditInfo() {
            return creditInfo;
        }

        public void setCreditInfo(String creditInfo) {
            this.creditInfo = creditInfo;
        }

        @Override
        public String toString() {
            return "SellerAddiInfo{" +
                    "comapnyName='" + comapnyName + '\'' +
                    ", streetNum='" + streetNum + '\'' +
                    ", buildingNum='" + buildingNum + '\'' +
                    ", poBox='" + poBox + '\'' +
                    ", apartmentNum='" + apartmentNum + '\'' +
                    ", town='" + town + '\'' +
                    ", governance='" + governance + '\'' +
                    ", zipCode='" + zipCode + '\'' +
                    ", floorNum='" + floorNum + '\'' +
                    ", creditInfo='" + creditInfo + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Seller{" +
                "sellerInfo=" + sellerInfo.toString() +
                "\n" + super.toString() +
                '}';
    }
}
