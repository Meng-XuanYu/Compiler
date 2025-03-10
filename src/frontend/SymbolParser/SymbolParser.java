package frontend.SymbolParser;

public class SymbolParser {
    private final String name;
    private final SymbolType type;
    private final int scope;

    public SymbolParser(String name, SymbolType type, int scope) {
        this.name = name;
        this.type = type;
        this.scope = scope;
    }

    @Override
    public String toString() {
        return name + " " + type + " " + scope;
    }

    public boolean isConst() {
        return type == SymbolType.ConstChar || type == SymbolType.ConstInt || type == SymbolType.ConstCharArray || type == SymbolType.ConstIntArray;
    }

    public String name() {
        return name;
    }

    public SymbolType type() {
        return type;
    }

    public int scope() {
        return scope;
    }
}
