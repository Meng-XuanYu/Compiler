package middleend.Symbol;

import frontend.SymbolParser.SymbolType;

import java.util.ArrayList;

public class SymbolConst extends Symbol {
    // 存储常量的值
    // 就算是char也用int存储，具体类型在生成LLVM IR时再转换
    private int valueInt;
    private ArrayList<Integer> valueIntArray;

    public SymbolConst(String name, SymbolType symbolType) {
        super(name, symbolType);
    }

    public void setValueInt(int valueInt) {
        this.valueInt = valueInt;
    }

    public void setValueIntArray(ArrayList<Integer> valueIntArray) {
        this.valueIntArray = valueIntArray;
    }

    public int getValueInt() {
        return valueInt;
    }

    public ArrayList<Integer> getValueIntArray() {
        return valueIntArray;
    }
}
