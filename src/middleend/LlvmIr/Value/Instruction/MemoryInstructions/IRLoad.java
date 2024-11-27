package middleend.LlvmIr.Value.Instruction.MemoryInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

public class IRLoad extends IRInstruction {
    private boolean isArray = false;
    private IRValue dimension1 = null;

    //普通变量
    public IRLoad(IRValueType type, IRValue value) {
        super(IRInstructionType.Load, type, 1);
        this.setOperand(value, 0);
        this.isArray = false;
    }

    //数组
    public IRLoad(IRValueType type, IRValue value, IRValue dimension1) {
        super(IRInstructionType.Load, type, 1);
        this.setOperand(value, 0);
        this.isArray = true;
        this.dimension1 = dimension1;
    }

    public boolean isArray() {
        return isArray;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        if (isArray()) {
            if (this.getType() instanceof IRIntegerType) {
                if (((IRIntegerType) this.getType()).getBitWidth() == 32) {
                    sb.append(this.getName()).append(" = load i32, i32* ");
                } else {
                    sb.append(this.getName()).append(" = load i8, i8* ");
                }
            }
            sb.append(this.getOperand(0).getName()).append("[").append(this.dimension1.getName()).append("]\n");
        } else {
            if (this.getType() instanceof IRIntegerType) {
                if (((IRIntegerType) this.getType()).getBitWidth() == 32) {
                    sb.append(this.getName()).append(" = load i32, i32* ");
                } else {
                    sb.append(this.getName()).append(" = load i8, i8* ");
                }
            }
            sb.append(this.getOperand(0).getName()).append("\n");
        }
        ans.add(sb.toString());
        return ans;
    }
}
