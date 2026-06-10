package Configuration;

import java.io.Serializable;
import java.util.UUID;

/**
 * This class stores the configuration of the time clock.
 * It contains the server IP, server port and refresh interval.
 */
public class TimeClockConfig implements Serializable {

    /**
     * Server IP address.
     */
    private String ip;

    /**
     * Server port.
     */
    private int port;

    /**
     * Refresh interval in seconds.
     */
    private int refreshSeconds;

    /**
     * timeclock uuid
     */
    private UUID id;

    /**
     * timeclock name
     */
    private String name;

    /**
     * Constructor.
     * @param ip : String
     * @param port : int
     * @param refreshSeconds : int
     */
    public TimeClockConfig(UUID id, String name, String ip, int port, int refreshSeconds) {
        this.id = id;
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.refreshSeconds = refreshSeconds;
    }
    //- - - GETTER & SETTER - - -

    /**
     * returns the ID of a timeclock
     * @return id : UUID
     */
    public UUID getId() { return id; }

    /**
     * returns the name of a timeclock
     * @return name : String
     */
    public String getName() { return name; }

    /**
     * sets the name of a timeclock
     * @param name : String
     */
    public void setName(String name) { this.name = this.name; }

    /**
     * returns the name of a timeclock
     * @return ip : String
     */
    public String getIp() {
        return ip;
    }

    /**
     * sets the ip of a timeclock
     * @param ip : String
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * returns the port of a timeclock
     * @return port : int
     */
    public int getPort() {
        return port;
    }

    /**
     * sets the port of a timeclock
     * @param port : int
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * returns the refreshSeconds of a timeclock
     * @return refreshSeconds : int
     */
    public int getRefreshSeconds() {
        return refreshSeconds;
    }

    /**
     * sets the refreshSeconds of a timeclock
     * @param refreshSeconds : int
     */
    public void setRefreshSeconds(int refreshSeconds) {
        this.refreshSeconds = refreshSeconds;
    }
}