package frontend;

import java.util.ArrayList;
import java.util.List;

public class ErrorList {
    private final List<int[]> errors;

    public ErrorList() {
        this.errors = new ArrayList<>();
    }

    public void addError(int line, Character error) {
        this.errors.add(new int[]{line, error});
    }

    public void sort() {
        this.errors.sort((a, b) -> {
            if (a[0] == b[0]) {
                return a[1] - b[1];
            }
            return a[0] - b[0];
        });
    }

    public boolean isEmpty() {
        return this.errors.isEmpty();
    }

    public int[][] getErrors() {
        return this.errors.toArray(new int[0][]);
    }
}
