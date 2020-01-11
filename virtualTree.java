public class virtualTree {
    public treeNode newTree;
    public double informationGain;
    public treeNode pointerToLeaf;

    public virtualTree(treeNode newTree, treeNode pointerToLeaf, double informationGain) {
        this.newTree = newTree;
        this.informationGain = informationGain;
        this.pointerToLeaf = pointerToLeaf;
    }

    // This method swaps the leaf in the original tree with the new tree by performing deep copy.
    // This method is needed because we want to replace the leaf with the tree only when it is the appropriate time.
    public void performSwap(){
        pointerToLeaf.replaceLeafByTree(newTree);
    }
}
