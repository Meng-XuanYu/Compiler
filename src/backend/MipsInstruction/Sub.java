package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Sub extends MipsInstruction {
    private final int target;
    private final int left;
    private final int right;

    public Sub(int target, int left, int right) {
        super("subu");
        this.target = target;
        this.left = left;
        this.right = right;
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ret = new ArrayList<>();
        ret.add(this.toString());
        return ret;
    }

    @Override
    public String toString() {
        return this.getInstName() + " " +
                RegisterName.getName(this.target) + ", " +
                RegisterName.getName(this.left) + ", " +
                RegisterName.getName(this.right) + "\n";
    }
}
