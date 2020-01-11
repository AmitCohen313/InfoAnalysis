import java.util.*;
import java.util.concurrent.*;

public class Algorithm {
    public static void main(String[] args) {
        if (args[0].equals("learntree")) {
            int conditionVer = Integer.parseInt(args[1]);
            int P = Integer.parseInt(args[2]);
            int L = Integer.parseInt(args[3]);
            String trainingset_filename = args[4];
            String outputtree_filename = args[5];
            learn(conditionVer,P,L,trainingset_filename,outputtree_filename);
        } else if (args[0].equals("predict")) {
            String tree_filename = args[1];
            String testset_filename = args[2];
            predict(tree_filename,testset_filename);
        }
        ////////////////////////////////////////////////////
    }


    public static void learn(int conditionVer, int P, int L, String trainSetPath, String outputFileName) {
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        ////////////// Image reading //////////////////////
        ArrayList<condition> conditionList = ConditionFactory.createConditions(conditionVer);
        ArrayList<Image> trainImageList = CSVReader.readImages(trainSetPath);
        ///////////////////////////////////////////////////

        ////////////// Tree creating //////////////////////
        Future<Integer> learnWithHoldoutSetFuture = executorService.submit(() -> learnBestTreeSize(L,P,trainImageList,conditionList));
        Future<treeNode> learnFuture = executorService.submit(() -> (treeNode)createTree((int)Math.pow(2,L),trainImageList,null,conditionList));
        treeNode tree;
        try {
            Integer bestTreeSize = learnWithHoldoutSetFuture.get();
            tree = learnFuture.get();
            trimTree(tree,bestTreeSize);
            Utilities.writeTreeToFile(tree,outputFileName);
        } catch (Exception e) {
            System.out.println("Exception raised while learning");
            return;
        } finally {
            executorService.shutdown();
        }
        ///////////////////////////////////////////////////

        System.out.println("num: " + trainImageList.size());
        System.out.println("error: " + (int)(100-(applyTreeOnDataSet(tree,trainImageList,false)*100)));
        System.out.println("size: " + (Utilities.size(tree)-1)/2);
    }


    public static void predict(String treeFilePath,String testSetPath) {
        treeNode treeTest = Utilities.readTreeFromFile(treeFilePath);
        ArrayList<Image> testSet = CSVReader.readImages(testSetPath);
        applyTreeOnDataSet(treeTest,testSet,true);
    }


    public static Integer learnBestTreeSize(int L, double P, ArrayList<Image> globalImageList, ArrayList<condition> conditionList) {
        // Divide the image set to training and validation set.
        ArrayList<Image> trainingSet = new ArrayList<>(globalImageList);
        int validationSetSize = (int)(globalImageList.size()*P/100);
        ArrayList<Image> validationSet = Utilities.getRandomElements(trainingSet,validationSetSize);
        int maxQueueSize = (int)Math.pow(2,L);

        Integer bestTreeSize = (Integer)createTree(maxQueueSize,trainingSet,validationSet,conditionList);
        return bestTreeSize;
    }


    // Create a tree with a given size. According to the whether validation set is null or not - can either return the best tree size or return the tree itself.
    private static Object createTree(int numOfIterations, ArrayList<Image> trainingSet, ArrayList<Image> validationSet, ArrayList<condition> conditionList) {
        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(numOfIterations,new virtualTreeComperator());
        int[] trainingFrequencies = countImageFrequencies(trainingSet);
        int label = Utilities.getIndexOfBiggestElement(trainingFrequencies);
        double entropy = Utilities.calcEntropy(trainingFrequencies,trainingSet.size());

        treeNode treeToReturn = new treeNode(trainingSet,entropy,label);
        virtualTreePriorityQueue.add(createChildren(treeToReturn, conditionList));
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        double currentBestScore = -1.0;
        int bestTreeSize = -1;
        for (int i = 1; i <= numOfIterations; i++) {
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            // If the queue is empty, stop the loop.
            if (virtualTree == null) {
                break;
            }
            // Replace the leaf in the tree with the tree created earlier by that node.
            virtualTree.performSwap();
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.setTimeStamp(i);
            Future<virtualTree> leftFuture = executorService.submit(() -> createChildren(nodeToReplace.getLeft(), conditionList));
            Future<virtualTree> rightFuture = executorService.submit(() -> createChildren(nodeToReplace.getRight(), conditionList));

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
                System.out.println("Exception raised while learning");
                break;
            }

            // Validation set exist means that the method should return tree size.
            if (validationSet != null) {
                // Check if the current tree is better than the current best tree.
                if (Utilities.powerOf2(i)) {
                    double currentScore = applyTreeOnDataSet(treeToReturn, validationSet,false);
                    if (currentScore > currentBestScore) {
                        bestTreeSize = i;
                        currentBestScore = currentScore;
                    }
                }
            }
        }
        executorService.shutdown();

        // Validation set exist means that the method should return tree size.
        if (validationSet != null){
            return bestTreeSize;
        } else {
            return treeToReturn;
        }
    }


    // Trim all the nodes that are older than the given timestamp.
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
    public static double applyTreeOnDataSet(treeNode tree, ArrayList<Image> dataSet, boolean shouldPrintLabels) {
        int numOfSuccesses = 0;
        for (Image img: dataSet) {
            if (applyTreeOnImage(tree,img,shouldPrintLabels)) {
                numOfSuccesses++;
            }
        }
        return (double)(numOfSuccesses)/dataSet.size();
    }


    // Gets a tree and an image, applies the tree on the image and returns whether the prediction is right or not.
    public static boolean applyTreeOnImage(treeNode tree, Image img, boolean shouldPrintLabels) {
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
        if (shouldPrintLabels){
            System.out.println(tree.getExpectedLabel());
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


    // Gets a node and creates it's right and left children.
    public static virtualTree createChildren(treeNode father, List<condition> conditionList) {
        double maxInfoGain = -1.0;
        treeNode currentBestTree = father;
        int condListSize = conditionList.size();
        for (int j = 0; j < condListSize; j++){
            condition cond = conditionList.get(j);
            ArrayList<Image> passedCond = new ArrayList<>(father.imageList.size());
            ArrayList<Image> failedCond = new ArrayList<>(father.imageList.size());
            int[] passedFrequencies = new int[10];
            int[] failedFrequencies = new int[10];
            int listSize = father.imageList.size();
            for (int i = 0; i<listSize; i++){
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
            double currentWeightedInfoGain = Utilities.calcWeightedInfoGain(passedEntropy,failedEntropy,passedCond.size(),failedCond.size(),father);
            // If the current IG better than the current maximum IG, replace the current best tree with a new one.
            if (currentWeightedInfoGain > maxInfoGain) {
                maxInfoGain = currentWeightedInfoGain;
                treeNode right = new treeNode(passedCond, passedEntropy, Utilities.getIndexOfBiggestElement(passedFrequencies));
                treeNode left = new treeNode(failedCond, failedEntropy, Utilities.getIndexOfBiggestElement(failedFrequencies));
                currentBestTree = new treeNode(father.imageList, cond, left, right, father.getExpectedLabel());
            }
        }
        // Return a tree that will later replace father node.
        return new virtualTree(currentBestTree, father, maxInfoGain);
    }
}