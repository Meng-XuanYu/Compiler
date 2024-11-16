package middleend.LlvmIr.Value.Constant;
import middleend.LlvmIr.Types.IRValueType;
import java.util.ArrayList;

public class IRConstantIntArray extends IRConstant {
    private ArrayList<IRConstantInt> values;
    private int size;

    // 用于常量数组
    public IRConstantIntArray(ArrayList<IRConstantInt> values, int size, IRValueType type) {
        super(type, values.size());
        this.values = values;
        this.size = size;
        for (int i = 0; i < values.size(); i++) {
            this.setOperand(values.get(i),i);
        }
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        ans.add("[");
        for (int i = 0; i < values.size(); i++) {
            ans.add(values.get(i).printIR().get(0));
            if (i != values.size() - 1) {
                ans.add(", ");
            }
        }
        ans.add("]");
        return ans;
    }

    public int getSize() {
        return size;
    }

    public ArrayList<IRConstantInt> getConstantInts() {
        return values;
    }
}
