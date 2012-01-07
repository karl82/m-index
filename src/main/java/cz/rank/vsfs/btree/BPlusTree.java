/**
 *
 */
package cz.rank.vsfs.btree;

/**
 * @author rank
 * 
 */
public class BPlusTree {
    public static void splitChild(final Node node, final int index, final Node fullNode) {
        int nodeDegree = fullNode.getDegree();
        Node z = new Node(nodeDegree);

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

    private Node root;

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
    public void insert(final double key) {
        Node r = root;

        if (r.isFull()) {
            Node s = new Node(getDegree());

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
    private void insertNonFull(final Node s, final double key) {
        int i = s.getKeysCount() - 1;

        if (s.isLeaf()) {
            while (i >= 0 && key < s.getKey(i)) {
                s.setKey(i + 1, s.getKey(i));
                i--;
            }

            s.setKey(i + 1, key);
            s.setKeysCount(s.getKeysCount() + 1);
        } else {
            while (i >= 0 && key < s.getKey(i)) {
                i--;
            }

            i++;

            if (s.getChild(i).isFull()) {
                splitChild(s, i, s.getChild(i));
                if (key > s.getKey(i)) {
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
    public double search(final double d) {
        return searchNode(root, d);
    }

    /**
     * @param root2
     * @param d
     * @return
     */
    private double searchNode(final Node node, final double d) {
        int i = 0;
        while (i < node.getKeysCount() && d > node.getKey(i)) {
            i++;
        }

        if (i < node.getKeysCount() && d == node.getKey(i)) {
            return node.getKey(i);
        }

        if (node.isLeaf()) {
            return 0;
        } else {
            return searchNode(node.getChild(i), d);
        }
    }

}
