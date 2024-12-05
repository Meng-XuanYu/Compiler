package middleend.Symbol;

import frontend.SymbolParser.SymbolType;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.IRAlloca;

import java.util.ArrayList;

public class SymbolConst extends Symbol {
    // 存储常量的值
    // 就算是char也用int存储，具体类型在生成LLVM IR时再转换
    private int valueInt;
    private ArrayList<Integer> valueIntArray;
    private IRValue irValue; // 中间代码的对应的指令

    public void setIns(IRValue irValue) {
        this.irValue = irValue;
    }

    public IRValue getIns() {
        return irValue;
    }

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
