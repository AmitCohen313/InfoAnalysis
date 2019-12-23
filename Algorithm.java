import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Algorithm {
    public static void main(String[] args) {
        long now = System.currentTimeMillis();
        int conditionVer = 1;
        int L = 15;
        int P = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        Future<ArrayList<Image>> readImagesFuture = executorService.submit(() -> CSVReader.readImages("deskewed_mnist_train.csv"));
        Future<ArrayList<Image>> readTestImagesFuture = executorService.submit(() -> CSVReader.readImages("deskewed_mnist_test.csv"));
        Future<ArrayList<condition>> createConditionsFuture = executorService.submit(() -> ConditionGroup.getConditions(conditionVer));

        ArrayList<condition> conditionList;
        ArrayList<Image> trainImageList;
        ArrayList<Image> testImageList;
        try {
            trainImageList = readImagesFuture.get();
            conditionList = createConditionsFuture.get();
            testImageList = readTestImagesFuture.get();
        } catch (Exception e){
            throw new IllegalArgumentException(e);
        }

        System.out.println("Reading images took " + ((float)(System.currentTimeMillis()-now)/1000) + " s");
        now = System.currentTimeMillis();
//        ArrayList<condition> conditionList = ConditionGroup.getConditions(Integer.parseInt(args[0]));
        //TODO: maybe remove newConditionList
        System.out.println("There are " + conditionList.size() + " conditions.");
        ArrayList<condition> newConditionList = new ArrayList<condition>(conditionList);//Utilities.getRandomElements(conditionList,1000);

        Future<Integer> learnWithHoldoutSetFuture = executorService.submit(() -> learnWithHoldoutSet(L,P,trainImageList,conditionList));
        Future<treeNode> learnFuture = executorService.submit(() -> learn((int)Math.pow(2,L),trainImageList,newConditionList));
        treeNode tree;
        try {
            Integer bestTreeSize = learnWithHoldoutSetFuture.get();
            tree = learnFuture.get();
            executorService.shutdown();
            System.out.println("\nScore on test set before trim is is " + applyTreeOnDataSet(tree,testImageList));
            trimTree(tree,bestTreeSize);
        // TODO: check which exception.
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        System.out.println("Learning took " + ((float)((System.currentTimeMillis()-now))/1000) + "s");
        now = System.currentTimeMillis();

        System.out.println("Running data set on the tree took " + (System.currentTimeMillis()-now));

        System.out.println("Tree size is " + size(tree));
        System.out.println("score is " + applyTreeOnDataSet(tree,trainImageList));
        System.out.println("score on test set is " + applyTreeOnDataSet(tree,testImageList));
    }

    public static treeNode learn(int treeSize, ArrayList<Image> globalImageList, ArrayList<condition> conditionList){
        System.out.println("Starting learning");
        PriorityQueue<virtualTree> virtualTreePriorityQueue = new PriorityQueue<>(treeSize,new virtualTreeComperator());
        // TODO: combine the calculations.
        // TODO: move to init method
        int[] trainingFrequencies = countImageFrequencies(globalImageList);
        int label = Utilities.getIndexOfBiggestElement(trainingFrequencies);
        double entropy = Utilities.calcEntropy(trainingFrequencies,globalImageList.size());
        treeNode t;

        t = new treeNode(0,globalImageList.size()-1,entropy,label);
        virtualTree vt = calcChildren(t, globalImageList, conditionList);

        virtualTreePriorityQueue.add(vt);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        for (int i = 1; i <= treeSize; i++) {
            //System.out.print(i + ".");
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            if (virtualTree == null) {
                continue;
            }
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            nodeToReplace.setTimeStamp(i);
            Future<virtualTree> leftFuture = executorService.submit(() -> calcChildren(nodeToReplace.getLeft(), globalImageList, conditionList));
            Future<virtualTree> rightFuture = executorService.submit(() -> calcChildren(nodeToReplace.getRight(), globalImageList, conditionList));
            try {
                virtualTree left = leftFuture.get();
                if (left.informationGain!= -1.0) {
                    virtualTreePriorityQueue.add(left);
                }
                virtualTree right = rightFuture.get();
                if (right.informationGain!= -1.0) {
                    virtualTreePriorityQueue.add(right);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        executorService.shutdown();

        return t;
    }


    public static int learnWithHoldoutSet(int L, double P, ArrayList<Image> globalImageList, ArrayList<condition> conditionList) {
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

        treeNode t = new treeNode(0,trainingSet.size()-1,entropy,label);
        virtualTree vt = calcChildren(t, trainingSet, conditionList);
        virtualTreePriorityQueue.add(vt);
        ExecutorService executorService = Executors.newFixedThreadPool(2);

        double currentBestScore = -1.0;
        int bestTreeSize = -1;
        for (int i = 1; i <= maxQueueSize; i++) {
            //System.out.print(i + ".");
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            //nodeToReplace.setTimeStamp(i);


            Future<virtualTree> leftFuture = executorService.submit(() -> calcChildren(nodeToReplace.getLeft(), trainingSet, conditionList));
            Future<virtualTree> rightFuture = executorService.submit(() -> calcChildren(nodeToReplace.getRight(), trainingSet, conditionList));

            try {
                virtualTree left = leftFuture.get();
                if (left.informationGain != -1.0) {
                    virtualTreePriorityQueue.add(left);
                }
                virtualTree right = rightFuture.get();
                if (right.informationGain != -1.0) {
                    virtualTreePriorityQueue.add(right);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            // Check if the current tree is better than the current best tree.
            if (powerOf2(i)) {
                double currentScore = applyTreeOnDataSet(t, validationSet);
                if (currentScore > currentBestScore) {
                    bestTreeSize = i;
                    currentBestScore = currentScore;
                }
            }
        }
        executorService.shutdown();

        System.out.print("\n");
        return bestTreeSize;

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
        int v11Cnt = 0;
        int v10Cnt = 0;
        int v5Cnt = 0;
        int v6Cnt = 0;
        int v9Cnt = 0;
        while (!tree.isLeaf()) {
            if (tree.getCondition() instanceof V11_Condition){
                v11Cnt++;
            } else if (tree.getCondition() instanceof V10_Condition) {
                v10Cnt++;
            } else if (tree.getCondition() instanceof V5_Condition) {
                v5Cnt++;
            } else if (tree.getCondition() instanceof V6_Condition) {
                v6Cnt++;
            } else if (tree.getCondition() instanceof V9_Condition) {
                v9Cnt++;
            }
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
        //System.out.println("Number of v11: " +v11Cnt + ", number of v10: " + v10Cnt + ", number of v9: "+ v9Cnt + ", number of v6: " + v6Cnt+ ", number of v5: " + v5Cnt);
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


    public static virtualTree calcChildren(treeNode father, ArrayList<Image> imageList, List<condition> conditionList) {
        double maxInfoGain = -1.0;
        treeNode currentBestTree = father;
        // TODO: remove this line.
        condition chosenCond = null;
        for (condition cond : conditionList) {
            int[] passedFrequencies = new int[10];
            int[] failedFrequencies = new int[10];
            AtomicIntegerArray concurrentPassedFrequencies = new AtomicIntegerArray(10);
            AtomicIntegerArray concurrentFailedFrequencies = new AtomicIntegerArray(10);
//            int numOfPassed = 0;
//            int numOfFailed = 0;
            AtomicInteger numOfPassed = new AtomicInteger(0);
            AtomicInteger numOfFailed = new AtomicInteger(0);

            int leftIndex = father.getStartIndex();
            int rightIndex = father.getEndIndex();
            int middleIndex = (leftIndex + rightIndex)/2;
//            int middleIndex = leftIndex + (rightIndex - leftIndex)/4;
            int middleIndex2 = leftIndex + (rightIndex - leftIndex)*2/4;
            int middleIndex3 = leftIndex + (rightIndex - leftIndex)*3/4;
            // TODO: Remove!
            if ((rightIndex - leftIndex) > 200000){
                Thread t1 = new Thread(()->
                    computeConditionFrequencies(leftIndex,middleIndex,imageList,cond,concurrentPassedFrequencies,concurrentFailedFrequencies,numOfPassed,numOfFailed)
                );
                Thread t2 = new Thread(()->
                    computeConditionFrequencies(middleIndex,rightIndex,imageList,cond,concurrentPassedFrequencies,concurrentFailedFrequencies,numOfPassed,numOfFailed)
                );
//                Thread t3 = new Thread(()->
//                        computeConditionFrequencies(middleIndex2,middleIndex3,imageList,cond,concurrentPassedFrequencies,concurrentFailedFrequencies,numOfPassed,numOfFailed)
//                );
//                Thread t4 = new Thread(()->
//                        computeConditionFrequencies(middleIndex3,rightIndex,imageList,cond,concurrentPassedFrequencies,concurrentFailedFrequencies,numOfPassed,numOfFailed)
//                );
                t1.start();
                t2.start();
//                t3.start();
//                t4.start();
                try {
                    t1.join();
                    t2.join();
//                    t3.join();
//                    t4.join();
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            } else {
                computeConditionFrequencies(leftIndex,rightIndex,imageList,cond,concurrentPassedFrequencies,concurrentFailedFrequencies,numOfPassed,numOfFailed);
            }

            for (int i = 0; i<10; i++){
                passedFrequencies[i] = concurrentPassedFrequencies.get(i);
                failedFrequencies[i] = concurrentFailedFrequencies.get(i);
            }
//            if ((rightIndex-leftIndex>4000) && (numOfFailed.get() == 0 || numOfPassed.get() == 0)){
//                    iter.remove();
//            }

            double passedEntropy = Utilities.calcEntropy(passedFrequencies, numOfPassed.get());
            double failedEntropy = Utilities.calcEntropy(failedFrequencies, numOfFailed.get());
            double currentInfoGain = calcWeightedInfoGain(passedEntropy,failedEntropy,numOfPassed.get(),numOfFailed.get(),father);
            if (currentInfoGain > maxInfoGain) {
                chosenCond = cond;
                maxInfoGain = currentInfoGain;
                treeNode right = new treeNode(leftIndex+numOfFailed.get()-1,father.getEndIndex(), passedEntropy,Utilities.getIndexOfBiggestElement(passedFrequencies));
                treeNode left = new treeNode(father.getStartIndex(),leftIndex+numOfFailed.get(), failedEntropy,Utilities.getIndexOfBiggestElement(failedFrequencies));
                currentBestTree = new treeNode(father.getStartIndex(),father.getEndIndex(), cond, left, right, father.getExpectedLabel());
            }
        }

        //TODO: remove
        if (chosenCond == null){
            System.out.print(".Did not find a rule.");
        } else {

            sortImagesByCondition(imageList, chosenCond, father.getStartIndex(), father.getEndIndex());
            // TODO: check if it is ok
            //conditionList.remove(chosenCond);
        }
        return new virtualTree(currentBestTree,father,maxInfoGain);
    }

    private static void sortImagesByCondition(ArrayList<Image> images,condition cond, int leftIndex, int rightIndex){
        Image temp;
        // Used to prevent redundant calculations.
        Boolean leftPred = null;
        Boolean rightPred = null;
        // Sort the array by the chosen condition (failed condition and then passed condition).
        while(leftIndex<rightIndex+1){
            if (leftPred == null) {
                leftPred = cond.applyCondition(images.get(leftIndex));
            }
            if (rightPred == null) {
                rightPred = cond.applyCondition(images.get(rightIndex));
            }
            if (leftPred && !rightPred){
                // Thread safe because working on different part of the list.
                temp = images.get(rightIndex);
                images.set(rightIndex,images.get(leftIndex));
                images.set(leftIndex,temp);
                leftPred = null;
                rightPred = null;
                leftIndex++;
                rightIndex--;
            }
            else if (!rightPred){
                leftIndex++;
                leftPred = null;
            } else if (leftPred) {
                rightIndex--;
                rightPred = null;
            } else {
                leftIndex++;
                rightIndex--;
                leftPred = null;
                rightPred = null;
            }
        }
    }

    private static double calcWeightedInfoGain(double passedEntropy, double failedEntropy, int numOfPassed, int numOfFailed, treeNode father){
        double relativePassedEntropy = (double)numOfPassed * passedEntropy / (father.getEndIndex()+1-father.getStartIndex());
        double relativeFailedEntropy = (double)numOfFailed * failedEntropy / (father.getEndIndex()+1-father.getStartIndex());
        double currentInfoGain = father.getEntropy() - relativePassedEntropy - relativeFailedEntropy;
//        if (currentInfoGain <= -1.0) {
//            throw new IllegalArgumentException("info gain is negative!");
//        }
        double currentWeightedInfoGain = currentInfoGain * (father.getEndIndex()+1-father.getStartIndex());
        return currentWeightedInfoGain;
    }

    private static void computeConditionFrequencies(int startIndex, int endIndex, ArrayList<Image> images, condition cond,AtomicIntegerArray passedFrequencies, AtomicIntegerArray failedFrequencies, AtomicInteger passedAmount, AtomicInteger failedAmount){
        for (int i = startIndex; i < endIndex; i++) {
            Image img = images.get(i);
            if (cond.applyCondition(img)) {
                passedFrequencies.incrementAndGet(img.getLabel());
                passedAmount.incrementAndGet();
            } else {
                failedFrequencies.incrementAndGet(img.getLabel());
                failedAmount.incrementAndGet();
            }
        }
    }

}
