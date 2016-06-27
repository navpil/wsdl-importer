package ua.lviv.navpil.wsdl_importer;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

class NodeListIterable  implements Iterable<Node> {

    private NodeList nodeList;

    NodeListIterable(NodeList nodeList) {
        this.nodeList = nodeList;
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeListIterator(nodeList);
    }

    private static class NodeListIterator implements Iterator<Node> {
        private NodeList nodeList;
        private int index;

        NodeListIterator(NodeList nodeList) {
            this.nodeList = nodeList;
            this.index = 0;
        }

        public boolean hasNext() {
            return nodeList != null && index < nodeList.getLength();
        }

        public Node next() {
            Node node = nodeList.item(index);
            if(node != null) {
                index++;
            }
            return node;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
