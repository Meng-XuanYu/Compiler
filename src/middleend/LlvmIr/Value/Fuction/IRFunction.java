package middleend.LlvmIr.Value.Fuction;
import middleend.LlvmIr.IRModule;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Value.BasicBlock.IRBasicBlock;
import middleend.LlvmIr.Value.IRNode;
import java.util.ArrayList;

public class IRFunction extends IRValue implements IRNode {
    private ArrayList<IRBasicBlock> blocks = new ArrayList<>();
    private IRModule module;
}
