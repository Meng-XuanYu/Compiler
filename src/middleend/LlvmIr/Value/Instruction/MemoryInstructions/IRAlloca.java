package middleend.LlvmIr.Value.Instruction.MemoryInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRPointerType;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

// alloca指令 : <result> = alloca <type>
public class IRAlloca extends IRInstruction {
    private IRValue value; // 操作对象
    private IRValueType type; // 分配的类型

    public IRAlloca(IRValueType type, IRValue value) {
        super(IRInstructionType.Alloca, new IRPointerType(type), 0);
        this.type = type;
        this.value = value;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        sb.append(this.getName()).append(" = alloca ").append(this.type.printIR().get(0));

        sb.append("\n");
        ans.add(sb.toString());
        return ans;
    }
}
