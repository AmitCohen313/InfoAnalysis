import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        ArrayList<Image> images = CSVReader.readImages();
        ArrayList<condition> conditionArrayList = ConditionGroup.getConditions(Integer.parseInt(args[0]));
        treeNode initialTree = InitTree(images);


    }

    private static treeNode InitTree(ArrayList<Image> imageList) {
        int label = mostFrequentLabel(imageList);
        treeNode t = new treeNode(imageList,label);
        return t;
    }

    private static int mostFrequentLabel(ArrayList<Image> imageList) {
        Map<Integer, Integer> hashMap = new HashMap<>();
        for (Image img : imageList) {
            hashMap.put(img.getLabel(), hashMap.getOrDefault(img.getLabel(), 0) + 1);
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

    private void runAlgo(List<treeNode> leaves, int iterationNumber, List<condition> conditionList,
                         List<Image> imageList) {
        for (int i = 0; i < iterationNumber; i++) {
            double maxInfoGain = Integer.MIN_VALUE;
            treeNode currentLeafToReplace = null;
            treeNode treeForReplacement = null;
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
                        currentLeafToReplace = leaf;
                        treeNode left = new treeNode(imageList, failedEntropy);
                        treeNode right = new treeNode(imageList, passedEntropy);
                        treeForReplacement = new treeNode(imageList, cond, left, right);
                    }
                }/**********************************************************************************/
            }/*TODO reference problem for currentBestLeaf, change the value inside the leaf!!!!!!!*/
            currentLeafToReplace = treeForReplacement;
        }
    }

    // Creates a list of all the leaves in a given tree.
    private ArrayList<treeNode> extractLeaves (treeNode tree) {
        ArrayList<treeNode> leafList = new ArrayList<>();
        extractLeaves(tree,leafList);
        return leafList;
    }

    // This method extracts the leaves from the tree
    private void extractLeaves (treeNode tree, ArrayList<treeNode> leafList) {
        if (tree.isLeaf()) {
            leafList.add(tree);
        }
        else {
            if (tree.getLeft() != null) {
                extractLeaves(tree.getLeft(), leafList);
            }
            if (tree.getRight() != null) {
                extractLeaves(tree.getRight(), leafList);
            }
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
    // Calc the entropy based on the given function
    private double calcEntropy(int[] frequencies, int totalAmount) {
        double result = 0.0;
        for (int i = 0; i < 10; i++) {
            result += (((float) frequencies[i] / totalAmount) * Math.log((float) totalAmount / frequencies[i]));
        }
        return result;
    }
}
