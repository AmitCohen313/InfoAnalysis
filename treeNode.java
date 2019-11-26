import java.util.List;

public class treeNode {

    private treeNode left;
    private treeNode right;
    private double entropy;
    private int expectedLabel;
    private int[] labelFrequencies;
    private condition condition;
    private List<Image> imageList;
    private int timeStamp;

    public treeNode(){
        this.left=null;
        this.right=null;
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

    public List<Image> getImageList() {
        return imageList;
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


    // TODO: Find better way to do this.
    public void replaceLeafByTree (treeNode tree) {
        this.left = tree.left;
        this.right = tree.right;
        this.entropy = tree.entropy;
        this.expectedLabel = tree.expectedLabel;
        this.labelFrequencies = tree.labelFrequencies;
        this.condition = tree.condition;
        this.imageList = tree.imageList;
        this.timeStamp = tree.timeStamp;
    }

}
