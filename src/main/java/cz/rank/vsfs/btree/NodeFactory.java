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
    public static Node allocateRoot(final int degree) {
        Node node = new Node(degree);
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
