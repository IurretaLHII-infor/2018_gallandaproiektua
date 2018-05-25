package classes;

public class FromDataObject {

    private int id;
    private String timestanp;
    private int speed;
    private int temperature;
    private int batteryCharge;

    public FromDataObject() {

    }

    public FromDataObject(int id, String timestanp, int speed, int temperature, int batteryCharge) {
        this.id = id;
        this.timestanp = timestanp;
        this.speed = speed;
        this.temperature = temperature;
        this.batteryCharge = batteryCharge;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTimestanp() {
        return timestanp;
    }

    public void setTimestanp(String timestanp) {
        this.timestanp = timestanp;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getBatteryCharge() {
        return batteryCharge;
    }

    public void setBatteryCharge(int batteryCharge) {
        this.batteryCharge = batteryCharge;
    }
}
