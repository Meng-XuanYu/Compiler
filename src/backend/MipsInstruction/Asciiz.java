package backend.MipsInstruction;

import java.util.ArrayList;

public class Asciiz extends MipsInstruction {
    private final String name; // 字符串常量名
    private String content;
    private int cnt; // 标记这是第几个字符串常量

    public Asciiz(String name, String content) {
        super(".asciiz");
        this.name = name;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    @Override
    public ArrayList<String> printMips() {
        ArrayList<String> ret = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        this.content = this.content.replaceAll("\n", "\\\\n");
        sb.append(this.name).append(": .asciiz \"").append(content).append("\"\n");
        ret.add(sb.toString());
        return ret;
    }

    public int getCnt() {
        return cnt;
    }

    public String getContent() {
        return content;
    }

    public void setCnt(int cnt) {
        this.cnt = cnt;
    }
}
