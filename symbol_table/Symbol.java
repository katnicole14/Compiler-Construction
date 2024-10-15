package symbol_table;

public class Symbol {
    String name;
    String type;  // function or variable
    String scope;

    public Symbol(String name, String type, String scope) {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }
}
