package backend.MipsSymbol;

import backend.MipsBlock.MipsBasicBlock;
import backend.MipsInstruction.Sw;

import java.util.HashMap;
import java.util.Stack;

public class RegisterTable {
    private MipsSymbolTable symbolTable; //双向存储
    private HashMap<Integer, Boolean> ifUsed;
    private HashMap<Integer, MipsSymbol> table;
    private final int regNum = 32;
    private final int regOld = 8;
    // 记录s0-s7的使用情况
    private Stack<Integer> sRegStack;

    public int getReg(boolean temp, MipsSymbol symbol, MipsBasicBlock basicBlock) {
        int freeReg = getFreeReg(temp);
        if (freeReg == -1) {
            // 无空闲寄存器，需要存入内存
            if (temp) {
                int ans =
            }
        }
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
                this.tempPtr = (this.tempPtr + 1 + 32) % 32;
                return symbol.getRegIndex();
            } else {
                this.tempPtr = (this.tempPtr + 1 + 32) % 32;
            }
        }
    }

    private void writeBack(MipsSymbol symbol, MipsBasicBlock basicBlock) {
        int regIndex = symbol.getRegIndex();
        int offset = symbol.getOffset();
        int base = symbol.getBase();
        Sw sw = new Sw(regIndex, base, offset);
        basicBlock.addInstruction(sw);
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

}
