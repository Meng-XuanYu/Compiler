package middleend.LlvmIr.Value.Instruction.MemoryInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRVoidType;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

// store <value_type> <value>, <pointer_type>* <pointer>
// <value_type>：存储的值的类型（例如 i32、i8）。
// <value>：要存储的值。
// <pointer_type>*：存储值的地址，指向一个变量或数组元素。
public class IRStore extends IRInstruction {
    private int dimensionRight = 0;    // 右侧操作数的维度（右值）
    private int dimensionPointer = 0;   // 左侧操作数的维度（指针）
    private int dimension1Right = 0;    // 右侧操作数的维度（右值）
    private int dimension1Pointer = 0;   // 左侧操作数的维度（指针）

    // 如果传入的维度是 IRValue 类型的，那么就需要处理这种情况
    private IRValue dimension1RightValue = null;
    private IRValue dimension1PointerValue = null;
    private boolean isIrValue = false;
    private final boolean isChar;

    // 默认构造器，处理左右都是普通变量的情况
    public IRStore(IRValue value, IRValue pointer, boolean isChar) {
        super(IRInstructionType.Store, IRVoidType.getVoidType(), 2);
        this.setOperand(value, 0);
        this.setOperand(pointer, 1);
        this.dimensionRight = 0;
        this.dimensionPointer = 0;
        this.isChar = isChar;
    }

    public IRStore(IRValue value, IRValue pointer) {
        super(IRInstructionType.Store, IRVoidType.getVoidType(), 2);
        this.setOperand(value, 0);
        this.setOperand(pointer, 1);
        this.dimensionRight = 0;
        this.dimensionPointer = 0;
        this.isChar = false;
    }

    // 处理涉及数组的赋值，维度变量是常量
    public IRStore(IRValue value, IRValue pointer, int dimensionRight, int dimensionPointer,
                    int dimension1Right, int dimension1Pointer) {
        super(IRInstructionType.Store, IRVoidType.getVoidType(), 2);
        this.setOperand(value, 0);
        this.setOperand(pointer, 1);
        this.dimensionRight = dimensionRight;
        this.dimensionPointer = dimensionPointer;
        this.dimension1Right = dimension1Right;
        this.dimension1Pointer = dimension1Pointer;
        this.isIrValue = false;
        this.isChar = false;
    }

    //处理涉及数组的赋值，维度变量是Exp（IrValue）
    public IRStore(IRValue value, IRValue pointer, int dimensionRight, int dimensionPointer,
                    IRValue dimension1RightValue, IRValue dimension1PointerValue, boolean isChar) {
        super(IRInstructionType.Store, IRVoidType.getVoidType(), 2);
        this.setOperand(value, 0);
        this.setOperand(pointer, 1);
        this.dimensionRight = dimensionRight;
        this.dimensionPointer = dimensionPointer;
        this.dimension1RightValue = dimension1RightValue;
        this.dimension1PointerValue = dimension1PointerValue;
        this.isIrValue = true;
        this.isChar = isChar;
    }

    public IRStore(IRValue value, IRValue pointer, int dimensionRight, int dimensionPointer,
                   IRValue dimension1RightValue, IRValue dimension1PointerValue) {
        super(IRInstructionType.Store, IRVoidType.getVoidType(), 2);
        this.setOperand(value, 0);
        this.setOperand(pointer, 1);
        this.dimensionRight = dimensionRight;
        this.dimensionPointer = dimensionPointer;
        this.dimension1RightValue = dimension1RightValue;
        this.dimension1PointerValue = dimension1PointerValue;
        this.isIrValue = true;
        this.isChar = false;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();

        if (isIrValue) {
            if (!isChar) {
                sb.append(this.getName()).append(" = store i32 ");
            } else {
                sb.append(this.getName()).append(" = store i8 ");
            }
            IRValue value = this.getOperand(0); // 被存储的值
            IRValue pointer = this.getOperand(1); // 存储的地址
            sb.append(value.getName());
            if (this.dimensionRight == 1) {
                sb.append("[").append(this.dimension1RightValue.getName()).append("]");
            }
            sb.append(", ");
            if (!isChar) {
                sb.append("i32* ");
            } else {
                sb.append("i8* ");
            }
            sb.append(pointer.getName());
            if (this.dimensionPointer == 1) {
                sb.append("[").append(this.dimension1PointerValue.getName()).append("]");
            }
            sb.append("\n");
        } else {
            if (this.dimensionRight == 0 && this.dimensionPointer == 0) {
                if (!isChar) {
                    sb.append("store i32 ");
                } else {
                    sb.append("store i8 ");
                }
                IRValue value = this.getOperand(0);
                IRValue pointer = this.getOperand(1);
                sb.append(value.getName()).append(", ");
                if (!isChar) {
                    sb.append("i32* ");
                } else {
                    sb.append("i8* ");
                }
                sb.append(pointer.getName()).append("\n");
            } else {
                if (!isChar) {
                    sb.append(this.getName()).append(" = store i32 ");
                } else {
                    sb.append(this.getName()).append(" = store i8 ");
                }
                IRValue value = this.getOperand(0); // 被存储的值
                IRValue pointer = this.getOperand(1); // 存储的地址
                sb.append(value.getName());
                if (this.dimensionRight == 1) {
                    sb.append("[").append(this.dimension1Right).append("]");
                }
                sb.append(", ");
                if (!isChar) {
                    sb.append("i32* ");
                } else {
                    sb.append("i8* ");
                }
                sb.append(pointer.getName());
                if (this.dimensionPointer == 1) {
                    sb.append("[").append(this.dimension1Pointer).append("]");
                }
                sb.append("\n");
            }
        }
        ans.add(sb.toString());
        return ans;
    }

    public boolean isIrValue() {
        return isIrValue;
    }

    public int getDimension1Pointer() {
        return dimension1Pointer;
    }

    public int getDimension1Right() {
        return dimension1Right;
    }

    public IRValue getDimension1PointerValue() {
        return dimension1PointerValue;
    }

    public IRValue getDimension1RightValue() {
        return dimension1RightValue;
    }

    public int getDimensionPointer() {
        return dimensionPointer;
    }
}
