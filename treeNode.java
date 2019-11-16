import java.util.List;

public class treeNode {

    private treeNode left;
    private treeNode right;
    private double entropy;
    private int expectedLabel;
    private int[] frequencies;
    private condition condition;
    private List<Image> imageList;

    public treeNode(List<Image> imageList, int expectedLabel) {
        this.left = null;
        this.right = null;
        this.imageList = imageList;
        this.expectedLabel = expectedLabel;
    }
    public treeNode(List<Image> imageList, double entropy) {
        this.left = null;
        this.right = null;
        this.imageList = imageList;
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

    public void setLeft(treeNode left) {
        this.left = left;
    }

    public void setRight(treeNode right) {
        this.right = right;
    }

    public double getEntropy() {
        return entropy;
    }

    public void setEntropy(double entropy) {
        this.entropy = entropy;
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

    public void setExpectedLabel(int label) {
        this.expectedLabel = label;
    }

}
