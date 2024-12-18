package backend.MipsFunction;

import backend.MipsBlock.MipsBasicBlock;
import backend.MipsInstruction.Jr;
import backend.MipsInstruction.Li;
import backend.MipsInstruction.Syscall;
import backend.MipsModule;
import backend.MipsNode;
import backend.MipsSymbol.MipsSymbolTable;

import java.util.ArrayList;

public class MipsFunction implements MipsNode {
    private String name;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks;
    private MipsModule father;
    private MipsSymbolTable symbolTable;

    public MipsFunction(MipsModule father, String name, MipsSymbolTable symbolTable) {
        this.father = father;
        this.mipsBasicBlocks = new ArrayList<>();
        this.name = name;
        this.symbolTable = symbolTable;
    }

    public void addMipsBasicBlock(MipsBasicBlock basicBlock) {
        this.mipsBasicBlocks.add(basicBlock);
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ans =  new ArrayList<>();
        ans.add("# ---------- " + this.name + "函数开始 ----------\n");
        ans.add(this.name + ":\n");
        ArrayList<String> temp;
        for (MipsBasicBlock block : this.mipsBasicBlocks) {
            temp = block.printMips();
            if (temp != null && !temp.isEmpty()) {
                ans.addAll(temp);
            }
            ans.add("\n");
        }
        if (this.name.equals("main")) {
            // main函数的最后一条指令是syscall
            Li li = new Li(2, 10);
            temp = li.printMips();
            ans.addAll(temp);
            Syscall syscall = new Syscall();
            temp = syscall.printMips();
            ans.addAll(temp);
        } else {
            // 非main函数的最后一条指令是jr $ra
            Jr jr = new Jr(31);
            temp = jr.printMips();
            ans.addAll(temp);
        }
        ans.add("# ********** " + this.name + "函数结束 **********\n");
        return ans;
    }

    public MipsSymbolTable getSymbolTable() {
        return symbolTable;
    }

    public String getName() {
        return name;
    }

    public boolean isMain() {
        return name.equals("main");
    }

    public MipsModule getParent() {
        return father;
    }
}
