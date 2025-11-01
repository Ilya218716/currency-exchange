package ru.skillbox.currency.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyListResponse {
    private List<CurrencyItem> currencies;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrencyItem {
        private String name;
        private Double value;
    }
}