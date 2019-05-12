package user.structure;

import android.app.Activity;
import android.net.Uri;

import java.io.Serializable;
import java.util.concurrent.ExecutionException;

import database.ActionExec;
import database.DatabaseActionExec;
import database.MessagesFunctions;
import wholesale.callback.CallBack;

public abstract class User implements Serializable {

    private final static String TAG = User.class.getSimpleName();
    protected String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String password;
    private String profileImage;
    protected String type;

    protected User() {
    }

    protected User(String email, String password) {
        this.email = email;
        this.password = password;
    }


    protected User(String userId, String firstName, String lastName, String email, Uri profileImage) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.profileImage = profileImage.toString();
    }

    protected User(String userId, String firstName, String lastName, String email, String phoneNumber, String password, String profileImage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.profileImage = profileImage;
        this.userId = userId;
    }

    protected User(String firstName, String lastName, String email, String phoneNumber, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public abstract void signIn(final Activity activity, CallBack callBack) throws ExecutionException, InterruptedException;

    public abstract void signUp(final Activity activity, CallBack callBack) throws ExecutionException, InterruptedException;

    public abstract void updateUserData(final Activity activity, CallBack callBack, int updateType) throws ExecutionException, InterruptedException;

    public abstract void deleteAccount(final Activity activity, CallBack callBack) throws ExecutionException, InterruptedException;

    public abstract Class<?> bringType();

    public void messagesFunctions(final Activity activity, CallBack callBack, String senderId, String receiverId, final int requestCode)
            throws ExecutionException, InterruptedException {
        DatabaseActionExec actionExec =
                new ActionExec(new MessagesFunctions(activity, senderId, receiverId, requestCode));
        actionExec.beginExecution(callBack);
    }

    @Override
    public String toString() {
        return "User{" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", password='" + password + '\'' +
                ", profileImage='" + profileImage + '\'' +
                ", ID='" + userId + '\'' +
                '}';
    }
}
