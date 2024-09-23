package com.example.demo.temperature;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class TemperatureControllerIT {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Autowired
	private TemperatureService temperatureService;

	@BeforeEach
	void setUp() {
		seedTemperatureData();
	}

	@Test
	void shouldReturnYearlyAverageTemperatureByCity() {
		// When
		final var response = makeGetRequest("/v1/temperatures/CityA", TemperatureResult[].class);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).hasSize(2);

		final TemperatureResult[] results = response.getBody();
		assertThat(results[0].year()).isEqualTo(2021);
		assertThat(results[0].averageTemperature()).isEqualTo(15.5);
		assertThat(results[1].year()).isEqualTo(2022);
		assertThat(results[1].averageTemperature()).isEqualTo(16.0);
	}

	@Test
	void shouldReturnEmptyListWhenCityDoesNotExist() {
		// When
		ResponseEntity<TemperatureResult[]> response = makeGetRequest("/v1/temperatures/UnknownCity", TemperatureResult[].class);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEmpty();
	}

	@Test
	void shouldHandleEmptyCityName() {
		// When
		final var response = makeGetRequest("/v1/temperatures/", String.class);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND); // URL should contain city name
	}

	@Test
	void shouldHandleMalformedCityName() {
		// When
		final var response = makeGetRequest("/v1/temperatures/@#$%", TemperatureResult[].class);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(response.getBody()).isEmpty(); // Invalid characters but valid path
	}

	@Test
	void shouldReturnBadRequestForInvalidRequestStructure() {
		// When
		final var response = makeGetRequest("", String.class);

		// Then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND); // Missing city in path
	}


	private void seedTemperatureData() {
		temperatureService.updateCityTemperatureData(List.of(
				new Temperature("CityA", LocalDateTime.of(2021, 1, 1, 0, 0), 15.5),
				new Temperature("CityA", LocalDateTime.of(2022, 1, 1, 0, 0), 16.0),
				new Temperature("CityB", LocalDateTime.of(2022, 1, 1, 0, 0), 17.0)
		));
	}

	private <T> ResponseEntity<T> makeGetRequest(String url, Class<T> responseType) {
		return restTemplate.getForEntity("http://localhost:" + port + url, responseType);
	}
}
