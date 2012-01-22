/**
 *
 */
package cz.rank.vsfs.btree;

import java.lang.reflect.Array;

/**
 * @author rank
 *
 */
public class Node<T extends Comparable<? super T>> {

    boolean leaf = false;
    private int keysCount = 0;
    private final int degree;
    private Node<T>[] children;
    private T[] keys;

    @SuppressWarnings("unchecked")
    public Node(final int degree) {
        if (degree < 2) {
            throw new IllegalArgumentException("Node degree must be at least 2. Current degree: " + degree);
        }

        this.degree = degree;

        children = new Node[maxChildren()];
        keys = (T[]) Array.newInstance(Comparable.class, maxKeys());
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
    public Node<T> getChild(final int i) {
        return children[i];
    }

    /**
     * @param i
     * @return
     * @return
     */
    public T getKey(final int i) {
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
    public void setChild(final int index, final Node<T> r) {
        children[index] = r;
    }

    /**
     * @param j
     * @param key
     */
    public void setKey(final int j, final T key) {
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

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < getKeysCount(); i++) {
            if (!isLeaf()) {
                builder.append(children[i]).append(',');
            }
            builder.append(keys[i].toString()).append(',');
        }

        if (!isLeaf()) {
            builder.append(children[getKeysCount()]).append(',');
        }
        builder.append("]");
        return builder.toString();
    }

}
