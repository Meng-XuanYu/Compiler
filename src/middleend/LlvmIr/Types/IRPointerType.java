package middleend.LlvmIr.Types;

// alloca,gep
public class IRPointerType extends IRValueType {
    private final IRValueType contained; // 指针的类型

    public IRPointerType(IRValueType contained) {
        this.contained = contained;
    }

    public IRValueType getContained() {
        return contained;
    }
}
