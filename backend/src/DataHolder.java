public class DataHolder<T> {
    private String x;
    private T y;

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public T getY() {
        return y;
    }

    public void setY(T y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "DataHolder{" +
                "x='" + x + '\'' +
                ", y=" + y +
                '}';
    }
}
