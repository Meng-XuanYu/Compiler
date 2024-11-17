package middleend.LlvmIr.Value.Instruction;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRValueType;

import java.util.ArrayList;

// 因为IRBinaryInstruction中的内容都很相似，所以放在同一个文件中
public class IRBinaryInstruction extends IRInstruction{
    public IRBinaryInstruction(IRValueType valueType, IRInstructionType irInstructionType, IRValue left, IRValue right) {
        super(irInstructionType, valueType, 2);

        // 二元算术运算
        if (this.isArithmeticBinary()) {
            this.setValueType(IRIntegerType.get32()); // char -> int
        }
        // 二元逻辑运算
        if (this.isLogicBinary()) {
            this.setValueType(IRIntegerType.get1());
        }

        // 左操作数，不会为空
        this.setOperand(left, 0);
        // 右操作数，可能为空
        if (right != null) {
            this.setOperand(right, 1);
        }
    }

    public IRValue getLeft() {
        return this.getOperand(0);
    }

    public IRValue getRight() {
        return this.getOperand(1);
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName());
        sb.append(" = ");
        switch (this.getInstructionType()) {
            case Add:
                sb.append("add ");
                break;
            case Sub:
                sb.append("sub ");
                break;
            case Mul:
                sb.append("mul ");
                break;
            case Div:
                sb.append("sdiv ");
                break;
            case Mod:
                sb.append("srem ");
                break;
            case Lt:
                sb.append("Lt ");
                break;
            case Gt:
                sb.append("Gt ");
                break;
            case Le:
                sb.append("Le ");
                break;
            case Ge:
                sb.append("Ge ");
                break;
            case Not:
                sb.append("Not ");
                break;
            case Ne:
                sb.append("Ne ");
                break;
            case Eq:
                sb.append("Eq ");
                break;
        }
        sb.append(this.getOperand(0).getName());
        if (this.getOperand(1) != null && this.getInstructionType() != IRInstructionType.Not) {
            // Not指令只有一个操作数
            sb.append(", ");
            sb.append(this.getOperand(1).getName());
        }
        sb.append("\n");
        ans.add(sb.toString());
        return ans;
    }
}
