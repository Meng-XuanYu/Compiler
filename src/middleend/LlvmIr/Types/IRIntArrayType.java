package middleend.LlvmIr.Types;

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
}
