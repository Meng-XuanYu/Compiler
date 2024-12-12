package backend.MipsInstruction;
import backend.MipsBlock.MipsBasicBlock;
import backend.MipsSymbol.MipsSymbol;
import backend.MipsSymbol.MipsSymbolTable;
import backend.MipsSymbol.RegisterTable;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Value.Instruction.IRBinaryInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstruction;
import middleend.LlvmIr.Value.Instruction.IRInstructionType;
import middleend.LlvmIr.Value.Instruction.IRLabel;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.IRAlloca;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.IRLoad;
import middleend.LlvmIr.Value.Instruction.MemoryInstructions.IRStore;
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
        } else {
            System.out.println("ERROR in MipsInstructionBuilder : should not reach here");
        }
        return null;
    }

    private boolean isCon(String name) {
        return !(name.contains("@") || name.contains("%"));
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
        IRLabel label = (IRLabel)inst.getLabel();
        leftSymbol.setUsed(true);
        rightSymbol.setUsed(true);
        ArrayList<MipsInstruction> sws = this.registerTable.writeBackAll();
        if (sws != null && !sws.isEmpty()) {
            ret.addAll(sws);
        }
        if (inst.getInstructionType().equals(IRInstructionType.Beq)) {
            Beq beq = new Beq(leftReg, rightReg, label.getName().substring(1));
            ret.add(beq);
        } else {
            Bne bne = new Bne(leftReg, rightReg, label.getName().substring(1));
            ret.add(bne);
        }
        return ret;
    }

    private ArrayList<MipsInstruction> generateFromLabel() {
        ArrayList<MipsInstruction> ans = new ArrayList<>();
        ArrayList<MipsInstruction> sws = this.registerTable.writeBackAll();
        if (sws != null && !sws.isEmpty()) {
            ans.addAll(sws);
        }
        IRLabel irLabel = (IRLabel)irInstruction;
        Label mipsLabel = new Label(irLabel.getName().substring(1));
        ans.add(mipsLabel);
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromGoto() {
        IRGoto irGoto = (IRGoto)irInstruction;
        IRLabel irLabel = (IRLabel) irGoto.getOperand(0);
        J j = new J(irLabel.getName().substring(1));
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
        int dimensionPointer = store.getDimensionPointer();
        if (dimensionPointer == 0) {
            rightReg = this.table.getRegIndex(rightName, false, parent);
            Move move = new Move(rightReg,leftReg);
            this.registerTable.getSymbol(leftReg).setUsed(true);
            ans.add(move);
        }
        boolean handleIrValue = store.isIrValue();
        if (dimensionPointer != 0) {
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
                -1, false, -1, true, false);
        this.table.addSymbol(leftName, leftSymbol);

        IRValue right = left.getOperand(0);
        String rightName = right.getName();
        MipsSymbol rightSymbol = this.table.getSymbol(rightName);

        int rightSize = right.getSize();
        int rightReg = -1;
        ArrayList<MipsInstruction> ans = new ArrayList<>();
        if (rightSize ==0) {
            int leftReg = this.registerTable.getReg(true, leftSymbol, parent);
            leftSymbol.setUsed(true);
            leftSymbol.setRegIndex(leftReg);
            rightReg = this.table.getRegIndex(rightName, true, parent);
            Move move = new Move(leftReg, rightReg);
            ans.add(move);
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
            default -> ans = generateFromNormalFunc();
        }
        return ans;
    }

    private ArrayList<MipsInstruction> generateFromNormalFunc() {
        IRCall call = (IRCall) irInstruction;
        ArrayList<MipsInstruction> saveAll = this.registerTable.saveAll();
        ArrayList<MipsInstruction> ans = new ArrayList<>(saveAll);

        if (!ans.isEmpty()) {
            this.parent.addInstruction(ans);
            ans = new ArrayList<>();
        }

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
        newRegisterTable.setTable(this.registerTable.cloneTable());
        newRegisterTable.setSRegStack(this.registerTable.cloneSRegStack());

        ArrayList<IRValue> parameters = call.getArgs();
        int size = parameters.size();
        int newOffset = 0;
        for (int i = 0;i < size;i++) {
            IRValue parameter = parameters.get(i);
            String name = parameter.getName();
            if (newTable.hasSymbol(name)) {
                int dimension = parameter.getSize();
                if (dimension == 0) {
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
                Li li = new Li(4 + i, Integer.parseInt(name));
                ans.add(li);
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
            this.table.addSymbol(call.getName(), leftSymbol);
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
        this.table.addSymbol(call.getName(), symbol);
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
        this.table.addSymbol(call.getName(), symbol);
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
        MipsSymbol symbol = null;
        symbol = new MipsSymbol(name, 30);
        if (size != 0) {
            symbol.setSize(size);
            int offset = this.table.getOffset();
            symbol.setOffset(offset);
            symbol.setHasRam(true);
            offset += 4 * size;
            this.table.setOffset(offset);
        }
        this.table.addSymbol(name, symbol);
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
        this.table.addSymbol(ansName, ansSymbol);
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
        } else {
            System.out.println("ERROR in MipsInstructionBuilder : should not reach here");
        }

        leftSymbol.setUsed(true);
        if (rightSymbol != null) {
            rightSymbol.setUsed(true);
        }
        return ans;
    }
}
