package parser;

import java.util.ArrayList;
import java.util.List;

class Node {
    private String name;
    private List<Node> children = new ArrayList<>();
    private int id;
    private Node parent; // Add parent field

    public Node(String name) {
        this.name = name;
        this.id = Parser.generateUniqueId();

    }

    public void addChild(Node child) {
        child.setParent(this);
        children.add(child);
    }

    public String getName() {
        return name;
    }

    public Node getParent() { // Add getParent method
        return parent;
    }
    public void setParent(Node parent) { // Add setParent method
        this.parent = parent;
    }

    public List<Node> getChildren() {
        return children;
    }
    public int getId() {
        return id;
    }
    public void print(String indent) {
        System.out.println(indent + name);
        for (Node child : children) {
            child.print(indent + "  ");
        }
    }

    public boolean isTerminal() {
        return children.isEmpty();
    }
}