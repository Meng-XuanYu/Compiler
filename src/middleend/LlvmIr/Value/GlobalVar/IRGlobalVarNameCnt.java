package middleend.LlvmIr.Value.GlobalVar;

public class IRGlobalVarNameCnt {
    private static int count = 0;

    public static int getCount() {
        int ans = count;
        count += 1;
        return ans;
    }
}
