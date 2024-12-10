package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Sw extends MipsInstruction {
    private final int rt;
    private final int base;
    private final int offset;

    public Sw(int rt, int base, int offset) {
        super("sw");
        this.rt = rt;
        this.base = base;
        this.offset = offset;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.getInstName() + " " +
                RegisterName.getName(rt) + ", " +
                offset +
                "(" + RegisterName.getName(base) + ")\n";
        ArrayList<String> ans = new ArrayList<>();
        ans.add(sb);
        return ans;
    }
    
    @Override
    public String toString() {
        return this.getInstName() + " " +
                RegisterName.getName(rt) + ", " +
                offset +
                "(" + RegisterName.getName(base) + ")\n";
    }
}
