package backend.MipsBlock;

// putch转字符串用的计数
public class AsciizCnt {
    private static int cnt = 0;

    public static int getCnt() {
        int ret = cnt;
        cnt += 1;
        return ret;
    }

    public static String getStrName(int cnt) {
        String ret = "str_";
        ret = ret + cnt;
        return ret;
    }
}
