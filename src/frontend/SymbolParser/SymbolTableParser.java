package frontend.SymbolParser;
import java.util.*;

public class SymbolTableParser {
    private final HashMap<Integer, HashMap<String, SymbolParser>> table;
    private final HashMap<Integer, ArrayList<SymbolParser>> tableForPrint;
    private final HashMap<Integer, Integer> scopeTree;
    private int currentScope;
    private int topScope;

    public SymbolTableParser() {
        this.table = new HashMap<>();
        this.tableForPrint = new HashMap<>();
        this.scopeTree = new HashMap<>();
        this.scopeTree.put(1, 0);
        this.currentScope = 1;
        this.topScope = 1;
    }

    public void enterScope() {
        this.topScope++;
        this.scopeTree.put(this.topScope, this.currentScope);
        this.currentScope = this.topScope;
    }

    public void exitScope() {
        this.currentScope = this.scopeTree.get(this.currentScope);
    }

    public void addSymbol(String name, SymbolType type) {
        SymbolParser symbol;
        if (type == SymbolType.VoidFunc || type == SymbolType.CharFunc || type == SymbolType.IntFunc) {
            symbol = new SymbolParserFunc(name, type, this.currentScope);
        } else {
            symbol = new SymbolParser(name, type, this.currentScope);
        }
        addSymbolToTable(name, symbol);
    }

    public SymbolParser addSymbolFuncPara(String name, SymbolType type) {
        SymbolParser symbol = new SymbolParser(name, type, this.topScope + 1);
        addSymbolToTable(name, symbol);
        return symbol;
    }

    public boolean containsSymbolFuncPara(String name) {
        return this.table.containsKey(this.topScope + 1) && this.table.get(this.topScope + 1).containsKey(name);
    }

    private void addSymbolToTable(String name, SymbolParser symbol) {
        if (!this.table.containsKey(symbol.scope())) {
            this.table.put(symbol.scope(), new HashMap<>());
        }
        this.table.get(symbol.scope()).put(name, symbol);

        if (!this.tableForPrint.containsKey(symbol.scope())) {
            this.tableForPrint.put(symbol.scope(), new ArrayList<>());
        }
        this.tableForPrint.get(symbol.scope()).add(symbol);
    }

    public SymbolParser getSymbol(String name) {
        for (int i = this.currentScope; i > 0; i = this.scopeTree.get(i)) {
            if (this.table.containsKey(i) && this.table.get(i).containsKey(name)) {
                return this.table.get(i).get(name);
            }
        }
        return null;
    }

    public boolean notContainsSymbol(String name) {
        return getSymbol(name) == null;
    }

    public boolean containsSymbolInCurrentScope(String name) {
        return this.table.containsKey(this.currentScope) && this.table.get(this.currentScope).containsKey(name);
    }

    @Deprecated
    public ArrayList<String> printSymbolTable() {
        ArrayList<String> result = new ArrayList<>();
        for (int i = 1; i <= this.topScope; i++) {
            if (this.tableForPrint.containsKey(i)) {
                for (int j = 0; j < this.tableForPrint.get(i).size(); j++) {
                    SymbolParser symbol = this.tableForPrint.get(i).get(j);
                    if (!Objects.equals(symbol.name(), "main")) {
                        result.add(i + " " + symbol.name() + " " + symbol.type());
                    }
                }
            }
        }
        return result;
    }

}