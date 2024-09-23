package com.example.demo.temperature;

import java.util.List;

interface TemperatureService {

	void updateCityTemperatureData(List<Temperature> temperatures);

	List<TemperatureResult> getYearlyAverageTemperature(String city);
}
