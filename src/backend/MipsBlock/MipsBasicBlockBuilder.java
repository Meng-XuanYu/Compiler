package backend.MipsBlock;

import backend.MipsFunction.MipsFunction;
import backend.MipsInstruction.*;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Value.BasicBlock.IRBasicBlock;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRCall;

import java.util.ArrayList;

public class MipsBasicBlockBuilder {
    private final MipsFunction parent;
    private final IRBasicBlock irBasicBlock;

    public MipsBasicBlockBuilder(MipsFunction parent, IRBasicBlock irBasicBlock) {
        this.parent = parent;
        this.irBasicBlock = irBasicBlock;
    }

    public MipsBasicBlock generateBasicBlock() {
        MipsBasicBlock block = new MipsBasicBlock(parent);
        ArrayList<IRInstruction> instructions = irBasicBlock.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            IRInstruction irInstruction = instructions.get(i);
//            // 处理多个字符打印合并为字符串打印
//            if (irInstruction instanceof IRCall) {
//                IRCall call = (IRCall) irInstruction;
//                String functionName = call.getFunctionName();
//                IRValue value1 = irInstruction.getOperand(1);
//                boolean flag;
//                try {
//                    Integer.valueOf(value1.getName());
//                    flag = true;
//                } catch (Exception e) {
//                    flag = false;
//                }
//                if (functionName.equals("@putch") && flag) {
//                    StringBuilder sb = new StringBuilder();
//                    IRInstruction temp = irInstruction;
//                    while (temp instanceof IRCall && ((IRCall) temp).getFunctionName().equals("@putch")) {
//                        IRValue value = temp.getOperand(1);
//                        sb.append((char) Integer.valueOf(value.getName()).intValue());
//                        i += 1;
//                        if (i >= instructions.size()) {
//                            break;
//                        }
//                        temp = irBasicBlock.getInstructions().get(i);
//                    }
//                    i -= 1;
//                    int cnt = AsciizCnt.getCnt();
//                    Asciiz asciiz = new Asciiz("str_" + cnt, sb.toString());
//                    asciiz.setCnt(cnt);
//
//                    this.parent.getParent().addAsciiz(asciiz);
//                    ArrayList<MipsInstruction> ans = new ArrayList<>();
//                    Move move = new Move(3, 4);
//                    Li li = new Li(2, 4);
//                    La la = new La(4, asciiz.getName());
//                    ans.add(move);
//                    ans.add(li);
//                    ans.add(la);
//                    Syscall syscall = new Syscall();
//                    ans.add(syscall);
//                    move = new Move(4, 3);
//                    ans.add(move);
//                    block.addInstruction(ans);
//                    continue;
//                }
//            }
            MipsInstructionBuilder builder = new MipsInstructionBuilder(irInstruction, block);
            block.addInstruction(builder.generateMipsInstruction());
        }
        return block;
    }
}
