package parser;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.awt.event.*;

public class TreeVisualizer extends JFrame {
    public TreeVisualizer(Node root) {
        setTitle("Parse Tree Visualization");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        TreePanel treePanel = new TreePanel(root);
        JScrollPane scrollPane = new JScrollPane(treePanel);
        add(scrollPane);

        // Add mouse wheel listener for zooming
        treePanel.addMouseWheelListener(new MouseAdapter() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (e.isControlDown()) {
                    if (e.getWheelRotation() < 0) {
                        treePanel.zoomIn();
                    } else {
                        treePanel.zoomOut();
                    }
                }
            }
        });
    }
}

class TreePanel extends JPanel {
    private Node root;
    private double zoomFactor = 1.0;

    public TreePanel(Node root) {
        this.root = root;
        setPreferredSize(new Dimension(800, 600)); // Initial preferred size
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.scale(zoomFactor, zoomFactor);

        if (root != null) {
            int treeWidth = calculateSubtreeWidth(g2d, root);
            int treeHeight = calculateSubtreeHeight(g2d, root);
            setPreferredSize(new Dimension((int) (treeWidth * zoomFactor), (int) (treeHeight * zoomFactor)));
            revalidate();

            // Calculate the center position horizontally
            int centerX = (int) ((getWidth() / 2) / zoomFactor);
            int topY = 50; // Fixed vertical position from the top

            drawTree(g2d, root, centerX, topY, treeWidth / 2, 50);
        }
    }

    private int calculateSubtreeWidth(Graphics g, Node node) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(node.getName());
        int width = textWidth + 40; // Add some padding for the circle

        List<Node> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            int childrenWidth = 0;
            for (Node child : children) {
                childrenWidth += calculateSubtreeWidth(g, child);
            }
            width = Math.max(width, childrenWidth);
        }

        return width;
    }

    private int calculateSubtreeHeight(Graphics g, Node node) {
        int height = 50; // Initial height for the root node

        List<Node> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            int maxChildHeight = 0;
            for (Node child : children) {
                maxChildHeight = Math.max(maxChildHeight, calculateSubtreeHeight(g, child));
            }
            height += maxChildHeight + 50; // Add height for children and spacing
        }

        return height;
    }

    public void zoomIn() {
        zoomFactor *= 1.1;
        revalidate();
        repaint();
    }

    public void zoomOut() {
        zoomFactor /= 1.1;
        revalidate();
        repaint();
    }

    private void drawTree(Graphics g, Node node, int x, int y, int xOffset, int yOffset) {
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(node.getName());
        int textHeight = fm.getAscent();
        int ovalWidth = textWidth + 20; // Set oval width based on text width with some padding
        int ovalHeight = textHeight + 10; // Set oval height based on text height with some padding
    
        // Draw the oval
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(1)); // Ensure all branch lines have a stroke of 1
        g2.drawOval(x - ovalWidth / 2, y - ovalHeight / 2, ovalWidth, ovalHeight);
    
        // Draw the node name inside the oval
        g.drawString(node.getName(), x - textWidth / 2, y + textHeight / 4);
    
        List<Node> children = node.getChildren();
        if (children != null && !children.isEmpty()) {
            int totalWidth = 0;
            for (Node child : children) {
                totalWidth += calculateSubtreeWidth(g, child);
            }
    
            int childX = x - totalWidth / 2;
            int childY = y + yOffset;
    
            // Draw vertical line from parent to the level of children
            g.drawLine(x, y + ovalHeight / 2, x, childY - ovalHeight / 2);
    
            for (Node child : children) {
                int childWidth = calculateSubtreeWidth(g, child);
    
                // Draw horizontal line from vertical line to each child
                g.drawLine(x, childY - ovalHeight / 2, childX + childWidth / 2, childY - ovalHeight / 2);
    
                drawTree(g, child, childX + childWidth / 2, childY, childWidth / 2, yOffset);
                childX += childWidth;
            }
        } else {
            // Set thicker stroke for leaf nodes
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x - ovalWidth / 2, y - ovalHeight / 2, ovalWidth, ovalHeight);
        }
    }
}