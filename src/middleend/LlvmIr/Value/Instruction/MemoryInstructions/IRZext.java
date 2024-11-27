package middleend.LlvmIr.Value.Instruction.MemoryInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

public class IRZext extends IRInstruction {

    public IRZext(IRValue value, IRValueType type) {
        super(IRInstructionType.Zext, type, 1);
        this.setOperand(value, 0);
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        String sb = this.getName() + " = zext " +
                "i8 " +
                this.getOperand(0).getName() +
                " to " +
                "i32\n";
        ans.add(sb);
        return ans;
    }
}
