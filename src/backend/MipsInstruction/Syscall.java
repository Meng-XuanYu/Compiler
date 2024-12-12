package backend.MipsInstruction;

import java.util.ArrayList;

public class Syscall extends MipsInstruction {
    public Syscall() {
        super("syscall");
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add(this.getInstName() + "\n");
        return ans;
    }
}
