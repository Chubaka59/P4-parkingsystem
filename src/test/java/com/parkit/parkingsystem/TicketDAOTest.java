package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.commons.lang.time.DateUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TicketDAOTest {
    private final static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;
    private final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

    @Mock
    private Ticket ticket;


    @BeforeAll
    public static void setUp() {
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }
    @BeforeEach
    public void setUpPerTest() {
        dataBasePrepareService.clearDataBaseEntries();
    }

    @Test
    public void saveTicketTest() {

        //GIVEN a ticket is init
        Date inTime = new Date();
        when(ticket.getParkingSpot()).thenReturn(parkingSpot);
        when(ticket.getVehicleRegNumber()).thenReturn("ABCDEF");
        when(ticket.getPrice()).thenReturn(BigDecimal.valueOf(0));
        when(ticket.getInTime()).thenReturn(inTime);

        // WHEN the ticket is saved

        Boolean response = ticketDAO.saveTicket(ticket);

        // THEN the method return true

        assertTrue(response);

    }
    @Test
    public void getTicketTest(){
        //GIVEN a ticket is saved
        Date inTime = new Date();
        Date roundedInTime = DateUtils.round(inTime, Calendar.SECOND);
        when(ticket.getParkingSpot()).thenReturn(parkingSpot);
        when(ticket.getVehicleRegNumber()).thenReturn("ABCDEF");
        when(ticket.getPrice()).thenReturn(BigDecimal.valueOf(0));
        when(ticket.getInTime()).thenReturn(inTime);
        ticketDAO.saveTicket(ticket);

        //WHEN we get a ticket
        Ticket returnedTicket = ticketDAO.getTicket("ABCDEF");

        //THEN we get the ticket informations
        assertEquals(1,returnedTicket.getParkingSpot().getId());
        assertEquals("ABCDEF",returnedTicket.getVehicleRegNumber());
        assertEquals(BigDecimal.valueOf(0).setScale(2),returnedTicket.getPrice());
        assertEquals(roundedInTime,returnedTicket.getInTime());
    }

    @Test
    public void updateTicketTest(){
        //GIVEN a ticket is saved
        Date inTime = new Date();
        Date outTime = new Date();
        when(ticket.getParkingSpot()).thenReturn(parkingSpot);
        when(ticket.getVehicleRegNumber()).thenReturn("ABCDEF");
        when(ticket.getPrice()).thenReturn(BigDecimal.valueOf(3));
        when(ticket.getInTime()).thenReturn(inTime);
        ticketDAO.saveTicket(ticket);

        when(ticket.getId()).thenReturn(1);
        when(ticket.getOutTime()).thenReturn(outTime);

        //WHEN a ticket is updated
        boolean response = ticketDAO.updateTicket(ticket);


        //THEN the ticket has been updated
        assertTrue(response);
    }

    @Test
    public void getTicketCountTest(){
        Date inTime = new Date();
        Date outTime = new Date();
        when(ticket.getParkingSpot()).thenReturn(parkingSpot);
        when(ticket.getVehicleRegNumber()).thenReturn("ABCDEF");
        when(ticket.getPrice()).thenReturn(BigDecimal.valueOf(3));
        when(ticket.getInTime()).thenReturn(inTime);
        when(ticket.getId()).thenReturn(1);
        when(ticket.getOutTime()).thenReturn(outTime);
        ticketDAO.saveTicket(ticket);
        ticketDAO.updateTicket(ticket);

        int count = ticketDAO.getTicketCount("ABCDEF");

        assertEquals(1, count);
    }
}
