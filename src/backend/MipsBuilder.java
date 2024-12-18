package backend;

import backend.MipsFunction.MipsFunctionBuilder;
import backend.MipsInstruction.Li;
import backend.MipsInstruction.Sw;
import backend.MipsSymbol.MipsSymbol;
import middleend.LlvmIr.IRModule;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.GlobalVar.IRGlobalVar;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsBuilder {
    private final IRModule irModule;

    public MipsBuilder(IRModule irModule) {
        this.irModule = irModule;
    }

    public MipsModule genMipsModule() {
        MipsModule mipsModule = new MipsModule();
        // 全局变量, 用$24即$t8不断li和sw
        HashMap<String, MipsSymbol> globalVariable = new HashMap<>();
        ArrayList<IRGlobalVar> variables = irModule.getGlobalVariables();
        int gpOffset = 0;
        for (IRGlobalVar variable : variables) {
            if (variable.getSize() == 0) {
                int value = variable.getIntInit();
                if (value != 0) {
                    mipsModule.addGlobal(new Li(24, value));
                    mipsModule.addGlobal(new Sw(24, 28, gpOffset));
                }
                MipsSymbol symbol = new MipsSymbol(variable.getName(), 28, gpOffset);
                globalVariable.put(symbol.getName(), symbol);
                gpOffset += 4;
            } else {
                ArrayList<Integer> inits = variable.getIntInitArray();
                int size = variable.getSize();
                MipsSymbol symbol = new MipsSymbol(variable.getName(), 28, false, -1, true, gpOffset, false, false, size);

                globalVariable.put(symbol.getName(), symbol);
                for (int i = 0; i < size; i++) {
                    if (i < inits.size()) {
                        int num = inits.get(i);
                        if (num != 0) {
                            mipsModule.addGlobal(new Li(24, num));
                            mipsModule.addGlobal(new Sw(24, 28, gpOffset));
                        }
                        gpOffset += 4;
                    } else {
                        gpOffset += 4;
                    }
                }
            }
        }

        // 函数
        ArrayList<IRFunction> functions = irModule.getFunctions();
        for (IRFunction function : functions) {
            MipsFunctionBuilder builder = new MipsFunctionBuilder(function, mipsModule, globalVariable);
            mipsModule.addFunction(builder.generateFunction());
        }
        return mipsModule;
    }
}
