package cz.rank.vsfs.mindex;

import java.util.ArrayList;
import java.util.List;

public class Cluster {
    private final int[] indexes;
    private final Point basePivot;
    private final int pivotsCount;
    
    public Cluster(Point pivot, int pivotsCount, int[] indexes) {
        this.basePivot = pivot;
        this.pivotsCount = pivotsCount;
        this.indexes = indexes.clone();
    }


    public int getIndex() {
        int tempIndex = 0;
        for (int i = 0; i < indexes.length; i++) {
            tempIndex += indexes[i] * Math.pow(pivotsCount, indexes.length - 1 - i);
        }
        return tempIndex;
    }


    public Point getBasePivot() {
        return basePivot;
    }
}
