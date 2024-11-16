package middleend.LlvmIr.Value.Fuction;

public class FunctionCnt {
    // debug:不用 static，因為每個 FunctionCnt 都是獨立的
    private int cnt;

    public FunctionCnt() {
        cnt = 0;
    }

    public int getCnt() {
        int ret = cnt;
        cnt += 1;
        return ret;
    }
}
