package middleend.LlvmIr.Value.Fuction;
import middleend.LlvmIr.IRModule;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRFunctionType;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.BasicBlock.IRBasicBlock;
import middleend.LlvmIr.Value.IRNode;
import java.util.ArrayList;

public class IRFunction extends IRValue implements IRNode {
    private ArrayList<IRBasicBlock> blocks = new ArrayList<>();
    private IRModule module;
    private ArrayList<IRParameter> parameters = new ArrayList<>();
    private FunctionCnt functionCnt;

    public IRFunction(IRValueType type, IRModule module) {
        super(type);
        this.initParameters();
        this.module = module;
    }

    public IRFunction(IRValueType type, IRModule module, String name) {
        this(type, module);
        this.setName(name);
    }

    public IRFunction(IRValueType type, IRModule module, String name, FunctionCnt functionCnt) {
        this(type, module, name);
        this.functionCnt = functionCnt;
    }

    // Initialize the parameters list from the function parameters' types
    private void initParameters() {
        if (this.getType() instanceof IRFunctionType) {
            IRFunctionType type = (IRFunctionType) this.getType();
            ArrayList<IRValueType> parameterTypes = type.getParameterTypes();
            ArrayList<IRValue> parameters = type.getParameters();
            for (int i = 0; i < parameterTypes.size(); i++) {
                IRValue parameter = parameters.get(i);
                IRParameter param = new IRParameter(parameterTypes.get(i), i);
                param.setName(parameter.getName());


                param.setSize(parameter.getSize());
                param.setName(parameter.getName());
                this.parameters.add(param);
            }
        }
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> init = new ArrayList<>();
        ArrayList<String> ans = new ArrayList<>();

        StringBuilder function = new StringBuilder("\ndefine dso_local ");
        function.append(this.getType().printIR().get(0));
        function.append(" ");
        function.append(this.getName()); // @在这里面
        function.append("(");
        for (int i = 0; i < this.parameters.size(); i++) {
            if (i != 0) {
                function.append(", ");
            }
            function.append(this.parameters.get(i).printIR().get(0));
        }
        function.append(") {\n");
        init.add(function.toString());

        for (IRBasicBlock block : this.blocks) {
            init.addAll(block.printIR());
        }

        init.add("}\n");

        // 对列表中的字符串进行重新排序
        // 借鉴于学长
        for (String string : init) {
            if (string.contains("dso_local")) {
                // 函数签名
                ans.add(string);
            }
        }
        for (String string : init) {
            if (!string.contains("dso_local") && string.contains("alloca")) {
                // 局部变量
                ans.add(string);
            }
        }
        for (String string : init) {
            if (!string.contains("dso_local") && !string.contains("alloca")) {
                // 基本块
                ans.add(string);
            }
        }
        return ans;
    }

    public ArrayList<IRParameter> getParameters() {
        return this.parameters;
    }

    public void addBlocks(ArrayList<IRBasicBlock> blocks) {
        this.blocks.addAll(blocks);
    }

    public ArrayList<IRBasicBlock> getBlocks() {
        return this.blocks;
    }
}
