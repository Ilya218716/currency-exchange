package ru.skillbox.currency.exchange.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.xml.bind.annotation.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "ValCurs")
@XmlAccessorType(XmlAccessType.FIELD)
public class ValCurs {

    @XmlElement(name = "Valute")
    private List<Valute> valutes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Valute {
        @XmlElement(name = "NumCode")
        private String numCode;

        @XmlElement(name = "CharCode")
        private String charCode;

        @XmlElement(name = "Nominal")
        private Long nominal;

        @XmlElement(name = "Name")
        private String name;

        @XmlElement(name = "Value")
        private String value;
    }
}