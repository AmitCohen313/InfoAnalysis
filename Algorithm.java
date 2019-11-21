import java.util.*;

public class Algorithm {

    public static void main(String[] args) {



        ArrayList<Image> globalImageList = CSVReader.readImages("mnist_train.csv");
        ArrayList<Image> testSet = CSVReader.readImages("mnist_test.csv");
        ArrayList<condition> conditionList = ConditionGroup.getConditions(Integer.parseInt(args[0]));

        treeNode tree = generateTree(1000,conditionList,globalImageList);

        //treeNode tree = executeAlgorithm(10,25,globalImageList,conditionList);

        double s = applyTreeOnDataSet(tree,globalImageList);
        double rs = applyTreeOnDataSet(tree,testSet);

        System.out.println("score is " + s);
        System.out.println("score on test set is " + rs);
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

            long start = System.currentTimeMillis();
            treeNode tree = generateTree(T,conditionList,trainingSet);
            double successPercentage = applyTreeOnDataSet(tree,validationSet);
            long now = System.currentTimeMillis();
            System.out.println("Iteration " + i + " Took " + (now-start) + " ms.");
            System.out.println("Success percentage is " + successPercentage);
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



    // Gets image list and returns a frequency array of the images' labels.
    private static int[] calcImageFrequencies(ArrayList<Image> imageList) {
        int[] result = new int[10];

        // Count the number of labels.
        for (Image img : imageList ) {
            result[img.getLabel()]++;
        }
        return result;
    }



    private static treeNode generateTree(int iterationNumber, List<condition> conditionList, ArrayList<Image> imageList) {
        // Initialize tree.
        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(iterationNumber,new virtualTreeComperator());
        int[] frequencies = calcImageFrequencies(imageList);
        int label = Utilities.calcIndexOfBiggestElement(frequencies);
        double entropy = Utilities.calcEntropy(frequencies,imageList.size());
        treeNode t = new treeNode(imageList,entropy,label);
        virtualTree vt = calcChildren(t,conditionList);
        virtualTreePriorityQueue.add(vt);


        for (int i = 0; i < iterationNumber; i++) {
            System.out.println(virtualTreePriorityQueue.size());
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            virtualTree leftTree = calcChildren(nodeToReplace.getLeft(), conditionList);
            virtualTree rightTree = calcChildren(nodeToReplace.getRight(), conditionList);
            virtualTreePriorityQueue.add(leftTree);
            virtualTreePriorityQueue.add(rightTree);
        }
        return t;
    }


    public static virtualTree calcChildren(treeNode father, List<condition> conditionList) {
        double maxInfoGain = -1.0;
        treeNode currentBestTree = father;
        for (condition cond : conditionList) {
            // Maybe the new takes a lot of time
            ArrayList<Image> passedCond = new ArrayList<>();
            ArrayList<Image> failedCond = new ArrayList<>();
            int[] passedFrequencies = new int[10];
            int[] failedFrequencies = new int[10];
            for (Image img : father.getImageList()) {
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
            double relativePassedEntropy = (double)passedCond.size() * passedEntropy / father.getImageList().size();
            double relativeFailedEntropy = (double)failedCond.size() * failedEntropy / father.getImageList().size();
            double currentInfoGain = father.getEntropy() - relativePassedEntropy - relativeFailedEntropy;
            double currentWeightedInfoGain = currentInfoGain * father.getImageList().size();
            if (currentWeightedInfoGain > maxInfoGain) {
                maxInfoGain = currentWeightedInfoGain;
                treeNode right = new treeNode(passedCond, passedEntropy,Utilities.calcIndexOfBiggestElement(passedFrequencies));
                treeNode left = new treeNode(failedCond, failedEntropy,Utilities.calcIndexOfBiggestElement(failedFrequencies));
                currentBestTree = new treeNode(father.getImageList(), cond, left, right);
            }
        }
        return new virtualTree(currentBestTree,father,maxInfoGain);
    }
}
