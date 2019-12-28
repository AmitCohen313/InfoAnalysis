import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class treeNode {

    public treeNode left;
    public treeNode right;
    private double entropy;
    private int expectedLabel;
    private int[] labelFrequencies;
    public condition condition;
    public List<Image> imageList;
    private int timeStamp;

    public treeNode(){
        this.left=null;
        this.right=null;
    }

    public treeNode(condition cond, int expectedLabel){
        this.condition = cond;
        this.expectedLabel = expectedLabel;
    }

    public treeNode(List<Image> imageList, double entropy , int expectedLabel) {
        this.left = null;
        this.right = null;
        this.imageList = imageList;
        this.entropy = entropy;
        this.expectedLabel = expectedLabel;
        this.timeStamp = Integer.MIN_VALUE;

    }


    public treeNode(List<Image> imageList, condition condition, treeNode left, treeNode right, int expectedLabel) {
        this.imageList = imageList;
        this.condition = condition;
        this.left = left;
        this.right = right;
        this.expectedLabel = expectedLabel;
        this.timeStamp = Integer.MIN_VALUE;
    }


    public boolean isLeaf() {
        return left == null && right == null;
    }

    public double getEntropy() {
        return entropy;
    }

    public treeNode getLeft() {
        return left;
    }

    public int getTimeStamp() {
        return timeStamp;
    }

    public void setLeft(treeNode left) {
        this.left = left;
    }

    public void setRight(treeNode right) {
        this.right = right;
    }

    public treeNode getRight() {
        return right;
    }


    public condition getCondition() {
        return condition;
    }

    public int getExpectedLabel() {
        return expectedLabel;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    // Encodes a tree to a single string.
    public String toString() {
        ArrayList<String> list = new ArrayList<>();
        LinkedList<treeNode> q = new LinkedList<>();
        q.offer(this);

        while (!q.isEmpty()) {
            treeNode h = q.poll();
            if (h == null) {
                list.add("#");
            } else {
                if (h.condition!= null) {
                    list.add("C!" + h.condition.toString() +"!" + h.expectedLabel);
                } else{
                    list.add("L!" + h.expectedLabel);
                }
                q.offer(h.left);
                q.offer(h.right);
            }
        }

        return String.join(",", list);
    }


    // TODO: Find better way to do this.
    public void replaceLeafByTree (treeNode tree) {
        this.left = tree.left;
        this.right = tree.right;
        this.entropy = tree.entropy;
        this.imageList = tree.imageList;
        this.expectedLabel = tree.expectedLabel;
        this.labelFrequencies = tree.labelFrequencies;
        this.condition = tree.condition;
        this.timeStamp = tree.timeStamp;
    }

}
