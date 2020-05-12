// OVERVIEW: a class to test the Sym and SymTable class methods under all situations, including boundary and non-boundary cases
public class P1 {

    // EFFECTS: call all test functions
    public static void main(String[] args) {
        testSym();
        testSymTable();
    }

    // EFFECTS: test Sym class and print info
    private static void testSym() {
        // print begin info
        System.out.println();
        System.out.println("---Test Begins: Sym Class---");

        // TEST: constructor
        // EXPECTED: a symbol with type "cs536"
        String test_type = "cs536";
        Sym test_sym = new Sym(test_type);
        System.out.println("Correct: Sym Constructor");

        // TEST: getType()
        // EXPECTED: return type to be "cs536"
        test_type = "cs536";
        test_sym = new Sym(test_type);
        String return_type_0 = test_sym.getType();
        if (return_type_0 == test_type) {
            System.out.println("Correct: Sym getType()");
        } else {
            System.out.println("Error: Sym getType() returns unexpected value");
        }

        // TEST: toString()
        // EXPECTED: return "cs536"
        test_type = "cs536";
        test_sym = new Sym(test_type);
        String return_type_1 = test_sym.toString();
        if (return_type_1 == test_type) {
            System.out.println("Correct: Sym toString()");
        } else {
            System.out.println("Error: Sym toString() returns unexpected value");
        }

        // print finish info
        System.out.println("---Test Ends: Sym Class---");
        System.out.println();
    }

    // EFFECTS: test SymTable class and print info
    private static void testSymTable() {
        // print begin info
        System.out.println();
        System.out.println("---Test Begins: SymTable Class---");

        // TEST: constructor
        // EXPECTED: a list with one empty scope
        SymTable test_symtable = new SymTable();
        test_symtable.print();
        System.out.println("Correct: Constructor");

        // TEST: addScope()
        // EXPECTED: a new empty scope is added
        test_symtable = new SymTable();
        test_symtable.print();
        test_symtable.addScope();
        test_symtable.print();
        System.out.println("Correct: addScope() adds a new empty scope");

        // TEST: addScope() for 9 times
        // EXPECTED: the list contains 10 empty scope
        test_symtable = new SymTable();
        test_symtable.print();
        for (int i = 0; i < 9; ++i)
            test_symtable.addScope();
        test_symtable.print();
        System.out.println("Correct: addScope() adds 9 new empty scope");

        // TEST: removeScope() when there is no scope
        // EXPECTED: throw EmptySymTableException
        test_symtable = new SymTable();
        try {
            test_symtable.removeScope();
            test_symtable.removeScope();
            System.out.println("Error: removeScope() doesn't throw EmptySymTableException");
        } catch (EmptySymTableException e) {
            System.out.println("Correct: removeScope() throws EmptySymTableException");
        } catch (Exception e) {
            System.out.println("Error: removeScope() doesn't throw EmptySymTableException");
        }

        // TEST: removeScope() when there are two scopes
        // EXPECTED: one scope is removed
        test_symtable = new SymTable();
        try {
            test_symtable.addScope();
            test_symtable.print();
            test_symtable.removeScope();
            test_symtable.print();
            System.out.println("Correct: removeScope() removes a scope");
        } catch (Exception e) {
            System.out.println("Error: removeScope() doesn't successfully remove a scope");
        }

        // TEST: removeScope() for 9 times
        // EXPECTED: remain one empty scope
        test_symtable = new SymTable();
        try {
            for (int i = 0; i < 9; ++i)
                test_symtable.addScope();
            test_symtable.print();
            for (int i = 0; i < 9; ++i)
                test_symtable.removeScope();
            test_symtable.print();
            System.out.println("Correct: removeScope() removes 9 empty scope");
        } catch (Exception e) {
            System.out.println("Error: removeScope() throws an exception");
        }

        // TEST: addDecl() when current scope is empty
        // EXPECTED: throw EmptySymTableException
        test_symtable = new SymTable();
        String test_name = "a";
        String test_type = "int";
        Sym test_sym = new Sym(test_type);
        try {
            test_symtable.removeScope();
            test_symtable.addDecl(test_name, test_sym);
            System.out.println("Error: addDecl() doesn't throw EmptySymTableException");
        } catch (EmptySymTableException e) {
            System.out.println("Correct: addDecl() throws EmptySymTableException");
        } catch (Exception e) {
            System.out.println("Error: addDecl() doesn't throw EmptySymTableException");
        }

        // TEST: addDecl() when argument is null
        // EXPECTED: throw IllegalArgumentException
        test_symtable = new SymTable();
        try {
            test_symtable.addDecl(null, null);
            System.out.println("Error: addDecl() doesn't throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            System.out.println("Correct: addDecl() throws IllegalArgumentException");
        } catch (Exception e) {
            System.out.println("Error: addDecl() doesn't throw IllegalArgumentException");
        }

        // TEST: addDecl() when the symbol is already in current scope
        // EXPECTED: throw DuplicateSymException
        test_symtable = new SymTable();
        test_name = "a";
        test_type = "int";
        test_sym = new Sym(test_type);
        try {
            test_symtable.addDecl(test_name, test_sym);
            test_symtable.addDecl(test_name, test_sym);
            System.out.println("Error: addDecl() doesn't throw DuplicateSymException");
        } catch (DuplicateSymException e) {
            System.out.println("Correct: addDecl() throws DuplicateSymException");
        } catch (Exception e) {
            System.out.println("Error: addDecl() doesn't throw DuplicateSymException");
        }

        // TEST: addDecl()
        // EXPECTED: the declaration is added to the current scope
        test_symtable = new SymTable();
        test_name = "a";
        test_type = "int";
        test_sym = new Sym(test_type);
        try {
            test_symtable.addDecl(test_name, test_sym);
            test_symtable.print();
            System.out.println("Correct: addDecl() adds a declaration");
        } catch (Exception e) {
            System.out.println("Error: addDecl() doesn't successfully add a declaration");
        }

        // TEST: lookupLocal() when there is no scope
        // EXPECTED: throw EmptySymTableException
        test_symtable = new SymTable();
        test_name = "a";
        test_type = "int";
        test_sym = new Sym(test_type);
        try {
            test_symtable.removeScope();
            test_symtable.lookupLocal(test_name);
            System.out.println("Error: lookupLocal() doesn't throw EmptySymTableException");
        } catch (EmptySymTableException e) {
            System.out.println("Correct: lookupLocal() throws EmptySymTableException");
        } catch (Exception e) {
            System.out.println("Error: lookupLocal() doesn't throw EmptySymTableException");
        }

        // TEST: lookupLocal() when the target exists
        // EXPECTED: return the corresponding symbol
        test_symtable = new SymTable();
        test_name = "a";
        test_type = "int";
        test_sym = new Sym(test_type);
        Sym test_return_symbol = null;
        try {
            test_symtable.addDecl(test_name, test_sym);
            test_return_symbol = test_symtable.lookupLocal(test_name);
            if (test_return_symbol != null && test_return_symbol.getType() == test_type) {
                System.out.println("Correct: lookupLocal() finds the existing target");
            } else {
                System.out.println("Error: lookupLocal() doesn't find the existing target");
            }
        } catch (Exception e) {
            System.out.println("Error: lookupLocal() throws an exception");
        }

        // TEST: lookupLocal() when the target doesn't exist
        // EXPECTED: return null
        test_symtable = new SymTable();
        test_name = "a";
        test_type = "int";
        test_sym = new Sym(test_type);
        test_return_symbol = null;
        try {
            test_return_symbol = test_symtable.lookupLocal(test_name);
            if (test_return_symbol == null) {
                System.out.println("Correct: lookupLocal() doesn't find the non-existing target");
            } else {
                System.out.println("Error: lookupLocal() finds the non-existing target");
            }
        } catch (Exception e) {
            System.out.println("Error: lookupLocal() throws an exception");
        }

        // TEST: lookupGlobal() when there is no scope
        // EXPECTED: throw EmptySymTableException
        test_symtable = new SymTable();
        test_name = "a";
        test_type = "int";
        test_sym = new Sym(test_type);
        test_return_symbol = null;
        try {
            test_symtable.removeScope();
            test_symtable.lookupGlobal(test_name);
            System.out.println("Error: lookupGlobal() doesn't throw EmptySymTableException");
        } catch (EmptySymTableException e) {
            System.out.println("Correct: lookupGlobal() throws EmptySymTableException");
        } catch (Exception e) {
            System.out.println("Error: lookupGlobal() doesn't throw EmptySymTableException");
        }

        // TEST: lookupGlobal() when there is one scope and target exists
        // EXPECTED: return the corresponding symbol
        test_symtable = new SymTable();
        test_name = "a";
        test_type = "int";
        test_sym = new Sym(test_type);
        test_return_symbol = null;
        try {
            test_symtable.addDecl(test_name, test_sym);
            test_return_symbol = test_symtable.lookupGlobal(test_name);
            if (test_return_symbol != null && test_return_symbol.getType() == test_type) {
                System.out.println("Correct: lookupGlobal() finds the existing target");
            } else {
                System.out.println("Error: lookupGlobal() doesn't find the existing target");
            }
        } catch (Exception e) {
            System.out.println("Error: lookupGlobal() throws an exception");
        }

        // TEST: lookupGlobal() when there is one scope and target doesn't exist
        // EXPECTED: return the corresponding symbol
        test_symtable = new SymTable();
        test_name = "a";
        test_type = "int";
        test_sym = new Sym(test_type);
        test_return_symbol = null;
        try {
            test_return_symbol = test_symtable.lookupGlobal(test_name);
            if (test_return_symbol == null) {
                System.out.println("Correct: lookupGlobal() doesn't find the non-existing target");
            } else {
                System.out.println("Error: lookupGlobal() finds the non-existing target");
            }
        } catch (Exception e) {
            System.out.println("Error: lookupGlobal() throws an exception");
        }

        // TEST: lookupGlobal() when there is two scope and target exists
        // EXPECTED: return the first corresponding symbol
        test_symtable = new SymTable();
        String test_name_1 = "a";
        String test_type_1 = "int";
        Sym test_sym_1 = new Sym(test_type_1);
        String test_name_2 = "b";
        String test_type_2 = "long";
        Sym test_sym_2 = new Sym(test_type_2);
        test_return_symbol = null;
        try {
            test_symtable.addDecl(test_name_1, test_sym_1);
            test_symtable.addDecl(test_name_2, test_sym_1);
            test_symtable.addScope();
            test_symtable.addDecl(test_name_2, test_sym_2);
            test_symtable.print();
            test_return_symbol = test_symtable.lookupGlobal(test_name_2);
            if (test_return_symbol != null && test_return_symbol.getType() == test_type_2) {
                System.out.println("Correct: lookupGlobal() finds the frirst existing target");
            } else {
                System.out.println("Correct: lookupGlobal() doesn't find the frirst existing target");
            }
        } catch (Exception e) {
            System.out.println("Error: lookupGlobal() throws an exception");
        }

        // TEST: lookupGlobal() when there is two scope and target doesn't exist
        // EXPECTED: return the first corresponding symbol
        test_symtable = new SymTable();
        test_name_1 = "a";
        test_type_1 = "int";
        test_sym_1 = new Sym(test_type_1);
        test_name_2 = "b";
        test_type_2 = "long";
        test_sym_2 = new Sym(test_type_2);
        test_return_symbol = null;
        try {
            test_symtable.addDecl(test_name_1, test_sym_1);
            test_symtable.addScope();
            test_symtable.addDecl(test_name_1, test_sym_2);
            test_symtable.print();
            test_return_symbol = test_symtable.lookupGlobal(test_name_2);
            if (test_return_symbol == null) {
                System.out.println("Correct: lookupGlobal() doesn't find the non-existing target");
            } else {
                System.out.println("Correct: lookupGlobal() finds the non-existing target");
            }
        } catch (Exception e) {
            System.out.println("Error: lookupGlobal() throws an exception");
        }

        // TEST: comprehensive test
        // EXPECTED: written in output message
        test_symtable = new SymTable();
        test_name_1 = "a";
        test_type_1 = "int";
        test_sym_1 = new Sym(test_type_1);
        test_name_2 = "b";
        test_type_2 = "long";
        test_sym_2 = new Sym(test_type_2);
        String test_name_3 = "c";
        String test_type_3 = "char";
        Sym test_sym_3 = new Sym(test_type_3);
        String test_name_4 = "d";
        String test_type_4 = "bool";
        Sym test_sym_4 = new Sym(test_type_4);
        Sym test_return_symbol_local = null;
        Sym test_return_symbol_global = null;

        try {
            test_symtable.addDecl(test_name_1, test_sym_1);
            test_symtable.addDecl(test_name_2, test_sym_2);
            test_symtable.addDecl(test_name_3, test_sym_3);
            test_symtable.addDecl(test_name_4, test_sym_4);
            test_symtable.addScope();
            test_symtable.addDecl(test_name_1, test_sym_2);
            test_symtable.addDecl(test_name_2, test_sym_3);
            test_symtable.addDecl(test_name_3, test_sym_4);
            test_symtable.addScope();
            test_symtable.addDecl(test_name_1, test_sym_3);
            test_symtable.addDecl(test_name_2, test_sym_4);
            test_symtable.addScope();
            test_symtable.addDecl(test_name_1, test_sym_4);
            test_symtable.print();
            System.out.println("Correct: Comprehensive test adds three scopes");
            test_symtable.removeScope();
            test_symtable.print();
            System.out.println("Correct: Comprehensive test removes one scope");
            test_return_symbol_local=test_symtable.lookupLocal(test_name_3);
            if(test_return_symbol_local==null){
                System.out.println("Correct: Comprehensive test cannot find "+test_name_3+" in local scope");
            }else{
                System.out.println("Error: Comprehensive test finds "+test_name_3+" in local scope");
            }
            test_return_symbol_local=test_symtable.lookupLocal(test_name_1);
            if(test_return_symbol_local!=null && test_return_symbol_local.getType()==test_type_3){
                System.out.println("Correct: Comprehensive test finds "+test_name_1+" with type "+test_type_3+" in local scope");
            }else{
                System.out.println("Error: Comprehensive test cannot find "+test_name_1+" in local scope");
            }
            test_return_symbol_global=test_symtable.lookupGlobal(test_name_3);
            if(test_return_symbol_global!=null && test_return_symbol_global.getType()==test_type_4){
                System.out.println("Correct: Comprehensive test finds "+test_name_3+" with type "+test_type_4+" in global scope");
            }else{
                System.out.println("Error: Comprehensive test cannot find "+test_name_3+" with type "+test_type_4+" in global scope");
            }
        } catch (Exception e) {
            System.out.println("Error: Comprehensive test throws an exception");
        }

        // print finish info
        System.out.println("---Test Ends: SymTable Class---");
        System.out.println();
    }

}