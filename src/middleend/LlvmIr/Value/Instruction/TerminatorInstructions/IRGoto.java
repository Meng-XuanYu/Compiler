package middleend.LlvmIr.Value.Instruction.TerminatorInstructions;

import middleend.LlvmIr.Types.IRLabelType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;
import middleend.LlvmIr.Value.Instruction.IRLabel;

import java.util.ArrayList;

public class IRGoto extends IRInstruction {
    public IRGoto(IRLabel label) {
        super(IRInstructionType.Goto, IRLabelType.getLabelType(), 1);
        setOperand(label, 0);
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add("goto " + this.getOperand(0).getName() + "\n");
        return ans;
    }
}
