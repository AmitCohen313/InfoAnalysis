import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class CSVReader {

    public static ArrayList<Image> readImages(String path) {
        String line;
        ArrayList<Image> imageList = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            while ((line = br.readLine()) != null) {
                Utilities.Triplet<Integer,Integer[],Integer[]> res = createImage(line);
                imageList.add(new Image(res.first,res.second,res.third));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageList;
    }

    public static Utilities.Triplet<Integer,Integer[],Integer[]> createImage(final String line) {
        StringTokenizer st = new StringTokenizer(line,",");
        Integer[] pixelsArr = new Integer[784];
        Integer[] integralPixelsArr = new Integer[784];
        Integer label = Integer.parseInt((String)st.nextElement());
        int i = 0;
        int j = 0;
        while (st.hasMoreElements()) {
            pixelsArr[Utilities.calcIndices(i,j)] = (Integer.parseInt((String)st.nextElement()));

            // Create the integral array with dynamic programming.
            if (i == 0 && j == 0){
                integralPixelsArr[Utilities.calcIndices(i,j)] = pixelsArr[Utilities.calcIndices(i,j)];
            } else if (i == 0){
                integralPixelsArr[Utilities.calcIndices(i,j)] = integralPixelsArr[Utilities.calcIndices(i,j-1)] + pixelsArr[Utilities.calcIndices(i,j)];
            } else if (j==0){
                integralPixelsArr[Utilities.calcIndices(i,j)] = integralPixelsArr[Utilities.calcIndices(i-1,j)] + pixelsArr[Utilities.calcIndices(i,j)];
            } else {
                integralPixelsArr[Utilities.calcIndices(i,j)] = integralPixelsArr[Utilities.calcIndices(i-1,j)] + integralPixelsArr[Utilities.calcIndices(i,j-1)] - integralPixelsArr[Utilities.calcIndices(i-1,j-1)] + pixelsArr[Utilities.calcIndices(i,j)];
            }

            if (i == 27){
                i = 0;
                j++;
            } else {
                i++;
            }
        }
        return new Utilities.Triplet<>(label,pixelsArr,integralPixelsArr);
    }

}
