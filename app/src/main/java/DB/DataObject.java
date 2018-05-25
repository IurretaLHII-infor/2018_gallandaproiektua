package DB;

import java.sql.Date;

/**
 * Created by sasiroot on 5/4/18.
 */

public class DataObject {
    private int temperature;
    private int speed;
    private int batterycharge;

    public DataObject ( int temperature, int speed, int batterycharge) {
        this.temperature = temperature;
        this.speed = speed;
        this.batterycharge = batterycharge;
    }

    public int getTemperature(){
        return this.temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public int getSpeed () {
        return this.speed;
    }

    public void setSpeed (int speed) {
        this.speed = speed;
    }

    public int getBatterycharge () {
        return this.batterycharge;
    }

    public void setbatterycharge (int batterycharge) {
        this.batterycharge = batterycharge;
    }

}
