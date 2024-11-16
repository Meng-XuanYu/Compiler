package middleend.LlvmIr.Types;

public class IRCharType extends IRValueType {
    // Represents the i8 type in LLVM IR

    public IRCharType() {
        super();
    }

    @Override
    public String toString() {
        return "i8";
    }
}
