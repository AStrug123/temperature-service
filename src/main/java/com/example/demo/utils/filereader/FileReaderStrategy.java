package com.example.demo.utils.filereader;

import java.util.function.Consumer;

public interface FileReaderStrategy {

	void readFile(Consumer<String> lineConsumer) throws java.io.IOException;
}
