package backend.MipsInstruction;
import backend.RegisterName;
import java.util.ArrayList;

public class Li extends MipsInstruction {
    private final int regNum;
    private final int immediate;

    public Li(int regNum, int immediate) {
        super("li");
        this.regNum = regNum;
        this.immediate = immediate;
    }

    @Override
    public ArrayList<String> printMips() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getInstName()).append(" ");
        sb.append(RegisterName.getName(this.regNum)).append(", ");
        if (immediate >= 0) {
            sb.append("0x").append(Integer.toHexString(this.immediate)).append("\n");
        } else {
            sb.append(this.immediate).append("\n");
        }
        ArrayList<String> ret = new ArrayList<>();
        ret.add(sb.toString());
        return ret;
    }
}
