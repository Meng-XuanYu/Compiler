package backend.MipsInstruction;
import backend.MipsBlock.MipsBasicBlock;
import backend.MipsSymbol.MipsSymbol;
import backend.MipsSymbol.MipsSymbolTable;
import backend.MipsSymbol.RegisterTable;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.IRIntegerType;
import middleend.LlvmIr.Value.Constant.IRConstantInt;
import middleend.LlvmIr.Value.Instruction.IRBinaryInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;
import middleend.LlvmIr.Value.Instruction.IRLabel;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.*;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRBr;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRCall;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRGoto;
import middleend.LlvmIr.Value.Instruction.TerminatorInstructions.IRRet;

import java.util.ArrayList;

public class MipsInstructionBuilder {
    private final IRInstruction irInstruction;
    private final MipsBasicBlock parent;
    private final MipsSymbolTable table;
    private final RegisterTable registerTable;

    public MipsInstructionBuilder(IRInstruction irInstruction, MipsBasicBlock parent) {
        this.irInstruction = irInstruction;
        this.parent = parent;
        this.table = parent.getTable();
        this.registerTable = this.table.getRegisterTable();
    }

    public ArrayList<MipsInstruction> generateMipsInstruction() {
        if (irInstruction == null) {
            return null;
        } else if (irInstruction instanceof IRAlloca) {
            return generateFromAlloca();
        } else if (irInstruction instanceof IRBinaryInstruction) {
            return generateFromBinaryInstruction();
        } else if (irInstruction instanceof IRCall) {
            return generateFromCall();
        } else if (irInstruction instanceof IRLoad) {
            return generateFromLoad();
        } else if (irInstruction instanceof IRRet) {
            return generateFromRet();
        } else if (irInstruction instanceof IRStore) {
            return generateFromStore();
        } else if (irInstruction instanceof IRGoto) {
            return generateFromGoto();
        } else if (irInstruction instanceof IRLabel) {
            return generateFromLabel();
        } else if (irInstruction instanceof IRBr) {
            return genMipsInstructionFromBr();
        } else if (irInstruction instanceof IRZext) {
            return genMipsInstructionFromZext();
        } else if (irInstruction instanceof IRTrunc) {
            return genMipsInstructionFromTrunc();
        } else if (irInstruction instanceof IRGetElementPtr) {
            return generateFromGetElementPtr();
        } else {
            System.err.println("Unknown instruction: " + irInstruction.printIR().get(0));
        }
        return null;
    }

    private void insertSymbolTable(String name , MipsSymbol symbol) {
        this.table.addSymbol(name,symbol);
    }

    private boolean isCon(String name) {
        return !(name.contains("@") || name.contains("%"));
    }

    private ArrayList<MipsInstruction> generateFromGetElementPtr() {
        IRGetElementPtr getElementPtr = (IRGetElementPtr) irInstruction;
        ArrayList<MipsInstruction> instructions = new ArrayList<>();

        // 获取基指针
        IRValue basePointer = getElementPtr.getBasePointer();
        String basePointerName = basePointer.getName();
        MipsSymbol basePointerSymbol;

        basePointerSymbol = this.table.getSymbol(basePointerName);
        int basePointerReg;
        int basePointerOffset;
        if (basePointerSymbol.isInReg()) {
            basePointerReg = basePointerSymbol.getRegIndex();
            basePointerOffset = 0;
        } else {
            if (basePointerName.contains("param") ) {
                int index = this.registerTable.getReg(false, basePointerSymbol, this.parent);
                Lw lw = new Lw(index, basePointerSymbol.getBase(), basePointerSymbol.getOffset());
                ArrayList<MipsInstruction> temp = new ArrayList<>();
                temp.add(lw);
                this.parent.addInstruction(temp);
                basePointerReg = index;
                basePointerOffset = 0;
            } else if (basePointerName.contains("Global")) {
                basePointerReg = 28;
                basePointerOffset = basePointerSymbol.getOffset();
            } else {
                basePointerReg = 30;
                basePointerOffset = basePointerSymbol.getOffset();
            }
        }

        int ansBasePointerReg;
        // 获取偏移量
        IRValue offset = getElementPtr.getOffset();
        String offsetName;
        int offsetValue;
        if (offset instanceof IRConstantInt) {
            offsetName = offset.printIR().get(0);
            offsetValue = Integer.parseInt(offsetName);
            ansBasePointerReg = basePointerReg;
        } else {
            offsetName = offset.getName();
            try {
                offsetValue = Integer.parseInt(offsetName);
                ansBasePointerReg = basePointerReg;
            } catch (NumberFormatException e) {
                // 如果偏移量不是常数，说明是一个变量
                offsetValue = 0;
                int offsetReg = this.table.getRegIndex(offsetName, true, this.parent);
                MipsSymbol newOffsetSymbol = new MipsSymbol(getElementPtr.getName(), 30, false, -1, false, -1, false, false);
                int newOffsetReg = this.registerTable.getReg(true, newOffsetSymbol , this.parent);
                Sll sll = new Sll(newOffsetReg, offsetReg, 2);
                instructions.add(sll);
                if (basePointerReg == newOffsetReg || basePointerReg == offsetReg) {
                    // 说明newOffsetReg get的时候把basePointerReg覆盖了，要把basePointerReg的值从内存中读出来再加上newOffsetReg
                    MipsSymbol tempSymbol = new MipsSymbol("temp", 30, false, -1, true, -1, true, false);
                    int tempReg = this.registerTable.getReg(true, tempSymbol , this.parent);
                    Lw lw = new Lw(tempReg, basePointerSymbol.getBase(), basePointerSymbol.getOffset());
                    instructions.add(lw);
                    Add add = new Add(newOffsetReg, tempReg, newOffsetReg);
                    instructions.add(add);
                    tempSymbol.setUsed(true);
                } else {
                    Add add = new Add(newOffsetReg, basePointerReg, newOffsetReg);
                    instructions.add(add);
                }
                ansBasePointerReg = newOffsetReg;
            }
        }

        // 创建结果符号并设置偏移量
        String resultName = getElementPtr.getName();
        MipsSymbol resultSymbol = new MipsSymbol(resultName, ansBasePointerReg , false, -1, false, -1, false, false);
        int totalOffset = offsetValue * 4 + basePointerOffset;
        resultSymbol.setOffset(totalOffset);
        insertSymbolTable(resultName, resultSymbol);

        return instructions;
    }

    private ArrayList<MipsInstruction> genMipsInstructionFromTrunc() {
        IRTrunc trunc = (IRTrunc) irInstruction;
        ArrayList<MipsInstruction> ret = new ArrayList<>();

        IRValue operand = trunc.getOperand(0);
        String operandName = operand.getName();
        int operandReg;
        MipsSymbol operandSymbol;

        if (isCon(operandName)) {
            operandSymbol = new MipsSymbol("temp", 30, false, -1, false, -1, true, false);
            operandReg = this.registerTable.getReg(true, operandSymbol, this.parent);
            Li li = new Li(operandReg, Integer.parseInt(operandName));
            ret.add(li);
        } else {
            operandReg = this.table.getRegIndex(operandName, true, this.parent);
            operandSymbol = this.table.getSymbol(operandName);
        }

        String resultName = trunc.getName();
        MipsSymbol resultSymbol = new MipsSymbol(resultName, 30, false, -1, false, -1, true, false);
        insertSymbolTable(resultName, resultSymbol);
        int resultReg = this.registerTable.getReg(true, resultSymbol, this.parent);

        Andi andi = new Andi(resultReg, operandReg, 0xFFFFFFFF);
        ret.add(andi);
        operandSymbol.setUsed(true);
        return ret;
    }

    private ArrayList<MipsInstruction> genMipsInstructionFromZext() {
        IRZext zext = (IRZext) irInstruction;
        ArrayList<MipsInstruction> ret = new ArrayList<>();

        // 获取操作数和目标寄存器
        IRValue operand = zext.getOperand(0);
        String operandName = operand.getName();
        int operandReg;
        MipsSymbol operandSymbol;

        if (isCon(operandName)) {
            operandSymbol = new MipsSymbol("temp", 30, false, -1, false, -1, true, false);
            operandReg = this.registerTable.getReg(true, operandSymbol, this.parent);
            Li li = new Li(operandReg, Integer.parseInt(operandName));
            ret.add(li);
        } else {
            operandReg = this.table.getRegIndex(operandName, true, this.parent);
            operandSymbol = this.table.getSymbol(operandName);
        }

        String resultName = zext.getName();
        MipsSymbol resultSymbol = new MipsSymbol(resultName, 30, false, -1, false, -1, true, false);
        insertSymbolTable(resultName, resultSymbol);
        int resultReg = this.registerTable.getReg(true, resultSymbol, this.parent);

        // 根据操作数类型生成相应的 MIPS 指令
        Andi andi;
        if (operand.getType() == IRIntegerType.get8()) {
            andi = new Andi(resultReg, operandReg, 0xFFFFFFFF);
        } else {
            andi = new Andi(resultReg, operandReg, 0xFFFFFFFF);
        }
        ret.add(andi);

        operandSymbol.setUsed(true);
        return ret;
    }

    private ArrayList<MipsInstruction> genMipsInstructionFromBr() {
        IRBr inst = (IRBr)irInstruction;
        IRValue left = inst.getLeft();
        String leftName = left.getName();
        int leftReg;
        MipsSymbol leftSymbol;
        ArrayList<MipsInstruction> ret = new ArrayList<>();
        if (isCon(leftName)) {
            leftSymbol = new MipsSymbol("temp", 30, false, -1, false,
                    -1, true, false);
            leftReg = this.registerTable.getReg(true, leftSymbol, this.parent);
            Li li = new Li(leftReg, Integer.parseInt(leftName));
            ret.add(li);
        } else {
            leftReg = this.table.getRegIndex(leftName, true, this.parent);
            leftSymbol = this.table.getSymbol(leftName);
        }
        IRValue right = inst.getRight();
        String rightName = right.getName();
        MipsSymbol rightSymbol;
        int rightReg;
        rightSymbol = new MipsSymbol("temp", 30, false, -1, false,
                -1, true, false);
        rightReg = this.registerTable.getReg(true, rightSymbol, this.parent);
        Li li = new Li(rightReg, Integer.parseInt(rightName));
        ret.add(li);
        IRLabel elselabel = (IRLabel)inst.getElseLabel();
        IRLabel iflabel = (IRLabel)inst.getLabel();
        leftSymbol.setUsed(true);
        rightSymbol.setUsed(true);
        ArrayList<MipsInstruction> sws = this.registerTable.writeBackAll();
        if (sws != null && !sws.isEmpty()) {
            ret.addAll(sws);
        }
        Beq beq = new Beq(leftReg, rightReg, elselabel.getName());
        Bne bne = new Bne(leftReg, rightReg, iflabel.getName());
        ret.add(beq);
        ret.add(bne);
        return ret;
    }

    private ArrayList<MipsInstruction> generateFromLabel() {
        ArrayList<MipsInstruction> ans = new ArrayList<>();
        ArrayList<MipsInstruction> sws = this.registerTable.writeBackAll();
        if (sws != null && !sws.isEmpty()) {
            ans.addAll(sws);
        }
        IRLabel irLabel = (IRLabel)irInstruction;
        Label mipsLabel = new Label(irLabel.getName());
        ans.add(mipsLabel);
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromGoto() {
        IRGoto irGoto = (IRGoto)irInstruction;
        IRLabel irLabel = (IRLabel) irGoto.getOperand(0);
        J j = new J(irLabel.getName());
        ArrayList<MipsInstruction> ret = new ArrayList<>();

        ArrayList<MipsInstruction> sws = this.registerTable.writeBackAll();
        if (sws != null && !sws.isEmpty()) {
            ret.addAll(sws);
        }
        ret.add(j);
        return ret;
    }

    private ArrayList<MipsInstruction> generateFromStore() {
        IRStore store = (IRStore) irInstruction;
        IRValue left = store.getOperand(0);
        String leftName = left.getName();
        IRValue right = store.getOperand(1);
        String rightName = right.getName();
        int rightReg;
        int leftReg;
        MipsSymbol tempSymbol = null;
        ArrayList<MipsInstruction> ans = new ArrayList<>();
        if (isCon(leftName)) {
            tempSymbol = new MipsSymbol("name", 30, false, -1, false,
                    -1, true, false);
            leftReg = this.registerTable.getReg(true, tempSymbol, parent);
            Li li = new Li(leftReg, Integer.parseInt(leftName));
            ans.add(li);
        } else {
            leftReg = this.table.getRegIndex(leftName, true, parent);
        }
        MipsSymbol rightSymbol = this.table.getSymbol(rightName);
        boolean isGetElementPtr = store.isGetElementPtr();
        if (!isGetElementPtr) {
            rightReg = this.table.getRegIndex(rightName, false, parent);
            Move move = new Move(rightReg,leftReg);
            this.registerTable.getSymbol(leftReg).setUsed(true);
            ans.add(move);
        }
        boolean handleIrValue = store.isIrValue();
        if (isGetElementPtr) {
            if (handleIrValue) {
                IRValue dimension1PointerValue = store.getDimension1PointerValue();
                String dimension1PointerValueName = dimension1PointerValue.getName();
                if (isCon(dimension1PointerValueName)) {
                    ArrayList<MipsInstruction> temp = this.registerTable.writeBackPublic(
                            leftReg, rightSymbol,
                            Integer.parseInt(dimension1PointerValueName) * 4, this.parent);
                    rightSymbol.setUsed(true);
                    if (temp != null && !temp.isEmpty()) {
                        ans.addAll(temp);
                    }
                } else {
                    int dimension1PointerReg = this.table.getRegIndex(dimension1PointerValueName, true, this.parent);
                    ArrayList<MipsInstruction> temp = this.registerTable.writeBackPublic(
                            leftReg, rightSymbol, dimension1PointerReg, 1, this.parent);
                    if (temp != null && !temp.isEmpty()) {
                        ans.addAll(temp);
                    }
                }
            } else {
                ArrayList<MipsInstruction> temp = this.registerTable.writeBackPublic(leftReg,
                        rightSymbol, store.getDimension1Pointer() * 4, this.parent);
                rightSymbol.setUsed(true);
                if (temp != null && !temp.isEmpty()) {
                    ans.addAll(temp);
                }
            }
        } else if (rightName.contains("Global")) {
            MipsInstruction temp = this.registerTable.writeBackPublic(rightSymbol);
            rightSymbol.setUsed(true);
            rightSymbol.setInReg(false);
            ans.add(temp);
        }
        if (tempSymbol != null) {
            tempSymbol.setUsed(true);
        }
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromRet() {
        IRRet ret = (IRRet) irInstruction;
        ArrayList<MipsInstruction> ans = new ArrayList<>();
        if (!ret.isVoid()) {
            String name = ret.getOperand(0).getName();
            int reg;
            MipsSymbol temp = null;
            if (isCon(name)) {
                temp = new MipsSymbol("temp", 30);
                reg = this.registerTable.getReg(true, temp, parent);
                Li li = new Li(reg, Integer.parseInt(name));
                ans.add(li);
            } else {
                reg = this.table.getRegIndex(name, true, parent);
            }
            Move move = new Move(2, reg);
            ans.add(move);
            if (temp != null) {
                temp.setUsed(true);
            }
            if (!this.parent.getParent().isMain()) {
                Jr jr = new Jr(31);
                ans.add(jr);
            } else {
                Li li = new Li(2, 0xa);
                ans.add(li);
                Syscall syscall = new Syscall();
                ans.add(syscall);
            }
        } else {
            Jr jr = new Jr(31);
            ans.add(jr);
        }
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromLoad() {
        IRLoad left = (IRLoad) irInstruction;
        String leftName = left.getName();

        MipsSymbol leftSymbol = new MipsSymbol(leftName, 30, false,
                -1, false, 0, true, false);
        insertSymbolTable(leftName, leftSymbol);

        IRValue right = left.getOperand(0);
        String rightName = right.getName();
        MipsSymbol rightSymbol = this.table.getSymbol(rightName);

        int rightSize = right.getSize();
        int rightReg;
        ArrayList<MipsInstruction> ans = new ArrayList<>();
        boolean isGetElementPtr = left.rightIsGEP();
        if (rightSize == 0) {
            if (isGetElementPtr) {
                int offset = rightSymbol.getOffset();
                int baseReg = rightSymbol.getBase();
                int leftReg = this.registerTable.getReg(true, leftSymbol, parent);
                Lw lw = new Lw(leftReg, baseReg, offset);
                ans.add(lw);
            } else {
                int leftReg = this.registerTable.getReg(true, leftSymbol, parent);
                leftSymbol.setInReg(true);
                leftSymbol.setRegIndex(leftReg);
                rightReg = this.table.getRegIndex(rightName, true, parent);
                Move move = new Move(leftReg, rightReg);
                ans.add(move);
            }
            this.parent.addInstruction(ans);
            ans = new ArrayList<>();
        } else {
            IRValue dimension1 = left.getDimension1Value();
            String dimension1Name = dimension1.getName();
            int reg1 = -1;
            MipsSymbol dimension1Symbol = null;
            if (isCon(dimension1Name)) {
                dimension1Symbol = new MipsSymbol("Temp", 30, false, -1, false,
                        -1, true, false);
                reg1 = this.registerTable.getReg(true, dimension1Symbol, this.parent);
                Li li = new Li(reg1, Integer.parseInt(dimension1Name));
                ans.add(li);
                this.parent.addInstruction(ans);
                ans = new ArrayList<>();
            } else {
                reg1 = this.table.getRegIndex(dimension1Name, true, parent);
            }
            ArrayList<MipsInstruction> instructions = this.registerTable.readBackPublic(leftSymbol, rightSymbol, reg1, this.parent);
            if (instructions != null) {
                ans.addAll(instructions);
                this.parent.addInstruction(ans);
                ans = new ArrayList<>();
            }
            if (dimension1Symbol != null) {
                dimension1Symbol.setUsed(true);
            }
        }
        if (rightName.contains("Global")) {
            rightSymbol.setInReg(false);
        }
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromCall() {
        IRCall call = (IRCall) irInstruction;
        String functionName = call.getFunctionName();
        ArrayList<MipsInstruction> ans = new ArrayList<>();
        switch (functionName) {
            case "@putint" -> {
                Move move = new Move(3, 4);
                ans.add(move);
                Li li = new Li(2, 1);
                ans.add(li);
                String name = call.getOperand(1).getName();
                MipsSymbol symbol = null;
                if (this.table.hasSymbol(name)) {
                    symbol = this.table.getSymbol(name);
                    int reg = this.table.getRegIndex(name, true, parent);
                    Move move1 = new Move(4, reg);
                    ans.add(move1);
                } else {
                    Li li1 = new Li(4, Integer.parseInt(name));
                    ans.add(li1);
                }
                Syscall syscall = new Syscall();
                ans.add(syscall);
                Move move1 = new Move(4, 3);
                ans.add(move1);
                if (symbol != null) {
                    symbol.setUsed(true);
                }
            }
            case "@getint" -> ans = generateFromGetIntFunc();
            case "@getchar" -> ans = generateFromGetCharFunc();
            case "@putch" -> ans = generateFromPutCharFunc();
            default -> ans = generateFromNormalFunc();
        }
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromPutCharFunc() {
        IRCall call = (IRCall) irInstruction;
        ArrayList<MipsInstruction> ans = new ArrayList<>();

        // Move $v0 to $v1 to protect it
        Move move = new Move(3, 2);
        ans.add(move);

        // Load the syscall code for print character (11) into $v0
        Li li = new Li(2, 11);
        ans.add(li);

        // Get the character to print from the operand and move it to $a0
        String name = call.getOperand(1).getName();
        MipsSymbol symbol = null;
        if (this.table.hasSymbol(name)) {
            symbol = this.table.getSymbol(name);
            int reg = this.table.getRegIndex(name, true, parent);
            Move move1 = new Move(4, reg);
            ans.add(move1);
        } else {
            Li li1 = new Li(4, Integer.parseInt(name));
            ans.add(li1);
        }

        // Perform the syscall
        Syscall syscall = new Syscall();
        ans.add(syscall);

        // Restore $v0 from $v1
        Move move1 = new Move(2, 3);
        ans.add(move1);

        if (symbol != null) {
            symbol.setUsed(true);
        }

        return ans;
    }

    private ArrayList<MipsInstruction> generateFromNormalFunc() {
        IRCall call = (IRCall) irInstruction;
        ArrayList<MipsInstruction> ans = new ArrayList<>();
//        ArrayList<MipsInstruction> saveAll = this.registerTable.writeBackAll();
//        ans.addAll(saveAll);
//
//        if (!ans.isEmpty()) {
//            this.parent.addInstruction(ans);
//            ans = new ArrayList<>();
//        }

        // 保存现场
        int spOffset = 0;
        for (int i = 2; i < 32; i++) {
            if (26 <= i && i <= 30) {
                continue;
            }
            if (this.registerTable.inReg(i) ||  i == 31) {
                Sw sw = new Sw(i, 29, spOffset);
                ans.add(sw);
                spOffset -= 4;
            }
        }

        // 传参
        Addi addi = new Addi(3, 30, (this.table.getFpOffset() + 32 * 4));
        ans.add(addi);
        this.parent.addInstruction(ans);
        ans = new ArrayList<>();

        RegisterTable newRegisterTable = new RegisterTable();
        MipsSymbolTable newTable = new MipsSymbolTable(newRegisterTable);
        newTable.setTable(this.table.cloneTable());
        newTable.setOffset(this.table.getFpOffset());
        newRegisterTable.setTable(newTable);
        newRegisterTable.setIfUsed(this.registerTable.cloneIfUsed());
        newRegisterTable.setTable(this.registerTable.cloneTable(newTable));
        newRegisterTable.setSRegStack(this.registerTable.cloneSRegStack());

        ArrayList<IRValue> parameters = call.getArgs();
        int size = parameters.size();
        int newOffset = 0;
        for (int i = 0;i < size;i++) {
            IRValue parameter = parameters.get(i);
            String name = parameter.getName();
            if (newTable.hasSymbol(name)) {
                boolean isGetElementPtr = parameter instanceof IRGetElementPtr;
                int dimension = parameter.getSize();
                if (dimension == 0 && !isGetElementPtr) {
                    int reg = newTable.getRegIndex(name, true, parent);
                    if (i < 4) {
                        Move move = new Move(4 + i, reg);
                        newTable.getSymbol(name).setUsed(true);
                        ans.add(move);
                    } else {
                        Sw sw = new Sw(reg, 3, newOffset);
                        ans.add(sw);
                    }
                    this.parent.addInstructions(ans);
                } else {
                    boolean isParam = parameter.isParam();
                    int dimensionValue = parameter.getDimensionValue();
                    if (isParam) {
                        if (dimensionValue == 0) {
                            IRValue dimension1 = parameter.getDimension1Value();
                            String name1 = dimension1.getName();
                            if (isCon(name1)) {
                                Li li = new Li(2, Integer.parseInt(name1));
                                ans.add(li);
                                parent.addInstructions(ans);
                                ans = new ArrayList<>();

                                Sll sll = new Sll(2, 2, 2);
                                ans.add(sll);
                            } else {
                                int reg = newTable.getRegIndex(name1, true, parent);
                                Move move = new Move(2, reg);
                                ans.add(move);
                                parent.addInstructions(ans);
                                ans = new ArrayList<>();

                                Sll sll = new Sll(2, 2, 2);
                                ans.add(sll);
                            }
                            parent.addInstructions(ans);
                            ans = new ArrayList<>();

                            int reg = newTable.getRegIndex(name, true, parent);
                            Add add = new Add(2, 2, reg);
                            ans.add(add);
                            parent.addInstructions(ans);
                            ans = new ArrayList<>();
                            Lw lw;
                            if (i < 4) {
                                lw = new Lw(4 + i, 2, 0);
                                ans.add(lw);
                            } else {
                                lw = new Lw(2, 2, 0);
                                ans.add(lw);
                                this.parent.addInstruction(ans);
                                ans = new ArrayList<>();
                                Sw sw = new Sw(2, 3, newOffset);
                                ans.add(sw);
                            }
                        } else {
                            int reg = newTable.getRegIndex(name, true, parent);
                            if (i < 4) {
                                Move move = new Move(4 + i, reg);
                                ans.add(move);
                            } else {
                                Sw sw = new Sw(reg, 3, newOffset);
                                ans.add(sw);
                            }
                        }
                    } else {
                        MipsSymbol symbol = this.table.getSymbol(name);
                        int offset = symbol.getOffset();
                        int base = symbol.getBase();
                        if (dimensionValue == 0) {
                            IRValue dimension1 = parameter.getDimension1Value();
                            String dimension1Name = dimension1.getName();
                            if (isCon(dimension1Name)) {
                                Li li = new Li(2, Integer.parseInt(dimension1Name));
                                ans.add(li);
                                this.parent.addInstruction(ans);
                                ans = new ArrayList<>();
                                Sll sll = new Sll(2, 2, 2);
                                ans.add(sll);
                            } else {
                                int dimension1Reg = newTable.getRegIndex(dimension1Name, true, this.parent);
                                Sll sll = new Sll(2, dimension1Reg, 2);
                                ans.add(sll);
                            }
                            this.parent.addInstruction(ans);
                            ans = new ArrayList<>();

                            Addi addi1 = new Addi(2, 2, offset);
                            ans.add(addi1);
                            this.parent.addInstruction(ans);
                            ans = new ArrayList<>();
                            // 计算绝对地址
                            Add add = new Add(2, 2, base);
                            ans.add(add);
                            this.parent.addInstruction(ans);
                            ans = new ArrayList<>();
                            Lw lw;
                            if (i < 4) {
                                lw = new Lw(4 + i, 2, 0);
                                ans.add(lw);
                            } else {
                                lw = new Lw(2, 2, 0);
                                ans.add(lw);
                                this.parent.addInstruction(ans);
                                ans = new ArrayList<>();
                                Sw sw = new Sw(2, 3, newOffset);
                                ans.add(sw);
                            }
                        } else {
                            if (i < 4) {
                                addi = new Addi(4 + i, base, offset);
                                ans.add(addi);
                            } else {
                                addi = new Addi(2, base, offset);
                                ans.add(addi);
                                this.parent.addInstruction(ans);
                                ans = new ArrayList<>();
                                Sw sw = new Sw(2, 3, newOffset);
                                ans.add(sw);
                            }
                        }
                    }
                    this.parent.addInstruction(ans);
                }
            } else {
                if (i < 4) {
                    Li li = new Li(4 + i, Integer.parseInt(name));
                    ans.add(li);
                } else {
                    Li li = new Li(2, Integer.parseInt(name));
                    ans.add(li);
                    this.parent.addInstruction(ans);
                    ans = new ArrayList<>();
                    Sw sw = new Sw(2, 3, newOffset);
                    ans.add(sw);
                }
                this.parent.addInstruction(ans);
            }
            ans = new ArrayList<>();
            if (i >= 4) {
                newOffset += 4;
            }
        }
        for (int i = 4;i < size;i++) {
            IRValue param = parameters.get(i);
            String name = param.getName();
            if (newTable.hasSymbol(name)) {
                int reg = newTable.getRegIndex(name, true, parent);
                Sw sw = new Sw(reg, 3, newOffset);
                ans.add(sw);
                this.table.getSymbol(name).setUsed(true);
            } else {
                Li li = new Li(8, Integer.parseInt(name));
                ans.add(li);
                Sw sw = new Sw(8, 3, newOffset);
                ans.add(sw);
            }
            this.parent.addInstruction(ans);
            ans = new ArrayList<>();
            newOffset += 4;
        }

        Move move = new Move(30,3);
        ans.add(move);
        Addi addi1 = new Addi(29, 29, spOffset);
        ans.add(addi1);

        Jal jal = new Jal(call.getFunctionName().substring(1));
        ans.add(jal);
        this.parent.addInstruction(ans);
        ans = new ArrayList<>();

        // 恢复现场
        Addi addi2 = new Addi(30, 30, -(this.table.getFpOffset() + 32 * 4));
        ans.add(addi2);
        Addi addi3 = new Addi(29, 29, -spOffset);
        ans.add(addi3);
        this.parent.addInstruction(ans);
        ans = new ArrayList<>();
        for (int i = 31; i >= 2; i--) {
            if (26 <= i && i <= 30) {
                continue;
            }
            if (this.registerTable.inReg(i) || i == 31) {
                spOffset += 4;
                Lw lw = new Lw(i, 29, spOffset);
                ans.add(lw);
            }
        }
        this.parent.addInstruction(ans);
        ans = new ArrayList<>();

        // 返回值
        if (!call.getName().isEmpty()) {
            MipsSymbol leftSymbol = new MipsSymbol(call.getName(), 30, false, -1, false,
                    0, true, false);
            insertSymbolTable(call.getName(), leftSymbol);
            int leftReg = this.table.getRegIndex(call.getName(), false, parent);
            Move move1 = new Move(leftReg, 2);
            ans.add(move1);
            this.parent.addInstruction(ans);
            ans = new ArrayList<>();
        }
        ans = new ArrayList<>();
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromGetCharFunc() {
        IRCall call = (IRCall) irInstruction;
        ArrayList<MipsInstruction> ans = new ArrayList<>();

        // v0到v1保护
        Move move = new Move(3, 2);
        ans.add(move);
        Li li = new Li(2, 12);
        ans.add(li);
        Syscall syscall = new Syscall();
        ans.add(syscall);

        MipsSymbol symbol = new MipsSymbol(call.getName(), 30, false, -1,
                false, -1, true, false);
        insertSymbolTable(call.getName(), symbol);
        int reg = this.table.getRegIndex(call.getName(), false, parent);
        Move move1 = new Move(reg, 2);
        ans.add(move1);
        Move move2 = new Move(2, 3);
        ans.add(move2);
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromGetIntFunc() {
        IRCall call = (IRCall) irInstruction;
        ArrayList<MipsInstruction> ans = new ArrayList<>();

        // v0到v1保护
        Move move = new Move(3, 2);
        ans.add(move);
        Li li = new Li(2, 5);
        ans.add(li);
        Syscall syscall = new Syscall();
        ans.add(syscall);

        MipsSymbol symbol = new MipsSymbol(call.getName(), 30, false, -1,
                false, -1, true, false);
        insertSymbolTable(call.getName(), symbol);
        int reg = this.table.getRegIndex(call.getName(), false, parent);
        Move move1 = new Move(reg, 2);
        ans.add(move1);
        Move move2 = new Move(2, 3);
        ans.add(move2);
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromAlloca() {
        // alloca仅将其加入符号表，暂时不分配
        IRAlloca alloca = (IRAlloca) irInstruction;
        String name = alloca.getName();
        int size = alloca.getSize();
        MipsSymbol symbol;
        symbol = new MipsSymbol(name, 30);
        if (size != 0) {
            symbol.setSize(size);
            int offset = this.table.getFpOffset();
            symbol.setOffset(offset);
            symbol.setHasRam(true);
            offset += 4 * size;
            this.table.setOffset(offset);
        }
        insertSymbolTable(name, symbol);
        return null;
    }

    private ArrayList<MipsInstruction> generateFromBinaryInstruction() {
        IRBinaryInstruction binaryInstruction = (IRBinaryInstruction) irInstruction;
        IRValue left = binaryInstruction.getLeft();
        String leftName = left.getName();
        MipsSymbol leftSymbol;
        ArrayList<MipsInstruction> ans = new ArrayList<>();
        int leftReg = -1;
        if (isCon(leftName)) {
            leftSymbol = new MipsSymbol("temp", 30, false, -1, false, -1, true, false);
            leftReg = this.registerTable.getReg(true, leftSymbol, parent);
            Li li = new Li(leftReg, Integer.parseInt(leftName));
            ans.add(li);
        } else {
            leftReg = this.table.getRegIndex(leftName, false, parent);
            leftSymbol = this.table.getSymbol(leftName);
        }

        IRValue right = binaryInstruction.getRight();
        int rightReg = -1;
        MipsSymbol rightSymbol = null;
        if (right != null) {
            String rightName = right.getName();
            if (isCon(rightName)) {
                rightSymbol = new MipsSymbol("temp", 30, false, -1, false, -1, true, false);
                rightReg = this.registerTable.getReg(true, rightSymbol, parent);
                Li li = new Li(rightReg, Integer.parseInt(rightName));
                ans.add(li);
            } else {
                rightReg = this.table.getRegIndex(rightName, false, parent);
                rightSymbol = this.table.getSymbol(rightName);
            }
        }
        String ansName = binaryInstruction.getName();
        MipsSymbol ansSymbol = new MipsSymbol(ansName, 30, false, -1, false, -1, true, false);
        insertSymbolTable(ansName, ansSymbol);
        int ansReg = this.registerTable.getReg(true, ansSymbol, parent);
        if (binaryInstruction.getInstructionType().equals(IRInstructionType.Add)) {
            Add add = new Add(ansReg, leftReg, rightReg);
            ans.add(add);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Sub)) {
            Sub sub = new Sub(ansReg, leftReg, rightReg);
            ans.add(sub);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Mul)) {
            Mul mul = new Mul(ansReg, leftReg, rightReg);
            ans.add(mul);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Div)) {
            Div div = new Div(ansReg, leftReg, rightReg);
            ans.add(div);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Mod)) {
            Div div = new Div(-1, leftReg, rightReg);
            ans.add(div);
            Mfhi mfhi = new Mfhi(ansReg);
            ans.add(mfhi);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Le)) {
            /* <= */
            Sle sle = new Sle(ansReg, leftReg, rightReg);
            ans.add(sle);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Gt)) {
            /* > */
            Sgt sgt = new Sgt(ansReg, leftReg, rightReg);
            ans.add(sgt);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Ge)) {
            /* >= */
            Sge sge = new Sge(ansReg, leftReg, rightReg);
            ans.add(sge);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Eq)) {
            /* ==*/
            Seq seq = new Seq(ansReg, leftReg, rightReg);
            ans.add(seq);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Ne)) {
            /* != */
            Sne sne = new Sne(ansReg, leftReg, rightReg);
            ans.add(sne);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Not)) {
            /* ! */
            Li li = new Li(3, 0);
            Seq seq = new Seq(ansReg, leftReg, 3);
            ans.add(li);
            ans.add(seq);
        } else if (binaryInstruction.getInstructionType().equals(IRInstructionType.Lt)) {
            Slt slt = new Slt(ansReg, leftReg, rightReg);
            ans.add(slt);
        } else {
            System.out.println("Error: Unknown binary instruction type");
        }

        leftSymbol.setUsed(true);
        if (rightSymbol != null) {
            rightSymbol.setUsed(true);
        }
        return ans;
    }
}
