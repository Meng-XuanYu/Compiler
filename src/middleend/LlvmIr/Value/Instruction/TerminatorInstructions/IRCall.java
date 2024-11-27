package middleend.LlvmIr.Value.Instruction.TerminatorInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRFunctionType;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRVoidType;
import middleend.LlvmIr.Value.Function.IRFunction;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

//%result = call <return_type> <function_name>(<arg1_type> <arg1>, <arg2_type> <arg2>, ...)
//<return_type>：调用函数的返回类型。如果函数没有返回值，通常使用void。
//<function_name>：被调用函数的名称，或者是函数指针。
//<argX_type>：传递给函数的参数类型。
//<argX>：传递给函数的参数值。
public class IRCall extends IRInstruction {
    private final String functionName;
    private int functionType; // 0表示void，1表示Integer，2表示Char

    public IRCall(IRFunction function, ArrayList<IRValue> args) {
        super(IRInstructionType.Call, ((IRFunctionType)function.getType()).getReturnType(), args.size() + 1);
        if (this.getType() instanceof IRVoidType) {
            this.functionType = 0;
        } else if (this.getType() instanceof IRIntegerType) {
            if (((IRIntegerType) this.getType()).getBitWidth() == 8) {
                this.functionType = 2;
            } else {
                this.functionType = 1;
            }
        }

        this.setOperand(function, 0); // 函数名称作为第一个操作数
        this.functionName = function.getName();

        // 设置函数参数
        for (int i = 0; i < args.size(); i++) {
            this.setOperand(args.get(i), i + 1);
        }
    }

    public String getFunctionName() {
        return this.functionName;
    }

    public ArrayList<IRValue> getArgs() {
        ArrayList<IRValue> args = new ArrayList<>();
        for (int i = 1; i < this.getNumOfOperands(); i++) {
            args.add(this.getOperand(i));
        }
        return args;
    }


    // **dimension**表示数组的维度（例如标量、1维数组）。
    // **dimensionValue**则决定了实际参数的维度，并根据维度的不同，生成不同的LLVM IR语法。
    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (this.functionType != 0) {
            sb.append(this.getName()).append(" = ");
        }
        sb.append("call ");

        if (this.functionType == 0) {
            sb.append("void ");
        } else if (this.functionType == 1) {
            sb.append("i32 ");
        } else if (this.functionType == 2) {
            sb.append("i8 ");
        }
        sb.append(this.functionName).append("(");
        for (int i = 1; i < this.getNumOfOperands(); i++) {
            IRValue arg = this.getOperand(i);

            if (arg.getDimensionValue() == -1) {
                sb.append(arg.getType().printIR().get(0)).append(" ");
                sb.append(arg.getName());
            } else if (arg.getDimensionValue() == 0) {
                sb.append(arg.getType().printIR().get(0)).append(" ");
                sb.append(arg.getName());
                // arg符号本身的维数不可能是0，因为是0不会走到setDimensionValue
                sb.append("[").append(arg.getDimension1Value().getName()).append("]");
            } else if (arg.getDimensionValue() == 1) {
                sb.append(arg.getType().printIR()).append(" ");
                sb.append(arg.getName());
                // 要传入一个1维参数, 就不需要再append什么内容
            } else {
                // 传入二维或更高维数组
                System.out.println("ERROR in IRCall");
            }
            if (i != this.getNumOfOperands() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")\n");
        ans.add(sb.toString());
        return ans;
    }

    public boolean isVoid() {
        return this.functionType == 0;
    }

    // getInt()函数返回一个整数值，该值表示调用函数的返回值是否为整数。
    public IRCall(String functionName) {
        super(IRInstructionType.Call, IRIntegerType.get32(), 0);
        this.functionName = functionName;
        this.functionType = 1;
    }

    // getChar()函数
    public IRCall(String functionName, boolean isChar) {
        super(IRInstructionType.Call, IRIntegerType.get8(), 0);
        this.functionName = functionName;
        this.functionType = 2;
    }

    // putch()函数
    public IRCall(String functionName, char c) {
        super(IRInstructionType.Call, IRVoidType.getVoidType(), 2);
        this.functionName = functionName;
        this.functionType = 0;
        this.setOperand(new IRValue(String.valueOf((int)c), IRIntegerType.get32()), 1);
    }

    // putint()函数
    public IRCall(String functionName, IRValue value) {
        super(IRInstructionType.Call, IRVoidType.getVoidType(), 2);
        this.functionName = functionName;
        this.functionType = 0;
        this.setOperand(value, 1);
    }
}
