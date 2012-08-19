package cz.rank.vsfs.mindex;

import net.jcip.annotations.Immutable;

@Immutable
public class Pivot<D extends Distanceable<D>> implements Distanceable<D> {
    private final int index;
    private final D object;

    public Pivot(int index, D object) {
        this.index = index;
        this.object = object;
    }

    public int getIndex() {
        return index;
    }

    public D getObject() {
        return object;
    }

    @Override
    public double distance(D object) {
        return this.object.distance(object);
    }

}
