/**
 *
 */
package cz.rank.vsfs.btree;

/**
 * @author rank
 *
 */
public class NodeFactory {

    /**
     * @return
     */
    public static <T extends Comparable<T>> Node<T> allocateRoot(final int degree) {
        Node<T> node = new Node<T>(degree);
        node.setLeaf(true);
        node.setKeysCount(0);

        return node;
    }

    /**
     *
     */
    private NodeFactory() {
    }

}
