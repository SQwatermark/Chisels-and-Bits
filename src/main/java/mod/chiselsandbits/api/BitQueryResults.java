package mod.chiselsandbits.api;

public class BitQueryResults {

    public final int total;
    public final int empty;
    public final int solid;
    public final int fluid;

    public BitQueryResults(int air, int solid, int fluid) {
        this.empty = air;
        this.solid = solid;
        this.fluid = fluid;
        total = air + solid + fluid;
    }

}
