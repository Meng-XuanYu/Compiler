package middleend.Symbol;
import java.util.HashMap;

// 符号表
// 每个block维护一个
public class SymbolTable {
    private final HashMap<String, Symbol> symbols;
    private final SymbolTable parent;
    private final int depth;

    // 第一个符号表，没有父符号表
    public SymbolTable() {
        this.symbols = new HashMap<>();
        this.parent = null;
        this.depth = 0;
    }

    public SymbolTable(SymbolTable parent) {
        this.symbols = new HashMap<>();
        this.parent = parent;
        this.depth = parent.getDepth() + 1;
    }

    public int getDepth() {
        return depth;
    }

    public SymbolTable getParent() {
        return parent;
    }

    // 因为到了中间代码生成这一步，已经保证了符号表中没有重复的符号
    public void addSymbol(Symbol symbol) {
        this.symbols.put(symbol.getName(), symbol);
    }

    // 从当前符号表开始，逐级向上查找符号
    public Symbol getSymbol(String name) {
        SymbolTable table = this;
        while (table != null) {
            if (table.symbols.containsKey(name)) {
                return table.symbols.get(name);
            }
            table = table.parent;
        }
        return null;
    }
}
