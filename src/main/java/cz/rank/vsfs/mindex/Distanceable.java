package cz.rank.vsfs.mindex;

public interface Distanceable<D> {

    /**
     * Meassure distance between objects
     * @param object distance between them
     * @return positive number or 0
     */
    double distance(D object);

}
