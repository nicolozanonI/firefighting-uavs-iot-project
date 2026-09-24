package it.iot.project.drone.model;

public abstract  class DroneDescriptor {

    public String id;

    public String manifacturer;

    public String type;

    public String softwareVersion;

    public double speed;

    public DroneDescriptor() {
    }

    public DroneDescriptor(String id, String manifacturer, String type, String softwareVersion, Double speed) {
        this.id = id;
        this.manifacturer = manifacturer;
        this.type = type;
        this.softwareVersion = softwareVersion;
        this.speed = speed;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManifacturer() {
        return manifacturer;
    }

    public void setManifacturer(String manifacturer) {
        this.manifacturer = manifacturer;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DroneDescriptor{");
        sb.append("id='").append(id).append('\'');
        sb.append(", manifacturer='").append(manifacturer).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", softwareVersion='").append(softwareVersion).append('\'');
        sb.append(", speed=").append(speed);
        sb.append('}');
        return sb.toString();
    }
}
