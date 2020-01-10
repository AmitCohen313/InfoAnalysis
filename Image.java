public class Image {
    private int label;
    private Integer[] pixels;
    private Integer[] integralPixels;

    public Image(int label, Integer[] pixels, Integer[] integralPixels) {
        this.label = label;
        this.pixels = pixels;
        this.integralPixels = integralPixels;
    }

    public int getLabel() {
        return label;
    }

    public Integer getPixelAt(int x, int y) {
        return pixels[Utilities.calcIndices(x,y)];
    }

    public Integer getIntegralPixelAt(int x, int y) {
        return integralPixels[Utilities.calcIndices(x,y)];
    }
}
