package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;



    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp() throws Exception {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
    }


    @Test
    public void testParkingACar() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actually saved in DB and Parking table is updated with availability -- check if i can reuse the databse connector
        Source source = new Source("jdbc:mysql://localhost:3306/test?serverTimezone=UTC", "root", "rootroot");
        Table ticketTable = new Table(source, "ticket");
        Table parkingTable = new Table(source, "parking");


        assertThat(ticketTable).row(0)
                .value(1).isEqualTo(1)
                .value(2).isEqualTo("ABCDEF");

        assertThat(parkingTable).column("AVAILABLE")
                .value().isEqualTo(false);
    }

    @Test
    public void testParkingLotExit() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket incomingTicket = new Ticket();
        Date incomingDate = new Date();
        incomingDate.setTime(System.currentTimeMillis() - 120 * 60 * 1000);
        incomingTicket.setParkingSpot(parkingService.getNextParkingNumberIfAvailable());
        incomingTicket.setInTime(incomingDate);
        incomingTicket.setVehicleRegNumber("ABCDEF");
        incomingTicket.setPrice(new BigDecimal(0));
        ticketDAO.saveTicket(incomingTicket);

        //WHEN the vehicle leave the parking
        parkingService.processExitingVehicle();

        //THEN an outTime and a fare is generated
        Ticket outgoingTicket = ticketDAO.getLastTicket("ABCDEF");
        Date date = new Date();
        Date nearestSecond = DateUtils.round (date, Calendar.SECOND);
        Timestamp expectedDate = new Timestamp(nearestSecond.getTime());
        assertThat(outgoingTicket.getOutTime().getTime()).isEqualTo(expectedDate.getTime());
        assertThat(outgoingTicket.getPrice()).isEqualTo(new BigDecimal(2 * Fare.CAR_RATE_PER_HOUR).setScale(2, RoundingMode.HALF_DOWN));
    }

    @Test
    public void testParkingCarExitTwice() {
        //GIVEN a Bike enter twice the parking
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        Ticket firstIncomingTicket = new Ticket();
        Date firstIncomingDate = new Date ();
        firstIncomingDate.setTime(System.currentTimeMillis() - 120 * 60 * 1000);
        firstIncomingTicket.setInTime(firstIncomingDate);
        firstIncomingTicket.setVehicleRegNumber("ABCDEF");
        firstIncomingTicket.setPrice(new BigDecimal(0));
        firstIncomingTicket.setParkingSpot(parkingService.getNextParkingNumberIfAvailable());
        ticketDAO.saveTicket(firstIncomingTicket);
        parkingService.processExitingVehicle();

        Ticket secondIncomingTicket= new Ticket();
        Date secondIncomingDate = new Date();
        secondIncomingDate.setTime(System.currentTimeMillis() - 60 * 60 * 1000);
        secondIncomingTicket.setInTime(secondIncomingDate);
        secondIncomingTicket.setVehicleRegNumber("ABCDEF");
        secondIncomingTicket.setPrice(new BigDecimal(0));
        secondIncomingTicket.setParkingSpot(parkingService.getNextParkingNumberIfAvailable());
        ticketDAO.saveTicket(secondIncomingTicket);

        //WHEN the car is leaving for the second time
        parkingService.processExitingVehicle();

        //THEN the price get 5 percent free
        System.out.println(new BigDecimal(1).multiply(new BigDecimal(1.5)).subtract((new BigDecimal(1).multiply(new BigDecimal(1.5).multiply(new BigDecimal(0.05))))).setScale(2,RoundingMode.HALF_DOWN));
        Ticket secondOutgoingTicket = ticketDAO.getLastTicket("ABCDEF");
        //Calculation = 1 hour * Fare.CAR_RATE_PER_HOUR - 1 hour * Fare.CAR_RATE_PER_HOUR * 5 percent
        BigDecimal expectedPrice = new BigDecimal(1).multiply(new BigDecimal(Fare.CAR_RATE_PER_HOUR)).subtract(new BigDecimal(1).multiply(new BigDecimal(Fare.CAR_RATE_PER_HOUR).multiply(new BigDecimal(0.05)))).setScale(2,RoundingMode.HALF_DOWN);
        BigDecimal actualPrice = secondOutgoingTicket.getPrice().setScale(2,RoundingMode.HALF_DOWN);
        assertThat(actualPrice).isEqualTo(expectedPrice);

    }
}
