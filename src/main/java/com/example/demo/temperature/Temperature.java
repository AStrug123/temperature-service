package com.example.demo.temperature;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
record Temperature(String city,
				   LocalDateTime timestamp,
				   double temperature) {
}
