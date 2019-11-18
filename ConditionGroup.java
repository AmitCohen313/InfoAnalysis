
import java.util.ArrayList;
import java.util.List;

public class ConditionGroup {/*TODO complete version 2*/

    public static ArrayList<condition> getConditions(int condition_ver) {
        ArrayList<condition> conditionList = new ArrayList<>();
        if (condition_ver == 1) {
            for (int i = 0; i < 28; i++) {
                for (int j = 0; j < 28; j++) {
                    conditionList.add(new V1_condition(i, j));
                }
            }
        } else {
            return null;
        }
        return conditionList;

    }
}
