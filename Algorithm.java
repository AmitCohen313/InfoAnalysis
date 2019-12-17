import java.util.*;

public class Algorithm {

    public static void main(String[] args) {

        long now = System.currentTimeMillis();
        ArrayList<Image> globalImageList = CSVReader.readImages("deskewed_mnist_train.csv");
        ArrayList<Image> testSet = CSVReader.readImages("deskewed_mnist_test.csv");
        System.out.println("Reading image took " + ((float)(System.currentTimeMillis()-now)/1000) + " s");
        now = System.currentTimeMillis();
//        ArrayList<condition> conditionList = ConditionGroup.getConditions(Integer.parseInt(args[0]));
        ArrayList<condition> conditionList = ConditionGroup.getConditions(7,globalImageList);
        //TODO: maybe remove newConditionList
        ArrayList<condition> newConditionList = Utilities.getRandomElements(conditionList,1000);

        System.out.println("Conditions creation took " + (System.currentTimeMillis()-now) + " ms");
        now = System.currentTimeMillis();

        treeNode tree = newExecuteAlgo(11,35,globalImageList,newConditionList);
        System.out.println("Creating the tree took " + ((float)((System.currentTimeMillis()-now))/1000) + "s");
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
            System.out.print(i + ".");
            virtualTree virtualTree = virtualTreePriorityQueue.poll();
            treeNode nodeToReplace = virtualTree.pointerToLeaf;
            nodeToReplace.replaceLeafByTree(virtualTree.tripletsTree);
            nodeToReplace.setTimeStamp(i);

//            Callable<virtualTree> callableLeftTree = () -> {
//                virtualTree left = calcChildren(nodeToReplace.getLeft(), conditionList);
//                return left;
//            };
//            Callable<virtualTree> callableRightTree = () -> {
//                virtualTree right = calcChildren(nodeToReplace.getRight(), conditionList);
//                return right;
//            };
//
//            ExecutorService e = Executors.newFixedThreadPool(2);
//            Future<virtualTree> leftTree = e.submit(callableLeftTree);
//            Future<virtualTree> rightTree = e.submit(callableRightTree);
            virtualTree leftTree = calcChildren(nodeToReplace.getLeft(), conditionList);
            virtualTree rightTree = calcChildren(nodeToReplace.getRight(), conditionList);
//            try {
            virtualTreePriorityQueue.add(leftTree);
            virtualTreePriorityQueue.add(rightTree);
//            } catch (Exception b) {
//
//            }


            // Check if the current tree is better than the current best tree.
            if (powerOf2(i)){
                double currentScore = applyTreeOnDataSet(t,validationSet);
                if (currentScore>currentBestScore) {

                    bestTreeSize = i;
                    currentBestScore = currentScore;
                }
            }
        }

        System.out.print("\n");
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
//             Maybe the new takes a lot of time
            ArrayList<Image> passedCond = new ArrayList<>(father.getImageList().size()/2);
            ArrayList<Image> failedCond = new ArrayList<>(father.getImageList().size()/2);

//            List<Image> passedCond = Collections.synchronizedList(new ArrayList<>());
//            List<Image> failedCond = Collections.synchronizedList(new ArrayList<>());

            int[] passedFrequencies = new int[10];
            int[] failedFrequencies = new int[10];
//
//            List<Image> images = father.getImageList();
//            class temp extends Thread {
//                private Image img;
//                private boolean passed;
//                private int label;
//                public temp(Image img) {
//                    this.img = img;
//                    this.label = img.getLabel();
//                }
//                public void run() {
//                    passed = cond.applyCondition(img);
//                }
//                public int getLabel(){
//                    return label;
//                }
//                public boolean isPassed() {
//                    return passed;
//                }
//            }
//
//            ExecutorService e = Executors.newFixedThreadPool(20);
//            List<temp> threadlist = new ArrayList<>();
//            for(Image img : images) {
//                threadlist.add(new temp(img));
//            }
//            int i =0;
//            ThreadGroup tg = new ThreadGroup("main");
//            while ( i < threadlist.size()){
//                if (tg.activeCount() < 24) {
//                    Thread t = threadlist.get(i);
//                    t.start();
//                    i++;
//                } else {
//                    try {Thread.sleep(100);} /*wait 0.1 second before checking again*/
//                    catch (InterruptedException e3) {e3.printStackTrace();}
//                }
//            }
//            while(tg.activeCount()>0)
//            {
//                try {Thread.sleep(100);}
//                catch (InterruptedException e2) {e2.printStackTrace();}
//            }
//
//            for (i=0;i<threadlist.size();i++)
//            {
//                temp t = threadlist.get(i);
//                if (t.isPassed()) {
//                    passedCond.add(t.img);
//                    passedFrequencies[t.getLabel()]++;
//                } else {
//                    failedCond.add(t.img);
//                    failedFrequencies[t.getLabel()]++;
//                }
//            }
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
