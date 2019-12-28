import java.util.List;

public class V1_condition implements condition {
    private int X;
    private int Y;
    private final int condVer = 1;

    @Override
    public boolean applyCondition(Image img) {
        return img.getPixelAt(X,Y) > 128;
    }

    public V1_condition(int x, int y) {
        this.X = x;
        this.Y = y;
    }

    public String toString(){
        return condVer+"!"+X+"!"+Y;
    }

    public static condition fromString(String[] serializedCond){
        return new V1_condition(Integer.parseInt(serializedCond[0]),Integer.parseInt(serializedCond[1]));
    }
}
