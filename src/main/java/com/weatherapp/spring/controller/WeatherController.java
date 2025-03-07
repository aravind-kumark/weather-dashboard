package com.weatherapp.spring.controller;

import com.weatherapp.spring.model.WeatherResponse;
import com.weatherapp.spring.service.WeatherService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/weather";
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam(required = false) String city, Model model) {
        if (city != null && !city.isEmpty()) {
            try {
                // Get current weather
                WeatherResponse response = weatherService.getWeather(city);

                if (response != null) {
                    model.addAttribute("weather", response);

                    // Get 5-day forecast
                    List<Map<String, Object>> forecast = weatherService.getForecast(city, 5);

                    if (forecast != null && !forecast.isEmpty()) {
                        model.addAttribute("forecast", forecast);
                    }
                } else {
                    model.addAttribute("error", "Unable to fetch weather data for " + city);
                }
            } catch (Exception e) {
                model.addAttribute("error", "An error occurred while processing your request: " + e.getMessage());
            }
        }

        return "weather";
    }
}