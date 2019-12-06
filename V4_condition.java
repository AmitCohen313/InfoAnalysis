public class V4_condition implements condition {
    private int X;
    private int Y;
    private int threshold;

    @Override
    public boolean applyCondition(Image img) {
        return img.getPixels().get(28 * Y + X) > threshold;
    }

    public V4_condition(int x, int y, int threshold) {
        this.X = x;
        this.Y = y;
        this.threshold = threshold;
    }
}
