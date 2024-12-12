package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Bne extends MipsInstruction {
    private final int left;
    private final int right;
    private final String label;

    public Bne(int left, int right, String label) {
        super("bne");
        this.left = left;
        this.right = right;
        this.label = label;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.getInstName() + " " +
                RegisterName.getName(this.left) + ", " +
                RegisterName.getName(this.right) + ", " +
                this.label + "\n";
        ArrayList<String> ret = new ArrayList<>();
        ret.add(sb);
        return ret;
    }
}
