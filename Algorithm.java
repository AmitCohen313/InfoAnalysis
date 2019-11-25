import java.util.*;

public class Algorithm {

    public static void main(String[] args) {



        ArrayList<Image> globalImageList = CSVReader.readImages("mnist_train.csv");
        ArrayList<Image> testSet = CSVReader.readImages("mnist_test.csv");
//        ArrayList<condition> conditionList = ConditionGroup.getConditions(Integer.parseInt(args[0]));
        ArrayList<condition> conditionList = ConditionGroup.getConditions(1,globalImageList);

        //treeNode tree = generateTree(1000,conditionList,globalImageList);

        treeNode tree = newExecuteAlgo(14,25,globalImageList,conditionList);

//        treeNode tree = executeAlgorithm(13,25,globalImageList,conditionList);

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




//    // The main method of the algorithm. Returns a prediction tree.
//    public static treeNode newExecuteAlgo(int L, double P, ArrayList<Image> globalImageList, ArrayList<condition> conditionList) {
//
//        // Generate a random validation set from training set.
//        ArrayList<Image> trainingSet = new ArrayList<>(globalImageList);
//        int validationSetSize = (int)(globalImageList.size()*P/100);
//        ArrayList<Image> validationSet = Utilities.getRandomElements(trainingSet,validationSetSize);
//
//        int maxQueueSize = (int)Math.pow(2,L);
//        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(maxQueueSize,new virtualTreeComperator());
//        int[] trainingFrequencies = calcImageFrequencies(trainingSet);
//        int label = Utilities.calcIndexOfBiggestElement(trainingFrequencies);
//        double entropy = Utilities.calcEntropy(trainingFrequencies,trainingSet.size());
//        treeNode t = new treeNode(trainingSet,entropy,label);
//        virtualTree vt = calcChildren(t,conditionList);
//        virtualTreePriorityQueue.add(vt);
//
//        // Check which tree size gives the best results.
//        double currentBestScore = 0.0;
//        treeNode currentBestTree = null;
//        for (int i = 0; i <= L; i++ ) {
//            int T = (int)Math.pow(2,i);
//
//            long start = System.currentTimeMillis();
//            treeNode tree = generateTree(T,conditionList,trainingSet);
//            double successPercentage = applyTreeOnDataSet(tree,validationSet);
//            long now = System.currentTimeMillis();
//            System.out.println("Iteration " + i + " Took " + (now-start) + " ms.");
//            System.out.println("Success percentage is " + successPercentage);
//            if (successPercentage > currentBestScore) {
//                currentBestScore = successPercentage;
//                currentBestTree = tree;
//            }
//        }
//        return currentBestTree;
//    }


    public static treeNode newExecuteAlgo(int L, double P, ArrayList<Image> globalImageList, ArrayList<condition> conditionList) {
        // Generate a random validation set from training set.
        ArrayList<Image> trainingSet = new ArrayList<>(globalImageList);
        int validationSetSize = (int)(globalImageList.size()*P/100);
        ArrayList<Image> validationSet = Utilities.getRandomElements(trainingSet,validationSetSize);
        int maxQueueSize = (int)Math.pow(2,L);

        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(maxQueueSize,new virtualTreeComperator());
        int[] trainingFrequencies = calcImageFrequencies(trainingSet);
        int label = Utilities.calcIndexOfBiggestElement(trainingFrequencies);
        double entropy = Utilities.calcEntropy(trainingFrequencies,trainingSet.size());
        treeNode t = new treeNode(trainingSet,entropy,label);
        virtualTree vt = calcChildren(t,conditionList,1);
        virtualTreePriorityQueue.add(vt);


        double currentBestScore = -1.0;
        int bestTreeSize = -1;
        for (int i = 1; i <= maxQueueSize; i++) {
            System.out.println(virtualTreePriorityQueue.size());
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            virtualTree leftTree = calcChildren(nodeToReplace.getLeft(), conditionList,i);
            virtualTree rightTree = calcChildren(nodeToReplace.getRight(), conditionList,i);
            virtualTreePriorityQueue.add(leftTree);
            virtualTreePriorityQueue.add(rightTree);


            if (powerOf2(i)){
                double currentScore = applyTreeOnDataSet(t,validationSet);
                if (currentScore>currentBestScore) {

                    bestTreeSize = i;
                    System.out.println("Current best size is: " + bestTreeSize);
                    currentBestScore = currentScore;
                    System.out.println("Current best score is: " + currentBestScore);
                }
            }
        }

        System.out.println("best size is: " + bestTreeSize);
        cleanTree(t, bestTreeSize);
        System.out.println("Current best score is: " + applyTreeOnDataSet(t,validationSet));

        return t;

    }

    private static boolean powerOf2(int number){
        return (number > 0) && ((number & (number - 1)) == 0);
    }

    private static void cleanTree(treeNode tree, int timeStamp) {

        if (tree.getLeft() != null) {
            if (tree.getLeft().getTimeStamp() > timeStamp) {
                tree.setLeft(null);
            } else {
                cleanTree(tree.getLeft(),timeStamp);
            }
        }

        if (tree.getRight() != null) {
            if (tree.getRight().getTimeStamp() > timeStamp) {
                tree.setRight(null);
            } else {
                cleanTree(tree.getRight(), timeStamp);
            }
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
        virtualTree vt = calcChildren(t,conditionList,1);
        virtualTreePriorityQueue.add(vt);


        for (int i = 0; i < iterationNumber; i++) {
            System.out.println(virtualTreePriorityQueue.size());
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            virtualTree leftTree = calcChildren(nodeToReplace.getLeft(), conditionList,i);
            virtualTree rightTree = calcChildren(nodeToReplace.getRight(), conditionList,i);
            virtualTreePriorityQueue.add(leftTree);
            virtualTreePriorityQueue.add(rightTree);
        }
        return t;
    }




    public static virtualTree calcChildren(treeNode father, List<condition> conditionList, int currentIteration) {
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
                currentBestTree = new treeNode(father.getImageList(), cond, left, right,currentIteration);
            }
        }
        return new virtualTree(currentBestTree,father,maxInfoGain);
    }
}
