package middleend.LlvmIr.Types;

import middleend.LlvmIr.Value.IRNode;

import java.util.ArrayList;

public class IRValueType implements IRNode {
    @Override
    public ArrayList<String> printIR() {
        return null;
    }

    public boolean isInt32() {
        return (this instanceof IRIntegerType && ((IRIntegerType) this).getBitWidth() == 32) ||
                (this instanceof IRIntArrayType && ((IRIntArrayType) this).getType() != null && ((IRIntArrayType) this).getType().getBitWidth() == 32) ||
                (this instanceof IRPointerType && ((IRPointerType) this).getContained() instanceof IRIntegerType && ((IRIntegerType) ((IRPointerType) this).getContained()).getBitWidth() == 32);
    }
}
