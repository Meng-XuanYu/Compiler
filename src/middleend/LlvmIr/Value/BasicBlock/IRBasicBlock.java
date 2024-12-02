package middleend.LlvmIr.Value.BasicBlock;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRLabelType;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.IRNode;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import java.util.ArrayList;

public class IRBasicBlock extends IRValue implements IRNode {
    private final ArrayList<IRInstruction> instructions;
    private IRFunction parentFunction;

    public IRBasicBlock() {
        super(IRLabelType.getLabelType());// 生成一个label
        this.instructions = new ArrayList<>();
    }

    public void setParentFunction(IRFunction parentFunction) {
        this.parentFunction = parentFunction;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        for (IRInstruction instruction : this.instructions) {
            ans.addAll(instruction.printIR());
        }
        return ans;
    }

    public IRFunction getParentFunction() {
        return this.parentFunction;
    }

    public ArrayList<IRInstruction> getInstructions() {
        return this.instructions;
    }

    public void addIrInstruction(IRInstruction instruction) {
        this.instructions.add(instruction);
    }

    public void addAllIrInstruction(ArrayList<IRInstruction> instructions) {
        this.instructions.addAll(instructions);
    }
}
