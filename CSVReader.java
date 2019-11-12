import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


public class CSVReader {

    public static ArrayList<Image> readImages() {
        String csvFile = "mnist_train.csv";
        String line = "";
        ArrayList<Image> imageList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            while ((line = br.readLine()) != null) {

                int[] imageArr = Arrays.stream(line.split(",")).mapToInt(Integer::parseInt).toArray();
                ArrayList<Integer> ImageList = new ArrayList<>(imageArr.length - 1);
                for (int i = 1; i < imageArr.length; i++) {
                    ImageList.add(imageArr[i]);
                }
                Image im = new Image(imageArr[0], ImageList);
                imageList.add(im);


            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageList;
    }

}
