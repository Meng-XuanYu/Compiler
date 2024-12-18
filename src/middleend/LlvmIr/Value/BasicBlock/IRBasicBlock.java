package middleend.LlvmIr.Value.BasicBlock;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRLabelType;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.IRNode;
import middleend.LlvmIr.Value.Instruction.IRInstruction;

import java.util.ArrayList;
import java.util.LinkedList;

public class IRBasicBlock extends IRValue implements IRNode {
    private final LinkedList<IRInstruction> instructions;
    private IRFunction parentFunction;

    public IRBasicBlock() {
        super(IRLabelType.getLabelType());// 生成一个label
        this.instructions = new LinkedList<>();
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
        return new ArrayList<>(this.instructions);
    }

    public void addIrInstruction(IRInstruction instruction) {
        this.instructions.add(instruction);
    }

    public void addAllIrInstruction(ArrayList<IRInstruction> instructions) {
        this.instructions.addAll(instructions);
    }

    public ArrayList<IRInstruction> extractAlloca() {
        ArrayList<IRInstruction> ans = new ArrayList<>();
        for (int i = 0; i < this.instructions.size(); i++) {
            IRInstruction instruction = this.instructions.get(i);
            if (instruction.printIR().get(0).contains("alloca")) {
                ans.add(instruction);
                this.instructions.remove(i);
                i--;
            }
        }
        return ans;
    }

    public ArrayList<IRInstruction> extractStore() {
        ArrayList<IRInstruction> ans = new ArrayList<>();
        for (int i = 0; i < this.instructions.size(); i++) {
            IRInstruction instruction = this.instructions.get(i);
            String string = instruction.printIR().get(0);
            if (string.contains("%param") && string.contains("store")) {
                ans.add(instruction);
                this.instructions.remove(i);
                i--;
            }
        }
        return ans;
    }

    public void addEntry(ArrayList<IRInstruction> instructions) {
        for (int i = instructions.size() - 1; i >= 0; i--) {
            this.instructions.add(0,instructions.get(i));
        }
    }
}
