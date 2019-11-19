import java.util.Comparator;
public class virtualTreeComperator implements Comparator<virtualTree> {
    public int compare(virtualTree a, virtualTree b) {
        if (a.informationGain-b.informationGain > 0)
            return -1;
        else if (a.informationGain-b.informationGain < 0)
            return 1;
        else
            return 0;
    }
}
