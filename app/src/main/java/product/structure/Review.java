package product.structure;

import java.io.Serializable;

public final class Review implements Serializable {
    private int reviewNum;
    private String reviewText;
    private String reviewerName;

    public Review() {
    }

    public Review(int reviewNum) {
        this.reviewNum = reviewNum;
    }

    public Review(String reviewText) {
        this.reviewText = reviewText;
    }

    public Review(int reviewNum, String reviewText) {
        this.reviewNum = reviewNum;
        this.reviewText = reviewText;
    }

    public Review(int reviewNum, String reviewText, String reviewerName) {
        this.reviewNum = reviewNum;
        this.reviewText = reviewText;
        this.reviewerName = reviewerName;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public int getReviewNum() {
        return reviewNum;
    }

    public void setReviewNum(int reviewNum) {
        this.reviewNum = reviewNum;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public boolean compareReview(Review review) {
        return this.reviewNum == review.getReviewNum()
                && this.reviewText.equals(review.getReviewText());
    }
}
