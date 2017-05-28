package cz.rank.vsfs.mindex;

class TestParams {
    final int pivotsCount;

    final int queryObjects;

    final int clusterMaxLevel;

    final int invocations;

    final double range;

    final int btreeLevel;

    public TestParams(int pivotsCount,
                      int queryObjects,
                      int clusterMaxLevel,
                      int invocations,
                      double range,
                      int btreeLevel) {
        this.pivotsCount = pivotsCount;
        this.queryObjects = queryObjects;
        this.clusterMaxLevel = clusterMaxLevel;
        this.invocations = invocations;
        this.range = range;
        this.btreeLevel = btreeLevel;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("{pivotsCount=")
          .append(pivotsCount);
        sb.append(", queryObjects=")
          .append(queryObjects);
        sb.append(", clusterMaxLevel=")
          .append(clusterMaxLevel);
        sb.append(", range=")
          .append(range);
        sb.append(", btreeLevel=")
          .append(btreeLevel);
        sb.append('}');
        return sb.toString();
    }
}
