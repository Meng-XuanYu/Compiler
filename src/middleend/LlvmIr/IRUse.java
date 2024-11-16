package middleend.LlvmIr;

public class IRUse {
    private IRValue value;
    private IRUser user;
    private int opPos; // 操作数位置

    public IRUse(IRValue value, IRUser user, int opPos) {
        this.value = value;
        this.user = user;
        this.opPos = opPos;
        this.value.addUse(this);
        this.user.addUse(this);
    }

    public IRValue getValue() {
        return value;
    }

    public int getOpPos() {
        return opPos;
    }

    public void setValue(IRValue value) {
        this.value = value;
    }
}
