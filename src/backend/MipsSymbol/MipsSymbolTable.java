package backend.MipsSymbol;

import backend.MipsBlock.MipsBasicBlock;

import java.util.HashMap;

public class MipsSymbolTable {
    private final RegisterTable registerTable;

    private HashMap<String, MipsSymbol> table;
    private int offset;

    public MipsSymbolTable(RegisterTable registerTable) {
        this.registerTable = registerTable;
        this.table = new HashMap<>();
        this.offset = 0;
    }

    public void addSymbol(String name, MipsSymbol symbol) {
        this.table.put(name, symbol);
    }

    public boolean hasSymbol(String name) {
        return this.table.containsKey(name);
    }

    public void addOffset(int offset) {
        this.offset += offset;
    }

    public int getOffset() {
        return this.offset;
    }

    public MipsSymbol getSymbol(String name) {
        return this.table.get(name);
    }

    public RegisterTable getRegisterTable() {
        return this.registerTable;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setTable(HashMap<String, MipsSymbol> table) {
        this.table = table;
    }

    public int getRegIndex(String name, boolean load, MipsBasicBlock basicBlock) {
        MipsSymbol symbol = this.table.get(name);
        if (symbol.isInReg()) {
            return symbol.getRegIndex();
        } else {
            int index = this.registerTable.getReg(symbol.isTemp(), symbol, basicBlock);
        }
    }
}
