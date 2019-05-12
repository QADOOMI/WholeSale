package fragments.java;

import android.Manifest;
import android.content.ClipData;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.button.MaterialButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mostafa.e_commerce.NavigationHost;
import com.example.mostafa.e_commerce.OtherActionsActivity;
import com.example.mostafa.e_commerce.R;
import com.example.mostafa.e_commerce.SellerMainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import database.ActionExec;
import database.DatabaseActionExec;
import database.JSONToObject;
import database.SellerAction;
import dialog.SelectChoiceFragment;
import io.reactivex.disposables.CompositeDisposable;
import okhttp3.ResponseBody;
import product.structure.IProductBuilder;
import product.structure.PriceRange;
import product.structure.Product;
import recyclerview.ProductsPicsAdapter;
import wholesale.callback.DataCallBack;

import static android.app.Activity.RESULT_OK;


public class AddProductFragment extends Fragment
        implements Serializable, View.OnClickListener, DataCallBack {

    private static final String TAG = AddProductFragment.class.getSimpleName();

    private transient HashMap<String, TextInputLayout> inputsLayout;
    private transient HashMap<String, TextInputEditText> inputsFields;
    private transient Uri[] pickedPicsUri;
    private RecyclerView productsPicsList;
    private ProductsPicsAdapter picsAdapter;
    private MaterialButton addProduct;
    private transient PriceRangeAdapter rangeAdapter;
    private static Product product;
    public static final int RESULT_SUCCESS = 7754;
    private RecyclerView priceRangeList;
    private transient final View.OnClickListener addPicListener = view -> {
        if (ActivityCompat.checkSelfPermission(getActivity()
                , Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent gallaryIntent = new Intent();
            gallaryIntent.setType("image/*");
            gallaryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);

            startActivityForResult(Intent.createChooser(gallaryIntent, "Select 5 Images Only"), RESULT_SUCCESS);
        } else {
            ActivityCompat.requestPermissions(
                    getActivity()
                    , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                    , RESULT_SUCCESS
            );
        }
    };

    public AddProductFragment() {
    }

    public static AddProductFragment newInstance(Product newProduct) {
        AddProductFragment choiceFragment = new AddProductFragment();

        product = newProduct;

        return choiceFragment;
    }

    public static AddProductFragment newInstance(String data, boolean showDialog) {
        AddProductFragment choiceFragment = new AddProductFragment();

        Bundle bundle = new Bundle();
        bundle.putString("prodType", data);
        bundle.putBoolean("showDialog", showDialog);

        choiceFragment.setArguments(bundle);
        return choiceFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.add_product_fragment, container, false);

        if (getActivity() instanceof SellerMainActivity) {
            ((SellerMainActivity) getActivity()).setPageTitle(getString(R.string.add_product_page_title));
        } else if (getActivity() instanceof OtherActionsActivity) {
            ((OtherActionsActivity) getActivity()).setPageTitle(getString(R.string.add_product_page_title));
        }


        initViews(view);

        if (getArguments() != null) {
            if (getArguments().getBoolean("showDialog")) {
                SelectChoiceFragment productTypeDialog = SelectChoiceFragment.getInstance(R.string.products_type_title
                        , R.array.products_types
                        , false, getActivity());
                assert getFragmentManager() != null;
                productTypeDialog.show(getFragmentManager(), "prodTypeDialog");
            }
            if (getArguments().getSerializable("product") != null) {
                addProduct.setText(R.string.submit_data_update_btn_text);
                addProduct.setEnabled(false);
                Product product = (Product) getArguments().getSerializable("product");
                try {
                    assert product != null;
                    product.fetchProductDetails(getActivity(), new DataCallBack() {
                        @Override
                        public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                            JSONObject productDetails = null;
                            try {
                                productDetails = new JSONObject(responseBody.string());
                                Product fetchedProduct = JSONToObject.convertToProductDetails(productDetails);

                                picsAdapter.addPics(convertUrisToBitmap(fetchedProduct.getPics()));
                                ((TextInputEditText) inputsFields.get(Key.PROD_NAME)).setText(product.getProductName());
                                ((TextInputEditText) inputsFields.get(Key.PROD_DESC)).setText(product.getDescription());

                                priceRangeList.setAdapter(
                                        new PriceRangeAdapter(
                                                new ArrayList<PriceRange>(Arrays.asList(product.getPriceRanges()))));
                                addProduct.setEnabled(true);
                            } catch (JSONException | IOException e) {
                                Log.e(TAG, e.getMessage(), e);
                                addProduct.setEnabled(true);
                            }

                        }

                        @Override
                        public void onDataNotExtracted(Throwable error) {
                            Log.e(TAG, error.getMessage(), error);
                        }
                    }, null);
                } catch (ExecutionException | InterruptedException | JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                }
            }
        } else {
            // fetch product price ranges
            try {
                DatabaseActionExec actionExec = new ActionExec(new SellerAction(getActivity()
                        , product
                        , null
                        , SellerAction.FETCH_PRODUCT_RANGES));
                actionExec.beginExecution(new DataCallBack() {
                    @Override
                    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
                        try {
                            JSONObject fetchState = new JSONObject(responseBody.string());

                            if (fetchState.getBoolean("fetchState")) {
                                JSONArray ranges = fetchState.getJSONArray("ranges");
                                ArrayList<PriceRange> listRanges = new ArrayList<>(ranges.length());
                                for (int i = 0; i < ranges.length(); i++) {
                                    listRanges.add(
                                            new PriceRange(
                                                    ranges.getJSONObject(i).getInt("min_units")
                                                    , ranges.getJSONObject(i).getInt("max_units")
                                                    , Double.parseDouble(ranges.getJSONObject(i).getString("range_price")))
                                    );
                                }
                                rangeAdapter.addManyRanges(listRanges, 1);
                            } else {
                                Log.e(TAG, "no ranges fetched");
                            }
                        } catch (JSONException | IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onDataNotExtracted(Throwable error) {
                        Log.e(TAG, error.getMessage(), error);
                    }
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

            ProductsPicsAdapter uriPicsAdapter = new ProductsPicsAdapter(null
                    , getActivity()
                    , addPicListener
                    , new ArrayList<Uri>(Arrays.asList(product.getPics())));
            productsPicsList.setAdapter(uriPicsAdapter);
            addProduct.setText(R.string.submit_data_update_btn_text);
            inputsFields.get(Key.PROD_NAME).setText(product.getProductName());
            Log.d(TAG, "desc: " + product.getDescription());
            inputsFields.get(Key.PROD_DESC).setText(product.getDescription());

        }

        for (Field userInputKeys : Key.class.getFields()) {
            trackTextChange(userInputKeys.getName());
            Log.d(TAG, userInputKeys.getName());
        }

        addProduct.setOnClickListener(this);

        return view;
    }

    private void initViews(View view) {
        inputsLayout = new HashMap<>();
        inputsFields = new HashMap<>();
        addProduct = view.findViewById(R.id.add_item_btn);

        ArrayList<Bitmap> productPics = new ArrayList<>();
        productPics.add(convertDrawToBitmap(getResources()
                .getDrawable(R.drawable.ic_add_white_24dp, null)));
        productsPicsList = view.findViewById(R.id.picked_pics_list);
        productsPicsList.setHasFixedSize(true);
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        productsPicsList.setLayoutManager(manager);
        picsAdapter = new ProductsPicsAdapter(productPics, getActivity(), addPicListener, null);
        productsPicsList.setAdapter(picsAdapter);

        rangeAdapter = new PriceRangeAdapter(new ArrayList<>(4));
        priceRangeList = (RecyclerView) view.findViewById(R.id.add_prices_list);
        priceRangeList.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        priceRangeList.setHasFixedSize(true);
        priceRangeList.setAdapter(rangeAdapter);
        rangeAdapter.addToLast(new PriceRange(0, 0, 0));

        inputsLayout.put(Key.PROD_NAME, view.findViewById(R.id.product_name_layout));
        inputsLayout.put(Key.PROD_DESC, view.findViewById(R.id.description_layout));

        inputsFields.put(Key.PROD_NAME, view.findViewById(R.id.product_name_field));
        inputsFields.put(Key.PROD_DESC, view.findViewById(R.id.description_field));

    }

    @Override
    public void onClick(View view) {
        ArrayList<TextInputLayout> emptyFields = getEmptyFields();
        if (view.getId() == R.id.add_item_btn) {
            if (getArguments() == null) {
                if (emptyFields.size() == 0) {
                    try {
                        Product product1 = new IProductBuilder.ProductBuilder(product.getType())
                                .setProductName(String.valueOf((inputsFields.get(Key.PROD_NAME).getText())))
                                .setDescription(String.valueOf(inputsFields.get(Key.PROD_DESC).getText()))
                                .setPriceRanges(rangeAdapter.getRanges())
                                .setProductId(product.getId())
                                .setPics(pickedPicsUri == null ? product.getPics() : pickedPicsUri)
                                .build();
                        product1.updateData(getActivity(), this, product, SellerAction.UPDATE);
                    } catch (ExecutionException | InterruptedException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }
            } else {
                if (emptyFields.size() == 0) {
                    if (productsPicsList.getAdapter().getItemCount() != 0) {
                        try {
                            Product product = new IProductBuilder.ProductBuilder(getArguments().getString("prodType"))
                                    .setProductName(String.valueOf(inputsFields.get(Key.PROD_NAME).getText()))
                                    .setDescription(String.valueOf(inputsFields.get(Key.PROD_DESC).getText()))
                                    .setPics(pickedPicsUri)
                                    .setTimePublished(System.currentTimeMillis())
                                    .setPriceRanges(rangeAdapter.getRanges())
                                    .build();

                            product.publishProduct(getActivity(), this);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }


                    } else {
                        Snackbar.make(
                                Objects.requireNonNull(
                                        getView()).findViewById(R.id.core_layout)
                                , "Pick Pictures for your product"
                                , Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    for (Field userInputKey : Key.class.getFields()) {
                        setLayoutHelperText(inputsLayout.get(userInputKey.getName())
                                , getResources().getString(R.string.empty_fields_required_msg)
                                , R.color.errorRed);
                    }
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            ((NavigationHost) getActivity()).navigateTo(new AllProductsFragment(), false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setLayoutHelperText(final TextInputLayout layout, final String text, final int COLOR) {
        new Thread(() -> layout.post(() -> {
            layout.setError(text);
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            layout.setBoxStrokeColor(getResources().getColor(COLOR));
        })).start();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        assert getActivity() != null;
        try {
            if (requestCode == RESULT_SUCCESS)
                if (resultCode == RESULT_OK)
                    if (null != data) {
                        if (data.getClipData() != null) {
                            ClipData mClipData = data.getClipData();
                            pickedPicsUri = new Uri[mClipData.getItemCount()];
                            for (int i = 0; i < pickedPicsUri.length; i++) {
                                ClipData.Item item = mClipData.getItemAt(i);
                                Uri uri = item.getUri();
                                pickedPicsUri[i] = uri;
                            }
                            picsAdapter.addPics(convertUrisToBitmap(pickedPicsUri));
                        }
                    } else {
                        Toast.makeText(getActivity(), "You haven't picked Image",
                                Toast.LENGTH_LONG).show();
                    }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
            Toast.makeText(getActivity(), "Something went wrong", Toast.LENGTH_LONG)
                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
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

    private ArrayList<Bitmap> convertUrisToBitmap(Uri[] imagesUri)
            throws IOException {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (Uri anImagesUri : imagesUri) {
            bitmaps.add(MediaStore.Images.Media
                    .getBitmap(getActivity().getContentResolver(), anImagesUri));
        }
        return bitmaps;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RESULT_SUCCESS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Intent gallaryIntent = new Intent();
                    gallaryIntent.setType("image/*");
                    gallaryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);

                    startActivityForResult(Intent.createChooser(gallaryIntent, "Select 5 Images Only"), RESULT_SUCCESS);
                }
                return;
                // other 'case' lines to check for other
                // permissions this app might request.
            }
        }

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent gallaryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            gallaryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

            startActivityForResult(Intent.createChooser(gallaryIntent, "Select 5 Images Only"), RESULT_SUCCESS);
        } else {
            ActivityCompat.requestPermissions(getActivity()
                    , new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}
                    , RESULT_SUCCESS);
        }
    }

    private void trackTextChange(final String index) {

        inputsFields.get(index).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                switch (index) {
                    case Key.PROD_NAME:
                        if (charSequence.length() < 2) {
                            setBoxColorInThread(index, getResources().getString(R.string.valid_pro_name), R.color.errorRed);
                        }
                        break;
                    case Key.PROD_DESC:
                        if (charSequence.length() < 30) {
                            setBoxColorInThread(index, getResources().getString(R.string.valid_pro_desc), R.color.errorRed);
                        }
                        break;
                    default:
                        break;
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setBoxColorInThread(index, null, R.color.colorAccent);
            }

            private void setBoxColorInThread(final String index, final String errorMsg, final int color) {
                new Thread(() -> inputsLayout.get(index).post(() -> {
                    inputsLayout.get(index).setBoxStrokeColor(getResources().getColor(color));
                    inputsLayout.get(index).setError(errorMsg);
                })).start();
            }
        });
    }

/*    private void removeErrorText(TextInputEditText input, final TextInputLayout layout) {
        input.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                changeFieldsToDefault(layout);
            }
        });
    }*/

    private void changeFieldsToDefault(final TextInputLayout layout) {
        new Thread(() -> layout.post(() -> {
            layout.setError(null);
            layout.setBoxStrokeColor(getResources().getColor(R.color.colorAccent));
        })).start();
    }

    private ArrayList<TextInputLayout> getEmptyFields() {
        ArrayList<TextInputLayout> emptyFields = new ArrayList<>();

        for (Field userInputKeys : Key.class.getFields()) {
            if (TextUtils.isEmpty(inputsFields.get(userInputKeys.getName()).getText())
                    && inputsLayout.get(userInputKeys.getName()).getVisibility() == View.VISIBLE) {
                emptyFields.add(inputsLayout.get(userInputKeys.getName()));
            }
            Log.d(TAG, userInputKeys.getName());
        }
        return emptyFields;
    }

    @Override
    public void onDataExtracted(ResponseBody responseBody, CompositeDisposable compositeDisposable) {
        try {
            JSONObject addState = new JSONObject(responseBody.string());
            if (getArguments() != null) {
                if (addState.getBoolean("addState")) {
                    Toast.makeText(getActivity(), "Published Successfully.", Toast.LENGTH_LONG).show();
                    picsAdapter.clearPics();
                    rangeAdapter.setToDefault();
                    inputsFields.get(Key.PROD_NAME).setText(null);
                    inputsFields.get(Key.PROD_DESC).setText(null);
                    ((SellerMainActivity) getActivity()).getNavigation().getMenu().findItem(R.id.navigation_home).setChecked(true);
                    ((SellerMainActivity) getActivity()).navigateTo(new SellerHomeFragment(), false);
                } else {
                    Toast.makeText(getActivity(), "Publish Failed.", Toast.LENGTH_LONG).show();
                }
            } else {
                if (addState.getBoolean("updateState")) {
                    Toast.makeText(getActivity(), "Updated Successfully.", Toast.LENGTH_LONG).show();
                    return;
                }
                Toast.makeText(getActivity(), "No data updated try again.", Toast.LENGTH_LONG).show();
            }

        } catch (JSONException | IOException e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void onDataNotExtracted(Throwable error) {
        Log.e(TAG, error.getMessage(), error);
    }

    private interface Key {
        String PROD_NAME = "PROD_NAME";
        String PROD_DESC = "PROD_DESC";
    }

    class PriceRangeAdapter extends RecyclerView.Adapter<PriceRangeAdapter.RangeViewHolder> {

        private ArrayList<PriceRange> ranges;
        private RangeViewHolder holder;
        private int fillRangesCode;

        PriceRangeAdapter(ArrayList<PriceRange> ranges) {
            this.ranges = ranges;
        }

        void setToDefault() {
            for (int i = ranges.size() - 1; i > 0; i++) {
                ranges.remove(i);
                notifyItemRemoved(i);
            }
            holder.price.setText(null);
            holder.maxQuantity.setText(null);
            holder.minQuantity.setText(null);
        }

        public RangeViewHolder getHolder() {
            return holder;
        }

        void addToLast(PriceRange newPriceRange) {
            if (ranges.size() < 4) {
                ranges.add(newPriceRange);
                notifyItemInserted(ranges.size() - 1);
            }
        }

        void removeRange(int postion) {
            ranges.remove(postion);
            notifyItemRemoved(postion);
        }

        void addManyRanges(ArrayList<PriceRange> newPriceRanges, int fillRangesCode) {
            this.fillRangesCode = fillRangesCode;
            ranges.clear();
            ranges.addAll(newPriceRanges);
            notifyDataSetChanged();
        }


        PriceRange[] getRanges() {
            Log.d(TAG, "getRanges: " + ranges.size());
            PriceRange[] priceRanges = new PriceRange[ranges.size()];
            for (int i = 0; i < ranges.size(); i++) {
                if (!ranges.get(i).isNull())
                    priceRanges[i] = ranges.get(i);
            }
            return priceRanges;
        }

        @NonNull
        @Override
        public PriceRangeAdapter.RangeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.add_price_range_item, parent, false);


            return new RangeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PriceRangeAdapter.RangeViewHolder holder, final int position) {
            this.holder = holder;
            if (fillRangesCode == 0) {
                setRangesControl(holder, position);
            } else {
                setRangesControl(holder, position);

                PriceRange priceRange = ranges.get(position);
                holder.minQuantity.setText(String.valueOf(priceRange.getMinQuantity()));
                holder.maxQuantity.setText(String.valueOf(priceRange.getMaxQuantity()));
                holder.price.setText(String.valueOf(priceRange.getPrice()));
            }

            holder.maxQuantity.addTextChangedListener(textListener(holder.maxQuantity, position));
            holder.minQuantity.addTextChangedListener(textListener(holder.minQuantity, position));
            holder.price.addTextChangedListener(textListener(holder.price, position));

        }

        private void setRangesControl(RangeViewHolder holder, int position) {
            if (position == 0) {
                holder.addNewRange.setOnClickListener(view -> addToLast(new PriceRange(0, 0, 0)));
            } else {
                holder.addNewRange.setImageResource(R.drawable.ic_remove_black_27dp);
                holder.addNewRange.setOnClickListener(view -> removeRange(position));
            }
        }

        @Override
        public int getItemCount() {
            return ranges.size();
        }

        TextWatcher textListener(TextView text, final int position) {
            return new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    try {
                        if (text.getId() == R.id.min_quantity_field) {
                            ranges.get(position).setMinQuantity(Integer.parseInt(String.valueOf(charSequence)));
                        } else if (text.getId() == R.id.max_quantity_field) {
                            ranges.get(position).setMaxQuantity(Integer.parseInt(String.valueOf(charSequence.toString())));
                        } else if (text.getId() == R.id.price_field) {
                            ranges.get(position).setPrice(Double.parseDouble(String.valueOf(charSequence.toString())));
                        }
                    } catch (NumberFormatException e) {
                        Log.e(TAG, e.getMessage(), e);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            };
        }

        class RangeViewHolder extends RecyclerView.ViewHolder {

            TextInputEditText minQuantity;
            TextInputEditText maxQuantity;
            TextInputEditText price;
            ImageButton addNewRange;

            RangeViewHolder(@NonNull View view) {
                super(view);

                minQuantity = view.findViewById(R.id.min_quantity_field);
                maxQuantity = view.findViewById(R.id.max_quantity_field);
                price = view.findViewById(R.id.price_field);
                addNewRange = view.findViewById(R.id.add_new_range_item_btn);

            }
        }
    }

}
