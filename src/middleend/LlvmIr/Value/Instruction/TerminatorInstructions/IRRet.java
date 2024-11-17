package middleend.LlvmIr.Value.Instruction.TerminatorInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRVoidType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

public class IRRet extends IRInstruction {
    private final int RetType;

    public IRRet(IRValue val) {
        super(IRInstructionType.Ret, val.getType(), 1);
        this.setOperand(val, 0);
        this.RetType = (val.getType() instanceof IRIntegerType) ? 1 : 2;
    }

    public IRRet() {
        super(IRInstructionType.Ret, IRVoidType.getVoidType(), 0);
        this.RetType = 0;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (this.RetType == 0) {
            sb.append("ret void\n");
        } else {
            sb.append("ret ");
            if (this.RetType == 1) {
                sb.append("i32 ");
            } else {
                sb.append("i8 ");
            }
            sb.append(this.getOperand(0).getName()).append("\n");
        }
        ans.add(sb.toString());
        return ans;
    }

    public int getRetType() {
        return RetType;
    }
}
