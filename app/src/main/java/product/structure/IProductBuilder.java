package product.structure;

import android.net.Uri;

public interface IProductBuilder {

    ProductBuilder setDescription(String des);

    ProductBuilder setPics(Uri[] pics);

    ProductBuilder setPriceRanges(PriceRange[] priceRanges);

    ProductBuilder setPrice(double price);

    ProductBuilder setProductName(String proName);

    ProductBuilder setTimePublished(long time);

    ProductBuilder setColors(String[] colors);

    ProductBuilder setViechleType(String type);

    ProductBuilder setBrand(String brand);

    ProductBuilder setProductBy(String by);

    ProductBuilder setProductId(String by);

    ProductBuilder setToFav(boolean fav);

    ProductBuilder setReviews(Review[] reviews);

    Product build();

    class ProductBuilder implements IProductBuilder {

        private Product product;

        public ProductBuilder(String iClass) {
            if (iClass.equals(Clothing.class.getSimpleName())) {
                product = new Clothing();
            } else if (iClass.equals(AutomotiveParts.class.getSimpleName())) {
                product = new AutomotiveParts();
            } else if (iClass.equals(Food.class.getSimpleName())) {
                product = new Food();
            } else if (iClass.equals(Baby.class.getSimpleName())) {
                product = new Baby();
            }
        }


        @Override
        public ProductBuilder setDescription(String des) {
            product.setDescription(des);
            return this;
        }

        @Override
        public ProductBuilder setPics(Uri[] pics) {
            product.setPics(pics);
            return this;
        }


        @Override
        public ProductBuilder setProductName(String proName) {
            product.setProductName(proName);
            return this;
        }

        @Override
        public ProductBuilder setTimePublished(long time) {
            product.setTimePublished(time);
            return this;
        }

        @Override
        public ProductBuilder setColors(String[] colors) {
            product.setColors(colors);
            return this;
        }

        @Override
        public ProductBuilder setViechleType(String type) {
            if (product instanceof AutomotiveParts)
                ((AutomotiveParts) product).setViechleType(type);
            return this;
        }

        @Override
        public ProductBuilder setBrand(String by) {
            product.setBrandName(by);
            return this;
        }

        @Override
        public ProductBuilder setProductBy(String by) {
            product.setProdBy(by);
            return this;
        }

        @Override
        public ProductBuilder setProductId(String productId) {
            product.setId(productId);
            return this;
        }

        @Override
        public ProductBuilder setToFav(boolean fav) {
            product.setToFav(fav);
            return this;
        }

        @Override
        public ProductBuilder setReviews(Review[] reviews) {
            product.setReview(reviews);
            return this;
        }

        @Override
        public ProductBuilder setPriceRanges(PriceRange[] priceRanges) {
            product.setPriceRanges(priceRanges);
            return this;
        }

        @Override
        public ProductBuilder setPrice(double price) {
            product.setPrice(price);
            return this;
        }

        @Override
        public Product build() {
            return product;
        }


    }
}
