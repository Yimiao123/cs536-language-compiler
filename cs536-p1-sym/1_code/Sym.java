// OVERVIEW: a class that represents a symol, containing information about that symbol
public class Sym {

    private String type; // the type of this symbol

    // EFFECTS: initialize the type
    public Sym(String type) {
        this.type = type;
    }

    // EFFECTS: return the type
    public String getType() {
        return type;
    }

    // EFFECTS: return the type
    public String toString() {
        return type;
    }

}