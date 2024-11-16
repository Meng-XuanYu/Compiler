package middleend.LlvmIr.Types;

import java.util.ArrayList;

public class IRIntegerType extends IRValueType {
    // The bit width of the integer type
    private int bitWidth;
    // 只有两种类型，32位和1位
    public static final IRIntegerType I1 = new IRIntegerType(1);
    public static final IRIntegerType I32 = new IRIntegerType(32);

    private IRIntegerType(int bitWidth) {
        this.bitWidth = bitWidth;
    }

    public static IRIntegerType get1() {
        return I1;
    }

    public static IRIntegerType get32() {
        return I32;
    }

    @Override
    public ArrayList<String> printIR() {
        ArrayList<String> ans = new ArrayList<>();
        if (this.equals(I32)) {
            ans.add("i32");
        } else if (this.equals(I1)) {
            ans.add("i1");
        } else {
            System.out.println("ERROR in IRIntegerType : should not reach here");
        }
        return ans;
    }
}
