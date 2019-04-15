import java.io.Serializable;

public class Topic implements Serializable {
    private String busLine;

    public String getBusLine() {
        return busLine;
    }

    public Topic(String busLine) {
        this.busLine = busLine;
    }
}
