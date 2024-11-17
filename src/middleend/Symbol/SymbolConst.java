package middleend.Symbol;

import frontend.SymbolParser.SymbolType;

import java.util.ArrayList;

public class SymbolConst extends Symbol {
    // 存储常量的值
    private int valueInt;
    private ArrayList<Integer> valueIntArray;
    private Character valueChar;
    private ArrayList<Character> valueCharArray;

    public SymbolConst(String name, SymbolType symbolType) {
        super(name, symbolType);
    }

    public void setValueInt(int valueInt) {
        this.valueInt = valueInt;
    }

    public void setValueIntArray(ArrayList<Integer> valueIntArray) {
        this.valueIntArray = valueIntArray;
    }

    public void setValueChar(Character valueChar) {
        this.valueChar = valueChar;
    }

    public void setValueCharArray(ArrayList<Character> valueCharArray) {
        this.valueCharArray = valueCharArray;
    }

    public int getValueInt() {
        return valueInt;
    }

    public ArrayList<Integer> getValueIntArray() {
        return valueIntArray;
    }

    public Character getValueChar() {
        return valueChar;
    }

    public ArrayList<Character> getValueCharArray() {
        return valueCharArray;
    }
}
