package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.Ticket;

import java.math.BigDecimal;

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
                    price = (BigDecimal.valueOf(durationInHours * Fare.CAR_RATE_PER_HOUR));
                    ticket.setPrice(price);
                    break;
                }
                case BIKE: {
                    price = (BigDecimal.valueOf(durationInHours * Fare.BIKE_RATE_PER_HOUR));
                    ticket.setPrice(price);
                    break;
                }
                default:
                    throw new IllegalArgumentException("Unknown Parking Type");
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

//    public boolean getRecurrentUser(String vehicleRegNumber){
//        try {
//            return this.ticketDAO.getTicket(vehicleRegNumber).getOutTime() != null;
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return false;
//    }

    public void calculateFivePercentFree(Ticket ticket){
        BigDecimal price;
        price = ticket.getPrice();
        price = price.subtract(price.multiply(FIVE_PERCENT_FREE));
        ticket.setPrice(price);
    }
}
