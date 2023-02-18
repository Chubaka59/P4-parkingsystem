package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ParkingSpotDAOTest {
    private final static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static DataBasePrepareService dataBasePrepareService;
    private static ParkingSpotDAO parkingSpotDAO;

    @BeforeAll
    public static void setUp(){
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest(){
        dataBasePrepareService.clearDataBaseEntries();
    }


    private static Stream<Arguments> parkingTypes(){
        return Stream.of(
                Arguments.of(ParkingType.CAR, 1),
                Arguments.of(ParkingType.BIKE, 4)
        );
    }

    @ParameterizedTest
    @MethodSource("parkingTypes")
    public void getNextAvailableSlotTest(ParkingType parkingType, int expectedSpots){
        int parkingSpotNumber = parkingSpotDAO.getNextAvailableSlot(parkingType);

        assertEquals(expectedSpots, parkingSpotNumber);
    }


    @ParameterizedTest
    @MethodSource("parkingTypes")
    public void updateParkingTest(ParkingType parkingType, int spot){
        ParkingSpot parkingSpot = new ParkingSpot(spot, parkingType, false);

        boolean response = parkingSpotDAO.updateParking(parkingSpot);

        assertTrue(response);
    }
}
