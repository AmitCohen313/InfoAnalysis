import java.util.ArrayList;

public class Image {
    private int label;
    private ArrayList<Integer> pixels;

    public Image(int label, ArrayList<Integer> pixels) {
        this.label = label;
        this.pixels = pixels;
    }

    public int getLabel() {
        return label;
    }

    public ArrayList<Integer> getPixels() {
        return pixels;
    }
}
