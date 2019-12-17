public class V4_condition implements condition {
    private int X;
    private int Y;
    private double threshold;

    @Override
    public boolean applyCondition(Image img) {
        return img.getPixelAt(X,Y) > threshold;
    }

    public V4_condition(int x, int y, double threshold) {
        this.X = x;
        this.Y = y;
        this.threshold = threshold;
    }
}
