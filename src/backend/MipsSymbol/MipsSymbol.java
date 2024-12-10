package backend.MipsSymbol;

public class MipsSymbol {
    private String name;
    private int offset; // 偏移量,相对于base
    private int base; // 基址
    private boolean inReg; // 是否在寄存器中
    private boolean hasRam; // 是否在内存中
    private boolean isTemp; // 是否是临时变量
    private int regIndex; // 寄存器编号
    private boolean used; // 是否被使用,针对于临时变量
    private int size; // 0 是普通变量,否则代表数组大小
    private boolean isParam; // 是否是参数

    public MipsSymbol(String name,
                      int base,
                      boolean inReg,
                      boolean hasRam,
                      int offset,
                      boolean isTemp) {
        this.name = name;
        this.base = base;
        this.inReg = inReg;
        this.hasRam = hasRam;
        this.offset = offset;
        this.isTemp = isTemp;
    }

    // alloca
    public MipsSymbol(String name,
                      int base,
                      boolean inReg,
                      int regIndex,
                      boolean hasRam,
                      int offset,
                      boolean isTemp,
                      boolean used) {
        this.name = name;
        this.base = base;
        this.inReg = inReg;
        this.regIndex = regIndex;
        this.hasRam = hasRam;
        this.offset = offset;
        this.isTemp = isTemp;
        this.used = used;
    }

    // global
    public MipsSymbol(String name,
                      int base,
                      int offset) {
        this.name = name;
        this.base = base;
        this.offset = offset;
        this.inReg = false;
        this.regIndex = -1;
        this.hasRam = true;
        this.isTemp = false;
    }

    public boolean isInReg() {
        return inReg;
    }

    public int getRegIndex() {
        return regIndex;
    }

    public boolean isTemp() {
        return isTemp;
    }

    public boolean isUsed() {
        return used;
    }

    public boolean hasRam() {
        return hasRam;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setHasRam(boolean b) {
        this.hasRam = b;
    }

    public int getOffset() {
        return offset;
    }

    public int getBase() {
        return base;
    }
}
