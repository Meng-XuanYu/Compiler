package middleend.LlvmIr.Value.Instruction.TerminatorInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRLabelType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

// 无条件跳转指令
// 在LLVM IR中的作用类似于传统编程语言中的 goto 或 jump
// 语法格式为：br label <label>
// 其中 label 是一个标签，表示跳转的目标
public class IRBr extends IRInstruction {
    public IRBr(IRValue oprand1, IRValue oprand2, IRValue label, IRInstructionType type) {
        super(type, IRLabelType.getLabelType(), 3);
        this.setOperand(oprand1, 0);
        this.setOperand(oprand2, 1);
        this.setOperand(label, 2);
        this.setName(type.name());
    }

    public IRValue getLeft() {
        return this.getOperand(0);
    }

    public IRValue getRight() {
        return this.getOperand(1);
    }

    public IRValue getLabel() {
        return this.getOperand(2);
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        sb.append(this.getName()).append(" ").append(this.getLeft().getName()).append(", ").append(this.getRight().getName()).append(", ").append(this.getLabel().getName()).append("\n");
        ans.add(sb.toString());
        return ans;
    }
}
