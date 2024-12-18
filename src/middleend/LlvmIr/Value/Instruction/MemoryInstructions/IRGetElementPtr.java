package middleend.LlvmIr.Value.Instruction.MemoryInstructions;

import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Types.IRPointerType;
import middleend.LlvmIr.Types.IRValueType;
import middleend.LlvmIr.Value.Constant.IRConstantInt;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;

import java.util.ArrayList;

public class IRGetElementPtr extends IRInstruction {
    public IRGetElementPtr(IRValueType type, IRValue array, IRValue offset) {
        super(IRInstructionType.GetElementPtr,new IRPointerType(type),2);
        this.setOperand(array,0);
        this.setOperand(offset,1);
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> arr = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        IRValue pointer = this.getOperand(0);
        IRValue offset = this.getOperand(1);
        String offsetValue;
        if (offset instanceof IRConstantInt) {
            offsetValue = offset.printIR().get(0);
        } else {
            offsetValue = offset.getName();
        }
        IRPointerType pointerType = (IRPointerType) pointer.getType();
        IRValueType pointType = pointerType.getContained();
        boolean isInt32 = false;
        boolean isInt8 = false;
        if (pointType instanceof IRIntegerType) {
            isInt32 = ((IRIntegerType) pointType).getBitWidth() == 32;
            isInt8 = ((IRIntegerType) pointType).getBitWidth() == 8;
        }
        if (isInt32) {
            stringBuilder.append(this.getName()).append(" = getelementptr inbounds i32, i32* ").append(pointer.getName()).append(", i32 ").append(offsetValue);
        } else if (isInt8) {
            stringBuilder.append(this.getName()).append(" = getelementptr inbounds i8, i8* ").append(pointer.getName()).append(", i32 ").append(offsetValue);
        } else {
            stringBuilder.append(this.getName()).append(" = getelementptr inbounds ").append(pointType.printIR().get(0)).append(", ").append(pointerType.printIR().get(0)).append(" ").append(pointer.getName()).append(", i32 0, i32 ").append(offsetValue);
        }
        stringBuilder.append("\n");
        arr.add(stringBuilder.toString());
        return arr;
    }

    public IRValue getBasePointer() {
        return this.getOperand(0);
    }

    public String getOffsetName() {
        IRValue offset = this.getOperand(1);
        String offsetValue;
        if (offset instanceof IRConstantInt) {
            offsetValue = offset.printIR().get(0);
        } else {
            offsetValue = offset.getName();
        }
        return offsetValue;
    }

    public IRValue getOffset() {
        return this.getOperand(1);
    }


}
