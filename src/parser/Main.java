package parser;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try {
            Parser.parseInit();
            Node tree = Parser.getTree();

            System.out.println("Compilation successful!");

        } catch (Exception e) {
            System.err.println("Compilation failed: " + e.getMessage());
        }
    }
}

