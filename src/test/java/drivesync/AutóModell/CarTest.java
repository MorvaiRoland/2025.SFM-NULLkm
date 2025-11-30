package drivesync.AutóModell;

import drivesync.AutoModell.Car;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class CarTest {

    @Test
    void testCarConstructorAndGetters() {
        // --- 1. Arrange (Előkészítés) ---
        // Tesztadatok definiálása
        int id = 1;
        int ownerId = 101;
        String license = "ABC-123";
        String brand = "Toyota";
        String type = "Corolla";
        int vintage = 2020;
        String engineType = "Hybrid";
        String fuelType = "Petrol/Electric";
        int km = 50000;
        int oil = 100; // Feltételezve, hogy ez százalék vagy mennyiség
        int tireSize = 16;
        LocalDate service = LocalDate.of(2023, 5, 20);
        LocalDate insurance = LocalDate.of(2024, 5, 20);

        // --- 2. Act (Végrehajtás) ---
        // Az objektum példányosítása a konstruktorral
        Car car = new Car(
                id, ownerId, license, brand, type, vintage,
                engineType, fuelType, km, oil, tireSize,
                service, insurance
        );

        // --- 3. Assert (Ellenőrzés) ---
        // Ellenőrizzük, hogy a getterek a várt értékeket adják-e vissza
        assertNotNull(car, "Az autó objektum nem lehet null");

        assertEquals(id, car.getId(), "Az ID nem egyezik");
        assertEquals(ownerId, car.getOwnerId(), "A tulajdonos ID nem egyezik");
        assertEquals(license, car.getLicense(), "A rendszám nem egyezik");
        assertEquals(brand, car.getBrand(), "A márka nem egyezik");
        assertEquals(type, car.getType(), "A típus nem egyezik");
        assertEquals(vintage, car.getVintage(), "Az évjárat nem egyezik");
        assertEquals(engineType, car.getEngineType(), "A motortípus nem egyezik");
        assertEquals(fuelType, car.getFuelType(), "Az üzemanyag nem egyezik");
        assertEquals(km, car.getKm(), "A kilométerállás nem egyezik");
        assertEquals(oil, car.getOil(), "Az olajszint nem egyezik");
        assertEquals(tireSize, car.getTireSize(), "A gumiméret nem egyezik");
        assertEquals(service, car.getService(), "A szerviz dátuma nem egyezik");
        assertEquals(insurance, car.getInsurance(), "A biztosítás dátuma nem egyezik");
    }
}