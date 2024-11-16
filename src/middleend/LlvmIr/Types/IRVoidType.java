package middleend.LlvmIr.Types;

public class IRVoidType extends IRValueType {
    // Represents the void type in LLVM IR
    private static IRVoidType voidType = new IRVoidType();

    private IRVoidType() {
        //super();
    }

    public static IRVoidType getVoidType() {
        return voidType;
    }
}
