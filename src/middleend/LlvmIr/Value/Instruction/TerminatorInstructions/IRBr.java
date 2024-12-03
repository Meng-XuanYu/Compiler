package middleend.LlvmIr.Value.Instruction.TerminatorInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRLabelType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

public class IRBr extends IRInstruction {
    private IRInstructionType type;
    public IRBr(IRValue oprand1, IRValue label, IRValue endLabel, IRInstructionType type) {
        super(type, IRLabelType.getLabelType(), 3);
        this.setOperand(oprand1, 0);
        this.setOperand(label, 1);
        this.setOperand(endLabel, 2);
        this.setName(type.name());
        this.type = type;
    }

    public IRValue getLeft() {
        return this.getOperand(0);
    }

    public IRValue getLabel() {
        return this.getOperand(1);
    }

    public IRValue getElseLabel() {
        return this.getOperand(2);
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append("br i1 ").append(this.getLeft().getName()).append(", label %").append(this.getLabel().getName()).append(", label %").append(this.getElseLabel().getName()).append("\n");


        ans.add(sb.toString());
        return ans;
    }
}
