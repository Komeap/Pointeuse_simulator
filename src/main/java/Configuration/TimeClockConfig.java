package Configuration;

import java.io.Serializable;

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
     * Constructor.
     * @param ip : String
     * @param port : int
     * @param refreshSeconds : int
     */
    public TimeClockConfig(String ip, int port, int refreshSeconds) {
        this.ip = ip;
        this.port = port;
        this.refreshSeconds = refreshSeconds;
    }
    //- - - GETTER & SETTER - - -
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