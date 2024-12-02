package middleend.Symbol;

import frontend.SymbolParser.SymbolType;
import middleend.LlvmIr.IRValue;

import java.util.ArrayList;

public class SymbolVar extends Symbol {
    private int initVal;
    private ArrayList<Integer> initValArray;
    boolean all0 = false;
    private IRValue irValue; // 中间代码的对应的指令

    public void setIns(IRValue irValue) {
        this.irValue = irValue;
    }

    public IRValue getIns() {
        return irValue;
    }

    public SymbolVar(String name, SymbolType symbolType) {
        super(name, symbolType);
    }

    public SymbolVar(String name, SymbolType symbolType, IRValue irValue) {
        super(name, symbolType);
        this.setValue(irValue);
    }

    public void setInitVal(int initVal) {
        this.initVal = initVal;
    }

    public void setAll0() {
        this.all0 = true;
    }

    public boolean getAll0() {
        return this.all0;
    }

    public void setInitValArray(ArrayList<Integer> initValArray) {
        this.initValArray = initValArray;
    }

    public int getInitVal() {
        return initVal;
    }

    public ArrayList<Integer> getInitValArray() {
        return initValArray;
    }
}
