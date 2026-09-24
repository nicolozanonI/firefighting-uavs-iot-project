package it.iot.project.drone.model;

public class GeolocalizationDescriptor {

    private double latitude;
    private double longitude;
    private double elevation;

    public GeolocalizationDescriptor() {
    }

    public GeolocalizationDescriptor(double latitude, double longitude, double elevation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.elevation = elevation;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("GeolocalizationDescriptor{");
        sb.append("latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append(", elevation=").append(elevation);
        sb.append('}');
        return sb.toString();
    }
}
