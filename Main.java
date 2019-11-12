import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        ArrayList<Image> images = CSVReader.readImages();
        ArrayList<condition> conditionArrayList = ConditionGroup.getConditions(Integer.parseInt(args[0]));


    }

    private void runAlgo(List<treeNode> leaves, int iterationNumber, List<condition> conditionList,
                         List<Image> imageList) {
        for (int i = 0; i < iterationNumber; i++) {
            double maxInfoGain = Integer.MIN_VALUE;
            treeNode currentBestLeaf = null;
            treeNode treeForReplace = null;
            for (treeNode leaf : leaves) {
                for (condition cond : conditionList) {
                    ArrayList<Image> passedCond = new ArrayList<>();
                    ArrayList<Image> failedCond = new ArrayList<>();
                    int[] passedFrequencies = new int[10];
                    int[] failedFrequencies = new int[10];
                    for (Image img : leaf.getImageList()) {
                        if (cond.applyCondition(img)) {
                            passedCond.add(img);
                            passedFrequencies[img.getLabel()]++;
                        } else {
                            failedCond.add(img);
                            failedFrequencies[img.getLabel()]++;
                        }
                    }/*TODO add entropies to each leaf*/
                    double passedEntropy = calcEntropy(passedFrequencies, passedCond.size());
                    double failedEntropy = calcEntropy(failedFrequencies, failedCond.size());
                    double relativePassedEntropy = passedCond.size() * passedEntropy / leaf.getImageList().size();
                    double relativeFailedEntropy = failedCond.size() * failedEntropy / leaf.getImageList().size();
                    double currentInfoGain = leaf.getEntropy() - relativePassedEntropy - relativeFailedEntropy;
                    if (currentInfoGain > maxInfoGain) {
                        maxInfoGain = currentInfoGain;
                        currentBestLeaf = leaf;
                        treeNode left = new treeNode(imageList, failedEntropy);
                        treeNode right = new treeNode(imageList, passedEntropy);
                        treeForReplace = new treeNode(imageList, cond, left, right);
                    }
                }/**********************************************************************************/
            }/*TODO reference problem for currentBestLeaf, change the value inside the leaf!!!!!!!*/
            currentBestLeaf = treeForReplace;
        }
    }

    private static int mostFrequent(int[] arr) {
        Map<Integer, Integer> hashMap = new HashMap<>();
        for (int num : arr) {
            hashMap.put(num, hashMap.getOrDefault(num, 0) + 1);
        }
        int max_count = 0, result = -1;
        for (Map.Entry<Integer, Integer> val : hashMap.entrySet()) {
            if (max_count < val.getValue()) {
                result = val.getKey();
                max_count = val.getValue();
            }
        }
        return result;
    }

    /*TODO ask sivan about the log base*/
    private double calcEntropy(int[] frequencies, int totalAmount) {
        double result = 0.0;
        for (int i = 0; i < 10; i++) {
            result += (((float) frequencies[i] / totalAmount) * Math.log((float) totalAmount / frequencies[i]));
        }
        return result;
    }
}
