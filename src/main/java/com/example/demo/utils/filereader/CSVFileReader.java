package com.example.demo.utils.filereader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public final class CSVFileReader implements FileReaderStrategy {

	private final ResourceLoader resourceLoader;

	@Value("${temperature.csv.file}")
	private String filePath;

	@Override
	public void readFile(Consumer<String> lineConsumer) throws IOException {
		final var resource = resourceLoader.getResource(filePath);

		try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lineConsumer.accept(line);
			}
		}
	}
}
