package ru.skillbox.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.skillbox.currency.exchange.dto.CurrencyDto;
import ru.skillbox.currency.exchange.dto.CurrencyListResponse;
import ru.skillbox.currency.exchange.entity.Currency;
import ru.skillbox.currency.exchange.mapper.CurrencyMapper;
import ru.skillbox.currency.exchange.repository.CurrencyRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {
    private final CurrencyMapper mapper;
    private final CurrencyRepository repository;

    public CurrencyDto getById(Long id) {
        log.info("CurrencyService method getById executed for id: {}", id);
        Currency currency = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Currency not found with id: " + id));
        return mapper.convertToDto(currency);
    }

    public List<CurrencyDto> getAllCurrenciesDebug() {
        log.info("CurrencyService method getAllCurrenciesDebug executed");
        List<Currency> currencies = repository.findAll();
        return currencies.stream()
                .map(currency -> {
                    CurrencyDto dto = new CurrencyDto();
                    dto.setId(currency.getId());
                    dto.setName(currency.getName());
                    dto.setValue(currency.getValue());
                    dto.setIsoNumCode(currency.getIsoNumCode());
                    dto.setIsoCharCode(currency.getIsoCharCode());
                    dto.setNominal(currency.getNominal());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public CurrencyListResponse getAllCurrencies() {
        log.info("CurrencyService method getAllCurrencies executed");
        List<Currency> currencies = repository.findAll();

        // Фильтруем только валюты с заполненным буквенным кодом
        List<CurrencyListResponse.CurrencyItem> currencyItems = currencies.stream()
                .filter(currency -> currency.getIsoCharCode() != null && !currency.getIsoCharCode().isEmpty())
                .map(currency -> new CurrencyListResponse.CurrencyItem(
                        currency.getName(),
                        currency.getValue()
                ))
                .collect(Collectors.toList());

        return new CurrencyListResponse(currencyItems);
    }

    public Double convertValue(Long value, Long numCode) {
        log.info("CurrencyService method convertValue executed. value: {}, numCode: {}", value, numCode);

        // Используем новый метод который ищет записи с заполненным charCode
        Optional<Currency> currencyOpt = repository.findByIsoNumCodeWithCharCode(numCode);

        if (!currencyOpt.isPresent()) {
            log.warn("Currency not found with numCode: {} (with non-empty charCode)", numCode);

            // Выведем доступные валюты для отладки
            List<Currency> availableCurrencies = repository.findAll().stream()
                    .filter(c -> c.getIsoCharCode() != null && !c.getIsoCharCode().isEmpty())
                    .collect(Collectors.toList());

            log.warn("Available currencies with non-empty charCode:");
            availableCurrencies.forEach(c ->
                    log.warn(" - {}: numCode={}, charCode={}, value={}",
                            c.getName(), c.getIsoNumCode(), c.getIsoCharCode(), c.getValue())
            );

            throw new RuntimeException("Currency not found with numCode: " + numCode);
        }

        Currency currency = currencyOpt.get();
        double result = value * currency.getValue();
        log.info("Conversion result: {} * {} = {}", value, currency.getValue(), result);
        return result;
    }

    public Double convertValueByCharCode(Long value, String charCode) {
        log.info("CurrencyService method convertValueByCharCode executed. value: {}, charCode: {}", value, charCode);

        Optional<Currency> currencyOpt = repository.findOptionalByIsoCharCode(charCode);
        if (!currencyOpt.isPresent()) {
            log.warn("Currency not found with charCode: {}", charCode);

            // Выведем доступные валюты для отладки
            List<Currency> availableCurrencies = repository.findAll().stream()
                    .filter(c -> c.getIsoCharCode() != null && !c.getIsoCharCode().isEmpty())
                    .collect(Collectors.toList());

            log.warn("Available currencies:");
            availableCurrencies.forEach(c ->
                    log.warn(" - {}: charCode={}, value={}",
                            c.getName(), c.getIsoCharCode(), c.getValue())
            );

            throw new RuntimeException("Currency not found with charCode: " + charCode);
        }

        Currency currency = currencyOpt.get();
        double result = value * currency.getValue();
        log.info("Conversion result: {} * {} = {}", value, currency.getValue(), result);
        return result;
    }

    public CurrencyDto create(CurrencyDto dto) {
        log.info("CurrencyService method create executed");
        return mapper.convertToDto(repository.save(mapper.convertToEntity(dto)));
    }

    @Transactional
    public void cleanupOldCurrencies() {
        log.info("Starting cleanup of old currencies with empty charCode");
        List<Currency> allCurrencies = repository.findAll();

        List<Currency> oldCurrencies = allCurrencies.stream()
                .filter(c -> c.getIsoCharCode() == null || c.getIsoCharCode().isEmpty())
                .collect(Collectors.toList());

        if (oldCurrencies.isEmpty()) {
            log.info("No old currencies found for cleanup");
            return;
        }

        log.info("Found {} old currencies to remove:", oldCurrencies.size());
        oldCurrencies.forEach(oldCurrency -> {
            log.info(" - Removing: {} - {} (id: {})", oldCurrency.getName(), oldCurrency.getIsoNumCode(), oldCurrency.getId());
            repository.delete(oldCurrency);
        });

        log.info("Cleanup completed successfully");
    }


}