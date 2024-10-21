package symbol_table;

import java.util.List;
import java.util.Objects;

public class Symbol {
    private String name;
    private char type;
    private String scope;
    private List<String> parameters;
    private String symb;
    

    public Symbol(String name, char type, String scope, String symb) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.symb = symb;
    
    }

       public Symbol(String name, char type, String scope, List<String> parameters, String symb) {
        this.name = name;
        this.type = type;
        this.scope = scope;
        this.parameters = parameters;
        this.symb = symb;
    }
    public String getName() {
        return name;
    }
    public String getScope() {
        return scope;
    }
    public char getType() {
        return type;
    }
    public String getSymb() {
        return symb;
    }
    public void setType(char type){
     this.type = type;
    } 
  
    public List<String> getParameters(){
        return parameters;
    }
    

    @Override
    public String toString() {
        return "Symbol: " + symb + ", Type: " + type + ", Scope: " + scope + ", Unique Name: " + name;
    }
    
}
