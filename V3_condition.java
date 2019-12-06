
import java.util.ArrayList;

public class V3_condition implements condition {
    private int X;
    private int Y;
    private int threshold;


    public V3_condition(int x, int y, int threshold) {
        this.X = x;
        this.Y = y;
        this.threshold = threshold;
    }

    public boolean applyCondition(Image img) {
        ArrayList<Integer> pixels = img.getPixels();
        return calcRec(pixels,X,Y)>threshold;

    }

    private double calcRec(ArrayList<Integer> pixels, int x, int y) {
        double sum = 0.0;
        for (int i = 0; i <= x ; i++){
            for(int j = 0; j <= y; j++) {
                sum += pixels.get(28 * j + i);
            }
        }
        return sum / ((x+1)*(y+1));
    }

}
