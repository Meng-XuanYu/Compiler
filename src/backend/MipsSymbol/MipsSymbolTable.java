package backend.MipsSymbol;

import backend.MipsBlock.MipsBasicBlock;
import backend.MipsInstruction.Lw;
import backend.MipsInstruction.MipsInstruction;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsSymbolTable {
    private final RegisterTable registerTable;

    private HashMap<String, MipsSymbol> table;
    private int offset;// fp offset

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
            if (load) {
                Lw lw = new Lw(index, symbol.getBase(), symbol.getOffset());
                ArrayList<MipsInstruction> temp = new ArrayList<>();
                temp.add(lw);
                basicBlock.addInstruction(temp);
            }
            return index;
        }
    }

    public int getFpOffset() {
        return this.offset;
    }

    public HashMap<String,MipsSymbol> cloneTable() {
        HashMap<String, MipsSymbol> newSymbols = new HashMap<>();
        for (String index : this.table.keySet()) {
            newSymbols.put(index, this.table.get(index).cloneMipsSymbol());
        }
        return newSymbols;
    }
}
