public class V4_condition implements condition {
    private int x1;
    private int y1;
    private int x2;
    private int y2;
    private int threshold;
    private final int condVer = 4;
    @Override
    public boolean applyCondition(Image img) {
        return (img.getIntegralPixelAt(x1,y1) + img.getIntegralPixelAt(x2,y2) - img.getIntegralPixelAt(x1,y2) - img.getIntegralPixelAt(x2,y1))/((x1-x2)*(y1-y2)) > threshold;
    }
    public V4_condition(int x1, int y1, int x2, int y2, int threshold){
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.threshold=threshold;
    }

    public String toString(){
        return condVer+"!"+x1+"!"+y1+"!"+x2+"!"+y2+"!"+threshold;
    }

    public static condition fromString(String[] cond){
        return new V4_condition(Integer.parseInt(cond[0]),Integer.parseInt(cond[1]),Integer.parseInt(cond[2]),Integer.parseInt(cond[3]),Integer.parseInt(cond[4]));
    }
}
