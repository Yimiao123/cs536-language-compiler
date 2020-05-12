import java.io.*;
import java.util.*;

// **********************************************************************
// The ASTnode class defines the nodes of the abstract-syntax tree that
// represents a Wumbo program.
//
// Internal nodes of the tree contain pointers to children, organized
// either in a list (for nodes that may have a variable number of
// children) or as a fixed set of fields.
//
// The nodes for literals and ids contain line and character number
// information; for string literals and identifiers, they also contain a
// string; for integer literals, they also contain an integer value.
//
// Here are all the different kinds of AST nodes and what kinds of children
// they have.  All of these kinds of AST nodes are subclasses of "ASTnode".
// Indentation indicates further subclassing:
//
//     Subclass            Kids
//     --------            ----
//     ProgramNode         DeclListNode
//     DeclListNode        linked list of DeclNode
//     DeclNode:
//       VarDeclNode       TypeNode, IdNode, int
//       FnDeclNode        TypeNode, IdNode, FormalsListNode, FnBodyNode
//       FormalDeclNode    TypeNode, IdNode
//       StructDeclNode    IdNode, DeclListNode
//
//     FormalsListNode     linked list of FormalDeclNode
//     FnBodyNode          DeclListNode, StmtListNode
//     StmtListNode        linked list of StmtNode
//     ExpListNode         linked list of ExpNode
//
//     TypeNode:
//       IntNode           -- none --
//       BoolNode          -- none --
//       VoidNode          -- none --
//       StructNode        IdNode
//
//     StmtNode:
//       AssignStmtNode      AssignNode
//       PostIncStmtNode     ExpNode
//       PostDecStmtNode     ExpNode
//       ReadStmtNode        ExpNode
//       WriteStmtNode       ExpNode
//       IfStmtNode          ExpNode, DeclListNode, StmtListNode
//       IfElseStmtNode      ExpNode, DeclListNode, StmtListNode,
//                                    DeclListNode, StmtListNode
//       WhileStmtNode       ExpNode, DeclListNode, StmtListNode
//       RepeatStmtNode      ExpNode, DeclListNode, StmtListNode
//       CallStmtNode        CallExpNode
//       ReturnStmtNode      ExpNode
//
//     ExpNode:
//       IntLitNode          -- none --
//       StrLitNode          -- none --
//       TrueNode            -- none --
//       FalseNode           -- none --
//       IdNode              -- none --
//       DotAccessNode       ExpNode, IdNode
//       AssignNode          ExpNode, ExpNode
//       CallExpNode         IdNode, ExpListNode
//       UnaryExpNode        ExpNode
//         UnaryMinusNode
//         NotNode
//       BinaryExpNode       ExpNode ExpNode
//         PlusNode
//         MinusNode
//         TimesNode
//         DivideNode
//         AndNode
//         OrNode
//         EqualsNode
//         NotEqualsNode
//         LessNode
//         GreaterNode
//         LessEqNode
//         GreaterEqNode
//
// Here are the different kinds of AST nodes again, organized according to
// whether they are leaves, internal nodes with linked lists of kids, or
// internal nodes with a fixed number of kids:
//
// (1) Leaf nodes:
//        IntNode,   BoolNode,  VoidNode,  IntLitNode,  StrLitNode,
//        TrueNode,  FalseNode, IdNode
//
// (2) Internal nodes with (possibly empty) linked lists of children:
//        DeclListNode, FormalsListNode, StmtListNode, ExpListNode
//
// (3) Internal nodes with fixed numbers of kids:
//        ProgramNode,     VarDeclNode,     FnDeclNode,     FormalDeclNode,
//        StructDeclNode,  FnBodyNode,      StructNode,     AssignStmtNode,
//        PostIncStmtNode, PostDecStmtNode, ReadStmtNode,   WriteStmtNode
//        IfStmtNode,      IfElseStmtNode,  WhileStmtNode,  CallStmtNode
//        ReturnStmtNode,  DotAccessNode,   AssignExpNode,  CallExpNode,
//        UnaryExpNode,    BinaryExpNode,   UnaryMinusNode, NotNode,
//        PlusNode,        MinusNode,       TimesNode,      DivideNode,
//        AndNode,         OrNode,          EqualsNode,     NotEqualsNode,
//        LessNode,        GreaterNode,     LessEqNode,     GreaterEqNode
//
// **********************************************************************

// **********************************************************************
// %%%ASTnode class (base class for all other kinds of nodes)
// **********************************************************************

abstract class ASTnode {
    // every subclass must provide an unparse operation
    abstract public void unparse(PrintWriter p, int indent);

    // this method can be used by the unparse methods to do indenting
    protected void addIndentation(PrintWriter p, int indent) {
        for (int k = 0; k < indent; k++)
            p.print(" ");
    }
}

// **********************************************************************
// ProgramNode, DeclListNode, FormalsListNode, FnBodyNode,
// StmtListNode, ExpListNode
// **********************************************************************

class ProgramNode extends ASTnode {
    public ProgramNode(DeclListNode L) {
        myDeclList = L;
    }

    /**
     * nameAnalysis Creates an empty symbol table for the outermost scope, then
     * processes all of the globals, struct defintions, and functions in the
     * program.
     */
    public void nameAnalysis() {
        SymTable symTab = new SymTable();
        myDeclList.nameAnalysis(symTab);
        // check if main function exists
        if (!symTab.hasMainFn())
            ErrMsg.fatal(0, 0, "No main function");
    }

    /**
     * typeCheck
     */
    public void typeCheck() {
        myDeclList.typeCheck();
    }

    public void codeGen() {
        myDeclList.codeGen();
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
    }

    // 1 kid
    private DeclListNode myDeclList;
}

class DeclListNode extends ASTnode {
    public DeclListNode(List<DeclNode> S) {
        myDecls = S;
    }

    /**
     * nameAnalysis Given a symbol table symTab, process all of the decls in the
     * list.
     */
    public void nameAnalysis(SymTable symTab) {
        nameAnalysis(symTab, symTab);
    }

    /**
     * nameAnalysis Given a symbol table symTab and a global symbol table globalTab
     * (for processing struct names in variable decls), process all of the decls in
     * the list.
     */
    public void nameAnalysis(SymTable symTab, SymTable globalTab) {
        for (DeclNode node : myDecls) {
            if (node instanceof VarDeclNode) {
                ((VarDeclNode) node).nameAnalysis(symTab, globalTab);
            } else {
                node.nameAnalysis(symTab);
            }
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck() {
        for (DeclNode node : myDecls) {
            node.typeCheck();
        }
    }

    public List<DeclNode> getDeclList() {
        return myDecls;
    }

    public int getLocalNum() {
        return myDecls.size();
    }

    public void codeGen() {
        for (DeclNode node : myDecls) {
            node.codeGen();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator it = myDecls.iterator();
        try {
            while (it.hasNext()) {
                ((DeclNode) it.next()).unparse(p, indent);
            }
        } catch (NoSuchElementException ex) {
            System.err.println("unexpected NoSuchElementException in DeclListNode.print");
            System.exit(-1);
        }
    }

    // list of kids (DeclNodes)
    private List<DeclNode> myDecls;
}

class FormalsListNode extends ASTnode {
    public FormalsListNode(List<FormalDeclNode> S) {
        myFormals = S;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: for each formal decl in the
     * list process the formal decl if there was no error, add type of formal decl
     * to list
     */
    public List<Type> nameAnalysis(SymTable symTab) {
        List<Type> typeList = new LinkedList<Type>();
        for (FormalDeclNode node : myFormals) {
            Sym sym = node.nameAnalysis(symTab);
            if (sym != null) {
                typeList.add(sym.getType());
            }
        }
        return typeList;
    }

    /**
     * Return the number of formals in this list.
     */
    public int length() {
        return myFormals.size();
    }

    public List<FormalDeclNode> getFormalsList() {
        return this.myFormals;
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<FormalDeclNode> it = myFormals.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of kids (FormalDeclNodes)
    private List<FormalDeclNode> myFormals;
}

class FnBodyNode extends ASTnode {
    public FnBodyNode(DeclListNode declList, StmtListNode stmtList) {
        myDeclList = declList;
        myStmtList = stmtList;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: - process the declaration list
     * - process the statement list
     */
    public void nameAnalysis(SymTable symTab) {
        myDeclList.nameAnalysis(symTab);
        // add offset to each delcl sym
        List<DeclNode> list = myDeclList.getDeclList();
        int offset = -2;
        for (DeclNode node : list) {
            ((VarDeclNode) node).getIdNode().getSym().setOffset(offset);
            offset--;
        }
        myStmtList.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myStmtList.typeCheck(retType);
    }

    public int getLocalNum() {
        return myDeclList.getLocalNum();
    }

    public void codeGen(String epilogueLabel) {
        myStmtList.codeGen(epilogueLabel);
    }

    public void unparse(PrintWriter p, int indent) {
        myDeclList.unparse(p, indent);
        myStmtList.unparse(p, indent);
    }

    // 2 kids
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class StmtListNode extends ASTnode {
    public StmtListNode(List<StmtNode> S) {
        myStmts = S;
    }

    /**
     * nameAnalysis Given a symbol table symTab, process each statement in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (StmtNode node : myStmts) {
            node.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        for (StmtNode node : myStmts) {
            node.typeCheck(retType);
        }
    }

    public void codeGen() {
        for (StmtNode node : myStmts) {
            node.codeGen();
        }
    }

    public void codeGen(String epilogueLabel) {
        for (StmtNode node : myStmts) {
            if (node instanceof ReturnStmtNode) {
                node.codeGen(epilogueLabel);
            } else {
                node.codeGen();
            }
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<StmtNode> it = myStmts.iterator();
        while (it.hasNext()) {
            it.next().unparse(p, indent);
        }
    }

    // list of kids (StmtNodes)
    private List<StmtNode> myStmts;
}

class ExpListNode extends ASTnode {
    public ExpListNode(List<ExpNode> S) {
        myExps = S;
    }

    public int size() {
        return myExps.size();
    }

    /**
     * nameAnalysis Given a symbol table symTab, process each exp in the list.
     */
    public void nameAnalysis(SymTable symTab) {
        for (ExpNode node : myExps) {
            node.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(List<Type> typeList) {
        int k = 0;
        try {
            for (ExpNode node : myExps) {
                Type actualType = node.typeCheck(); // actual type of arg

                if (!actualType.isErrorType()) { // if this is not an error
                    Type formalType = typeList.get(k); // get the formal type
                    if (!formalType.equals(actualType)) {
                        ErrMsg.fatal(node.lineNum(), node.charNum(), "Type of actual does not match type of formal");
                    }
                }
                k++;
            }
        } catch (NoSuchElementException e) {
            System.err.println("unexpected NoSuchElementException in ExpListNode.typeCheck");
            System.exit(-1);
        }
    }

    public void codeGen() {
        Collections.reverse(myExps);
        for (ExpNode node : myExps) {
            node.codeGen();
        }
    }

    public void unparse(PrintWriter p, int indent) {
        Iterator<ExpNode> it = myExps.iterator();
        if (it.hasNext()) { // if there is at least one element
            it.next().unparse(p, indent);
            while (it.hasNext()) { // print the rest of the list
                p.print(", ");
                it.next().unparse(p, indent);
            }
        }
    }

    // list of kids (ExpNodes)
    private List<ExpNode> myExps;
}

// **********************************************************************
// DeclNode and its subclasses
// **********************************************************************

abstract class DeclNode extends ASTnode {
    /**
     * Note: a formal decl needs to return a sym
     */
    abstract public Sym nameAnalysis(SymTable symTab);

    // default version of typeCheck for non-function decls
    public void typeCheck() {
    }

    public void codeGen() {
    }
}

class VarDeclNode extends DeclNode {
    public VarDeclNode(TypeNode type, IdNode id, int size) {
        myType = type;
        myId = id;
        mySize = size;
    }

    /**
     * nameAnalysis (overloaded) Given a symbol table symTab, do: if this name is
     * declared void, then error else if the declaration is of a struct type, lookup
     * type name (globally) if type name doesn't exist, then error if no errors so
     * far, if name has already been declared in this scope, then error else add
     * name to local symbol table
     *
     * symTab is local symbol table (say, for struct field decls) globalTab is
     * global symbol table (for struct type names) symTab and globalTab can be the
     * same
     */
    public Sym nameAnalysis(SymTable symTab) {
        return nameAnalysis(symTab, symTab);
    }

    public Sym nameAnalysis(SymTable symTab, SymTable globalTab) {
        boolean badDecl = false;
        String name = myId.name();
        Sym sym = null;
        IdNode structId = null;

        if (myType instanceof VoidNode) { // check for void type
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Non-function declared void");
            badDecl = true;
        }

        else if (myType instanceof StructNode) {
            structId = ((StructNode) myType).idNode();

            try {
                sym = globalTab.lookupGlobal(structId.name());
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " + " in VarDeclNode.nameAnalysis");
            }

            // if the name for the struct type is not found,
            // or is not a struct type
            if (sym == null || !(sym instanceof StructDefSym)) {
                ErrMsg.fatal(structId.lineNum(), structId.charNum(), "Invalid name of struct type");
                badDecl = true;
            } else {
                structId.link(sym);
            }
        }

        Sym symCheckMul = null;

        try {
            symCheckMul = symTab.lookupLocal(name);
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in VarDeclNode.nameAnalysis");
        }

        if (symCheckMul != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Multiply declared identifier");
            badDecl = true;
        }

        if (!badDecl) { // insert into symbol table
            try {
                if (myType instanceof StructNode) {
                    sym = new StructSym(structId);
                } else {
                    sym = new Sym(myType.type());
                    // set global
                    if (symTab.size() == 1) {
                        sym.setGlobal();
                    }
                }
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " + " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " + " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (IllegalArgumentException ex) {
                System.err.println("Unexpected IllegalArgumentException " + " in VarDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return sym;
    }

    public IdNode getIdNode() {
        return myId;
    }

    public void codeGen() {
        // for global variables
        if (myId.isGlobal()) {
            Codegen.generate(".data");
            Codegen.generate(".align 4"); // TODO: 4?
            Codegen.genLabel("_" + myId.name());
            Codegen.generate(".space 4");
        }
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.println(";");
    }

    // 3 kids
    private TypeNode myType;
    private IdNode myId;
    private int mySize; // use value NOT_STRUCT if this is not a struct type

    public static int NOT_STRUCT = -1;
}

class FnDeclNode extends DeclNode {
    public FnDeclNode(TypeNode type, IdNode id, FormalsListNode formalList, FnBodyNode body) {
        myType = type;
        myId = id;
        myFormalsList = formalList;
        myBody = body;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: if this name has already been
     * declared in this scope, then error else add name to local symbol table in any
     * case, do the following: enter new scope process the formals if this function
     * is not multiply declared, update symbol table entry with types of formals
     * process the body of the function exit scope
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        FnSym sym = null;
        Sym symCheckMul = null;

        try {
            symCheckMul = symTab.lookupLocal(name);
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in FnDeclNode.nameAnalysis");
        }

        if (symCheckMul != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Multiply declared identifier");
        }

        else { // add function name to local symbol table
            try {
                sym = new FnSym(myType.type(), myFormalsList.length());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " + " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " + " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (IllegalArgumentException ex) {
                System.err.println("Unexpected IllegalArgumentException " + " in FnDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        symTab.addScope(); // add a new scope for locals and params

        // process the formals
        List<Type> typeList = myFormalsList.nameAnalysis(symTab);
        if (sym != null) {
            sym.addFormals(typeList);
        }

        // set offset for formals
        List<FormalDeclNode> list = myFormalsList.getFormalsList();
        int offset = 1;
        for (FormalDeclNode node : list) {
            node.getIdNode().getSym().setOffset(offset);
            offset++;
        }

        myBody.nameAnalysis(symTab); // process the function body

        try {
            symTab.removeScope(); // exit scope
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in FnDeclNode.nameAnalysis");
            System.exit(-1);
        }

        // record local num
        myLocalNum = myBody.getLocalNum();

        return null;
    }

    /**
     * typeCheck
     */
    public void typeCheck() {
        myBody.typeCheck(myType.type());
    }

    public void codeGen() {
        Codegen.generate(".text");
        String epilogueLabel = Codegen.nextLabel();

        // generate preamble
        Codegen.genLabel(myId.name());
        if (myId.name().equals("main"))
            Codegen.genLabel("__start");
        // generate prologue
        Codegen.p.write("\t# prologue\n");
        Codegen.genPush(Codegen.RA); // save ra
        Codegen.genPush(Codegen.FP); // save fp
        Codegen.generate("subu", Codegen.SP, Codegen.SP, 4 * myLocalNum); // alloc local
        Codegen.generate("addu", Codegen.FP, Codegen.SP, 4 * (myLocalNum + 2)); // update fp
        // generate body
        Codegen.p.write("\t# body\n");
        myBody.codeGen(epilogueLabel);
        // generate epilogue
        Codegen.p.write("\t# epilogue\n");
        Codegen.genLabel(epilogueLabel);
        Codegen.generateIndexed("lw", Codegen.RA, Codegen.FP, 0); // restore ra
        Codegen.generate("move", Codegen.T0, Codegen.FP);// record fp
        Codegen.generateIndexed("lw", Codegen.FP, Codegen.FP, -4); // restore fp
        Codegen.generate("move", Codegen.SP, Codegen.T0);// restore sp
        if (myId.name().equals("main")) { // return control
            Codegen.generate("li", Codegen.V0, 10);
            Codegen.generate("syscall");
        } else {
            Codegen.generate("jr", Codegen.RA);
        }
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
        p.print("(");
        myFormalsList.unparse(p, 0);
        p.println(") {");
        myBody.unparse(p, indent + 4);
        p.println("}\n");
    }

    // 4 kids
    private TypeNode myType;
    private IdNode myId;
    private FormalsListNode myFormalsList;
    private FnBodyNode myBody;
    private int myLocalNum;
}

class FormalDeclNode extends DeclNode {
    public FormalDeclNode(TypeNode type, IdNode id) {
        myType = type;
        myId = id;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: if this formal is declared
     * void, then error else if this formal is already in the local symble table,
     * then issue multiply declared error message and return null else add a new
     * entry to the symbol table and return that Sym
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;
        Sym sym = null;

        if (myType instanceof VoidNode) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Non-function declared void");
            badDecl = true;
        }

        Sym symCheckMul = null;

        try {
            symCheckMul = symTab.lookupLocal(name);
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in FormalDeclNode.nameAnalysis");
        }

        if (symCheckMul != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Multiply declared identifier");
            badDecl = true;
        }

        if (!badDecl) { // insert into symbol table
            try {
                sym = new Sym(myType.type());
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " + " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " + " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (IllegalArgumentException ex) {
                System.err.println("Unexpected IllegalArgumentException " + " in FormalDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return sym;
    }

    public IdNode getIdNode() {
        return myId;
    }

    public void unparse(PrintWriter p, int indent) {
        myType.unparse(p, 0);
        p.print(" ");
        p.print(myId.name());
    }

    // 2 kids
    private TypeNode myType;
    private IdNode myId;
}

class StructDeclNode extends DeclNode {
    public StructDeclNode(IdNode id, DeclListNode declList) {
        myId = id;
        myDeclList = declList;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: if this name is already in the
     * symbol table, then multiply declared error (don't add to symbol table) create
     * a new symbol table for this struct definition process the decl list if no
     * errors add a new entry to symbol table for this struct
     */
    public Sym nameAnalysis(SymTable symTab) {
        String name = myId.name();
        boolean badDecl = false;

        Sym symCheckMul = null;

        try {
            symCheckMul = symTab.lookupLocal(name);
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in StructDeclNode.nameAnalysis");
        }

        if (symCheckMul != null) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Multiply declared identifier");
            badDecl = true;
        }

        if (!badDecl) {
            try { // add entry to symbol table
                SymTable structSymTab = new SymTable();
                myDeclList.nameAnalysis(structSymTab, symTab);
                StructDefSym sym = new StructDefSym(structSymTab);
                symTab.addDecl(name, sym);
                myId.link(sym);
            } catch (DuplicateSymException ex) {
                System.err.println("Unexpected DuplicateSymException " + " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " + " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            } catch (IllegalArgumentException ex) {
                System.err.println("Unexpected IllegalArgumentException " + " in StructDeclNode.nameAnalysis");
                System.exit(-1);
            }
        }

        return null;
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("struct ");
        p.print(myId.name());
        p.println("{");
        myDeclList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("};\n");

    }

    // 2 kids
    private IdNode myId;
    private DeclListNode myDeclList;
}

// **********************************************************************
// TypeNode and its Subclasses
// **********************************************************************

abstract class TypeNode extends ASTnode {
    /* all subclasses must provide a type method */
    abstract public Type type();
}

class IntNode extends TypeNode {
    public IntNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new IntType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("int");
    }
}

class BoolNode extends TypeNode {
    public BoolNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new BoolType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("bool");
    }
}

class VoidNode extends TypeNode {
    public VoidNode() {
    }

    /**
     * type
     */
    public Type type() {
        return new VoidType();
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("void");
    }
}

class StructNode extends TypeNode {
    public StructNode(IdNode id) {
        myId = id;
    }

    public IdNode idNode() {
        return myId;
    }

    /**
     * type
     */
    public Type type() {
        return new StructType(myId);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("struct ");
        p.print(myId.name());
    }

    // 1 kid
    private IdNode myId;
}

// **********************************************************************
// StmtNode and its subclasses
// **********************************************************************

abstract class StmtNode extends ASTnode {
    abstract public void nameAnalysis(SymTable symTab);

    abstract public void typeCheck(Type retType);

    public void codeGen() {
    }

    public void codeGen(String epilogueLabel) {
    }
}

class AssignStmtNode extends StmtNode {
    public AssignStmtNode(AssignNode assign) {
        myAssign = assign;
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myAssign.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myAssign.typeCheck();
    }

    public void codeGen() {
        myAssign.codeGen();
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myAssign.unparse(p, -1); // no parentheses
        p.println(";");
    }

    // 1 kid
    private AssignNode myAssign;
}

class PostIncStmtNode extends StmtNode {
    public PostIncStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Arithmetic operator applied to non-numeric operand");
        }
    }

    public void codeGen() {
        Codegen.p.write("\t# PostInc Start\n");
        myExp.codeGen();
        ((IdNode) myExp).codeGenAddr();
        Codegen.genPop(Codegen.T1);
        Codegen.genPop(Codegen.T0);
        Codegen.generate("addi", Codegen.T0, Codegen.T0, 1);
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);
        Codegen.p.write("\t# PostInc End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myExp.unparse(p, 0);
        p.println("++;");
    }

    // 1 kid
    private ExpNode myExp;
}

class PostDecStmtNode extends StmtNode {
    public PostDecStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Arithmetic operator applied to non-numeric operand");
        }
    }

    public void codeGen() {
        Codegen.p.write("\t# PostDec Start\n");
        myExp.codeGen();
        ((IdNode) myExp).codeGenAddr();
        Codegen.genPop(Codegen.T1);
        Codegen.genPop(Codegen.T0);
        Codegen.generate("addi", Codegen.T0, Codegen.T0, -1);
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);
        Codegen.p.write("\t# PostDec End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myExp.unparse(p, 0);
        p.println("--;");
    }

    // 1 kid
    private ExpNode myExp;
}

class ReadStmtNode extends StmtNode {
    public ReadStmtNode(ExpNode e) {
        myExp = e;
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Attempt to read a function");
        }

        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Attempt to read a struct name");
        }

        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Attempt to read a struct variable");
        }
    }

    public void codeGen() {
        Codegen.p.write("\t# Read Start\n");

        Codegen.generate("li", Codegen.V0, 5); // set up syscall
        Codegen.generate("syscall"); // read into V0
        ((IdNode) myExp).codeGenAddr(); // push var addr into stack
        Codegen.genPop(Codegen.T0); // pop var addr into T0
        Codegen.generateIndexed("sw", Codegen.V0, Codegen.T0, 0); // save V0 into 0(T0)

        Codegen.p.write("\t# Read End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("cin >> ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid (actually can only be an IdNode or an ArrayExpNode)
    private ExpNode myExp;
}

class WriteStmtNode extends StmtNode {
    public WriteStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (type.isFnType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Attempt to write a function");
        }

        if (type.isStructDefType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Attempt to write a struct name");
        }

        if (type.isStructType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Attempt to write a struct variable");
        }

        if (type.isVoidType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Attempt to write void");
        }
    }

    public void codeGen() {
        Codegen.p.write("\t# Write Start\n");

        myExp.codeGen(); // push exp val into stack
        Codegen.genPop(Codegen.A0); // pop val into A0
        if (myExp instanceof StringLitNode) {
            Codegen.generate("li", Codegen.V0, 4); // set up syscall
        } else {
            Codegen.generate("li", Codegen.V0, 1); // set up syscall
        }
        Codegen.generate("syscall"); // write

        Codegen.p.write("\t# Write End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("cout << ");
        myExp.unparse(p, 0);
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp;
}

class IfStmtNode extends StmtNode {
    public IfStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myDeclList = dlist;
        myExp = exp;
        myStmtList = slist;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: - process the condition - enter
     * a new scope - process the decls and stmts - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in IfStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Non-bool expression used as an if condition");
        }

        myStmtList.typeCheck(retType);
    }

    public void codeGen() {
        Codegen.p.write("\t# If Start\n");

        String falseExit = Codegen.nextLabel();
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, falseExit);
        // exp is true, do codeGen for body
        myDeclList.codeGen(); // TODO: no need?
        myStmtList.codeGen();
        // exit for if
        Codegen.genLabel(falseExit);
        Codegen.generate("nop");

        Codegen.p.write("\t# If End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    // e kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class IfElseStmtNode extends StmtNode {
    public IfElseStmtNode(ExpNode exp, DeclListNode dlist1, StmtListNode slist1, DeclListNode dlist2,
            StmtListNode slist2) {
        myExp = exp;
        myThenDeclList = dlist1;
        myThenStmtList = slist1;
        myElseDeclList = dlist2;
        myElseStmtList = slist2;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: - process the condition - enter
     * a new scope - process the decls and stmts of then - exit the scope - enter a
     * new scope - process the decls and stmts of else - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myThenDeclList.nameAnalysis(symTab);
        myThenStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in IfElseStmtNode.nameAnalysis");
            System.exit(-1);
        }
        symTab.addScope();
        myElseDeclList.nameAnalysis(symTab);
        myElseStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in IfElseStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Non-bool expression used as an if condition");
        }

        myThenStmtList.typeCheck(retType);
        myElseStmtList.typeCheck(retType);
    }

    public void codeGen() {
        Codegen.p.write("\t# IfElse Start\n");

        String falseExit = Codegen.nextLabel();
        String finalExit = Codegen.nextLabel();
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, falseExit);
        // exp is true, do codeGen for if
        myThenDeclList.codeGen(); // TODO: no need?
        myThenStmtList.codeGen();
        Codegen.generate("j", finalExit);
        // exp is false, do codeGen for else
        Codegen.genLabel(falseExit);
        myElseDeclList.codeGen();
        myElseStmtList.codeGen();
        // exit
        Codegen.genLabel(finalExit);
        Codegen.generate("nop");

        Codegen.p.write("\t# IfElse End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("if (");
        myExp.unparse(p, 0);
        p.println(") {");
        myThenDeclList.unparse(p, indent + 4);
        myThenStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
        addIndentation(p, indent);
        p.println("else {");
        myElseDeclList.unparse(p, indent + 4);
        myElseStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    // 5 kids
    private ExpNode myExp;
    private DeclListNode myThenDeclList;
    private StmtListNode myThenStmtList;
    private StmtListNode myElseStmtList;
    private DeclListNode myElseDeclList;
}

class WhileStmtNode extends StmtNode {
    public WhileStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: - process the condition - enter
     * a new scope - process the decls and stmts - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in WhileStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Non-bool expression used as a while condition");
        }

        myStmtList.typeCheck(retType);
    }

    public void codeGen() {
        Codegen.p.write("\t# While Start\n");
        String whileEntry = Codegen.nextLabel();
        String whileExit = Codegen.nextLabel();

        Codegen.genLabel(whileEntry);
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, whileExit);
        myDeclList.codeGen();
        myStmtList.codeGen();
        Codegen.generate("j", whileEntry);
        // exit
        Codegen.genLabel(whileExit);
        Codegen.generate("nop");

        Codegen.p.write("\t# While End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("while (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class RepeatStmtNode extends StmtNode {
    public RepeatStmtNode(ExpNode exp, DeclListNode dlist, StmtListNode slist) {
        myExp = exp;
        myDeclList = dlist;
        myStmtList = slist;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: - process the condition - enter
     * a new scope - process the decls and stmts - exit the scope
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
        symTab.addScope();
        myDeclList.nameAnalysis(symTab);
        myStmtList.nameAnalysis(symTab);
        try {
            symTab.removeScope();
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in RepeatStmtNode.nameAnalysis");
            System.exit(-1);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        Type type = myExp.typeCheck();

        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Non-integer expression used as a repeat clause");
        }

        myStmtList.typeCheck(retType);
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("repeat (");
        myExp.unparse(p, 0);
        p.println(") {");
        myDeclList.unparse(p, indent + 4);
        myStmtList.unparse(p, indent + 4);
        addIndentation(p, indent);
        p.println("}");
    }

    // 3 kids
    private ExpNode myExp;
    private DeclListNode myDeclList;
    private StmtListNode myStmtList;
}

class CallStmtNode extends StmtNode {
    public CallStmtNode(CallExpNode call) {
        myCall = call;
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myCall.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        myCall.typeCheck();
    }

    public void codeGen() {
        myCall.codeGen();
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        myCall.unparse(p, indent);
        p.println(";");
    }

    // 1 kid
    private CallExpNode myCall;
}

class ReturnStmtNode extends StmtNode {
    public ReturnStmtNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's child, if it has one
     */
    public void nameAnalysis(SymTable symTab) {
        if (myExp != null) {
            myExp.nameAnalysis(symTab);
        }
    }

    /**
     * typeCheck
     */
    public void typeCheck(Type retType) {
        if (myExp != null) { // return value given
            Type type = myExp.typeCheck();

            if (retType.isVoidType()) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Return with a value in a void function");
            }

            else if (!retType.isErrorType() && !type.isErrorType() && !retType.equals(type)) {
                ErrMsg.fatal(myExp.lineNum(), myExp.charNum(), "Bad return value");
            }
        }

        else { // no return value given -- ok if this is a void function
            if (!retType.isVoidType()) {
                ErrMsg.fatal(0, 0, "Missing return value");
            }
        }

    }

    public void codeGen(String epilogueLabel) {
        Codegen.p.write("\t# Return Start\n");
        // save return val into V0
        if (myExp != null) {
            myExp.codeGen();
            Codegen.genPop(Codegen.V0);
        }
        // jump to epilogue
        Codegen.generate("j", epilogueLabel);
        Codegen.p.write("\t# Return End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        addIndentation(p, indent);
        p.print("return");
        if (myExp != null) {
            p.print(" ");
            myExp.unparse(p, 0);
        }
        p.println(";");
    }

    // 1 kid
    private ExpNode myExp; // possibly null
}

// **********************************************************************
// ExpNode and its subclasses
// **********************************************************************

abstract class ExpNode extends ASTnode {
    /**
     * Default version for nodes with no names
     */
    public void nameAnalysis(SymTable symTab) {
    }

    abstract public Type typeCheck();

    abstract public int lineNum();

    abstract public int charNum();

    public void codeGen() {
    }
}

class IntLitNode extends ExpNode {
    public IntLitNode(int lineNum, int charNum, int intVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myIntVal = intVal;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new IntType();
    }

    public void codeGen() {
        Codegen.generate("li", Codegen.T0, myIntVal);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myIntVal);
    }

    private int myLineNum;
    private int myCharNum;
    private int myIntVal;
}

class StringLitNode extends ExpNode {
    public StringLitNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new StringType();
    }

    public void codeGen() {
        // TODO: how to push string into stack??
        String label = Codegen.nextLabel();
        Codegen.generate(".data");
        // Codegen.generate(".align 4"); // TODO: 4?
        Codegen.genLabel(label);
        Codegen.generate(".asciiz ", myStrVal);
        Codegen.generate(".text");
        Codegen.generate("la", Codegen.T0, label);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
}

class TrueNode extends ExpNode {
    public TrueNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }

    public void codeGen() {
        Codegen.generate("li", Codegen.T0, Codegen.TRUE);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("true");
    }

    private int myLineNum;
    private int myCharNum;
}

class FalseNode extends ExpNode {
    public FalseNode(int lineNum, int charNum) {
        myLineNum = lineNum;
        myCharNum = charNum;
    }

    /**
     * Return the line number for this literal.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this literal.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return new BoolType();
    }

    public void codeGen() {
        Codegen.generate("li", Codegen.T0, Codegen.FALSE);
        Codegen.genPush(Codegen.T0);
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("false");
    }

    private int myLineNum;
    private int myCharNum;
}

class IdNode extends ExpNode {
    public IdNode(int lineNum, int charNum, String strVal) {
        myLineNum = lineNum;
        myCharNum = charNum;
        myStrVal = strVal;
    }

    /**
     * Link the given symbol to this ID.
     */
    public void link(Sym sym) {
        mySym = sym;
    }

    /**
     * Return the name of this ID.
     */
    public String name() {
        return myStrVal;
    }

    /**
     * Return the symbol associated with this ID.
     */
    public Sym sym() {
        return mySym;
    }

    /**
     * Return the line number for this ID.
     */
    public int lineNum() {
        return myLineNum;
    }

    /**
     * Return the char number for this ID.
     */
    public int charNum() {
        return myCharNum;
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: - check for use of undeclared
     * name - if ok, link to symbol table entry
     */
    public void nameAnalysis(SymTable symTab) {
        Sym sym = null;

        try {
            sym = symTab.lookupGlobal(myStrVal);
        } catch (EmptySymTableException ex) {
            System.err.println("Unexpected EmptySymTableException " + " in IdNode.nameAnalysis");
            System.exit(-1);
        }

        if (sym == null) {
            ErrMsg.fatal(myLineNum, myCharNum, "Undeclared identifier");
        } else {
            link(sym);
        }
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (mySym != null) {
            return mySym.getType();
        } else {
            System.err.println("ID with null sym field in IdNode.typeCheck");
            System.exit(-1);
        }
        return null;
    }

    public boolean isGlobal() {
        return mySym.isGlobal();
    }

    public Sym getSym() {
        return mySym;
    }

    public void codeGenAddr() {
        // push addr into stack
        if (this.isGlobal()) {
            Codegen.generate("la", Codegen.T0, "_" + myStrVal);
        } else {
            Codegen.generateIndexed("la", Codegen.T0, Codegen.FP, 4 * mySym.getOffset());
        }
        Codegen.genPush(Codegen.T0);
    }

    public void codeGen() {
        // push val into stack
        if (this.isGlobal()) {
            Codegen.generate("lw", Codegen.T0, "_" + myStrVal);
        } else {
            Codegen.generateIndexed("lw", Codegen.T0, Codegen.FP, 4 * mySym.getOffset());
        }
        Codegen.genPush(Codegen.T0); // TODO: merge two
    }

    public void unparse(PrintWriter p, int indent) {
        p.print(myStrVal);
        if (mySym != null) {
            p.print("(" + mySym + ")");
        }
    }

    private int myLineNum;
    private int myCharNum;
    private String myStrVal;
    private Sym mySym;
}

class DotAccessExpNode extends ExpNode {
    public DotAccessExpNode(ExpNode loc, IdNode id) {
        myLoc = loc;
        myId = id;
        mySym = null;
    }

    /**
     * Return the symbol associated with this dot-access node.
     */
    public Sym sym() {
        return mySym;
    }

    /**
     * Return the line number for this dot-access node. The line number is the one
     * corresponding to the RHS of the dot-access.
     */
    public int lineNum() {
        return myId.lineNum();
    }

    /**
     * Return the char number for this dot-access node. The char number is the one
     * corresponding to the RHS of the dot-access.
     */
    public int charNum() {
        return myId.charNum();
    }

    /**
     * nameAnalysis Given a symbol table symTab, do: - process the LHS of the
     * dot-access - process the RHS of the dot-access - if the RHS is of a struct
     * type, set the sym for this node so that a dot-access "higher up" in the AST
     * can get access to the symbol table for the appropriate struct definition
     */
    public void nameAnalysis(SymTable symTab) {
        badAccess = false;
        SymTable structSymTab = null; // to lookup RHS of dot-access
        Sym sym = null;

        myLoc.nameAnalysis(symTab); // do name analysis on LHS

        // if myLoc is really an ID, then sym will be a link to the ID's symbol
        if (myLoc instanceof IdNode) {
            IdNode id = (IdNode) myLoc;
            sym = id.sym();

            // check ID has been declared to be of a struct type

            if (sym == null) { // ID was undeclared
                badAccess = true;
            } else if (sym instanceof StructSym) {
                // get symbol table for struct type
                Sym tempSym = ((StructSym) sym).getStructType().sym();
                structSymTab = ((StructDefSym) tempSym).getSymTable();
            } else { // LHS is not a struct type
                ErrMsg.fatal(id.lineNum(), id.charNum(), "Dot-access of non-struct type");
                badAccess = true;
            }
        }

        // if myLoc is really a dot-access (i.e., myLoc was of the form
        // LHSloc.RHSid), then sym will either be
        // null - indicating RHSid is not of a struct type, or
        // a link to the Sym for the struct type RHSid was declared to be
        else if (myLoc instanceof DotAccessExpNode) {
            DotAccessExpNode loc = (DotAccessExpNode) myLoc;

            if (loc.badAccess) { // if errors in processing myLoc
                badAccess = true; // don't continue proccessing this dot-access
            } else { // no errors in processing myLoc
                sym = loc.sym();

                if (sym == null) { // no struct in which to look up RHS
                    ErrMsg.fatal(loc.lineNum(), loc.charNum(), "Dot-access of non-struct type");
                    badAccess = true;
                } else { // get the struct's symbol table in which to lookup RHS
                    if (sym instanceof StructDefSym) {
                        structSymTab = ((StructDefSym) sym).getSymTable();
                    } else {
                        System.err.println("Unexpected Sym type in DotAccessExpNode");
                        System.exit(-1);
                    }
                }
            }

        }

        else { // don't know what kind of thing myLoc is
            System.err.println("Unexpected node type in LHS of dot-access");
            System.exit(-1);
        }

        // do name analysis on RHS of dot-access in the struct's symbol table
        if (!badAccess) {

            try {
                sym = structSymTab.lookupGlobal(myId.name()); // lookup
            } catch (EmptySymTableException ex) {
                System.err.println("Unexpected EmptySymTableException " + " in DotAccessExpNode.nameAnalysis");
            }

            if (sym == null) { // not found - RHS is not a valid field name
                ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Invalid struct field name");
                badAccess = true;
            }

            else {
                myId.link(sym); // link the symbol
                // if RHS is itself as struct type, link the symbol for its struct
                // type to this dot-access node (to allow chained dot-access)
                if (sym instanceof StructSym) {
                    mySym = ((StructSym) sym).getStructType().sym();
                }
            }
        }
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        return myId.typeCheck();
    }

    public void unparse(PrintWriter p, int indent) {
        myLoc.unparse(p, 0);
        p.print(".");
        myId.unparse(p, 0);
    }

    // 2 kids
    private ExpNode myLoc;
    private IdNode myId;
    private Sym mySym; // link to Sym for struct type
    private boolean badAccess; // to prevent multiple, cascading errors
}

class AssignNode extends ExpNode {
    public AssignNode(ExpNode lhs, ExpNode exp) {
        myLhs = lhs;
        myExp = exp;
    }

    /**
     * Return the line number for this assignment node. The line number is the one
     * corresponding to the left operand.
     */
    public int lineNum() {
        return myLhs.lineNum();
    }

    /**
     * Return the char number for this assignment node. The char number is the one
     * corresponding to the left operand.
     */
    public int charNum() {
        return myLhs.charNum();
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's two children
     */
    public void nameAnalysis(SymTable symTab) {
        myLhs.nameAnalysis(symTab);
        myExp.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type typeLhs = myLhs.typeCheck();
        Type typeExp = myExp.typeCheck();
        Type retType = typeLhs;

        if (typeLhs.isFnType() && typeExp.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Function assignment");
            retType = new ErrorType();
        }

        if (typeLhs.isStructDefType() && typeExp.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct name assignment");
            retType = new ErrorType();
        }

        if (typeLhs.isStructType() && typeExp.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Struct variable assignment");
            retType = new ErrorType();
        }

        if (!typeLhs.equals(typeExp) && !typeLhs.isErrorType() && !typeExp.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Type mismatch");
            retType = new ErrorType();
        }

        if (typeLhs.isErrorType() || typeExp.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }

    public void codeGen() {
        Codegen.p.write("\t#Assignment\n");
        myExp.codeGen(); // push RHS val TODO: when rhs is not int/bool/id
        ((IdNode) myLhs).codeGenAddr(); // push LHS addr
        Codegen.genPop(Codegen.T1); // pop LHS addr into T1
        Codegen.genPop(Codegen.T0); // pop RHS val into T0
        Codegen.generateIndexed("sw", Codegen.T0, Codegen.T1, 0);// do assign
    }

    public void unparse(PrintWriter p, int indent) {
        if (indent != -1)
            p.print("(");
        myLhs.unparse(p, 0);
        p.print(" = ");
        myExp.unparse(p, 0);
        if (indent != -1)
            p.print(")");
    }

    // 2 kids
    private ExpNode myLhs;
    private ExpNode myExp;
}

class CallExpNode extends ExpNode {
    public CallExpNode(IdNode name, ExpListNode elist) {
        myId = name;
        myExpList = elist;
    }

    public CallExpNode(IdNode name) {
        myId = name;
        myExpList = new ExpListNode(new LinkedList<ExpNode>());
    }

    /**
     * Return the line number for this call node. The line number is the one
     * corresponding to the function name.
     */
    public int lineNum() {
        return myId.lineNum();
    }

    /**
     * Return the char number for this call node. The char number is the one
     * corresponding to the function name.
     */
    public int charNum() {
        return myId.charNum();
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's two children
     */
    public void nameAnalysis(SymTable symTab) {
        myId.nameAnalysis(symTab);
        myExpList.nameAnalysis(symTab);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        if (!myId.typeCheck().isFnType()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Attempt to call a non-function");
            return new ErrorType();
        }

        FnSym fnSym = (FnSym) (myId.sym());

        if (fnSym == null) {
            System.err.println("null sym for Id in CallExpNode.typeCheck");
            System.exit(-1);
        }

        if (myExpList.size() != fnSym.getNumParams()) {
            ErrMsg.fatal(myId.lineNum(), myId.charNum(), "Function call with wrong number of args");
            return fnSym.getReturnType();
        }

        myExpList.typeCheck(fnSym.getParamTypes());
        return fnSym.getReturnType();
    }

    public void codeGen() {
        Codegen.p.write("\t# Call Start\n");

        // evaluate all exps and push into stack in reverse order
        if (myExpList != null)
            myExpList.codeGen();
        // jal
        Codegen.generate("jal", myId.name());
        // free actuals
        Codegen.generate("addu", Codegen.SP, Codegen.SP, 4 * (((FnSym) myId.getSym()).getNumParams()));
        // save return val into stack
        Codegen.genPush(Codegen.V0);

        Codegen.p.write("\t# Call End\n");
    }

    // ** unparse **
    public void unparse(PrintWriter p, int indent) {
        myId.unparse(p, 0);
        p.print("(");
        if (myExpList != null) {
            myExpList.unparse(p, 0);
        }
        p.print(")");
    }

    // 2 kids
    private IdNode myId;
    private ExpListNode myExpList; // possibly null
}

abstract class UnaryExpNode extends ExpNode {
    public UnaryExpNode(ExpNode exp) {
        myExp = exp;
    }

    /**
     * Return the line number for this unary expression node. The line number is the
     * one corresponding to the operand.
     */
    public int lineNum() {
        return myExp.lineNum();
    }

    /**
     * Return the char number for this unary expression node. The char number is the
     * one corresponding to the operand.
     */
    public int charNum() {
        return myExp.charNum();
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's child
     */
    public void nameAnalysis(SymTable symTab) {
        myExp.nameAnalysis(symTab);
    }

    public void codeGen() {
    }

    // one child
    protected ExpNode myExp;
}

abstract class BinaryExpNode extends ExpNode {
    public BinaryExpNode(ExpNode exp1, ExpNode exp2) {
        myExp1 = exp1;
        myExp2 = exp2;
    }

    /**
     * Return the line number for this binary expression node. The line number is
     * the one corresponding to the left operand.
     */
    public int lineNum() {
        return myExp1.lineNum();
    }

    /**
     * Return the char number for this binary expression node. The char number is
     * the one corresponding to the left operand.
     */
    public int charNum() {
        return myExp1.charNum();
    }

    /**
     * nameAnalysis Given a symbol table symTab, perform name analysis on this
     * node's two children
     */
    public void nameAnalysis(SymTable symTab) {
        myExp1.nameAnalysis(symTab);
        myExp2.nameAnalysis(symTab);
    }

    public void codeGen() {
    }

    // two kids
    protected ExpNode myExp1;
    protected ExpNode myExp2;
}

// **********************************************************************
// Subclasses of UnaryExpNode
// **********************************************************************

class UnaryMinusNode extends UnaryExpNode {
    public UnaryMinusNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new IntType();

        if (!type.isErrorType() && !type.isIntType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (type.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }

    public void codeGen() {
        Codegen.p.write("\t# Neg Start\n");
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        Codegen.generate("add", Codegen.T1, Codegen.T0, Codegen.T0); // T1=T0+T0
        Codegen.generate("sub", Codegen.T0, Codegen.T0, Codegen.T1); // T0=T0-T1=-T0
        Codegen.genPush(Codegen.T0);
        Codegen.p.write("\t# Neg End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(-");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

class NotNode extends UnaryExpNode {
    public NotNode(ExpNode exp) {
        super(exp);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type = myExp.typeCheck();
        Type retType = new BoolType();

        if (!type.isErrorType() && !type.isBoolType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }

        if (type.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }

    public void codeGen() {
        Codegen.p.write("\t# Not Start\n");
        myExp.codeGen();
        Codegen.genPop(Codegen.T0);
        // Codegen.generate("nor", Codegen.T0, Codegen.T0, Codegen.FALSE); // T0=T0 nor
        // false
        Codegen.generate("xor", Codegen.T0, Codegen.T0, Codegen.TRUE); // T0=T0 nor false
        Codegen.genPush(Codegen.T0);
        Codegen.p.write("\t# Not End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(!");
        myExp.unparse(p, 0);
        p.print(")");
    }
}

// **********************************************************************
// Subclasses of BinaryExpNode
// **********************************************************************

abstract class ArithmeticExpNode extends BinaryExpNode {
    public ArithmeticExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new IntType();

        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(), "Arithmetic operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }
}

abstract class LogicalExpNode extends BinaryExpNode {
    public LogicalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();

        if (!type1.isErrorType() && !type1.isBoolType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(), "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }

        if (!type2.isErrorType() && !type2.isBoolType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(), "Logical operator applied to non-bool operand");
            retType = new ErrorType();
        }

        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }
}

abstract class EqualityExpNode extends BinaryExpNode {
    public EqualityExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();

        if (type1.isVoidType() && type2.isVoidType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Equality operator applied to void functions");
            retType = new ErrorType();
        }

        if (type1.isFnType() && type2.isFnType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Equality operator applied to functions");
            retType = new ErrorType();
        }

        if (type1.isStructDefType() && type2.isStructDefType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Equality operator applied to struct names");
            retType = new ErrorType();
        }

        if (type1.isStructType() && type2.isStructType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Equality operator applied to struct variables");
            retType = new ErrorType();
        }

        if (!type1.equals(type2) && !type1.isErrorType() && !type2.isErrorType()) {
            ErrMsg.fatal(lineNum(), charNum(), "Type mismatch");
            retType = new ErrorType();
        }

        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }
}

abstract class RelationalExpNode extends BinaryExpNode {
    public RelationalExpNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    /**
     * typeCheck
     */
    public Type typeCheck() {
        Type type1 = myExp1.typeCheck();
        Type type2 = myExp2.typeCheck();
        Type retType = new BoolType();

        if (!type1.isErrorType() && !type1.isIntType()) {
            ErrMsg.fatal(myExp1.lineNum(), myExp1.charNum(), "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (!type2.isErrorType() && !type2.isIntType()) {
            ErrMsg.fatal(myExp2.lineNum(), myExp2.charNum(), "Relational operator applied to non-numeric operand");
            retType = new ErrorType();
        }

        if (type1.isErrorType() || type2.isErrorType()) {
            retType = new ErrorType();
        }

        return retType;
    }
}

class PlusNode extends ArithmeticExpNode {
    public PlusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# Plus Begin\n");
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("add", Codegen.T0, Codegen.T0, Codegen.T1); // do add
        Codegen.genPush(Codegen.T0); // push result
        Codegen.p.write("\t# Plus End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" + ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class MinusNode extends ArithmeticExpNode {
    public MinusNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# Minus Begin\n");
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("sub", Codegen.T0, Codegen.T0, Codegen.T1); // do minus
        Codegen.genPush(Codegen.T0); // push result
        Codegen.p.write("\t# Minus End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" - ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class TimesNode extends ArithmeticExpNode {
    public TimesNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# Mult Begin\n");
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("mult", Codegen.T0, Codegen.T1); // do mult
        Codegen.generate("mflo", Codegen.T0);// save result into T0
        Codegen.genPush(Codegen.T0); // push result
        Codegen.p.write("\t# Mult End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" * ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class DivideNode extends ArithmeticExpNode {
    public DivideNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# Div Begin\n");
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("div", Codegen.T0, Codegen.T1); // do div
        Codegen.generate("mflo", Codegen.T0);// save result into T0
        Codegen.genPush(Codegen.T0); // push result
        Codegen.p.write("\t# Div End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" / ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class AndNode extends LogicalExpNode {
    public AndNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# And Begin\n");
        String shortCircuitExit = Codegen.nextLabel();
        // TODO: what if non-bool is used in and??
        // push op0, op1, jump if short circuit
        myExp1.codeGen(); // push op0
        Codegen.genPop(Codegen.T0); // pop op0 into T0
        Codegen.generate("beq", Codegen.T0, Codegen.FALSE, shortCircuitExit); // jump if short circuit
        Codegen.genPush(Codegen.T0); // push op0 if not short circuit
        myExp2.codeGen(); // push op1
        // if not short circuit, pop op0, op1, do evaluation
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("and", Codegen.T0, Codegen.T0, Codegen.T1); // T0 = T0 and T1
        // push result
        Codegen.genLabel(shortCircuitExit);
        Codegen.genPush(Codegen.T0); // push result
        Codegen.p.write("\t# And End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" && ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class OrNode extends LogicalExpNode {
    public OrNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# Or Begin\n");
        String shortCircuitExit = Codegen.nextLabel();
        // TODO: what if non-bool is used in and??
        // push op0, op1, jump if short circuit
        myExp1.codeGen(); // push op0
        Codegen.genPop(Codegen.T0); // pop op0 into T0
        Codegen.generate("beq", Codegen.T0, Codegen.TRUE, shortCircuitExit); // jump if short circuit
        Codegen.genPush(Codegen.T0); // push op0 if not short circuit
        myExp2.codeGen(); // push op1
        // if not short circuit, pop op0, op1, do evaluation
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("or", Codegen.T0, Codegen.T0, Codegen.T1); // T0 = T0 or T1
        // push result
        Codegen.genLabel(shortCircuitExit);
        Codegen.genPush(Codegen.T0); // push result
        Codegen.p.write("\t# Or End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" || ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class EqualsNode extends EqualityExpNode {
    public EqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# Equals Begin\n");
        String falseExit = Codegen.nextLabel();
        String finalExit = Codegen.nextLabel();
        // push op0, op1, pop op1, op0, check equal
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("bne", Codegen.T0, Codegen.T1, falseExit);
        // equal, push 1, skip not equal
        Codegen.generate("li", Codegen.T0, Codegen.TRUE);
        Codegen.genPush(Codegen.T0);
        Codegen.generate("j", finalExit);
        // not equal, push 0, fall through
        Codegen.genLabel(falseExit);
        Codegen.generate("li", Codegen.T0, Codegen.FALSE);
        Codegen.genPush(Codegen.T0);
        // final exit, nop
        Codegen.genLabel(finalExit);
        Codegen.generate("nop");
        Codegen.p.write("\t# Equals End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" == ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class NotEqualsNode extends EqualityExpNode {
    public NotEqualsNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# NotEquals Begin\n");
        String falseExit = Codegen.nextLabel();
        String finalExit = Codegen.nextLabel();
        // push op0, op1, pop op1, op0, check equal
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("beq", Codegen.T0, Codegen.T1, falseExit);
        // evaluate to be true, push 1, skip false
        Codegen.generate("li", Codegen.T0, Codegen.TRUE);
        Codegen.genPush(Codegen.T0);
        Codegen.generate("j", finalExit);
        // evaluate to be false, push 0, fall through
        Codegen.genLabel(falseExit);
        Codegen.generate("li", Codegen.T0, Codegen.FALSE);
        Codegen.genPush(Codegen.T0);
        // final exit, nop
        Codegen.genLabel(finalExit);
        Codegen.generate("nop");
        Codegen.p.write("\t# NotEquals End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" != ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessNode extends RelationalExpNode {
    public LessNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# Less Begin\n");
        String trueExit = Codegen.nextLabel();
        String finalExit = Codegen.nextLabel();
        // push op0, op1, pop op1, op0, check equal
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("blt", Codegen.T0, Codegen.T1, trueExit);
        // evaluate to be false, push 1, skip false
        Codegen.generate("li", Codegen.T0, Codegen.FALSE);
        Codegen.genPush(Codegen.T0);
        Codegen.generate("j", finalExit);
        // evaluate to be true, push 0, fall through
        Codegen.genLabel(trueExit);
        Codegen.generate("li", Codegen.T0, Codegen.TRUE);
        Codegen.genPush(Codegen.T0);
        // final exit, nop
        Codegen.genLabel(finalExit);
        Codegen.generate("nop");
        Codegen.p.write("\t# Less End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" < ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterNode extends RelationalExpNode {
    public GreaterNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# Greater Begin\n");
        String trueExit = Codegen.nextLabel();
        String finalExit = Codegen.nextLabel();
        // push op0, op1, pop op1, op0, check equal
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("bgt", Codegen.T0, Codegen.T1, trueExit);
        // evaluate to be false, push 1, skip false
        Codegen.generate("li", Codegen.T0, Codegen.FALSE);
        Codegen.genPush(Codegen.T0);
        Codegen.generate("j", finalExit);
        // evaluate to be true, push 0, fall through
        Codegen.genLabel(trueExit);
        Codegen.generate("li", Codegen.T0, Codegen.TRUE);
        Codegen.genPush(Codegen.T0);
        // final exit, nop
        Codegen.genLabel(finalExit);
        Codegen.generate("nop");
        Codegen.p.write("\t# Greater End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" > ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class LessEqNode extends RelationalExpNode {
    public LessEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# LessEq Begin\n");
        String trueExit = Codegen.nextLabel();
        String finalExit = Codegen.nextLabel();
        // push op0, op1, pop op1, op0, check equal
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("ble", Codegen.T0, Codegen.T1, trueExit);
        // evaluate to be false, push 1, skip false
        Codegen.generate("li", Codegen.T0, Codegen.FALSE);
        Codegen.genPush(Codegen.T0);
        Codegen.generate("j", finalExit);
        // evaluate to be true, push 0, fall through
        Codegen.genLabel(trueExit);
        Codegen.generate("li", Codegen.T0, Codegen.TRUE);
        Codegen.genPush(Codegen.T0);
        // final exit, nop
        Codegen.genLabel(finalExit);
        Codegen.generate("nop");
        Codegen.p.write("\t# LessEq End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" <= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}

class GreaterEqNode extends RelationalExpNode {
    public GreaterEqNode(ExpNode exp1, ExpNode exp2) {
        super(exp1, exp2);
    }

    public void codeGen() {
        Codegen.p.write("\t# GreaterEq Begin\n");
        String trueExit = Codegen.nextLabel();
        String finalExit = Codegen.nextLabel();
        // push op0, op1, pop op1, op0, check equal
        myExp1.codeGen(); // push op0
        myExp2.codeGen(); // push op1
        Codegen.genPop(Codegen.T1); // pop op1
        Codegen.genPop(Codegen.T0); // pop op0
        Codegen.generate("bge", Codegen.T0, Codegen.T1, trueExit);
        // evaluate to be false, push 1, skip false
        Codegen.generate("li", Codegen.T0, Codegen.FALSE);
        Codegen.genPush(Codegen.T0);
        Codegen.generate("j", finalExit);
        // evaluate to be true, push 0, fall through
        Codegen.genLabel(trueExit);
        Codegen.generate("li", Codegen.T0, Codegen.TRUE);
        Codegen.genPush(Codegen.T0);
        // final exit, nop
        Codegen.genLabel(finalExit);
        Codegen.generate("nop");
        Codegen.p.write("\t# GreaterEq End\n");
    }

    public void unparse(PrintWriter p, int indent) {
        p.print("(");
        myExp1.unparse(p, 0);
        p.print(" >= ");
        myExp2.unparse(p, 0);
        p.print(")");
    }
}
