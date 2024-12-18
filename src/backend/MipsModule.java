package backend;

import backend.MipsInstruction.*;
import backend.MipsFunction.MipsFunction;
import java.util.ArrayList;

public class MipsModule implements MipsNode {
    private final ArrayList<Asciiz> asciizs;
    // 加载全局变量到内存的指令
    private final ArrayList<MipsInstruction> globals;
    private final ArrayList<MipsFunction> functions;
    // 加载函数运行栈
    private final Li li;
    // 跳转到main函数
    private final J jmain;
    // 插入一条nop
    private final Nop nop;

    public MipsModule() {
        this.asciizs = new ArrayList<>();
        this.functions = new ArrayList<>();
        this.globals = new ArrayList<>();
        this.li = new Li(30, 0x10040000);
        this.jmain = new J("main");
        this.nop = new Nop();
    }

    public void addAsciiz(Asciiz asciiz) {
        this.asciizs.add(asciiz);
    }

    public void addFunction(MipsFunction function) {
        this.functions.add(function);
    }

    public void addGlobal(MipsInstruction instruction) {
        this.globals.add(instruction);
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add(".data\n");
        for (Asciiz asciiz : asciizs) {
            ans.addAll(asciiz.printMips());
        }
        ans.add(".text\n");
        ans.addAll(li.printMips());
        for (MipsInstruction global : globals) {
            ans.addAll(global.printMips());
        }
        ans.addAll(jmain.printMips());
        ans.add(nop.printMips().get(0));
        for (MipsFunction function : functions) {
            ans.add("\n");
            ans.addAll(function.printMips());
        }
        return ans;
    }
}
