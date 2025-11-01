package ru.skillbox.currency.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.skillbox.currency.exchange.dto.CurrencyDto;
import ru.skillbox.currency.exchange.dto.CurrencyListResponse;
import ru.skillbox.currency.exchange.service.CurrencyService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/currency")
public class CurrencyController {
    private final CurrencyService service;

    @GetMapping(value = "/{id}")
    public ResponseEntity<CurrencyDto> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @GetMapping("/debug/currencies")
    public ResponseEntity<List<CurrencyDto>> getAllCurrenciesDebug() {
        return ResponseEntity.ok(service.getAllCurrenciesDebug());
    }

    @GetMapping
    public ResponseEntity<CurrencyListResponse> getAllCurrencies() {
        return ResponseEntity.ok(service.getAllCurrencies());
    }

    @GetMapping(value = "/convert")
    public ResponseEntity<Double> convertValue(@RequestParam("value") Long value,
                                               @RequestParam("numCode") Long numCode) {
        return ResponseEntity.ok(service.convertValue(value, numCode));
    }

    @GetMapping(value = "/convert-by-charcode")
    public ResponseEntity<Double> convertValueByCharCode(@RequestParam("value") Long value,
                                                         @RequestParam("charCode") String charCode) {
        return ResponseEntity.ok(service.convertValueByCharCode(value, charCode));
    }

    @PostMapping("/create")
    public ResponseEntity<CurrencyDto> create(@RequestBody CurrencyDto dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupOldCurrencies() {
        service.cleanupOldCurrencies();
        return ResponseEntity.ok("Old currencies cleanup completed successfully");
    }
}