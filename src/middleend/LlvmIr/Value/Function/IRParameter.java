package middleend.LlvmIr.Value.Function;
import middleend.LlvmIr.IRValue;
import middleend.LlvmIr.Types.*;
import middleend.LlvmIr.Value.IRNode;
import java.util.ArrayList;

public class IRParameter extends IRValue implements IRNode {
    private final int position;

    public IRParameter(IRValueType valueType, int position) {
        super(valueType);
        this.position = position;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (this.getType() instanceof IRIntegerType) {
            sb.append(this.getType().printIR().get(0)).append(" ").append(this.getName());
        } else if (this.getType() instanceof IRIntArrayType) {
            sb.append(this.getType().printIR().get(0)).append(" ").append(this.getName());
            sb.append("[] ");
        }
        ans.add(sb.toString());
        return ans;
    }

    public int getPosition() {
        return position;
    }
}
