public class V5_Condition implements condition {
    private int Y;
    private int NumOfPixelsGreaterThanThreshold;
    private int threshold;

    @Override
    public boolean applyCondition(Image img) {
        int numOfHits = 0;
        for (int i = 0; i < 28; i++){
            if (img.getPixels().get(28 * Y + i) > threshold) {
                numOfHits++;
            }
        }
        return numOfHits > NumOfPixelsGreaterThanThreshold;
    }

    public V5_Condition(int y, int threshold, int NumOfPixelsGreaterThanThreshold) {
        this.NumOfPixelsGreaterThanThreshold = NumOfPixelsGreaterThanThreshold;
        this.Y = y;
        this.threshold = threshold;
    }
}
