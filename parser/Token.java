package parser;
/**
 * The Token class represents a lexical token with an ID, class, and value.
 */
public class Token {
    private int id;
    private String tokenClass;
    private String word;

    public Token(int id, String tokenClass, String word) {
        this.id = id;
        this.tokenClass = tokenClass;
        this.word = word;
    }

    public int getId() {
        return id;
    }

    public String getTokenClass() {
        return tokenClass;
    }

    public String getWord() {
        return word;
    }

    @Override
    public String toString() {
        return "<TOK>\n" +
                "  <ID>" + id + "</ID>\n" +
                "  <CLASS>" + tokenClass + "</CLASS>\n" +
                "  <WORD>" + word + "</WORD>\n" +
                "</TOK>";
    }
}
