import java.util.*;

public class Sym {
    private String type;

    public Sym(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public String toString() {
        return type;
    }
}

class VarSym extends Sym {
    private String type;

    // constructor
    public VarSym(String type) {
        super(null);
        this.type = type;
    }

    public String toString() {
        return type;
    }
}

class FnSym extends Sym {
    private String returnType;
    private List<String> formalType;

    // constructor
    public FnSym(String returnType, List<String> formalType) {
        super(null);
        this.returnType = returnType;
        this.formalType = formalType;
    }

    public String toString() {
        String ret = "";
        Iterator it = formalType.iterator();
        while (it.hasNext()) {
            ret += it.next();
            if (it.hasNext()) {
                ret += ",";
            }
        }
        ret += "->" + returnType;
        return ret;
    }
}

class StructDeclSym extends Sym {
    private SymTable fields;

    public StructDeclSym(SymTable fields) {
        super(null);
        this.fields = fields;
    }

    public SymTable getFields() {
        return fields;
    }

    public String toString() {
        return "StructDeclSym";
    }
}

class StructSym extends Sym {
    private String type;
    private SymTable fields;

    public StructSym(String type, SymTable fields) {
        super(null);
        this.type = type;
        this.fields = fields;
    }

    public String toString() {
        return type;
    }

    public SymTable getFields() {
        return fields;
    }
}