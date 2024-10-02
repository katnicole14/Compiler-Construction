import java.util.List;

public class symbol_info{
    //attributes 
    private String name;
    private String type;
    private int scopeLevel;
    private boolean isFunction;
    private List<String> parameters;
    private String returnType;
    private boolean isReservedKeyword;  //is it a reserved keyword 


    //constructors

     // Constructor for variables
     public symbol_info(String name, String type, int scopeLevel, boolean isReservedKeyword) {
        this.name = name;
        this.type = type;
        this.scopeLevel = scopeLevel;
        this.isFunction = false;
        this.isReservedKeyword = isReservedKeyword;
    }

    public symbol_info(String name, String returnType, int scopeLevel, List<String> parameters) {
        this.name = name;
        this.returnType = returnType;
        this.scopeLevel = scopeLevel;
        this.isFunction = true;
        this.parameters = parameters;
        this.isReservedKeyword = false;
    }

    //getters and  setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public void setScopeLevel(int scopeLevel) {
        this.scopeLevel = scopeLevel;
    }

    public boolean isFunction() {
        return isFunction;
    }

    public void setFunction(boolean isFunction) {
        this.isFunction = isFunction;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public boolean isReservedKeyword() {
        return isReservedKeyword;
    }

    public void setReservedKeyword(boolean isReservedKeyword) {
        this.isReservedKeyword = isReservedKeyword;
    }

    //printing function 

    @Override
    public String toString() {
        return "SymbolInfo{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", scopeLevel=" + scopeLevel +
                ", isFunction=" + isFunction +
                ", parameters=" + parameters +
                ", returnType='" + returnType + '\'' +
                ", isReservedKeyword=" + isReservedKeyword +
                '}';
    }



}