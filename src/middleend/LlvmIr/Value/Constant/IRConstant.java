package middleend.LlvmIr.Value.Constant;
import middleend.LlvmIr.IRUser;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.IRNode;

import java.util.ArrayList;

public class IRConstant extends IRUser implements IRNode {
    public IRConstant(IRValueType IRValueType) {
        super(IRValueType);
    }

    // 用于常量数组
    public IRConstant(IRValueType IRValueType, int numOfOperands) {
        super(IRValueType, numOfOperands);
    }
}
