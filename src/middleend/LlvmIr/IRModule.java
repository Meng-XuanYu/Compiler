package middleend.LlvmIr;
import middleend.LlvmIr.Value.GlobalVar.IRGlobalVar;
import middleend.LlvmIr.Value.IRNode;

import java.util.ArrayList;

public class IRModule implements IRNode {
    private ArrayList<IRGlobalVar> globalVars;
    private ArrayList<IRFunction> functions;
}
