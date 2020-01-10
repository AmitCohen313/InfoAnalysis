public class V2_condition implements condition {
    private int X;
    private int Y;
    private int threshold;
    private final int condVer = 2;

    @Override
    public boolean applyCondition(Image img) {
        return img.getPixelAt(X,Y) > threshold;
    }

    public V2_condition(int x, int y, int threshold) {
        this.X = x;
        this.Y = y;
        this.threshold = threshold;
    }

    public String toString(){
        return condVer+"!"+X+"!"+Y+"!"+threshold;
    }

    public static condition fromString(String[] cond){
        return new V2_condition(Integer.parseInt(cond[0]),Integer.parseInt(cond[1]),Integer.parseInt(cond[2]));
    }
}
