package middleend.LlvmIr.Types;

public class IRCharArrayType extends IRValueType {
    private int size;
    private IRCharType valueType;

    public IRCharArrayType(IRCharType valueType, int size) {
        this.valueType = valueType;
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public IRCharType getType() {
        return valueType;
    }
}
