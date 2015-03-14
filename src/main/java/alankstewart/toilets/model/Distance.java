package alankstewart.toilets.model;

public class Distance implements Comparable<Distance> {

    private final double metres;

    private Distance(final double metres) {
        this.metres = metres;
    }

    public double inKilometres() {
        return metres / 1000;
    }

    public double inMetres() {
        return metres;
    }

    public static Distance kilometres(double km) {
        return metres(km * 1000);
    }

    public static Distance metres(double m) {
        return new Distance(m);
    }

    @Override
    public int compareTo(Distance o) {
        return Double.compare(metres, o.inMetres());
    }
}
