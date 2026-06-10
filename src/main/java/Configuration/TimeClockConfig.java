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

    public UUID getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = this.name; }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getRefreshSeconds() {
        return refreshSeconds;
    }

    public void setRefreshSeconds(int refreshSeconds) {
        this.refreshSeconds = refreshSeconds;
    }
}