package backend.MipsBlock;

import backend.MipsFunction.MipsFunction;
import backend.MipsInstruction.MipsInstruction;
import backend.MipsNode;
import backend.MipsSymbol.MipsSymbolTable;

import java.util.ArrayList;

public class MipsBasicBlock implements MipsNode {
    private final MipsSymbolTable symbolTable;
    private final MipsFunction parent;
    private final ArrayList<MipsInstruction> instructions;

    public MipsBasicBlock(MipsFunction parent) {
        this.parent = parent;
        this.instructions = new ArrayList<>();
        this.symbolTable = this.parent.getSymbolTable();
    }

    public void addInstruction(ArrayList<MipsInstruction> instructions) {
        if (instructions != null && !instructions.isEmpty()) {
            this.instructions.addAll(instructions);
        }
    }

    public MipsSymbolTable getSymbolTable() {
        return symbolTable;
    }

    public MipsFunction getParent() {
        return this.parent;
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ans = new ArrayList<>();
        for (MipsInstruction instruction : instructions) {
            ArrayList<String> temp = instruction.printMips();
            if (temp != null && !temp.isEmpty()) {
                ans.addAll(temp);
            }
        }
        return ans;
    }

    public void addInstructions(ArrayList<MipsInstruction> initsInstructions) {
        if (initsInstructions != null && !initsInstructions.isEmpty()) {
            this.instructions.addAll(initsInstructions);
        }
    }

    public ArrayList<MipsInstruction> getInstructions() {
        return instructions;
    }

    public MipsSymbolTable getTable() {
        return symbolTable;
    }
}
