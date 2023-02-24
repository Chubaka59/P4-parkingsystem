package com.parkit.parkingsystem.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

public class Ticket {
    private int id;
    private ParkingSpot parkingSpot;
    private String vehicleRegNumber;
    private BigDecimal price;
    private Date inTime;
    private Date outTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public ParkingSpot getParkingSpot() {
        return new ParkingSpot(parkingSpot.getId(), parkingSpot.getParkingType(), parkingSpot.isAvailable());
    }

    public void setParkingSpot(ParkingSpot parkingSpot) {
        this.parkingSpot = new ParkingSpot(parkingSpot.getId(), parkingSpot.getParkingType(), parkingSpot.isAvailable());
    }

    public String getVehicleRegNumber() {
        return vehicleRegNumber;
    }

    public void setVehicleRegNumber(String vehicleRegNumber) {
        this.vehicleRegNumber = vehicleRegNumber;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price.setScale(2,RoundingMode.HALF_DOWN);
    }

    public Date getInTime() {
        return new Date(inTime.getTime());
    }

    public void setInTime(Date inTime) {
        this.inTime = new Date(inTime.getTime());
    }

    public Date getOutTime() {
        return (this.outTime == null)
                ? null
                : new Date(this.outTime.getTime());
    }

    public void setOutTime(Date outTime) {
        this.outTime = (outTime == null)
                ? null
                : new Date(outTime.getTime());
    }
}
