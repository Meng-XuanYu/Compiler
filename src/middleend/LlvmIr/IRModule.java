package middleend.LlvmIr;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.GlobalVar.IRGlobalVar;
import middleend.LlvmIr.Value.IRNode;

import java.util.ArrayList;

public class IRModule implements IRNode {
    private ArrayList<IRGlobalVar> globalVars;
    private ArrayList<IRFunction> functions;

    public IRModule() {
        this.globalVars = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public void addGlobalVar(IRGlobalVar globalVar) {
        this.globalVars.add(globalVar);
    }

    public void addFunction(IRFunction function) {
        this.functions.add(function);
    }

    public ArrayList<IRGlobalVar> getGlobalVars() {
        return this.globalVars;
    }

    public ArrayList<IRFunction> getFunctions() {
        return this.functions;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        String initStr = "declare i32 @getint()          ; 读取一个整数\n" +
                "declare i32 @getchar()     ; 读取一个字符\n" +
                "declare void @putint(i32)      ; 输出一个整数\n" +
                "declare void @putch(i32)       ; 输出一个字符\n";
        ans.add(initStr);
        for (IRGlobalVar globalVar : this.globalVars) {
            ans.addAll(globalVar.printIR());
        }
        for (IRFunction function : this.functions) {
            ans.addAll(function.printIR());
        }
        return ans;
    }

    public ArrayList<IRGlobalVar> getGlobalVariables() {
        return this.globalVars;
    }
}
