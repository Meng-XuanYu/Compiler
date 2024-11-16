package middleend.LlvmIr.Value.Constant;

import middleend.LlvmIr.Types.IRValueType;

import java.util.ArrayList;

public class IRConstantInt extends IRConstant {
    private int value;

    public IRConstantInt(int value, IRValueType type) {
        super(type);
        this.value = value;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(String.valueOf(this.value));
        return ret;
    }
}
