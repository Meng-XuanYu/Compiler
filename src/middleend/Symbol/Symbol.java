package middleend.Symbol;

import frontend.SymbolParser.SymbolType;
import middleend.LlvmIr.IRValue;

public class Symbol {
    private final String name;
    private final SymbolType symbolType;
    private IRValue value;

    public Symbol(String name, SymbolType symbolType) {
        this.name = name;
        this.symbolType = symbolType;
    }

    public Symbol(String name, SymbolType symbolType, IRValue value) {
        this.name = name;
        this.symbolType = symbolType;
        this.value = value;
    }

    public void setValue(IRValue value) {
        this.value = value;
    }

    public IRValue getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public boolean isArray() {
        return symbolType == SymbolType.CharArray || symbolType == SymbolType.ConstCharArray || symbolType == SymbolType.IntArray || symbolType == SymbolType.ConstIntArray;
    }

    public boolean isChar() {
        return symbolType == SymbolType.Char || symbolType == SymbolType.ConstChar || symbolType == SymbolType.CharArray || symbolType == SymbolType.ConstCharArray;
    }
}
