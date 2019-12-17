public class V10_Condition implements condition {
    private int threshold;
    private int maxNumOfPixels;
    private int diagonalLevel;
    @Override
    public boolean applyCondition(Image img) {
        int i;
        int j;
        int currNumOfPixels = 0;
        if (diagonalLevel<0){
            i = -1 * diagonalLevel;
            j = 0;
        } else {
            i = 0;
            j = diagonalLevel;
        }
        while (i<= 27 && j<= 27){
            if (img.getPixelAt(i,j) > threshold) {
                currNumOfPixels++;
            }
            i++;
            j++;
        }

        return (currNumOfPixels>maxNumOfPixels);
    }
    public V10_Condition(int threshold, int maxNumOfPixels, int diagonalLevel){
        this.threshold = threshold;
        this.maxNumOfPixels = maxNumOfPixels;
        this.diagonalLevel = diagonalLevel;
    }
}
