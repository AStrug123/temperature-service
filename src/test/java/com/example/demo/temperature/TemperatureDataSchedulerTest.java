package com.example.demo.temperature;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.demo.utils.exceptions.FileProcessingException;
import com.example.demo.utils.filereader.FileReaderStrategy;

@ExtendWith(MockitoExtension.class)
class TemperatureDataSchedulerTest {

	@InjectMocks
	private TemperatureDataScheduler temperatureDataScheduler;

	@Mock
	private FileReaderStrategy fileReaderStrategy;

	@Mock
	private ExecutorService executorService;

	@Mock
	private TemperatureService temperatureService;

	private List<Temperature> mockTemperatures;
	private Future<List<Temperature>> mockFuture;

	@BeforeEach
	void setUp() throws Exception {
		mockTemperatures = List.of(
				Temperature.builder()
						.city("CityA")
						.timestamp(LocalDateTime.of(2022, 1, 1, 0, 0))
						.temperature(15.5)
						.build()
		);

		mockFuture = mock(Future.class);
		lenient().when(mockFuture.get()).thenReturn(mockTemperatures);
		lenient().when(executorService.submit(any(Callable.class))).thenReturn(mockFuture);
	}

	@Test
	void testRefreshData_shouldUpdateCityTemperatureData() throws Exception {
		//Given
		doAnswer(invocation -> {
			java.util.function.Consumer<String> consumer = invocation.getArgument(0);
			consumer.accept("CityA;2022-01-01 00:00:00.000;15.5");
			return null;
		}).when(fileReaderStrategy).readFile(any());

		//When
		temperatureDataScheduler.refreshData();

		//Then
		verify(temperatureService).updateCityTemperatureData(mockTemperatures);
		verify(executorService, atLeastOnce()).submit(any(Callable.class));
	}

	@Test
	void testRefreshData_withExecutionException_shouldThrowFileProcessingException() throws Exception {
		//Given
		Future<List<Temperature>> mockFuture = mock(Future.class);
		when(mockFuture.get()).thenThrow(new ExecutionException(new RuntimeException("Simulated Exception")));

		when(executorService.submit(any(Callable.class))).thenReturn(mockFuture);

		doAnswer(invocation -> {
			java.util.function.Consumer<String> consumer = invocation.getArgument(0);
			consumer.accept("CityA;2022-01-01 00:00:00.000;15.5");
			return null;
		}).when(fileReaderStrategy).readFile(any());

		//When & Then
		assertThatThrownBy(() -> temperatureDataScheduler.refreshData())
				.isInstanceOf(FileProcessingException.class)
				.hasMessageContaining("Error processing temperature data in parallel");

		verify(mockFuture, times(1)).get();
		verify(temperatureService, never()).updateCityTemperatureData(anyList());
	}

	@Test
	void testRefreshData_withEmptyFile_shouldNotUpdateCityTemperatureData() throws Exception {
		//Given
		doAnswer(invocation -> {
			return null;
		}).when(fileReaderStrategy).readFile(any());

		//When
		temperatureDataScheduler.refreshData();

		//Then
		verify(executorService, never()).submit(any(Callable.class));
		verify(temperatureService, never()).updateCityTemperatureData(anyList());
	}

	@Test
	void testRefreshData_withMalformedDataLine_shouldNotUpdateCityTemperatureData() throws Exception {
		//given
		doAnswer(invocation -> {
			java.util.function.Consumer<String> consumer = invocation.getArgument(0);
			consumer.accept("CityA;InvalidTimestamp;InvalidTemperature");  // Simulate malformed data
			return null;
		}).when(fileReaderStrategy).readFile(any());

		//when
		temperatureDataScheduler.refreshData();

		//then
		verify(executorService, never()).submit(any(Callable.class));  // Ensure no tasks are submitted
		verify(temperatureService, never()).updateCityTemperatureData(anyList());  // Ensure no update
	}

	@Test
	void testRefreshData_withIOException_shouldThrowFileProcessingException() throws Exception {
		//Given
		doThrow(new java.io.IOException("Test IOException")).when(fileReaderStrategy).readFile(any());

		//When & Then
		assertThatThrownBy(() -> temperatureDataScheduler.refreshData())
				.isInstanceOf(FileProcessingException.class)
				.hasMessageContaining("Error reading the temperature file");

		verify(executorService, never()).submit(any(Callable.class));
		verify(temperatureService, never()).updateCityTemperatureData(anyList());
	}
}
