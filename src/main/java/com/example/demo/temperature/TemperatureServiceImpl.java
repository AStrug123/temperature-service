package com.example.demo.temperature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
class TemperatureServiceImpl implements TemperatureService {

	private final Map<String, List<Temperature>> cityTemperatureData = new ConcurrentHashMap<>();

	@Override
	public synchronized void updateCityTemperatureData(List<Temperature> temperatures) {
		for (Temperature temperature : temperatures) {
			cityTemperatureData
					.computeIfAbsent(temperature.city(), cityKey -> new ArrayList<>())
					.add(temperature);
		}
	}

	@Cacheable(value = "cityTemperatures", key = "#city")
	@Override
	public List<TemperatureResult> getYearlyAverageTemperature(String city) {
		final List<Temperature> cityTemperatures = cityTemperatureData.get(city);

		if (cityTemperatures == null || cityTemperatures.isEmpty()) {
			return Collections.emptyList();
		}

		return cityTemperatures.stream()
				.collect(groupTemperaturesByYear())
				.entrySet()
				.stream()
				.map(this::buildResult)
				.collect(Collectors.toList());
	}

	private TemperatureResult buildResult(Entry<Integer, List<Double>> entry) {
		final int year = entry.getKey();
		final List<Double> temps = entry.getValue();
		final double averageTemperature = calculateAverage(temps);
		return new TemperatureResult(year, averageTemperature);
	}

	private double calculateAverage(List<Double> temps) {
		double average = temps.stream()
				.mapToDouble(Double::doubleValue)
				.average()
				.orElse(0.0);

		return BigDecimal.valueOf(average)
				.setScale(1, RoundingMode.HALF_UP)
				.doubleValue();
	}

	private Collector<Temperature, ?, TreeMap<Integer, List<Double>>> groupTemperaturesByYear() {
		return Collectors.groupingBy(
				temp -> temp.timestamp().getYear(),
				TreeMap::new,
				Collectors.mapping(Temperature::temperature, Collectors.toList())
		);
	}
}
