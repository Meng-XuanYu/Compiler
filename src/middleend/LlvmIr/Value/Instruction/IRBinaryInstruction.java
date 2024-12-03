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
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Sub:
                sb.append("sub ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Mul:
                sb.append("mul ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Div:
                sb.append("sdiv ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Mod:
                sb.append("srem ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Lt:
                sb.append("icmp slt ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Gt:
                sb.append("icmp sgt ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Le:
                sb.append("icmp sle ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Ge:
                sb.append("icmp sge ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Not:
                sb.append("icmp eq i32 0,");
                sb.append(this.getOperand(0).getName());
                break;
            case Ne:
                sb.append("icmp ne ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
            case Eq:
                sb.append("icmp eq ");
                sb.append("i32 ");
                sb.append(this.getOperand(0).getName());
                sb.append(", ");
                sb.append(this.getOperand(1).getName());
                break;
        }

        sb.append("\n");
        ans.add(sb.toString());
        return ans;
    }
}
