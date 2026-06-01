package com.cts.mrfp.carrygo.dto;

// Body the frontend sends when a regular user signs up as a porter
// (POST /api/users/{userId}/register-commuter).
// licenceExpiry is sent as a plain "YYYY-MM-DD" string so we don't run into
// LocalDate JSON-parsing quirks.
public class CommuterRegistrationRequest {
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleModel;
    private String licenceNumber;
    private String licenceExpiry;

    public String getVehicleType()   { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleModel()  { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getLicenceNumber() { return licenceNumber; }
    public void setLicenceNumber(String licenceNumber) { this.licenceNumber = licenceNumber; }

    public String getLicenceExpiry() { return licenceExpiry; }
    public void setLicenceExpiry(String licenceExpiry) { this.licenceExpiry = licenceExpiry; }
}
