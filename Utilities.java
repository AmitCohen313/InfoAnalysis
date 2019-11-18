import java.util.ArrayList;
import java.util.Random;

public class Utilities {
    // Returns a random sublist of the the given list (Those elements are removed from the original list).
    public static <T> ArrayList<T> getRandomElements(ArrayList<T> list, int numberOfElements)
    {
        Random rand = new Random();
        ArrayList<T> newList = new ArrayList<>();
        for (int i = 0; i < numberOfElements; i++) {

            // take a random index between 0 to size
            // of given List
            int randomIndex = rand.nextInt(list.size());

            // add element in temporary list
            newList.add(list.get(randomIndex));

            // Remove selected element from original list
            list.remove(randomIndex);
        }
        return newList;
    }

    public static int calcIndexOfBiggestElementInArray(int[] arr) {
        int max = 0;
        for (int i = 1; i < arr.length ; i++) {
            if (arr[i] > arr[max]) {
                max = i;
            }
        }
        return max;
    }

    /*TODO ask sivan about the log base*/
    // Calc the entropy based on the given function
    public static double calcEntropy(int[] frequencies, int totalAmount) {
        double result = 0.0;
        if (totalAmount != 0) {
            for (int i = 0; i < 10; i++) {
                result += (((float) frequencies[i] / totalAmount) * Math.log((float) totalAmount / frequencies[i]));
            }
        }
        return result;
    }

    // Creates a list of all the leaves in a given tree.
    public static ArrayList<treeNode> extractLeaves (treeNode tree) {
        ArrayList<treeNode> leafList = new ArrayList<>();
        extractLeaves(tree,leafList);
        return leafList;
    }

    // This method extracts the leaves from the tree
    private static void extractLeaves (treeNode tree, ArrayList<treeNode> leafList) {
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
}
