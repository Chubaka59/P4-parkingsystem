package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Date;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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
    public void processExitingVehicleTest() throws Exception {
        //GIVEN a vehicle already entered the parking
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        //WHEN the vehicle is leaving
        parkingService.processExitingVehicle();

        //THEN the parking spot is updated
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processIncomingVehicleTest() throws Exception {
        //GIVEN a vehicle would use the parking
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(inputReaderUtil.readSelection()).thenReturn(2);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.BIKE)).thenReturn(4);

        //WHEN the vehicle is entering the parking
        parkingService.processIncomingVehicle();

        //THEN a ticket is registered and a parking spot assigned
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));

    }

    @Test
    public void processIncomingVehicleErrorTest() throws Exception {
        // GIVEN the vehicle registration number cannot be read
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenThrow(new Exception("Error in the vehicle registration number"));

        //WHEN the vehicle try to enter the parking
        parkingService.processIncomingVehicle();

        //THEN no tickets are saved
        verify(ticketDAO, Mockito.never()).saveTicket(any(Ticket.class));
    }

    private static Stream<Arguments> parkingTypes(){
        return Stream.of(
                Arguments.of(ParkingType.CAR, 1),
                Arguments.of(ParkingType.BIKE, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("parkingTypes")
    public void getNextParkingNumberIfAvailableTest(ParkingType expectedParkingType, int expectedId) {
        //GIVEN a vehicle is comming

        when(inputReaderUtil.readSelection()).thenReturn(expectedId);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(expectedId);

        //WHEN the system check if a slot is available
        ParkingSpot response = parkingService.getNextParkingNumberIfAvailable();

        //THEN the system send a slot as response
        assertThat(response)
                .isNotNull()
                .satisfies( parkingSpot -> {
                    assertThat(parkingSpot.getId()).isEqualTo(expectedId);
                    assertThat(parkingSpot.getParkingType()).isEqualTo(expectedParkingType);
                    assertThat(parkingSpot.isAvailable()).isTrue();
                });
    }


    @ParameterizedTest
    @MethodSource("parkingTypes")
    public void getNextParkingNumberIfAvailableErrorTest(ParkingType expectedParkingType, int expectedId) {
        //GIVEN there is no parking spot available
        when(inputReaderUtil.readSelection()).thenReturn(expectedId);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);

        //WHEN the system check if a slot is available
        ParkingSpot response = parkingService.getNextParkingNumberIfAvailable();

        //THEN the system send a response
        assertThat(response)
                .isNull();
    }
    
    @Test
    public void getNextParkingNumberIfAvailableErrorWithImpossibleType() {
        //GIVEN a vehicle is coming
        when(inputReaderUtil.readSelection()).thenReturn(3);

        //WHEN the system check if a slot is available
        ParkingSpot response = parkingService.getNextParkingNumberIfAvailable();

        //THEN the system send a response
        assertThat(response).isNull();
    }

    @Test
    public void processExitingVehicleForRecurrentUser() throws Exception {
        //GIVEN recurrent user is using the parking
        BigDecimal expectedPrice = BigDecimal.valueOf(1.42);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.getTicketCount(anyString())).thenReturn(1);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);

        //WHEN he leaves
        parkingService.processExitingVehicle();

        //THEN he gets a reduction on the price
        assertThat(ticket.getPrice()).isEqualTo(expectedPrice);
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void processIncomingVehicleWhileAlreadyInTheParking() throws Exception {
        //GIVEN the vehicle is already in the parking
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);

        //WHEN the vehicle try to enter once again
        parkingService.processIncomingVehicle();

        //THEN no ticket is created
        verify(ticketDAO, Mockito.never()).saveTicket(any(Ticket.class));
    }
}
