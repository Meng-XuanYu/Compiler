package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class La extends MipsInstruction {
    private final int reg;
    private final String label;

    public La(int reg, String label) {
        super("la");
        this.reg = reg;
        this.label = label;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.toString();
        ArrayList<String> ret = new ArrayList<>();
        ret.add(sb);
        return ret;
    }

    @Override
    public String toString() {
        return this.getInstName() + " " +
                RegisterName.getName(reg) + ", " +
                label + "\n";
    }
}
