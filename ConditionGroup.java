
import java.util.ArrayList;

public class ConditionGroup {/*TODO complete version 2*/

    public static ArrayList<condition> getConditions(int condition_ver, ArrayList<Image> imageList) {
        ArrayList<condition> conditionList = new ArrayList<>();
        if (condition_ver == 1) {
            for (int i = 0; i < 28; i++) {
                for (int j = 0; j < 28; j++) {
                    conditionList.add(new V1_condition(i, j));
                }
            }
        } else if (condition_ver == 2) {
            for(Image img: imageList) {
                double [][] tempArr = new double[28][28];
                for (int i = 0; i<28; i++) {
                    for (int j = 0; j<28; j++) {
                        if (i==0 && j==0) {
                            tempArr[i][j] = (double)img.getPixels().get(0);
                        }
                        else if (i==0 && j!=0) {
                            tempArr[i][j] = tempArr[i][j-1]*(j-1)/j;
                        }
                        else if (i!=0 && j==0) {
                            tempArr[i][j] = tempArr[i-1][j]*(i-1)/i;
                        }
                        else {
                            tempArr[i][j] = ((tempArr[i - 1][j] * (i * (j + 1)) + (tempArr[i][j - 1] * (i + 1) * j - tempArr[i - 1][j - 1] * i * j) * j)+img.getPixels().get(j*27+i)) / ((i + 1) * (j + 1));
                        }
                    }
                }
                System.out.println("hi");
            }
        }
        return conditionList;

    }
}
