package parking.service;

import parking.model.*;

public final class VehicleFactory {
    private VehicleFactory() { }

    public static Vehicle create(String type, String regNo) {
        switch (type.toUpperCase()) {
            case "TWO", "TWOWHEELER": return new TwoWheeler(regNo);
            case "CAR":               return new Car(regNo);
            case "EV", "ELECTRICCAR": return new ElectricCar(regNo);
            default: throw new IllegalArgumentException("unknown vehicle type: " + type);
        }
    }
}
