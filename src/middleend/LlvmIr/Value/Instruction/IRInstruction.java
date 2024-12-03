package middleend.LlvmIr.Value.Instruction;

import middleend.LlvmIr.IRUser;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.BasicBlock.IRBasicBlock;
import middleend.LlvmIr.Value.IRNode;

public class IRInstruction extends IRUser implements IRNode {
    private IRBasicBlock parent; // The basic block that contains this instruction
    private IRInstructionType type;

    public IRInstruction(IRInstructionType type, IRValueType valueType, int numOfOperands) {
        super(valueType, numOfOperands);
        this.type = type;
    }

    public IRInstruction(IRInstructionType type, IRValueType valueType, int numOfOperands, IRBasicBlock parent) {
        this(type, valueType, numOfOperands);
        this.parent = parent;
    }

    public boolean isArithmeticBinary() {
        return this.type.ordinal() <= IRInstructionType.Div.ordinal();
    }

    public boolean isLogicBinary() {
        return this.type.ordinal() >= IRInstructionType.Lt.ordinal() && this.type.ordinal() <= IRInstructionType.Or.ordinal();
    }

    public IRInstructionType getInstructionType() {
        return this.type;
    }

}
