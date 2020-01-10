import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Utilities {

    // Returns a random sublist of the the given list (Those elements are removed from the original list).
    public static <T> ArrayList<T> getRandomElements(ArrayList<T> list, int numberOfElements) {
        Random rand = new Random();
        ArrayList<T> newList = new ArrayList<>();
        for (int i = 0; i < numberOfElements; i++) {
            int randomIndex = rand.nextInt(list.size());
            newList.add(list.get(randomIndex));
            list.remove(randomIndex);
        }
        return newList;
    }

    public static int getIndexOfBiggestElement(int[] arr) {
        int max = 0;
        for (int i = 1; i < arr.length ; i++) {
            if (arr[i] > arr[max]) {
                max = i;
            }
        }
        return max;
    }

    public static double calcEntropy(int[] frequencies, int totalAmount) {
        double result = 0.0;
        if (totalAmount != 0) {
            for (int i = 0; i < 10; i++) {
                if (frequencies[i] != 0) {
                    result += (((float) frequencies[i] / totalAmount) * Math.log((float) totalAmount / frequencies[i]));
                }
            }
        }
        return result;
    }

    public static double calcWeightedInfoGain(double passedEntropy, double failedEntropy, int numOfPassed, int numOfFailed, treeNode father){
        double relativePassedEntropy = (double)numOfPassed * passedEntropy / father.imageList.size();
        double relativeFailedEntropy = (double)numOfFailed * failedEntropy / father.imageList.size();
        double currentInfoGain = father.getEntropy() - relativePassedEntropy - relativeFailedEntropy;
        if (currentInfoGain <= -1.0) {
            throw new IllegalArgumentException("info gain is negative!");
        }
        double currentWeightedInfoGain = currentInfoGain * father.imageList.size();
        return currentWeightedInfoGain;
    }

    public static int calcIndices(int x, int y){
        return 28*y+x;
    }

    public static class Triplet<T1,T2,T3> {
        public T1 first;
        public T2 second;
        public T3 third;
        public Triplet(T1 t1, T2 t2, T3 t3) {
            first = t1;
            second = t2;
            third = t3;
        }
    }

    public static class Pair<T1,T2> {
        public T1 first;
        public T2 second;
        public Pair(T1 t1, T2 t2) {
            first = t1;
            second = t2;
        }
    }

    public static void writeTreeToFile(treeNode tree, String filepath) {
        try (PrintWriter out = new PrintWriter(filepath)) {
            out.print(tree.toString());
        } catch (Exception e) {
            System.out.println("IOException is caught");
            e.printStackTrace();
        }
    }

    public static treeNode readTreeFromFile(String path) {
        String data = readLineByLine(path);
        String[] arr = data.split(",");
        if (arr[0].equals("#")) {
            return null;
        }

        treeNode root = stringToNode(arr[0]);
        LinkedList<treeNode> q = new LinkedList<>();
        q.offer(root);

        int i = 1;

        while (!q.isEmpty()) {
            treeNode h = q.poll();
            if (h != null) {
                treeNode left = null;
                if (!arr[i].contains("#")) {
                    left = stringToNode(arr[i]);
                }
                h.left = left;
                q.offer(left);
                i++;

                treeNode right = null;
                if (!arr[i].contains("#")) {
                    right = stringToNode(arr[i]);
                }
                h.right = right;
                q.offer(right);
                i++;
            }
        }
        return root;
    }



    public static treeNode stringToNode(String nodeString) {
        String[] node = nodeString.split("!");
        condition cond = null;
        if (node[0].equals("C")) {
            switch (node[1]) {
                case "1":
                    cond = V1_condition.fromString(Arrays.copyOfRange(node, 2, node.length));
                    break;
                case "2":
                    cond = V2_condition.fromString(Arrays.copyOfRange(node, 2, node.length));
                    break;
                case "3":
                    cond = V3_condition.fromString(Arrays.copyOfRange(node, 2, node.length));
                    break;
                case "4":
                    cond = V4_condition.fromString(Arrays.copyOfRange(node, 2, node.length));
                    break;
            }
        }
        int label = Integer.parseInt(node[node.length-1]);
        return new treeNode(cond,label);
    }

    public static String readLineByLine(String filePath)
    {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines( Paths.get(filePath), StandardCharsets.UTF_8))
        {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    public static int size(treeNode node)
    {
        if (node == null)
            return 0;
        else
            return(size(node.getLeft()) + 1 + size(node.getRight()));
    }

    public static boolean powerOf2(int number){
        return (number > 0) && ((number & (number - 1)) == 0);
    }

}
