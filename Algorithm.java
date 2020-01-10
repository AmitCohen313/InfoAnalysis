import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class Algorithm {
    public static void main(String[] args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MM_dd_HH_mm_ss");
        LocalDateTime now = LocalDateTime.now();
        int numOfRepeats = 10;
        int conditionVer = 2;
        try (PrintWriter out = new PrintWriter("confMatrix" + "_R" + numOfRepeats  + "_V" + conditionVer + "_T"+ dtf.format(now) + ".txt")) {
            out.println("Results are:");
            //////////////// Confusion matrix section //////////
            out.println("Learn with mnist and predict mnist:");
            learn(13, 40, conditionVer, "tree.txt", "deskewed_mnist_train.csv",60000);
            predictWithConfusionMatrix("tree.txt", "deskewed_mnist_test.csv", out);
            out.println("Done first");
            out.println("Learn with kannada and predict with kannada:");
            learn(13, 40, conditionVer, "tree.txt", "deskewed_kannada_mnist_train.csv",60000);
            predictWithConfusionMatrix("tree.txt", "deskewed_kannada_mnist_test.csv", out);
            out.println("Done!");
            ////////////////////////////////////////////////////

//            int i = 50;
//
//            while (i<=60000) {
//                double sum = 0.0;
//                out.println("Sample size is " + i);
//                for (int k = 1; k<= numOfRepeats; k++) {
//                    learn(13, 40, conditionVer, "tree.txt", "deskewed_kannada_mnist_train.csv",i);
//                    sum += predict("tree.txt", "deskewed_kannada_mnist_test.csv", out);
//                }
//                out.println("Average result is "+ sum/numOfRepeats);
//                out.println("\n\n");
//                if (i < 200) {
//                    i += 50;
//                }
//                else if (i < 1000) {
//                    i += 200;
//                } else if (i < 10000) {
//                    i+=1000;
//                } else if (i < 60000) {
//                    i+=5000;
//                } else {
//                    i++;
//                }
//            }
        } catch (Exception e) {
            System.out.println("IOException is caught");
            e.printStackTrace();
        }


    }

    public static void learn(int L, int P, int conditionVer, String outputFileName, String trainSetPath, int sampleSizeLimitForGraph) {
        long now = System.currentTimeMillis();

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        ////////////// Image reading //////////////////////
        ArrayList<condition> conditionList = ConditionFactory.createConditions(conditionVer);
        System.out.println("There are " + conditionList.size() + " conditions.");
        ArrayList<Image> trainImageList = CSVReader.readImages(trainSetPath);
        // TODO: remove after graph is created and use trainImageList instead.
        ArrayList<Image> imageListForGraph = Utilities.getRandomElements(trainImageList,sampleSizeLimitForGraph);
        System.out.println("Reading images took " + ((float)(System.currentTimeMillis()-now)/1000) + " s");
        ///////////////////////////////////////////////////

        ////////////// Tree creating //////////////////////
        Future<Integer> learnWithHoldoutSetFuture = executorService.submit(() -> learnBestTreeSize(L,P,imageListForGraph,conditionList));
        Future<treeNode> learnFuture = executorService.submit(() -> (treeNode)createTree((int)Math.pow(2,L),imageListForGraph,null,conditionList));
        treeNode tree;
        try {
            Integer bestTreeSize = learnWithHoldoutSetFuture.get();
            tree = learnFuture.get();
            trimTree(tree,bestTreeSize);
            Utilities.writeTreeToFile(tree,outputFileName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalArgumentException(e);
        } finally {
            executorService.shutdown();
        }
        ///////////////////////////////////////////////////

        System.out.println("Learning took " + ((float)((System.currentTimeMillis()-now))/1000) + "s");

        System.out.println("num: " + trainImageList.size());
        System.out.println("error: " + (int)(100-(applyTreeOnDataSet(tree,imageListForGraph)*100)));
        System.out.println("size: " + (Utilities.size(tree)-1)/2);
    }



    public static double predict(String treeFilePath,String testSetPath,PrintWriter out) {
        treeNode treeTest = Utilities.readTreeFromFile(treeFilePath);
        ArrayList<Image> testSet = CSVReader.readImages(testSetPath);
        out.println("Tree size is " + (Utilities.size(treeTest)-1)/2);
        double ret = applyTreeOnDataSet(treeTest,testSet);
        out.println("Score on test set is " + applyTreeOnDataSet(treeTest,testSet));
        System.out.println("Done iteration");
        // TODO: remove return and change to void.
        // TODO: add print of all the predicted labels.
        return ret;
    }

    public static double predictWithConfusionMatrix(String treeFilePath,String testSetPath,PrintWriter out) {
        Integer[][] confMatxrix = new Integer[10][10];
        for (int i = 0; i<10; i++){
            for (int j = 0; j<10; j++) {
                confMatxrix[i][j] = 0;
            }
        }
        treeNode treeTest = Utilities.readTreeFromFile(treeFilePath);
        ArrayList<Image> testSet = CSVReader.readImages(testSetPath);
        out.println("Tree size is " + (Utilities.size(treeTest)-1)/2);
        double ret = applyTreeOnDataSetWithConfusionMatrix(treeTest,testSet,confMatxrix);
        out.println("Score on test set is " + ret);
        for (int i = 0; i<10; i++){
            for (int j = 0; j<10; j++) {
                out.print(confMatxrix[i][j] + " ");
            }
            out.print('\n');
        }
        System.out.println("Done iteration");
        // TODO: remove return and change to void.
        // TODO: add print of all the predicted labels.
        return ret;
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


    // Create a tree with a given size. According to the learnVersion - can either return the best tree size or return the tree itself.
    private static Object createTree(int numOfIterations, ArrayList<Image> trainingSet, ArrayList<Image> validationSet, ArrayList<condition> conditionList) {
        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(numOfIterations,new virtualTreeComperator());
        int[] trainingFrequencies = countImageFrequencies(trainingSet);
        int label = Utilities.getIndexOfBiggestElement(trainingFrequencies);
        double entropy = Utilities.calcEntropy(trainingFrequencies,trainingSet.size());

        treeNode treeToReturn = new treeNode(trainingSet,entropy,label);
        virtualTreePriorityQueue.add(createChildren(treeToReturn, conditionList));
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        double currentBestScore = -1.0;
        Integer bestTreeSize = -1;
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
                executorService.shutdown();
                e.printStackTrace();
            }

            // Validation set exist means that the method should return tree size.
            if (validationSet != null) {
                // Check if the current tree is better than the current best tree.
                if (Utilities.powerOf2(i)) {
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
    public static double applyTreeOnDataSet(treeNode tree, ArrayList<Image> dataSet) {
        int numOfSuccesses = 0;
        for (Image img: dataSet) {
            if (applyTreeOnImage(tree,img)) {
                numOfSuccesses++;
            }
        }
        return (double)(numOfSuccesses)/dataSet.size();
    }

    // Gets a data set and a tree, applies the tree on the data set and returns the success percentage.
    public static double applyTreeOnDataSetWithConfusionMatrix(treeNode tree, ArrayList<Image> dataSet, Integer[][] confMatrix) {
        int numOfSuccesses = 0;
        for (Image img: dataSet) {
            if (applyTreeOnImageWithConfusionMatrix(tree,img,confMatrix)) {
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

    public static boolean applyTreeOnImageWithConfusionMatrix(treeNode tree, Image img, Integer[][] confMatrix) {
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
        confMatrix[img.getLabel()][tree.getExpectedLabel()]++;
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
