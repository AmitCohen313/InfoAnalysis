import java.util.ArrayList;

public class ConditionFactory {

    public static ArrayList<condition> createConditions(int condition_ver) {
        ArrayList<condition> conditionList = new ArrayList<>(5000);
        if (condition_ver == 1) {
            for (int i = 0; i < 28; i+=2) {
                for (int j = 0; j < 28; j+=2) {
                    conditionList.add(new V1_condition(i, j));
                }
            }
        } else if (condition_ver==2) {

            for (int i = 2; i < 25; i = i + 2) {
                for (int j = 2; j < 25; j = j + 8) {
                    for (int k = 0; k < 256; k = k + 100) {
                        conditionList.add(new V2_condition(i, j, k));
                    }
                }
            }

            for (int j = -7; j <= 7; j = j + 7) {
                for (int k = 0; k < 256; k = k + 100) {
                    conditionList.add(new V3_condition(k, j));
                }
            }

            for (int x2 = 4; x2 < 23; x2 = x2 + 3) {
                for (int y2 = 4; y2 < 23; y2 = y2 + 4) {
                    for (int x1 = x2 + 2; x1 < 25; x1 = x1 + 4) {
                        for (int y1 = y2 + 2; y1 < 25; y1 = y1 + 4) {
                            for (int k = 15; k <= 240; k = k * 2) {
                                conditionList.add(new V4_condition(x1, y1, x2, y2, k));
                            }
                        }
                    }
                }
            }
        }

        return conditionList;
    }
}
