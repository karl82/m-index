/**
 *
 */
package cz.rank.vsfs.btree;

/**
 * @author rank
 *
 */
public class BPlusTree<T extends Comparable<T>> {
    public static <T extends Comparable<T>> void splitChild(final Node<T> node, final int index, final Node<T> fullNode) {
        int nodeDegree = fullNode.getDegree();
        Node<T> z = new Node<T>(nodeDegree);

        z.leaf = fullNode.leaf;
        z.setKeysCount(nodeDegree - 1);

        for (int j = 0; j < nodeDegree - 1; j++) {
            z.setKey(j, fullNode.getKey(j + nodeDegree));
        }

        if (!fullNode.isLeaf()) {
            for (int j = 0; j < nodeDegree; j++) {
                z.setChild(j, fullNode.getChild(j + nodeDegree));
            }
        }

        fullNode.setKeysCount(nodeDegree - 1);

        for (int j = node.getKeysCount(); j > index + 1; j--) {
            node.setChild(j + 1, node.getChild(j));
        }

        node.setChild(index + 1, z);

        for (int j = node.getKeysCount() - 1; j > index; j--) {
            node.setKey(j + 1, node.getKey(j));
        }

        node.setKey(index, fullNode.getKey(nodeDegree - 1));

        node.setKeysCount(node.getKeysCount() + 1);
    }

    private Node<T> root;

    private final int degree;

    /**
     * @param i
     */
    public BPlusTree(final int degree) {
        this.degree = degree;

        root = NodeFactory.allocateRoot(getDegree());
    }

    /**
     * @return the degree
     */
    public int getDegree() {
        return degree;
    }

    /**
     * @param d
     */
    public void insert(final T key) {
        Node<T> r = root;

        if (r.isFull()) {
            Node<T> s = new Node<T>(getDegree());

            root = s;
            s.setLeaf(false);
            s.setKeysCount(0);

            s.setChild(0, r);

            BPlusTree.splitChild(s, 0, r);
            insertNonFull(s, key);
        } else {
            insertNonFull(r, key);
        }
    }

    /**
     * @param s
     * @param key
     */
    private void insertNonFull(final Node<T> s, final T key) {
        int i = s.getKeysCount() - 1;

        if (s.isLeaf()) {
            while (i >= 0 && key.compareTo(s.getKey(i)) < 0) {
                s.setKey(i + 1, s.getKey(i));
                i--;
            }

            s.setKey(i + 1, key);
            s.setKeysCount(s.getKeysCount() + 1);
        } else {
            while (i >= 0 && key.compareTo(s.getKey(i)) < 0) {
                i--;
            }

            i++;

            if (s.getChild(i).isFull()) {
                splitChild(s, i, s.getChild(i));
                if (key.compareTo(s.getKey(i)) > 0) {
                    i++;
                }
            }

            insertNonFull(s.getChild(i), key);
        }
    }

    /**
     * @param d
     * @return
     */
    public T search(final T d) {
        return searchNode(root, d);
    }

    /**
     * @param root2
     * @param d
     * @return
     */
    private T searchNode(final Node<T> node, final T d) {
        int i = 0;
        while (i < node.getKeysCount() && d.compareTo(node.getKey(i)) > 0) {
            i++;
        }

        if (i < node.getKeysCount() && d.equals(node.getKey(i))) {
            return node.getKey(i);
        }

        if (node.isLeaf()) {
            return null;
        } else {
            return searchNode(node.getChild(i), d);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return root.toString();
    }
}
