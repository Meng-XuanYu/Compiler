package middleend.LlvmIr.Value.Instruction;

import middleend.LlvmIr.Types.IRLabelType;
import java.util.ArrayList;

// Instruction 的一种, 用于表示一个标签
// 如: label1:
public class IRLabel extends IRInstruction {
    public IRLabel(String name) {
        super(IRInstructionType.Label, IRLabelType.getLabelType(), 0);
        super.setName(name);
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add(this.getName() + ": \n");
        return ans;
    }
}
