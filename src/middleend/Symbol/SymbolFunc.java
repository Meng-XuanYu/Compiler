package middleend.Symbol;

import frontend.SymbolParser.SymbolType;

import java.util.ArrayList;

// 相当没用的类，就是存个符号表，暂时保留吧
public class SymbolFunc extends Symbol{
    private ArrayList<Symbol> symbols = new ArrayList<>();

    public SymbolFunc(String name, SymbolType symbolType) {
        super(name, symbolType);
    }

    public void addSymbol(Symbol symbol) {
        this.symbols.add(symbol);
    }

    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }
}
