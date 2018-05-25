package classes;

import java.io.Serializable;

/**
 * Created by sasiroot on 3/2/18.
 */

public class Device implements Serializable{

    private String type; // car, arduino, phoneimg...
    private String ip; // device ip
    private String name; // device name

    public Device(String type, String ip, String name) {
        this.type = type;
        this.ip = ip;
        this.name = name;
    }
    public Device(){

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        String toString = getName() + "     //      " + getIp();
        return toString;
    }
}
