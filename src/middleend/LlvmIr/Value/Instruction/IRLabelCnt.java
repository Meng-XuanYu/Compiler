package middleend.LlvmIr.Value.Instruction;

public class IRLabelCnt {
    private static int cnt = 0;

    public static int getCnt() {
        int ret = cnt;
        cnt += 1;
        return cnt;
    }

    public static String getName(int num) {
        return "%Label_" + num;
    }
}
