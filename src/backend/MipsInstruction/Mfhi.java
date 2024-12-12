package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Mfhi extends MipsInstruction {
    private final int target;

    public Mfhi(int target) {
        super("mfhi");
        this.target = target;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.getInstName() + " " +
                RegisterName.getName(this.target) + "\n";
        ArrayList<String> ret = new ArrayList<>();
        ret.add(sb);
        return ret;
    }
}
