package com.example.demo.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.api.service.CryptoService;
import com.example.demo.exceptions.CryptoNotSupportedException;
import com.example.demo.exceptions.NoCryptoValuesException;
import com.example.demo.model.CryptoStatistics;

import lombok.AllArgsConstructor;

@RequestMapping(value = "/api", produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
@AllArgsConstructor
public class Api {

    private final CryptoService cryptoService;

    @GetMapping(path = "/crypto/statistics/{crypto}")
    public ResponseEntity<CryptoStatistics> getCryptoStatistics(@PathVariable String crypto,
            @RequestParam(name = "startDate", required = false, defaultValue = "01.01.2022") @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate startDate,
            @RequestParam(name = "endDate", required = false, defaultValue = "31.01.2022") @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate endDate)
            throws CryptoNotSupportedException, NoCryptoValuesException {
        return new ResponseEntity<>(cryptoService.calculateCryptoStatistics(crypto, startDate, endDate), HttpStatus.OK);
    }

    @GetMapping(path = "/cryptos/normrange/sorted")
    public ResponseEntity<List<String>> getCryptos() {
        return new ResponseEntity<>(cryptoService.sortCryptosByNormRange(), HttpStatus.OK);
    }

    @GetMapping(path = "/cryptos/normrange/highest")
    public ResponseEntity<String> getCryptoWithHighestNormRangeForDate(
            @RequestParam("date") @DateTimeFormat(pattern = "dd.MM.yyyy") LocalDate date)
            throws NoCryptoValuesException {
        return new ResponseEntity<>(cryptoService.getCryptoWithHighestNormRangeForDate(date), HttpStatus.OK);
    }

}
