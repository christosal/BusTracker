import java.io.Serializable;

public class Bus implements Serializable {
    private String lineNumber,routeCode,vechicleId,busLineId;

    public Bus(String lineNumber, String routeCode, String vechicleId, String busLineId) {
        this.lineNumber = lineNumber;
        this.routeCode = routeCode;
        this.vechicleId = vechicleId;
        this.busLineId = busLineId;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(String lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getRouteCode() {
        return routeCode;
    }

    public void setRouteCode(String routeCode) {
        this.routeCode = routeCode;
    }

    public String getVechicleId() {
        return vechicleId;
    }

    public void setVechicleId(String vechicleId) {
        this.vechicleId = vechicleId;
    }

    public String getBusLineId() {
        return busLineId;
    }

    public void setBusLineId(String busLineId) {
        this.busLineId = busLineId;
    }

}
