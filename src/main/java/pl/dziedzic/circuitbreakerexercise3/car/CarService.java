package pl.dziedzic.circuitbreakerexercise3.car;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CarService {
    private final CarRepository carRepository;
    private final CacheManager cacheManager;

    public CarService(CarRepository carRepository, CacheManager cacheManager) {
        this.carRepository = carRepository;
        this.cacheManager = cacheManager;
    }

    public Car saveCar(Car car) {
        return carRepository.save(car);
    }

    @CircuitBreaker(name = "fetchCarData", fallbackMethod = "getLastValidListOfCars")
    @Cacheable(value = "cars", key="#error")
    public List<Car> getAllCars(boolean error) {
        if (error) {
            throw new NullPointerException();
        }
        return carRepository.findAll();
    }

    public List<Car> getLastValidListOfCars(boolean error, Throwable t) {
        ConcurrentMapCache cacheCarList = (ConcurrentMapCache) cacheManager.getCache("cars");
        if (cacheCarList != null) {
            ConcurrentHashMap<Object, Object> storeCarList = (ConcurrentHashMap<Object, Object>) cacheCarList.getNativeCache();
            if (!storeCarList.isEmpty()) {
                Object value = storeCarList.getOrDefault(!error, new ArrayList<>());
                if (value instanceof List<?> carList) {
                    if (!carList.isEmpty() && carList.getFirst() instanceof Car) {
                        @SuppressWarnings("unchecked")
                        List<Car> fetchedCars = (List<Car>) carList;
                        return fetchedCars;
                    }
                }
            }
        }
        return new ArrayList<>();
    }
}
