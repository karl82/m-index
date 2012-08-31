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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Pivot pivot = (Pivot) o;

        if (index != pivot.index) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return index;
    }
}
