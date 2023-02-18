package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;

    Ticket ticket;

    @BeforeEach
    public void setUpPerTest() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        this.ticket = new Ticket();
        this.ticket.setInTime(new Date(System.currentTimeMillis() - (60 * 60 * 1000)));
        this.ticket.setParkingSpot(parkingSpot);
        this.ticket.setVehicleRegNumber("ABCDEF");
        parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processExitingVehicleTest() {
        try {
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
            when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
            when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);


            parkingService.processExitingVehicle();

            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to set up test mock objects");

        }
    }

    @Test
    public void processIncomingVehicleTest() throws Exception {

            when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
            when(inputReaderUtil.readSelection()).thenReturn(2);
            when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(4);

            parkingService.processIncomingVehicle();

            verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
            verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

    }

    @Test
    public void processIncomingVehicleErrorTest() throws Exception {
        // GIVEN

        // WHEN
            when(inputReaderUtil.readSelection()).thenReturn(1);
            when(parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(1);
            when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new Exception("zut"));

            parkingService.processIncomingVehicle();

            verify(ticketDAO, Mockito.never()).saveTicket(any(Ticket.class));
    }

    @Test
    public void getVehicleRegNumberErrorTest() throws Exception {
        //GIVEN
        final Exception expectedException = new Exception("zut");

        //WHEN
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(expectedException);

       Exception throwsException = assertThrows(Exception.class, () -> parkingService.getVehicleRegNumber());

        // THEN
        Assertions.assertThat(throwsException)
                .isInstanceOf(Exception.class)
                .hasMessage(expectedException.getMessage());
    }
}
