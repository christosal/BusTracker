public class Bus {
    private String lineNumber,routeCode,vechicleId,lineName,busLineId,info;

    public Bus(String lineNumber, String routeCode, String vechicleId, String linaName, String busLineId, String info) {
        this.lineNumber = lineNumber;
        this.routeCode = routeCode;
        this.vechicleId = vechicleId;
        this.lineName = linaName;
        this.busLineId = busLineId;
        this.info = info;
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

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String linaName) {
        this.lineName = linaName;
    }

    public String getBusLineId() {
        return busLineId;
    }

    public void setBusLineId(String busLineId) {
        this.busLineId = busLineId;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
