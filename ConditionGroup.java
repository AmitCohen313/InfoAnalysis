
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
                        else if (i == 0 && j != 0) {
                            tempArr[i][j] = tempArr[i][j - 1] * (j - 1) / j;
                        }
                        else if (i != 0 && j == 0) {
                            tempArr[i][j] = tempArr[i - 1][j] * (i - 1) / i;
                        }
                        else {
                            tempArr[i][j] = ((tempArr[i - 1][j] * (i * (j + 1)) + (tempArr[i][j - 1] * (i + 1) * j - tempArr[i - 1][j - 1] * i * j) * j)+img.getPixels().get(j*27+i)) / ((i + 1) * (j + 1));
                        }
                    }
                }
                System.out.println("hi");
            }
        } else if (condition_ver == 3) {
            for (int i = 2; i < 26; i++) {
                for (int j = 2; j < 26; j++) {
                    for (int k = 0; k < 256; k = k + 2) {
                        conditionList.add(new V2_condition(i, j, k,5));
                    }
                }
            }
        } else if (condition_ver == 4) {
        for (int i = 0; i <= 27; i=i+2) {
            for (int j = 0; j <= 27; j=j+2) {
                for (int k = 1; k <= 255; k=k+10) {
                    conditionList.add(new V3_condition(i, j, k));
                }
            }
        }

        } else if (condition_ver == 5) {
            for (int i = 0; i <= 27; i=i+1) {
                for (int j = 0; j <= 27; j=j+1) {
                    for (int k = 0; k <= 255; k=k+10) {
                        conditionList.add(new V4_condition(i, j, k));
                    }
                }
            }
        } else if (condition_ver == 6) {
            for (int i = 1; i < 27; i++) {
                for (int j = 1; j < 27; j++) {
                    for (int k = 0; k < 256; k = k + 4) {
                        conditionList.add(new V2_condition(i, j, k, 3));
                    }
                }
            }
        } else if (condition_ver == 7) {
            // first set
            for (int i = 1; i < 26; i = i + 2) {
                for (int j = 1; j < 26; j = j + 2) {
                    for (int k = 0; k < 256; k = k + 50) {
                        conditionList.add(new V2_condition(i, j, k, 3));
                    }
                }
            }

//            // Third set
//            for (int i = 2; i <= 25; i = i + 2) {
//                for (int j = 2; j <= 25; j = j + 2) {
//                    for (int k = 0; k <= 255; k = k + 50) {
//                        conditionList.add(new V4_condition(i, j, k));
//                    }
//                }
//            }

            for (int i = 2; i <= 25; i = i + 2) {
                for (int j = 0; j < 10; j = j + 2){
                    for (int k = 0; k < 256; k = k + 64) {
                        conditionList.add(new V6_Condition(i,k,j));
                        conditionList.add(new V5_Condition(i,k,j));
                    }
                }
            }

            for (int i = 2; i < 26; i = i + 3) {
                for (int j = 2; j < 26; j = j + 3) {
                    for (int k = 0; k < 256; k = k + 50) {
                        conditionList.add(new V2_condition(i, j, k,5));
                    }
                }
            }

        } else if (condition_ver == 8) {
            for (int i = 0; i <= 27; i++) {
                for (int j = 0; j < 20; j = j + 2){
                    for (int k = 0; k < 256; k = k + 16) {
                        conditionList.add(new V5_Condition(i,k,j));
                        conditionList.add(new V6_Condition(i,k,j));
                    }
                }
            }
        }
        if (condition_ver == 9) {
            for (int i = 0; i < 28; i++) {
                for (int j = 0; j < 28; j++) {
                    for (int k = 0; k < 256; k = k + 2){
                        conditionList.add(new V7_Condition(i, j,k));
                    }
                }
            }
        }

        return conditionList;
    }
}
