package middleend.LlvmIr.Types;

import java.util.ArrayList;

// alloca,gep
public class IRPointerType extends IRValueType {
    private final IRValueType contained; // 指针的类型

    public IRPointerType(IRValueType contained) {
        this.contained = contained;
    }

    public IRValueType getContained() {
        return contained;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        sb.append(contained.printIR().get(0)).append("*");
        ans.add(sb.toString());
        return ans;
    }
}
