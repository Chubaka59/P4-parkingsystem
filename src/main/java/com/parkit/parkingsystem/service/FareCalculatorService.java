package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;
import java.time.Duration;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        double durationInHours = getDurationInHours(ticket);

        switch (ticket.getParkingSpot().getParkingType()){
            case CAR: {
                if (durationInHours < 0.5) {
                    ticket.setPrice(BigDecimal.valueOf(0));
                    break;
                } else {
                    ticket.setPrice(BigDecimal.valueOf(durationInHours * Fare.CAR_RATE_PER_HOUR));
                    break;
                }
            }
            case BIKE: {
                if (durationInHours < 0.5) {
                    ticket.setPrice(BigDecimal.valueOf(0));
                    break;
                } else {
                    ticket.setPrice(BigDecimal.valueOf(durationInHours * Fare.BIKE_RATE_PER_HOUR));
                    break;
                }
            }
            default: throw new IllegalArgumentException("Unkown Parking Type");
        }
    }
}