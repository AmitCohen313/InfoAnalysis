public class V8_Condition implements condition {
    private int X;
    private int Y;
    private int[] weights;
    private int threshold;


    public V8_Condition(int x, int y, int threshold) {
        this.X = x;
        this.Y = y;
        this.threshold = threshold;
    }

    public boolean applyCondition(Image img) {
        return img.getPixels().get(28 * Y + X) > threshold;

    }
}
