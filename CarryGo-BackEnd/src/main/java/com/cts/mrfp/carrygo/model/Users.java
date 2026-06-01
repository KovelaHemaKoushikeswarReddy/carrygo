package com.cts.mrfp.carrygo.model;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// Database table for every person on the platform — regular users and porters alike.
// One column "role" can hold multiple roles (e.g. "user,porter") when a user signs up as a porter.
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="user_id")
    private Integer userId;

    @NotBlank
    private String name;

    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String password;

    private String role;
    private String authProvider;
    private String themePreference;

    // Porter-only fields — only set after the user registers as a porter.
    private String licenceNumber;
    private LocalDate licenceExpiry;
    private String vehicleType;
    private String vehicleNumber;
    private String vehicleModel;
    private Boolean isOnline = false;
    private Double avgRating;

    // KYC — personal info the porter must submit before they can take rides.
    private String gender;
    private String dateOfBirth;
    private String idType;
    private String idNumber;

    // KYC — residential address.
    private String houseNo;
    private String street;
    private String city;
    private String state;
    private String pinCode;

    // KYC — bank account where payouts will go.
    private String bankAccountHolder;
    private String bankAccountNumber;
    private String bankIfscCode;
    private String bankName;

    // Overall KYC verification status (PENDING / APPROVED / REJECTED).
    private String kycStatus;

    // ID card images stored as Base64 strings. MEDIUMTEXT keeps each one in MySQL.
    @Column(columnDefinition = "MEDIUMTEXT")
    private String idFrontImage;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String idBackImage;

    public Users() {}

    public Users(Integer userId, String name, String email, String phone, String password, String role, String authProvider,
                 String themePreference, String licenceNumber, LocalDate licenceExpiry, String vehicleType, String vehicleNumber,
                 String vehicleModel) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.authProvider = authProvider;
        this.themePreference = themePreference;
        this.licenceNumber = licenceNumber;
        this.licenceExpiry = licenceExpiry;
        this.vehicleType = vehicleType;
        this.vehicleNumber = vehicleNumber;
        this.vehicleModel = vehicleModel;
    }

    // Getters and Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAuthProvider() { return authProvider; }
    public void setAuthProvider(String authProvider) { this.authProvider = authProvider; }
    public String getThemePreference() { return themePreference; }
    public void setThemePreference(String themePreference) { this.themePreference = themePreference; }
    public String getLicenceNumber() { return licenceNumber; }
    public void setLicenceNumber(String licenceNumber) { this.licenceNumber = licenceNumber; }
    public LocalDate getLicenceExpiry() { return licenceExpiry; }
    public void setLicenceExpiry(LocalDate licenceExpiry) { this.licenceExpiry = licenceExpiry; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public Boolean getIsOnline() { return isOnline; }
    public void setIsOnline(Boolean isOnline) { this.isOnline = isOnline; }
    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }

    public String getIdNumber() { return idNumber; }
    public void setIdNumber(String idNumber) { this.idNumber = idNumber; }

    public String getHouseNo() { return houseNo; }
    public void setHouseNo(String houseNo) { this.houseNo = houseNo; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }

    public String getBankAccountHolder() { return bankAccountHolder; }
    public void setBankAccountHolder(String bankAccountHolder) { this.bankAccountHolder = bankAccountHolder; }

    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }

    public String getBankIfscCode() { return bankIfscCode; }
    public void setBankIfscCode(String bankIfscCode) { this.bankIfscCode = bankIfscCode; }

    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }

    public String getKycStatus() { return kycStatus; }
    public void setKycStatus(String kycStatus) { this.kycStatus = kycStatus; }

    public String getIdFrontImage() { return idFrontImage; }
    public void setIdFrontImage(String idFrontImage) { this.idFrontImage = idFrontImage; }

    public String getIdBackImage() { return idBackImage; }
    public void setIdBackImage(String idBackImage) { this.idBackImage = idBackImage; }
}