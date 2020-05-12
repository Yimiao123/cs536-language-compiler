
import java.util.*; // in order to use list and hashmap

// OVERVIEW: a class that represents a symbol table, it's a list of hashmaps, each hashmap is a scope
public class SymTable {

    private List<HashMap<String, Sym>> symbol_table; // the symbol table data structure

    // EFFECTS: initialize the list to be empty hashmap
    public SymTable() {
        symbol_table = new LinkedList<HashMap<String, Sym>>(); // create an empty list
        symbol_table.add(new HashMap<String, Sym>()); // add an empty hashmap to the list
    }

    // EFFECTS: add a empty hashmap to the front of the list
    public void addScope() {
        symbol_table.add(0, new HashMap<String, Sym>());
    }

    // EFFECTS: remove the first hashmap in the list
    // EXCEPTIONS: if list is empty, throw EmptySymTableException
    public void removeScope() throws EmptySymTableException {
        if (symbol_table.isEmpty())
            throw new EmptySymTableException();

        symbol_table.remove(0);
    }

    // EFFECTS: add the name and sym to the symbol table's first hashmap
    // EXCEPTIONS:
    // if list is empty, throw EmptySymTableException
    // if arguments contain null, throw IllegalArgumentException
    // if first hashmap already contains the argumnt, throw DuplicateSymException
    public void addDecl(String name, Sym sym)
            throws EmptySymTableException, IllegalArgumentException, DuplicateSymException {
        if (symbol_table.isEmpty())
            throw new EmptySymTableException();
        if (name == null || sym == null)
            throw new IllegalArgumentException();
        if (symbol_table.get(0).containsKey(name))
            throw new DuplicateSymException();

        symbol_table.get(0).put(name, sym);
    }

    // EFFECTS: search name in first hashmap, if found return symbol, otherwise
    // return null
    // EXCEPTIONS: if the list is empty, throw EmptySymTableException
    public Sym lookupLocal(String name) throws EmptySymTableException {
        if (symbol_table.isEmpty())
            throw new EmptySymTableException();

        return symbol_table.get(0).get(name);
    }

    // EFFECTS: traverse list, search name, if found return symbol, otherwise return
    // null
    // EXCEPTIONS: if the list is empty, throw EmptySymTableException
    public Sym lookupGlobal(String name) throws EmptySymTableException {
        if (symbol_table.isEmpty())
            throw new EmptySymTableException();

        ListIterator<HashMap<String, Sym>> iter = symbol_table.listIterator();
        Sym target_symbol = null;

        while (iter.hasNext()) {
            target_symbol = iter.next().get(name);
            if (target_symbol != null)
                return target_symbol;
        }

        return null;
    }

    // EFFECTS: print the whole list
    public void print() {
        ListIterator<HashMap<String, Sym>> iter = symbol_table.listIterator();

        System.out.print("\nSym Table\n");
        while (iter.hasNext())
            System.out.println(iter.next().toString());
        System.out.println();
    }

}