package product.structure;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class PriceRange {

    private int minQuantity;
    private int maxQuantity;
    private double price;

    public PriceRange(int minQuantity, int maxQuantity, double price) {
        this.minQuantity = minQuantity;
        this.maxQuantity = maxQuantity;
        this.price = price;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public int getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(int maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public static int getMinOrder(PriceRange[] priceRanges) {
        if (priceRanges.length > 0) {
            int minQuantity = priceRanges[0].getMinQuantity();
            for (int i = 1; i < priceRanges.length; i++) {
                if (minQuantity > priceRanges[i].getMinQuantity())
                    minQuantity = priceRanges[i].getMinQuantity();
            }
            return minQuantity;
        }

        return 10;
    }

    public boolean isNull() {
        return this.maxQuantity == 0
                && this.minQuantity == 0
                && this.price == 0;
    }

    public static Double getMinPrice(PriceRange[] priceRanges) {
        if (priceRanges.length > 0) {
            int index = 0;
            for (int i = 1; i < priceRanges.length; i++) {
                if (priceRanges[i].getPrice() < priceRanges[i - 1].getPrice())
                    index = i;
                else
                    index = i - 1;
            }
            return priceRanges[index].getPrice();
        }

        return null;
    }

    public static JSONArray rangesToJSONArray(PriceRange[] ranges) throws JSONException {
        JSONArray jsonRanges = new JSONArray();
        int index = 0;
        for (PriceRange range : ranges) {
            JSONObject jsonRange = new JSONObject();
            jsonRange.put("price", range.getPrice());
            jsonRange.put("minQuantity", range.getMinQuantity());
            jsonRange.put("maxQuantity", range.getMaxQuantity());
            jsonRanges.put(index, jsonRange);
            index++;
        }
        return jsonRanges;
    }

    @Override
    public String toString() {
        return "PriceRange{" +
                "minQuantity=" + minQuantity +
                ", maxQuantity=" + maxQuantity +
                ", price=" + price +
                '}';
    }
}


