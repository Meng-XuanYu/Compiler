package backend.MipsBlock;

import backend.MipsFunction.MipsFunction;
import backend.MipsInstruction.MipsInstruction;
import backend.MipsNode;
import backend.MipsSymbol.MipsSymbolTable;

import java.util.ArrayList;

public class MipsBasicBlock implements MipsNode {
    private MipsSymbolTable symbolTable;
    private final MipsFunction parent;
    private ArrayList<MipsInstruction> instructions;

    public MipsBasicBlock(MipsFunction parent) {
        this.parent = parent;
        this.instructions = new ArrayList<>();
        this.symbolTable = this.parent.getSymbolTable();
    }
}
