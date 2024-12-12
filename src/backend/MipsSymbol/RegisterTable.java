package backend.MipsSymbol;

import backend.MipsBlock.MipsBasicBlock;
import backend.MipsInstruction.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class RegisterTable {
    private MipsSymbolTable symbolTable; //双向存储
    private HashMap<Integer, Boolean> ifUsed;
    private HashMap<Integer, MipsSymbol> table;
    private final int regNum = 32;
    private int regOld = 8;
    // 记录s0-s7的使用情况
    private Stack<Integer> sRegStack;

    // 常量
    public int getReg(boolean temp, MipsSymbol symbol, MipsBasicBlock basicBlock) {
        int freeReg = getFreeReg(temp);
        if (freeReg == -1) {
            // 无空闲寄存器，需要存入内存
            if (temp) {
                int ans = getOldReg(basicBlock);
                symbol.setRegIndex(ans);
                symbol.setInReg(true);

                this.table.put(ans, symbol);
                this.ifUsed.put(ans, true);
                return ans;
            } else {
                // 找出一个s寄存器
                int oldReg = this.sRegStack.pop();
                MipsSymbol oldSymbol = this.table.get(oldReg);
                if (oldSymbol.hasRam()) {
                    if (oldSymbol.isInReg()) {
                        writeBack(oldSymbol, basicBlock);
                    }
                } else {
                    if (oldSymbol.isInReg()) {
                        allocRam(oldSymbol);
                        writeBack(oldSymbol, basicBlock);
                    }
                }
                oldSymbol.setInReg(false);
                oldSymbol.setRegIndex(-1);

                symbol.setRegIndex(oldReg);
                symbol.setInReg(true);

                this.table.put(oldReg, symbol);
                this.sRegStack.push(oldReg);

                if (symbol.hasRam()) {
                    read(oldReg, basicBlock, symbol);
                }
                if (oldReg == this.regOld) {
                    this.regOld = (this.regOld + 1 + 32) % 32;
                }
                return oldReg;
            }
        } else {
            if (!temp) {
                // 如果是s寄存器，需要将其加入sRegStack
                this.sRegStack.push(freeReg);
            }
            symbol.setRegIndex(freeReg);
            symbol.setInReg(true);
            this.table.put(freeReg, symbol);
            this.ifUsed.put(freeReg, true);
            if (symbol.hasRam()) {
                read(freeReg, basicBlock, symbol);
            }
            if (freeReg == this.regOld) {
                this.regOld = (this.regOld + 1 + 32) % 32;
            }
            return freeReg;
        }
    }

    private void read(int regIndex, MipsBasicBlock basicBlock, MipsSymbol symbol) {
        Lw lw = new Lw(regIndex, symbol.getBase(), symbol.getOffset());
        ArrayList<MipsInstruction> instructions = new ArrayList<>();
        instructions.add(lw);
        basicBlock.addInstruction(instructions);
    }

    // 获取一个空闲的寄存器
    private int getFreeReg(boolean isTemp) {
        for (int i = 0; i < regNum; i++) {
            // 如果是临时寄存器，只能使用t0-t9
            if (isTempReg(i) & isTemp) {
                if (!ifUsed.get(i)) {
                    return i;
                } else {
                    MipsSymbol symbol = table.get(i);
                    if (symbol.isTemp() && symbol.isUsed()) {
                        return i;
                    }
                }
            } else if (isSReg(i) && !isTemp) {
                if (!ifUsed.get(i)) {
                    return i;
                } else if (!table.get(i).isInReg()) {
                    return i;
                }
            }
        }
        return -1;
    }

    // 获取最久未使用的寄存器
    private int getOldReg(MipsBasicBlock basicBlock) {
        while (true) {
            if (this.isTempReg(this.regOld)) {
                MipsSymbol symbol = this.table.get(this.regOld);
                if (!symbol.hasRam()) {
                    allocRam(symbol);
                }
                writeBack(symbol, basicBlock);
                symbol.setInReg(false);
                this.regOld = (this.regOld + 1 + 32) % 32;
                return symbol.getRegIndex();
            } else {
                this.regOld = (this.regOld + 1 + 32) % 32;
            }
        }
    }

    private void writeBack(MipsSymbol symbol, MipsBasicBlock basicBlock) {
        int regIndex = symbol.getRegIndex();
        int offset = symbol.getOffset();
        int base = symbol.getBase();
        Sw sw = new Sw(regIndex, base, offset);
        ArrayList<MipsInstruction> instructions = new ArrayList<>();
        instructions.add(sw);
        basicBlock.addInstruction(instructions);
    }

    private void allocRam(MipsSymbol symbol) {
        int offset = this.symbolTable.getOffset();
        this.symbolTable.addOffset(4);
        symbol.setOffset(offset);
        symbol.setHasRam(true);
        this.symbolTable.addOffset(8);
    }

    private boolean isTempReg(int index) {
        return (8 <= index && index <= 15) || (24 <= index && index <= 25);
    }

    private boolean isSReg(int index) {
        return 16 <= index && index <= 23;
    }

    private boolean isVreg(int index) {
        return 2 <= index && index <= 3;
    }

    private boolean isAreg(int index) {
        return 4 <= index && index <= 7;
    }

    private boolean isRareg(int index) {
        return index == 31;
    }

    public void setTable(MipsSymbolTable table) {
        this.symbolTable = table;
    }

    public void addSymbol(int i, MipsSymbol symbol) {
        this.table.put(i, symbol);
        this.ifUsed.put(i, true);
    }

    // 释放寄存器
    public ArrayList<MipsInstruction> saveAll() {
        ArrayList<MipsInstruction> instructions = new ArrayList<>();
        for(int i = 0; i < regNum; i++) {
            // t
            if (this.isTempReg(i)) {
                if (this.ifUsed.get(i)) {
                    MipsSymbol symbol = this.table.get(i);
                    if (!symbol.isTemp() || (symbol.isTemp() && !symbol.isUsed())) {
                        if ((!symbol.hasRam())) {
                            allocRam(symbol);
                        }
                        Sw sw = new Sw(i, symbol.getBase(), symbol.getOffset());
                        instructions.add(sw);
                        symbol.setInReg(false);
                        symbol.setRegIndex(-1);
                    }
                    this.ifUsed.put(i, false);
                }
            } else if (this.isSReg(i)) {
                if (this.ifUsed.get(i)) {
                    MipsSymbol symbol = this.table.get(i);
                    if (!symbol.isInReg()) {
                        continue;
                    }
                    if (!symbol.hasRam()) {
                        allocRam(symbol);
                    }
                    Sw sw = new Sw(i, symbol.getBase(), symbol.getOffset());
                    instructions.add(sw);
                    symbol.setInReg(false);
                    symbol.setRegIndex(-1);
                }
                this.ifUsed.put(i, false);
            } else if (isVreg(i) ||
                    isRareg(i) ||
                    isAreg(i)) {
                if (this.ifUsed.get(i)) {
                    MipsSymbol symbol = this.table.get(i);
                    if (!symbol.hasRam()) {
                        allocRam(symbol);
                    }
                    Sw sw = new Sw(i, symbol.getBase(), symbol.getOffset());
                    instructions.add(sw);
                    symbol.setInReg(false);
                    symbol.setRegIndex(-1);
                }
                this.ifUsed.put(i, false);
            }
        }
        while (!this.sRegStack.empty()) {
            this.sRegStack.pop();
        }
        this.regOld = 8;
        return instructions;
    }

    public boolean inReg(int i) {
        return this.ifUsed.get(i);
    }

    public void setIfUsed(HashMap<Integer, Boolean> ifUsed) {
        this.ifUsed = ifUsed;
    }

    public HashMap<Integer, Boolean> cloneIfUsed() {
        HashMap<Integer, Boolean> newIfUsed = new HashMap<>();
        for (Integer index : this.ifUsed.keySet()) {
            newIfUsed.put(index, this.ifUsed.get(index));
        }
        return newIfUsed;
    }

    public void setTable(HashMap<Integer, MipsSymbol> table) {
        this.table = table;
    }

    public HashMap<Integer, MipsSymbol> cloneTable() {
        HashMap<Integer, MipsSymbol> newRegs = new HashMap<>();
        for (Integer index : this.table.keySet()) {
            String name = this.table.get(index).getName();
            MipsSymbol symbol = this.symbolTable.getSymbol(name);
            if (symbol == null) {
                newRegs.put(index, this.table.get(index).cloneMipsSymbol());
            } else {
                newRegs.put(index, this.symbolTable.getSymbol(name));
            }
        }
        return newRegs;
    }

    public void setSRegStack(Stack<Integer> sRegStack) {
        this.sRegStack = sRegStack;
    }

    public Stack<Integer> cloneSRegStack() {
        Stack<Integer> newSRegStack = new Stack<>();
        for (Integer index : this.sRegStack) {
            newSRegStack.push(index);
        }
        return newSRegStack;
    }

    public ArrayList<MipsInstruction> readBackPublic(MipsSymbol leftSymbol,
                                                     MipsSymbol symbol,
                                                     int reg1,
                                                     MipsBasicBlock basicBlock) {
        ArrayList<MipsInstruction> instructions = new ArrayList<>();
        boolean isParam = symbol.isParam();
        Sll sll = new Sll(3, reg1, 2);
        instructions.add(sll);
        if (isParam) {
            int reg = this.symbolTable.getRegIndex(symbol.getName(), true, basicBlock);
            Add add = new Add(3, 3, reg);
            instructions.add(add);
        } else {
            int base = symbol.getBase();
            int fpOffset = symbol.getOffset();
            Addi addi = new Addi(3, 3, fpOffset);
            instructions.add(addi);
            Add add = new Add(3, 3, base);
            instructions.add(add);
        }

        int leftReg = this.getReg(true, leftSymbol, basicBlock);
        leftSymbol.setInReg(true);
        leftSymbol.setRegIndex(leftReg);
        Lw lw = new Lw(leftReg, 3, 0);
        instructions.add(lw);
        return instructions;
    }

    public MipsSymbol getSymbol(int reg) {
        return this.table.get(reg);
    }

    public ArrayList<MipsInstruction> writeBackPublic(int leftReg,
                                                      MipsSymbol symbol,
                                                      int deltaOffset,
                                                      MipsBasicBlock basicBlock) {
        String name = symbol.getName();
        boolean isParam = symbol.isParam();
        Sw sw;
        ArrayList<MipsInstruction> ret = new ArrayList<>();
        if (isParam) {
            int reg = this.symbolTable.getRegIndex(name, true, basicBlock);
            Addi addi = new Addi(3, reg, deltaOffset);
            ret.add(addi);
            sw = new Sw(leftReg, 3, 0);
            ret.add(sw);
        } else {
            int base = symbol.getBase();
            int offset = symbol.getOffset() + deltaOffset;
            sw = new Sw(leftReg, base, offset);
            ret.add(sw);
        }
        return ret;
    }

    public MipsInstruction writeBackPublic(MipsSymbol symbol) {
        int rt = symbol.getRegIndex();
        int base = symbol.getBase();
        int offset = symbol.getOffset();
        return new Sw(rt, base, offset);
    }

    public ArrayList<MipsInstruction> writeBackAll() {
        ArrayList<MipsInstruction> instructions = new ArrayList<>();
        for (int i = 0; i < this.regNum; i++) {
            if (this.isTempReg(i)) {
                if (this.ifUsed.get(i)) {
                    MipsSymbol symbol = this.table.get(i);
                    if (!symbol.isTemp() ||
                            (symbol.isTemp() && !symbol.isUsed())) {
                        if (!symbol.hasRam()) {
                            allocRam(symbol);
                        }
                        Sw sw = new Sw(i, symbol.getBase(), symbol.getOffset());
                        symbol.setInReg(false);
                        symbol.setRegIndex(-1);
                        instructions.add(sw);
                    }
                    this.ifUsed.put(i, false);
                }
            } else if (this.isSReg(i)) {
                /* s寄存器 */
                if (this.ifUsed.get(i)) {
                    /* s寄存器内有值 */
                    MipsSymbol symbol = this.table.get(i);
                    if (!symbol.isInReg()) {
                        /* 不在寄存器中则不写回 */
                        continue;
                    }
                    if (!symbol.hasRam()) {
                        allocRam(symbol);
                    }
                    Sw sw = new Sw(i, symbol.getBase(), symbol.getOffset());
                    symbol.setInReg(false);
                    symbol.setRegIndex(-1);
                    instructions.add(sw);
                }
                this.ifUsed.put(i, false);
            } else if (this.isVreg(i) ||
                    this.isRareg(i) ||
                    this.isAreg(i)) {
                if (this.ifUsed.get(i)) {
                    MipsSymbol symbol = this.table.get(i);
                    if (!symbol.hasRam()) {
                        allocRam(symbol);
                    }
                    Sw sw = new Sw(i, symbol.getBase(), symbol.getOffset());
                    symbol.setInReg(false);
                    symbol.setRegIndex(-1);
                    instructions.add(sw);
                }
                this.ifUsed.put(i, false);
            }
        }
        while (!this.sRegStack.empty()) {
            this.sRegStack.pop();
        }
        this.regOld = 8;
        return instructions;
    }

    // dimension表示数组的维度，是为了防止方法重名
    public ArrayList<MipsInstruction> writeBackPublic(int leftReg,
                                                      MipsSymbol symbol,
                                                      int reg1,
                                                      int dimension,
                                                      MipsBasicBlock basicBlock) {
        ArrayList<MipsInstruction> ret = new ArrayList<>();
        boolean isParam = symbol.isParam();
        if (isParam) {
            String name = symbol.getName();
            Sll sll = new Sll(3, reg1, 2);
            ret.add(sll);
            int reg = this.symbolTable.getRegIndex(name, true, basicBlock);
            Add add = new Add(3, 3, reg);
            ret.add(add);
            Sw sw = new Sw(leftReg, 3, 0);
            ret.add(sw);
        } else {
            int base = symbol.getBase();
            int fpOffset = symbol.getOffset();
            Sll sll = new Sll(3, reg1, 2);
            ret.add(sll);
            Addi addi = new Addi(3, 3, fpOffset);
            ret.add(addi);
            Add add = new Add(3, 3, base);
            ret.add(add);
            Sw sw = new Sw(leftReg, 3, 0);
            ret.add(sw);
        }
        return ret;
    }
}
