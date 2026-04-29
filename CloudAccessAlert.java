/*class CloudAccessAlert {
    String alertId;
    String alertType;
    String message;

    public CloudAccessAlert(String alertId, String log) {
        this.alertId = alertId;
        this.alertType = log.substring(0, log.indexOf(":")).trim();
        this.message = log.substring(log.indexOf(":") + 2);
    }

    public String toString() {
        return "ALERT ID: " + alertId +
               " | TYPE: " + alertType +
               " | MESSAGE: " + message;
    }
}*/

class CloudAccessAlert {
    String alertId;
    String alertType;
    String message;
    String status; // NEW

    public CloudAccessAlert(String alertId, String log) {
        this.alertId = alertId;
        this.alertType = log.substring(0, log.indexOf(":")).trim();
        this.message = log.substring(log.indexOf(":") + 2);
        this.status = "ACTIVE"; // default
    }

    public String toString() {
        return "ALERT ID: " + alertId +
               " | TYPE: " + alertType +
               " | MESSAGE: " + message +
               " | STATUS: " + status;
    }
}