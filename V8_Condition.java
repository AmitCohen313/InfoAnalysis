public class V8_Condition implements condition {
    private int X;
    private int Y;
    private int[] weights;
    private double threshold;


    public V8_Condition(int x, int y, double threshold) {
        this.X = x;
        this.Y = y;
        this.threshold = threshold;
    }

    public boolean applyCondition(Image img) {
        return ((float)img.getPixelAt(X,Y-1)*0.125+
                (float)img.getPixelAt(X,Y+1)*0.125+
                (float)img.getPixelAt(X-1,Y)*0.125+
                (float)img.getPixelAt(X+1,Y) *0.125+
                (float)img.getPixelAt(X , Y)*0.25+
                (float)img.getPixelAt(X-1,Y-1  )*0.0625+
                (float)img.getPixelAt(X-1 , Y+1)*0.0625+
                (float)img.getPixelAt(X+1 , Y-1)*0.0625+
                (float)img.getPixelAt(X+1 , Y+1)*0.0625
                ) > threshold;

    }
}
