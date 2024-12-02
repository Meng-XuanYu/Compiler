package middleend.LlvmIr.Types;

import java.util.ArrayList;

public class IRIntArrayType extends IRValueType{
    private int size; // -1代表未知大小，出现在形参部分
    private IRIntegerType valueType;

    public IRIntArrayType(IRValueType valueType, int size) {
        this.valueType = (IRIntegerType) valueType;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public IRIntegerType getType() {
        return valueType;
    }

    public boolean ischar() {
        return this.valueType.getBitWidth() == 8;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(size).append(" x ").append(valueType.printIR().get(0)).append("]");
        ans.add(sb.toString());
        return ans;
    }
}
