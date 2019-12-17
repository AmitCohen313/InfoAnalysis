
public class V1_condition implements condition {
    private int X;
    private int Y;


    @Override
    public boolean applyCondition(Image img) {
        return img.getPixelAt(X,Y) > 128;
    }

    public V1_condition(int x, int y) {
        this.X = x;
        this.Y = y;
    }


}
