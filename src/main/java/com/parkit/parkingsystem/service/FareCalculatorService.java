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
        if (durationInHours < 0.5) {
            ticket.setPrice(BigDecimal.valueOf(0));
        } else {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    ticket.setPrice(BigDecimal.valueOf(durationInHours * Fare.CAR_RATE_PER_HOUR));
                    break;
                }
                case BIKE: {
                    ticket.setPrice(BigDecimal.valueOf(durationInHours * Fare.BIKE_RATE_PER_HOUR));
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unkown Parking Type");
            }
        }
    }

    /**
     * calculate the duration in hours
     * @param ticket : the ticket vehicle
     * @return double time in hours
     */

    private static double getDurationInHours(Ticket ticket) {
        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();


        double duration = outHour - inHour;
        return duration / 3_600_000;
    }

    private static boolean isInvalidTime(Ticket ticket) {
        return (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()));
    }
}