import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Algorithm {
    public static void main(String[] args) {
        long now = System.currentTimeMillis();
        int conditionVer = 2;
        int L = 12;
        int P = 30;
        String outputFilename = "tree.txt";
        String trainSetPath = "deskewed_mnist_train.csv";
        String testSetPath = "deskewed_mnist_test.csv";
        ExecutorService executorService = Executors.newFixedThreadPool(3);

        ////////////// Image reading //////////////////////
        Future<ArrayList<Image>> readImagesFuture = executorService.submit(() -> CSVReader.readImages(trainSetPath));
        Future<ArrayList<Image>> readTestImagesFuture = executorService.submit(() -> CSVReader.readImages(testSetPath));
        ArrayList<condition> conditionList = ConditionGroup.getConditions(conditionVer);
        System.out.println("There are " + conditionList.size() + " conditions.");

        ArrayList<Image> trainImageList;
        ArrayList<Image> testImageList;
        try {
            trainImageList = readImagesFuture.get();
            testImageList = readTestImagesFuture.get();
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        System.out.println("Reading images took " + ((float)(System.currentTimeMillis()-now)/1000) + " s");
        ///////////////////////////////////////////////////


        Future<Integer> learnWithHoldoutSetFuture = executorService.submit(() -> learnWithHoldoutSet(L,P,trainImageList,conditionList));
        Future<treeNode> learnFuture = executorService.submit(() -> learn((int)Math.pow(2,L),trainImageList,conditionList));
        treeNode tree;
        try {
            Integer bestTreeSize = learnWithHoldoutSetFuture.get();
            tree = learnFuture.get();
            System.out.println("\nScore on test set before trim is is " + applyTreeOnDataSet(tree,testImageList));
            trimTree(tree,bestTreeSize);
            Utilities.writeTreeToFile(tree,outputFilename);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        } finally {
            executorService.shutdown();
        }



        System.out.println("Learning took " + ((float)((System.currentTimeMillis()-now))/1000) + "s");
        System.out.println("Tree size is " + size(tree));
        System.out.println("score is " + applyTreeOnDataSet(tree,trainImageList));
        System.out.println("score on test set is " + applyTreeOnDataSet(tree,testImageList));

        predict(outputFilename,testSetPath);
    }

    public static void predict(String treeFilePath,String testSetPath) {
        treeNode treeTest = Utilities.readTreeFromFile(treeFilePath);
        ArrayList<Image> testSet = CSVReader.readImages(testSetPath);
        System.out.println("New tree size is " + size(treeTest));
        System.out.println("new score on test set is " + applyTreeOnDataSet(treeTest,testSet));
    }

    public static treeNode learn(int treeSize, ArrayList<Image> globalImageList, ArrayList<condition> conditionList) {
        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(treeSize,new virtualTreeComperator());
        int[] trainingFrequencies = countImageFrequencies(globalImageList);
        int label = Utilities.getIndexOfBiggestElement(trainingFrequencies);
        double entropy = Utilities.calcEntropy(trainingFrequencies,globalImageList.size());

        treeNode treeToReturn = new treeNode(globalImageList,entropy,label);

        virtualTreePriorityQueue.add(calcChildren(treeToReturn, conditionList));
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for (int i = 1; i <= treeSize; i++) {
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            if (virtualTree == null) {
                break;
            }
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            nodeToReplace.setTimeStamp(i);
            Future<virtualTree> leftFuture = executorService.submit(() -> calcChildren(nodeToReplace.getLeft(), conditionList));
            Future<virtualTree> rightFuture = executorService.submit(() -> calcChildren(nodeToReplace.getRight(), conditionList));
            try {
                virtualTree left = leftFuture.get();
                if (left.informationGain>0) {
                    virtualTreePriorityQueue.add(left);
                }
                virtualTree right = rightFuture.get();
                if (right.informationGain>0){
                    virtualTreePriorityQueue.add(right);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();

        return treeToReturn;
    }


    public static Integer learnWithHoldoutSet(int L, double P, ArrayList<Image> globalImageList, ArrayList<condition> conditionList) {
        // Divide the image set to training and validation set.
        ArrayList<Image> trainingSet = new ArrayList<>(globalImageList);
        int validationSetSize = (int)(globalImageList.size()*P/100);
        ArrayList<Image> validationSet = Utilities.getRandomElements(trainingSet,validationSetSize);

        // Initialize the tree.
        int maxQueueSize = (int)Math.pow(2,L);
        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(maxQueueSize,new virtualTreeComperator());
        int[] trainingFrequencies = countImageFrequencies(trainingSet);
        int label = Utilities.getIndexOfBiggestElement(trainingFrequencies);
        double entropy = Utilities.calcEntropy(trainingFrequencies,trainingSet.size());

        treeNode treeToReturn = new treeNode(trainingSet,entropy,label);
        virtualTreePriorityQueue.add(calcChildren(treeToReturn, conditionList));
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        double currentBestScore = -1.0;
        Integer bestTreeSize = -1;
        for (int i = 1; i <= maxQueueSize; i++) {
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            if (virtualTree == null){
                break;
            }
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            nodeToReplace.setTimeStamp(i);
            Future<virtualTree> leftFuture = executorService.submit(() -> calcChildren(nodeToReplace.getLeft(), conditionList));
            Future<virtualTree> rightFuture = executorService.submit(() -> calcChildren(nodeToReplace.getRight(), conditionList));

            try {
                virtualTree left = leftFuture.get();
                if (left.informationGain>0) {
                    virtualTreePriorityQueue.add(left);
                }
                virtualTree right = rightFuture.get();
                if (right.informationGain>0){
                    virtualTreePriorityQueue.add(right);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            // Check if the current tree is better than the current best tree.
            if (powerOf2(i)) {
                System.out.print(i + ".");
                double currentScore = applyTreeOnDataSet(treeToReturn, validationSet);
                if (currentScore > currentBestScore) {
                    bestTreeSize = i;
                    currentBestScore = currentScore;
                }
            }
        }
        executorService.shutdown();

        return bestTreeSize;
    }


    private Object createTree(int numOfIterations, int learnVersion, ArrayList<Image> trainingSet, ArrayList<Image> validationSet, ArrayList<condition> conditionList) {
        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(numOfIterations,new virtualTreeComperator());
        int[] trainingFrequencies = countImageFrequencies(trainingSet);
        int label = Utilities.getIndexOfBiggestElement(trainingFrequencies);
        double entropy = Utilities.calcEntropy(trainingFrequencies,trainingSet.size());

        treeNode treeToReturn = new treeNode(trainingSet,entropy,label);
        virtualTreePriorityQueue.add(calcChildren(treeToReturn, conditionList));
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        double currentBestScore = -1.0;
        Integer bestTreeSize = -1;
        for (int i = 1; i <= numOfIterations; i++) {
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            if (virtualTree == null){
                break;
            }
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            nodeToReplace.setTimeStamp(i);
            Future<virtualTree> leftFuture = executorService.submit(() -> calcChildren(nodeToReplace.getLeft(), conditionList));
            Future<virtualTree> rightFuture = executorService.submit(() -> calcChildren(nodeToReplace.getRight(), conditionList));

            try {
                virtualTree left = leftFuture.get();
                if (left.informationGain>0) {
                    virtualTreePriorityQueue.add(left);
                }
                virtualTree right = rightFuture.get();
                if (right.informationGain>0){
                    virtualTreePriorityQueue.add(right);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (learnVersion == 1) {
                // Check if the current tree is better than the current best tree.
                if (powerOf2(i)) {
                    System.out.print(i + ".");
                    double currentScore = applyTreeOnDataSet(treeToReturn, validationSet);
                    if (currentScore > currentBestScore) {
                        bestTreeSize = i;
                        currentBestScore = currentScore;
                    }
                }
            }
        }
        executorService.shutdown();

        if (learnVersion == 1){
            return bestTreeSize;
        } else {
            return treeToReturn;
        }
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


    private static void trimTree(treeNode tree, int timeStamp) {
        if (tree.getTimeStamp() > timeStamp) {
            tree.setLeft(null);
            tree.setRight(null);
        }

        if (tree.getLeft() != null) {
            trimTree(tree.getLeft(), timeStamp);
        }
        if (tree.getRight() != null) {
            trimTree(tree.getRight(), timeStamp);
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
    private static int[] countImageFrequencies(ArrayList<Image> imageList) {
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
        int condListSize = conditionList.size();
//        for (condition cond : conditionList) {
        for (int j = 0; j< condListSize; j++){
            condition cond = conditionList.get(j);
            ArrayList<Image> passedCond = new ArrayList<>(father.imageList.size());
            ArrayList<Image> failedCond = new ArrayList<>(father.imageList.size());
            int[] passedFrequencies = new int[10];
            int[] failedFrequencies = new int[10];
            int listSize = father.imageList.size();
//            for (Image img : father.imageList) {
            for (int i = 0;i<listSize;i++){
                Image img = father.imageList.get(i);
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
            double currentWeightedInfoGain = calcWeightedInfoGain(passedEntropy,failedEntropy,passedCond.size(),failedCond.size(),father);
            if (currentWeightedInfoGain > maxInfoGain) {
                maxInfoGain = currentWeightedInfoGain;
                treeNode right = new treeNode(passedCond, passedEntropy, Utilities.getIndexOfBiggestElement(passedFrequencies));
                treeNode left = new treeNode(failedCond, failedEntropy, Utilities.getIndexOfBiggestElement(failedFrequencies));
                currentBestTree = new treeNode(father.imageList, cond, left, right, father.getExpectedLabel());
            }
        }
        return new virtualTree(currentBestTree, father, maxInfoGain);
    }


    private static double calcWeightedInfoGain(double passedEntropy, double failedEntropy, int numOfPassed, int numOfFailed, treeNode father){
        double relativePassedEntropy = (double)numOfPassed * passedEntropy / father.imageList.size();
        double relativeFailedEntropy = (double)numOfFailed * failedEntropy / father.imageList.size();
        double currentInfoGain = father.getEntropy() - relativePassedEntropy - relativeFailedEntropy;
        if (currentInfoGain <= -1.0) {
            throw new IllegalArgumentException("info gain is negative!");
        }
        double currentWeightedInfoGain = currentInfoGain * father.imageList.size();
        return currentWeightedInfoGain;
    }
}
