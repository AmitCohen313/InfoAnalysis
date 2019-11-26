public class virtualTree {
    public treeNode tripletsTree;
    public double informationGain;
    public treeNode pointerToLeaf;

    public virtualTree(treeNode tripletsTree, treeNode pointerToLeaf,double informationGain) {
        this.tripletsTree = tripletsTree;
        this.informationGain = informationGain;
        this.pointerToLeaf = pointerToLeaf;
    }
}
