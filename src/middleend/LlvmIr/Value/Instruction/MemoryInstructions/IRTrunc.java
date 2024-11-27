package middleend.LlvmIr.Value.Instruction.MemoryInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

public class IRTrunc extends IRInstruction {
    public IRTrunc(IRValue value, IRValueType type) {
        super(IRInstructionType.Trunc, type, 1);
        this.setOperand(value, 0);
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        String sb = this.getName() + " = trunc " +
                "i32 " +
                this.getOperand(0).getName() +
                " to " +
                "i8\n";
        ans.add(sb);
        return ans;
    }
}
