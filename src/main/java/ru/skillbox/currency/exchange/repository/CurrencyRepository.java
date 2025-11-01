package ru.skillbox.currency.exchange.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.skillbox.currency.exchange.entity.Currency;

import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    // УДАЛИТЬ этот метод если хотите убрать предупреждение
    // Currency findByIsoNumCode(Long isoNumCode);

    // Новый метод - ищет только записи с заполненным charCode
    @Query("SELECT c FROM Currency c WHERE c.isoNumCode = :isoNumCode AND c.isoCharCode != ''")
    Optional<Currency> findByIsoNumCodeWithCharCode(@Param("isoNumCode") Long isoNumCode);

    Currency findByIsoCharCode(String isoCharCode);

    // Метод для поиска по буквенному коду с Optional
    default Optional<Currency> findOptionalByIsoCharCode(String isoCharCode) {
        Currency currency = findByIsoCharCode(isoCharCode);
        return Optional.ofNullable(currency);
    }
}