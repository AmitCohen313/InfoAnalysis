import java.util.List;

public class treeNode {

    private treeNode left;
    private treeNode right;
    private double entropy;
    private int expectedLabel;
    private int[] labelFrequencies;
    private condition condition;
    private List<Image> imageList;

    public treeNode(List<Image> imageList, double entropy , int expectedLabel) {
        this.left = null;
        this.right = null;
        this.imageList = imageList;
        this.expectedLabel = expectedLabel;
        this.entropy = entropy;
    }

    public treeNode(List<Image> imageList, condition condition, treeNode left, treeNode right) {
        this.imageList = imageList;
        this.condition = condition;
        this.left = left;
        this.right = right;
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


    // TODO: Find better way to do this.
    public void replaceLeafByTree (treeNode tree) {
        this.left = tree.left;
        this.right = tree.right;
        this.entropy = tree.entropy;
        this.expectedLabel = tree.expectedLabel;
        this.labelFrequencies = tree.labelFrequencies;
        this.condition = tree.condition;
        this.imageList = tree.imageList;
    }

}
