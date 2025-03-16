package pl.dziedzic.circuitbreakerexercise3.car;


import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/car")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    public List<Car> getCars(@RequestParam(required = false) boolean error) {
        return this.carService.getAllCars(error);
    }

    @PostMapping
    public Car saveCar(@RequestBody Car car) {
        return this.carService.saveCar(car);
    }
}
