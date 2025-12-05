package drivesync.AutoModell;

import java.time.LocalDate;

public class Car {
    private int id;
    private int ownerId;
    private String license;
    private String brand;
    private String type;
    private int vintage;
    private String engineType;
    private String fuelType;
    private int km;
    private int oil;
    private int tireSize;
    private LocalDate service;
    private LocalDate insurance;

    public Car(int id, int ownerId, String license, String brand, String type, int vintage,
               String engineType, String fuelType, int km, int oil, int tireSize,
               LocalDate service, LocalDate insurance) {
        this.id = id;
        this.ownerId = ownerId;
        this.license = license;
        this.brand = brand;
        this.type = type;
        this.vintage = vintage;
        this.engineType = engineType;
        this.fuelType = fuelType;
        this.km = km;
        this.oil = oil;
        this.tireSize = tireSize;
        this.service = service;
        this.insurance = insurance;
    }

    // Getterek és setterek
    public int getId() { return id; }
    public int getOwnerId() { return ownerId; }
    public String getLicense() { return license; }
    public String getBrand() { return brand; }
    public String getType() { return type; }
    public int getVintage() { return vintage; }
    public String getEngineType() { return engineType; }
    public String getFuelType() { return fuelType; }
    public int getKm() { return km; }
    public int getOil() { return oil; }
    public int getTireSize() { return tireSize; }
    public java.time.LocalDate getService() { return service; }
    public java.time.LocalDate getInsurance() { return insurance; }
}
