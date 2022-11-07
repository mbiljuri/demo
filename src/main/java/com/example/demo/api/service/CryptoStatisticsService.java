package com.example.demo.api.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.exceptions.NoCryptoValuesException;
import com.example.demo.model.Crypto;

@Service
public class CryptoStatisticsService {

    public Double findMaxValue(List<Crypto> cryptoValues) {
        return cryptoValues.stream().mapToDouble(c -> c.getPrice()).max().getAsDouble();
    }

    public Double findMinValue(List<Crypto> cryptoValues) {
        return cryptoValues.stream().mapToDouble(c -> c.getPrice()).min().getAsDouble();
    }

    public Double findOldestValue(List<Crypto> cryptoValues) {
        Crypto oldestCrypto = cryptoValues.stream().min(Comparator.comparingLong(c -> c.getTimeStamp())).get();
        return oldestCrypto.getPrice();
    }

    public Double findNewestValue(List<Crypto> cryptoValues) {
        Crypto newestCrypto = cryptoValues.stream().max(Comparator.comparingLong(c -> c.getTimeStamp())).get();
        return newestCrypto.getPrice();
    }

    public Double calculateNormalizedRange(List<Crypto> cryptoValues) {
        return (findMaxValue(cryptoValues) - findMinValue(cryptoValues) / findMinValue(cryptoValues));
    }

    public Double calculateNormRangeForSpecificDay(List<Crypto> cryptoValues, LocalDate requestedDay) throws NoCryptoValuesException {
        List<Crypto> filteredCrypto = cryptoValues.stream()
                .filter(c -> requestedDay
                        .equals(Instant.ofEpochMilli(c.getTimeStamp()).atZone(ZoneId.systemDefault()).toLocalDate()))
                .collect(Collectors.toList());
        if(filteredCrypto.isEmpty()) throw new NoCryptoValuesException("No values for chosen date!");
        return calculateNormalizedRange(filteredCrypto);
    }

}
