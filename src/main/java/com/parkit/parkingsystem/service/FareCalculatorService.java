package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;

public class FareCalculatorService {

    public static final BigDecimal FIVE_PERCENT_FREE = BigDecimal.valueOf(.05);
    public TicketDAO ticketDAO = new TicketDAO();

    /**
     * Calculate the fare when a vehicle is leaving
     * @param ticket : the ticket of the vehicle
     */
    public void calculateFare(Ticket ticket) {
        if (isInvalidTime(ticket)) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        double durationInHours = getDurationInHours(ticket);

        BigDecimal price = BigDecimal.valueOf(0);

        if (durationInHours > 0.5) {
            switch (ticket.getParkingSpot().getParkingType()) {
                case CAR: {
                    price = (BigDecimal.valueOf(durationInHours * Fare.CAR_RATE_PER_HOUR));
                    break;
                }
                case BIKE: {
                    price = (BigDecimal.valueOf(durationInHours * Fare.BIKE_RATE_PER_HOUR));
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
            }
        }
        ticket.setPrice(price);
    }

    /**
     * calculate the duration in hours
     * @param ticket : the ticket of the vehicle
     * @return double time in hours
     */
    private static double getDurationInHours(Ticket ticket) {
        double inHour = ticket.getInTime().getTime();
        double outHour = ticket.getOutTime().getTime();

        double duration = outHour - inHour;
        return duration / 3_600_000;
    }

    /**
     * Check that the outTime is not null or not before the inTime
     * @param ticket : the ticket of the vehicle
     * @return a boolean
     */
    private static boolean isInvalidTime(Ticket ticket) {
        return (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()));
    }

    /**
     * Calculate the fare with five percent free
     * @param ticket : the ticket of the vehicle
     */
    public void calculateFivePercentFree(Ticket ticket){
        BigDecimal price;
        price = ticket.getPrice();
        price = price.subtract(price.multiply(FIVE_PERCENT_FREE));
        ticket.setPrice(price);
    }
}
