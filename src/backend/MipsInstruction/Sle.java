package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Sle extends MipsInstruction {
    private final int target;
    private final int left;
    private final int right;

    public Sle(int target, int left, int right) {
        super("sle");
        this.target = target;
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.getInstName() + " " +
                RegisterName.getName(this.target) + ", " +
                RegisterName.getName(this.left) + ", " +
                RegisterName.getName(this.right) + "\n";
        ArrayList<String> ret = new ArrayList<>();
        ret.add(sb);
        return ret;
    }

}
