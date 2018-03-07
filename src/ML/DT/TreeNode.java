package ML.DT;

/**
 * Created by zxl on 2018/3/6.
 */
public class TreeNode {
    private String c;
    private boolean isleaf;

    public TreeNode(String c, boolean isleaf) {
        this.c = c;
        this.isleaf = isleaf;
    }

    public String getC() {
        return c;
    }

    public boolean isIsleaf() {
        return isleaf;
    }

    public void setC(String c) {
        this.c = c;
    }

    public void setIsleaf(boolean isleaf) {
        this.isleaf = isleaf;
    }
}
