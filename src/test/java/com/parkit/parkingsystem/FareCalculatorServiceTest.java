package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FareCalculatorServiceTest {
    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private double expectedPrice;


    @BeforeEach
    public void setUpPerTest() {
        fareCalculatorService = new FareCalculatorService();
        ticket = new Ticket();
    }

    @Test
    public void calculateFare(){

    }

    @Test
    public void calculateFareCar(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        expectedPrice = Fare.CAR_RATE_PER_HOUR;
        assertEquals(new BigDecimal(expectedPrice).setScale(2) , ticket.getPrice().setScale(2, RoundingMode.HALF_DOWN));
    }

    @Test
    public void calculateFareBike(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        expectedPrice = Fare.BIKE_RATE_PER_HOUR;
        assertEquals(new BigDecimal(expectedPrice).setScale(2) , ticket.getPrice().setScale(2, RoundingMode.HALF_DOWN));
    }

    @Test
    public void calculateFareUnknownType(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() + (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        expectedPrice = (0.75 * Fare.BIKE_RATE_PER_HOUR);
        assertEquals(new BigDecimal(expectedPrice).setScale(2, RoundingMode.HALF_DOWN), ticket.getPrice().setScale(2) );
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  45 * 60 * 1000) );//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        expectedPrice = (0.75 * Fare.CAR_RATE_PER_HOUR);
        assertEquals(new BigDecimal(expectedPrice).setScale(2, RoundingMode.HALF_DOWN) , ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  24 * 60 * 60 * 1000) );//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        expectedPrice = (24 * Fare.CAR_RATE_PER_HOUR);
        assertEquals(new BigDecimal(expectedPrice).setScale(2, RoundingMode.HALF_DOWN), ticket.getPrice());
    }

    @Test
    public void calculateFareWithLessThanThirtyMinutes(){
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  25 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(new BigDecimal(0).setScale(2,RoundingMode.HALF_DOWN), ticket.getPrice());
    }

    @Test
    public void calculateFivePercentFreeTest(){
        //GIVEN
        BigDecimal price = BigDecimal.valueOf(3);
        ticket.setPrice(price);

        //WHEN
        fareCalculatorService.calculateFivePercentFree(ticket);

        //THEN
        BigDecimal priceWithFivePercentFree = price.multiply(fareCalculatorService.FIVE_PERCENT_FREE);
        BigDecimal expectedPrice = price.subtract(priceWithFivePercentFree).setScale(2, RoundingMode.HALF_DOWN);
        assertEquals(expectedPrice, ticket.getPrice());
    }

    private BigDecimal toBigDecimal(double price){
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_DOWN);
    }
}
