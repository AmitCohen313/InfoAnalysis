import java.util.*;

public class Algorithm {

    public static void main(String[] args) {

        ArrayList<Image> globalImageList = CSVReader.readImages();
        ArrayList<condition> conditionList = ConditionGroup.getConditions(Integer.parseInt(args[0]));
        treeNode tree = executeAlgorithm(3,50,globalImageList,conditionList);
        double s = applyTreeOnDataSet(tree,globalImageList);

        System.out.print("score is " + s);
    }

    // The main method of the algorithm. Returns a prediction tree.
    public static treeNode executeAlgorithm(int L, double P, ArrayList<Image> globalImageList, ArrayList<condition> conditionList) {

        // Generate a random validation set from training set.
        ArrayList<Image> trainingSet = new ArrayList<>(globalImageList);
        int validationSetSize = (int)(globalImageList.size()*P/100);
        ArrayList<Image> validationSet = Utilities.getRandomElements(trainingSet,validationSetSize);

        // Check which tree size gives the best results.
        double currentBestScore = 0.0;
        treeNode currentBestTree = null;
        for (int i = 0; i <= L; i++ ) {
            int T = (int)Math.pow(2,i);
            treeNode tree = InitTree(trainingSet);
            generateTree(tree,T,conditionList);
            double successPercentage = applyTreeOnDataSet(tree,validationSet);
            if (successPercentage > currentBestScore) {
                currentBestScore = successPercentage;
                currentBestTree = tree;
            }
        }
        return currentBestTree;
    }

    // Gets a data set and a tree, applies the tree on the data set and returns the success percentage.
    public static double applyTreeOnDataSet(treeNode tree, ArrayList<Image> dataSet) {
        int numOfSuccesses = 0;
        for (Image img: dataSet) {
            if (applyTreeOnImage(tree,img)) {
                numOfSuccesses++;
            }
        }
        return (double)(numOfSuccesses)/dataSet.size();
    }

    // Gets a tree and an image, applies the tree on the image and returns whether the prediction is right or not.
    public static boolean applyTreeOnImage(treeNode tree, Image img) {
        while (!tree.isLeaf()) {
            if (tree.getCondition().applyCondition(img)) {
                tree = tree.getRight();
            } else {
                tree = tree.getLeft();
            }
        }
        return tree.getExpectedLabel() == img.getLabel();
    }


    // Creates a new tree with one leaf. The label of the leaf is the most common label in the data set.
    private static treeNode InitTree(ArrayList<Image> imageList) {
        int[] frequencies = calcImageFrequencies(imageList);
        int label = Utilities.calcIndexOfBiggestElementInArray(frequencies);
        double entropy = Utilities.calcEntropy(frequencies,imageList.size());
        treeNode t = new treeNode(imageList,entropy,label);
        return t;
    }

    // Gets image list and returns a frequency array of the images' labels.
    private static int[] calcImageFrequencies(ArrayList<Image> imageList) {
        int[] result = new int[10];

        // Count the number of labels.
        for (Image img : imageList ) {
            result[img.getLabel()]++;
        }
        return result;
    }



    private static void generateTree(treeNode tree, int iterationNumber, List<condition> conditionList) {
        for (int i = 0; i < iterationNumber; i++) {
            // Extract the leaves from the tree.
            ArrayList<treeNode> leaves = Utilities.extractLeaves(tree);
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
                    }
                    double passedEntropy = Utilities.calcEntropy(passedFrequencies, passedCond.size());
                    double failedEntropy = Utilities.calcEntropy(failedFrequencies, failedCond.size());
                    double relativePassedEntropy = passedCond.size() * passedEntropy / leaf.getImageList().size();
                    double relativeFailedEntropy = failedCond.size() * failedEntropy / leaf.getImageList().size();
                    double currentInfoGain = leaf.getEntropy() - relativePassedEntropy - relativeFailedEntropy;
                    double currentWeightedInfoGain = currentInfoGain*leaf.getImageList().size();
                    if (currentWeightedInfoGain > maxInfoGain) {
                        maxInfoGain = currentWeightedInfoGain;
                        currentLeafToReplace = leaf;
                        treeNode left = new treeNode(failedCond, failedEntropy,Utilities.calcIndexOfBiggestElementInArray(failedFrequencies));
                        treeNode right = new treeNode(passedCond, passedEntropy,Utilities.calcIndexOfBiggestElementInArray(passedFrequencies));
                        treeForReplacement = new treeNode(leaf.getImageList(), cond, left, right);
                    }
                }
            }
            currentLeafToReplace.replaceLeafByTree(treeForReplacement);
        }
    }
}
