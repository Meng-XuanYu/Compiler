package middleend.LlvmIr.Value.Constant;
import middleend.LlvmIr.Types.IRValueType;
import java.util.ArrayList;

public class IRConstantChar extends IRConstant {
    private char value;

    public IRConstantChar(IRValueType type, char value) {
        super(type);
        this.value = value;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add("'" + this.value + "'");
        return ans;
    }
}
