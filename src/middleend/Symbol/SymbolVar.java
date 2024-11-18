package middleend.Symbol;

import frontend.SymbolParser.SymbolType;

import java.util.ArrayList;

public class SymbolVar extends Symbol {
    private int initVal;
    private ArrayList<Integer> initValArray;
    boolean all0 = false;

    public SymbolVar(String name, SymbolType symbolType) {
        super(name, symbolType);
    }

    public void setInitVal(int initVal) {
        this.initVal = initVal;
    }

    public void setAll0(boolean all0) {
        this.all0 = all0;
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
