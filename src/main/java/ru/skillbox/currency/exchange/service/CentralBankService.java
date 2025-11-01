package ru.skillbox.currency.exchange.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.skillbox.currency.exchange.dto.ValCurs;
import ru.skillbox.currency.exchange.entity.Currency;
import ru.skillbox.currency.exchange.repository.CurrencyRepository;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import java.io.StringReader;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CentralBankService {

    private final CurrencyRepository currencyRepository;

    @Value("${app.central-bank.url}") // Добавьте эту аннотацию
    private String centralBankUrl;

    @Scheduled(fixedRate = 3600000) // Этот метод используется планировщиком!
    public void updateCurrenciesFromCbr() {
        log.info("Starting currency update from Central Bank");

        try {
            String xmlData = fetchCurrencyData();
            ValCurs valCurs = parseXmlData(xmlData);
            updateCurrencies(valCurs);

            log.info("Successfully updated currencies from Central Bank");
        } catch (Exception e) {
            log.error("Error updating currencies from Central Bank: {}", e.getMessage(), e);
        }
    }

    private String fetchCurrencyData() {
        WebClient webClient = WebClient.create();
        return webClient.get()
                .uri(centralBankUrl)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private ValCurs parseXmlData(String xmlData) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ValCurs.class);
        return (ValCurs) jaxbContext.createUnmarshaller()
                .unmarshal(new StringReader(xmlData));
    }

    private void updateCurrencies(ValCurs valCurs) {
        if (valCurs == null || valCurs.getValutes() == null) {
            log.warn("No currency data received from Central Bank");
            return;
        }

        valCurs.getValutes().stream()
                .map(this::convertToCurrency)
                .forEach(this::saveOrUpdateCurrency);
    }

    private Currency convertToCurrency(ValCurs.Valute valute) {
        Currency currency = new Currency();
        currency.setName(valute.getName());
        currency.setNominal(valute.getNominal());
        currency.setIsoNumCode(Long.valueOf(valute.getNumCode()));
        currency.setIsoCharCode(valute.getCharCode());

        String valueStr = valute.getValue().replace(",", ".");
        currency.setValue(Double.valueOf(valueStr));

        return currency;
    }

    private void saveOrUpdateCurrency(Currency currency) {
        Optional<Currency> existingCurrency = Optional.ofNullable(
                currencyRepository.findByIsoCharCode(currency.getIsoCharCode())
        );

        if (existingCurrency.isPresent()) {
            Currency existing = existingCurrency.get();
            existing.setName(currency.getName());
            existing.setNominal(currency.getNominal());
            existing.setValue(currency.getValue());
            existing.setIsoNumCode(currency.getIsoNumCode());
            currencyRepository.save(existing);
            log.debug("Updated currency: {}", currency.getIsoCharCode());
        } else {
            currencyRepository.save(currency);
            log.debug("Created new currency: {}", currency.getIsoCharCode());
        }
    }
}