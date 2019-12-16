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
                Pair<Integer,ArrayList<Integer>> res = split(line);
                imageList.add(new Image(res.first,res.second));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageList;
    }

    public static Pair<Integer,ArrayList<Integer>> split(final String line) {
        StringTokenizer st = new StringTokenizer(line,",");
        ArrayList<Integer> pixelsArr = new ArrayList<>();
        Integer label = Integer.parseInt((String)st.nextElement());

        while (st.hasMoreElements()) {
            pixelsArr.add(Integer.parseInt((String)st.nextElement()));
        }
        return new Pair<>(label,pixelsArr);
    }
    private static class Pair<T1,T2> {
        protected T1 first;
        protected T2 second;
        protected Pair(T1 t1, T2 t2) {
            first = t1;
            second = t2;
        }
    }

}
