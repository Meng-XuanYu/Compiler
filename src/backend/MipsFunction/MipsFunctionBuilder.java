package backend.MipsFunction;

import backend.MipsBlock.MipsBasicBlock;
import backend.MipsBlock.MipsBasicBlockBuilder;
import backend.MipsInstruction.MipsInstruction;
import backend.MipsInstruction.Move;
import backend.MipsModule;
import backend.MipsSymbol.MipsSymbol;
import backend.MipsSymbol.MipsSymbolTable;
import backend.MipsSymbol.RegisterTable;
import middleend.LlvmIr.Value.BasicBlock.IRBasicBlock;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.Function.IRParameter;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsFunctionBuilder {
    private IRFunction irFunction;
    private MipsModule father; // 父module
    private MipsSymbolTable table;
    private MipsFunction function;
    private MipsBasicBlock moveFromAreg;
    private ArrayList<MipsInstruction> initsInstructions = new ArrayList<>();

    public MipsFunctionBuilder(IRFunction irFunction, MipsModule father, HashMap<String, MipsSymbol> globalVariable) {
        this.irFunction = irFunction;
        this.father = father;
        initMipsSymbolTable(globalVariable);
    }

    private void initMipsSymbolTable(HashMap<String, MipsSymbol> globalVariable) {
        RegisterTable registerTable = new RegisterTable();
        this.table = new MipsSymbolTable(registerTable);
        registerTable.setTable(this.table);

        for (String index : globalVariable.keySet()) {
            this.table.addSymbol(index, globalVariable.get(index));
        }

        ArrayList<IRParameter> parameters = this.irFunction.getParameters();
        int cnt = parameters.size();
        int index = 0;
        while (index < cnt) {
            IRParameter target = null;
            for (IRParameter parameter : parameters) {
                if (parameter.getIndex() == index) {
                    target = parameter;
                    break;
                }
            }
            if (target != null) {
                String name = target.getName();
                MipsSymbol symbol;
                if (index < 4) {
                    symbol = new MipsSymbol(name, 30, true, index + 8, false);
                    registerTable.addSymbol(index + 8, symbol);
                    Move move = new Move(index + 8, index + 4);
                    initsInstructions.add(move);
                } else {
                    symbol = new MipsSymbol(name, 30, false, true, (index - 4) * 4, false);
                    this.table.addOffset(4);
                }
                symbol.setSize(target.getSize());
                symbol.setParam(true);
                this.table.addSymbol(name, symbol);
                index++;
            }
        }
    }

    public MipsFunction generateFunction() {
        this.function = new MipsFunction(this.father, this.irFunction.getName().substring(1), this.table);
        this.moveFromAreg = new MipsBasicBlock(this.function);
        this.moveFromAreg.addInstructions(this.initsInstructions);
        this.function.addMipsBasicBlock(this.moveFromAreg);
        ArrayList<IRBasicBlock> basicBlocks = this.irFunction.getBasicBlocks();
        for (IRBasicBlock block : basicBlocks) {
            MipsBasicBlockBuilder mipsBasicBlockBuilder = new MipsBasicBlockBuilder(this.function, block);
            this.function.addMipsBasicBlock(mipsBasicBlockBuilder.generateBasicBlock());
        }
        return this.function;
    }
}
