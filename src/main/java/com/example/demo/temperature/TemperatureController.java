package com.example.demo.temperature;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/v1/temperatures")
@RequiredArgsConstructor
class TemperatureController {

	private final TemperatureService temperatureService;

	@GetMapping("/{city}")
	List<TemperatureResult> getYearlyAverageTemperatureByCity(@PathVariable String city) {
		return temperatureService.getYearlyAverageTemperature(city);
	}
}
