package com.example.demo.temperature;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TemperatureServiceTest {

	@InjectMocks
	private TemperatureServiceImpl temperatureService;

	@Test
	void testGetYearlyAverageTemperature() {
		// Given
		final var temperatures = List.of(
				Temperature.builder().city("CityA").timestamp(LocalDateTime.of(2022, 1, 1, 0, 0))
						.temperature(15.5).build(),
				Temperature.builder().city("CityA").timestamp(LocalDateTime.of(2022, 2, 1, 0, 0))
						.temperature(17.5).build(),
				Temperature.builder().city("CityA").timestamp(LocalDateTime.of(2021, 1, 1, 0, 0))
						.temperature(20.0).build()
		);

		// When
		temperatureService.updateCityTemperatureData(temperatures);
		final var results = temperatureService.getYearlyAverageTemperature("CityA");

		// Then
		assertThat(results).hasSize(2);
		assertThat(results).extracting(TemperatureResult::year).containsExactly(2021, 2022);
		assertThat(results).extracting(TemperatureResult::averageTemperature)
				.containsExactly(20.0, 16.5);
	}

	@Test
	void testGetYearlyAverageTemperature_emptyCity() {
		// When
		final var results = temperatureService.getYearlyAverageTemperature("CityB");

		// Then
		assertThat(results).isEmpty();
	}

	@Test
	void testUpdateCityTemperatureData() {
		// Given
		final var temperatures = List.of(
				Temperature.builder().city("CityA").timestamp(LocalDateTime.of(2022, 1, 1, 0, 0))
						.temperature(15.5).build(),
				Temperature.builder().city("CityA").timestamp(LocalDateTime.of(2022, 2, 1, 0, 0))
						.temperature(17.5).build()
		);

		// When
		temperatureService.updateCityTemperatureData(temperatures);

		// Then
		assertThat(temperatureService.getYearlyAverageTemperature("CityA")).hasSize(1);
		assertThat(temperatureService.getYearlyAverageTemperature("CityA").get(0)
				.averageTemperature()).isEqualTo(16.5);
	}
}
