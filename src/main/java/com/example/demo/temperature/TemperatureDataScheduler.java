package com.example.demo.temperature;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.example.demo.utils.Constants;
import com.example.demo.utils.exceptions.FileProcessingException;
import com.example.demo.utils.filereader.FileReaderStrategy;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
@RequiredArgsConstructor
public class TemperatureDataScheduler {

	private static final String ERROR_PROCESSING_TEMPERATURE_DATA_IN_PARALLEL = "Error processing temperature data in parallel";

	private static final String ERROR_READING_THE_TEMPERATURE_FILE = "Error reading the temperature file";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
			"yyyy-MM-dd HH:mm:ss.SSS");

	private final FileReaderStrategy fileReader;

	private final ExecutorService executorService;

	private final TemperatureService temperatureService;

	@PostConstruct
	public void initializeData() {
		log.info("Refreshing temperature data started...");
		refreshData();
		log.info("Refreshing temperature data ended...");
	}

	@Scheduled(fixedRate = Constants.FILE_READ_INTERVAL_MS)
	public void refreshDataScheduled() {
		log.info("Scheduled Refreshing temperature data started...");
		refreshData();
		log.info("Scheduled Refreshing temperature data ended...");
	}

	public void refreshData() {
		final var futures = submitLinesForProcessing();
		final var results = waitForResults(futures);
		updateCityTemperatures(results);
	}

	private List<Future<List<Temperature>>> submitLinesForProcessing() {
		final List<Future<List<Temperature>>> futures = new ArrayList<>();

		try {
			fileReader.readFile(line -> {
				final List<Temperature> temperatures = processLineToTemperature(line);
				if (!temperatures.isEmpty()) {  // Only submit if temperatures list is not empty
					final Future<List<Temperature>> future = executorService.submit(() -> temperatures);
					futures.add(future);
				}
			});
		} catch (IOException e) {
			throw new FileProcessingException(ERROR_READING_THE_TEMPERATURE_FILE, e);
		}

		return futures;
	}

	private List<Temperature> waitForResults(List<Future<List<Temperature>>> futures) {
		final List<Temperature> allTemperatures = new ArrayList<>();

		for (Future<List<Temperature>> future : futures) {
			try {
				final var temperatures = future.get();
				allTemperatures.addAll(temperatures);
			} catch (InterruptedException | ExecutionException e) {
				throw new FileProcessingException(ERROR_PROCESSING_TEMPERATURE_DATA_IN_PARALLEL, e);
			}
		}

		return allTemperatures;
	}


	private void updateCityTemperatures(List<Temperature> results) {
		if (!results.isEmpty()) {
			log.info("Updating city temperature");
			temperatureService.updateCityTemperatureData(results);
		}
	}

	private List<Temperature> processLineToTemperature(String line) {
		final String[] parts = line.split(Constants.DELIMITER);

		if (parts.length == 3) {
			try {
				final String city = parts[0];
				final LocalDateTime timestamp = LocalDateTime.parse(parts[1], DATE_TIME_FORMATTER);
				final double temperatureValue = Double.parseDouble(parts[2]);

				return List.of(Temperature.builder()
						.city(city)
						.timestamp(timestamp)
						.temperature(temperatureValue)
						.build());
			} catch (Exception e) {
				return Collections.emptyList();
			}
		}

		return Collections.emptyList();
	}
}
