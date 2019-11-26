import java.util.*;

public class Algorithm {

    public static void main(String[] args) {

        long now = System.currentTimeMillis();
        ArrayList<Image> globalImageList = CSVReader.readImages("mnist_train.csv");
        ArrayList<Image> testSet = CSVReader.readImages("mnist_test.csv");
        System.out.println("Reading image took " + (System.currentTimeMillis()-now));
        now = System.currentTimeMillis();
//        ArrayList<condition> conditionList = ConditionGroup.getConditions(Integer.parseInt(args[0]));
        ArrayList<condition> conditionList = ConditionGroup.getConditions(1,globalImageList);
        System.out.println("Conditions creation took " + (System.currentTimeMillis()-now));
        now = System.currentTimeMillis();

        treeNode tree = newExecuteAlgo(13,20,globalImageList,conditionList);
        System.out.println("Creating the tree took " + (((System.currentTimeMillis()-now))/1000) + "s");
        now = System.currentTimeMillis();

        double s = applyTreeOnDataSet(tree,globalImageList);
        double rs = applyTreeOnDataSet(tree,testSet);

        System.out.println("Running data set on the tree took " + (System.currentTimeMillis()-now));

        System.out.println("Tree size is " + size(tree));
        System.out.println("score is " + s);
        System.out.println("score on test set is " + rs);
    }


    public static treeNode newExecuteAlgo(int L, double P, ArrayList<Image> globalImageList, ArrayList<condition> conditionList) {
        // Divide the image set to training and validation set.
        ArrayList<Image> trainingSet = new ArrayList<>(globalImageList);
        int validationSetSize = (int)(globalImageList.size()*P/100);
        ArrayList<Image> validationSet = Utilities.getRandomElements(trainingSet,validationSetSize);

        // Initialize the tree.
        int maxQueueSize = (int)Math.pow(2,L);
        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(maxQueueSize,new virtualTreeComperator());
        int[] trainingFrequencies = calcImageFrequencies(trainingSet);
        int label = Utilities.calcIndexOfBiggestElement(trainingFrequencies);
        double entropy = Utilities.calcEntropy(trainingFrequencies,trainingSet.size());
        treeNode t = new treeNode(trainingSet,entropy,label);
        virtualTree vt = calcChildren(t,conditionList);
        virtualTreePriorityQueue.add(vt);


        //
        double currentBestScore = -1.0;
        int bestTreeSize = -1;
        for (int i = 1; i <= maxQueueSize; i++) {
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            nodeToReplace.setTimeStamp(i);
            virtualTree leftTree = calcChildren(nodeToReplace.getLeft(), conditionList);
            virtualTree rightTree = calcChildren(nodeToReplace.getRight(), conditionList);
            virtualTreePriorityQueue.add(leftTree);
            virtualTreePriorityQueue.add(rightTree);


            // Check if the current tree is better than the current best tree.
            if (powerOf2(i)){
                double currentScore = applyTreeOnDataSet(t,validationSet);
                if (currentScore>currentBestScore) {

                    bestTreeSize = i;
                    currentBestScore = currentScore;
                }
            }
        }

        cleanTree(t, bestTreeSize);
        return t;

    }

    private static int size(treeNode node)
    {
        if (node == null)
            return 0;
        else
            return(size(node.getLeft()) + 1 + size(node.getRight()));
    }

    private static boolean powerOf2(int number){
        return (number > 0) && ((number & (number - 1)) == 0);
    }


    private static void cleanTree(treeNode tree, int timeStamp) {

        if (tree.getTimeStamp() > timeStamp) {
            tree.setLeft(null);
            tree.setRight(null);
        }

        if (tree.getLeft() != null) {
            cleanTree(tree.getLeft(), timeStamp);
        }
        if (tree.getRight() != null) {
            cleanTree(tree.getRight(), timeStamp);
        }
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
                if (tree.getRight() != null) {
                    tree = tree.getRight();
                }
            } else {
                if (tree.getLeft() != null) {
                    tree = tree.getLeft();
                }
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
                currentBestTree = new treeNode(father.getImageList(), cond, left, right, father.getExpectedLabel());
            }
        }
        return new virtualTree(currentBestTree,father,maxInfoGain);
    }
}
