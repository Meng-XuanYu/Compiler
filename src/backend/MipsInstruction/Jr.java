package backend.MipsInstruction;

import backend.RegisterName;

import java.util.ArrayList;

public class Jr extends MipsInstruction {
    private final int regNum; // 跳转的寄存器编号

    public Jr(int regNum) {
        super("jr");
        this.regNum = regNum;
    }

    @Override
    public ArrayList<String> printMips() {
        String sb = this.getInstName() + " " +
                RegisterName.getName(regNum) + "\n";
        ArrayList<String> ans = new ArrayList<>();
        ans.add(sb);
        return ans;
    }
}
