/**
 *
 */
package cz.rank.vsfs.btree;

/**
 * @author rank
 * 
 */
public class Node {

    boolean leaf = false;
    private int keysCount = 0;
    private final int degree;
    private Node[] children;
    private double[] keys;

    public Node(final int degree) {
        this.degree = degree;

        children = new Node[maxChildren()];
        keys = new double[maxKeys()];
    }

    /**
     * @return
     */
    public int getDegree() {
        return degree;
    }

    /**
     * @param i
     * @return
     */
    public Node getChild(final int i) {
        return children[i];
    }

    /**
     * @param i
     * @return
     */
    public double getKey(final int i) {
        return keys[i];
    }

    /**
     * @return
     */
    public int getKeysCount() {
        return keysCount;
    }

    /**
     * @return
     */
    public boolean isFull() {
        return keysCount == maxKeys();
    }

    /**
     * @return
     */
    public boolean isLeaf() {
        return leaf;
    }

    /**
     * @return
     */
    private int maxChildren() {
        return maxKeys() + 1;
    }

    /**
     * @return
     */
    private int maxKeys() {
        return 2 * degree - 1;
    }

    /**
     * @param i
     * @param r
     */
    public void setChild(final int index, final Node r) {
        children[index] = r;
    }

    /**
     * @param j
     * @param key
     */
    public void setKey(final int j, final double key) {
        keys[j] = key;
    }

    /**
     * @param i
     */
    public void setKeysCount(final int keysCount) {
        this.keysCount = keysCount;
    }

    /**
     * @param b
     */
    public void setLeaf(final boolean leaf) {
        this.leaf = leaf;
    }

}
