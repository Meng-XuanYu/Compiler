package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Lw extends MipsInstruction {
    private final int rt; // 存放数据的寄存器编号
    private final int base; // 存放base地址的寄存器编号
    private final int offset;

    public Lw(int rt, int base, int offset) {
        super("lw");
        this.rt = rt;
        this.base = base;
        this.offset = offset;
    }

    @Override
    public ArrayList<String> printMips() {
        String lw = this.toString();
        ArrayList<String> ans = new ArrayList<>();
        ans.add(lw);
        return ans;
    }
    
    @Override
    public String toString() {
        return this.getInstName() + " " +
                RegisterName.getName(rt) + ", " +
                this.offset + "(" + RegisterName.getName(base) + ")\n";
    }
}
