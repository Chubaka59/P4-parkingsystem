package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FareCalculatorServiceTest {
    private FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private double expectedPrice;


    @BeforeEach
    public void setUpPerTest() {
        fareCalculatorService = new FareCalculatorService();
        ticket = new Ticket();
    }

    private static Stream<Arguments> vehicleTypes(){
        return Stream.of(
                Arguments.of(Fare.CAR_RATE_PER_HOUR, ParkingType.CAR, 1),
                Arguments.of(Fare.BIKE_RATE_PER_HOUR, ParkingType.BIKE, 4)
        );
    }

    @ParameterizedTest
    @MethodSource("vehicleTypes")
    public void calculateFare(double fareRate, ParkingType parkingType, int spot){
        //GIVEN a ticket parameters is set with the inTime as 1 hour before
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(spot, parkingType, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //WHEN the price needs to be calculated
        fareCalculatorService.calculateFare(ticket);

        //THEN the price is calculated
        assertEquals(toBigDecimal(fareRate), ticket.getPrice());
    }

    @Test
    public void calculateFareUnknownType(){
        //GIVEN a ticket is set without a parkingType
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  60 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null,false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @ParameterizedTest
    @MethodSource("vehicleTypes")
    public void calculateFareWithFutureInTime(double fareRate, ParkingType parkingType, int spot){
        //GIVEN a ticket is set with the inTime as a future time
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(spot, parkingType, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //WHEN the fare is calculated THEN an IllegalArgumentException is raised
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @ParameterizedTest
    @MethodSource("vehicleTypes")
    public void calculateFareWithLessThanOneHourParkingTime(double fareRate, ParkingType parkingType, int spot){
        //GIVEN a ticket is set with the inTime as 45 minutes before
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(spot, parkingType, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        expectedPrice = (0.75 * fareRate);

        //WHEN the fare is calculated
        fareCalculatorService.calculateFare(ticket);

        //THEN the price is generated
        assertEquals(toBigDecimal(expectedPrice), ticket.getPrice());
    }

    @ParameterizedTest
    @MethodSource("vehicleTypes")
    public void calculateFareWithMoreThanADayParkingTime(double fareRate, ParkingType parkingType, int spot){
        //GIVEN a ticket is created with an inTime 48 hours before
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (48 * 60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(spot, parkingType, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        expectedPrice = 48 * fareRate;

        //WHEN the price is calculated
        fareCalculatorService.calculateFare(ticket);

        //THEN the price is generated
        assertEquals(toBigDecimal(expectedPrice), ticket.getPrice());
    }

    @ParameterizedTest
    @MethodSource("vehicleTypes")
    public void calculateFareWithLessThanThirtyMinutes(double fareRate, ParkingType parkingType, int spot){
        //GIVEN a ticket is created with the inTime less than 30 minutes before
        Date inTime = new Date();
        inTime.setTime( System.currentTimeMillis() - (  25 * 60 * 1000) );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(spot, parkingType, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);

        //WHEN the fare is calculated
        fareCalculatorService.calculateFare(ticket);

        //THEN the price is set to 0
        assertEquals(BigDecimal.ZERO.setScale(2), ticket.getPrice());
    }

    @Test
    public void calculateFivePercentFreeTest(){
        //GIVEN a price is set on the ticket
        BigDecimal price = BigDecimal.valueOf(3);
        ticket.setPrice(price);

        BigDecimal priceWithFivePercentFree = price.multiply(FareCalculatorService.FIVE_PERCENT_FREE);
        BigDecimal expectedPrice = price.subtract(priceWithFivePercentFree).setScale(2, RoundingMode.HALF_DOWN);

        //WHEN the five percent free can be applied
        fareCalculatorService.calculateFivePercentFree(ticket);

        //THEN the price is calculated with the percent free
        assertEquals(expectedPrice, ticket.getPrice());
    }

    /**
     * Convert double to BigDecimal with a scale of 2
     * @param price
     * @return price in BigDecimal
     */
    private BigDecimal toBigDecimal(double price){
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_DOWN);
    }
}
