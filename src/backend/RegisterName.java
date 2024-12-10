package backend;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// RegisterName类用于获取寄存器的名字,方便取用
public class RegisterName {
    private static List<String> names =  Collections.unmodifiableList(Arrays.asList(
            "zero", "at", "v0", "v1", "a0", "a1", "a2", "a3",
            "t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7",
            "s0", "s1", "s2", "s3", "s4", "s5", "s6", "s7",
            "t8", "t9", "k0", "k1", "gp", "sp", "fp", "ra"));

    public static String getName(int cnt) {
        return "$" + names.get(cnt);
    }

}
