public class V6_Condition implements condition {
    private int X;
    private int NumOfPixelsGreaterThanThreshold;
    private int threshold;

    @Override
    public boolean applyCondition(Image img) {
        int numOfHits = 0;
        for (int i = 0; i < 28; i++){
            if (img.getPixelAt(X,i) > threshold) {
                numOfHits++;
            }
        }
        return numOfHits > NumOfPixelsGreaterThanThreshold;
    }

    public V6_Condition(int x, int threshold, int NumOfPixelsGreaterThanThreshold) {
        this.NumOfPixelsGreaterThanThreshold = NumOfPixelsGreaterThanThreshold;
        this.X = x;
        this.threshold = threshold;
    }
}
