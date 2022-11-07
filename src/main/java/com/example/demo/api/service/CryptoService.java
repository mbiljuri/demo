package com.example.demo.api.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.exceptions.CryptoNotSupportedException;
import com.example.demo.exceptions.NoCryptoValuesException;
import com.example.demo.model.Crypto;
import com.example.demo.model.CryptoStatistics;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CryptoService {

    private CryptoStatisticsService cryptoStatisticService;
    @Value("${crypto.values.location}")
    private String cryptoLocation;

    public CryptoService(CryptoStatisticsService cryptoStatisticsService) {
        this.cryptoStatisticService = cryptoStatisticsService;
    }

    public CryptoStatistics calculateCryptoStatistics(String requestedCrypto) throws CryptoNotSupportedException {
        CryptoStatistics cryptoStatistics = new CryptoStatistics();
        try (FileReader fileReader = new FileReader(cryptoLocation + requestedCrypto.toUpperCase() + "_values.csv")) {
            List<Crypto> cryptoValues = new CsvToBeanBuilder<Crypto>(fileReader).withType(Crypto.class).build().parse();

            cryptoStatistics.setMaxValue(cryptoStatisticService.findMaxValue(cryptoValues));
            cryptoStatistics.setMinValue(cryptoStatisticService.findMinValue(cryptoValues));
            cryptoStatistics.setNewestValue(cryptoStatisticService.findNewestValue(cryptoValues));
            cryptoStatistics.setOldestValue(cryptoStatisticService.findOldestValue(cryptoValues));

        } catch (FileNotFoundException e) {
            log.info("Crypto is not supported, file: {} does not exist",
                    cryptoLocation + requestedCrypto.toUpperCase() + "_values.csv");
            e.printStackTrace();
            throw new CryptoNotSupportedException("Not supported crypto: " + requestedCrypto);
        } catch (IOException e) {
            log.info("Error while closing file reader");
            e.printStackTrace();
            throw new RuntimeException();
        }
        return cryptoStatistics;

    }

    public List<String> sortCryptosByNormRange() {
        File cryptoFolder = new File(cryptoLocation);
        Map<String, Double> cryptosWithNormRange = new HashMap<>();
        List<String> sortedCryptosByNormRange = new ArrayList<>();
        for (File cryptoFile : cryptoFolder.listFiles()) {
            try (FileReader fileReader = new FileReader(cryptoFile)) {
                List<Crypto> cryptoValues = new CsvToBeanBuilder<Crypto>(fileReader).withType(Crypto.class).build()
                        .parse();
                cryptosWithNormRange.put(cryptoValues.get(0).getSymbol(),
                        cryptoStatisticService.calculateNormalizedRange(cryptoValues));
            } catch (IOException e) {
                log.info("Error while closing file reader");
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
        Stream<Map.Entry<String, Double>> sorted = cryptosWithNormRange.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        sorted.forEach(c -> sortedCryptosByNormRange.add(c.getKey()));
        return sortedCryptosByNormRange;
    }

    public String getCryptoWithHighestNormRangeForDate(LocalDate requestedDate) throws NoCryptoValuesException {
        File cryptoFolder = new File(cryptoLocation);
        Map<String, Double> cryptosWithNormRangeForDate = new HashMap<>();
        for (File cryptoFile : cryptoFolder.listFiles()) {
            try (FileReader fileReader = new FileReader(cryptoFile)) {
                List<Crypto> cryptoValues = new CsvToBeanBuilder<Crypto>(fileReader).withType(Crypto.class).build()
                        .parse();
                cryptosWithNormRangeForDate.put(cryptoValues.get(0).getSymbol(),
                        cryptoStatisticService.calculateNormRangeForSpecificDay(cryptoValues, requestedDate));

            } catch (IOException e) {
                log.info("Error while closing file reader");
                e.printStackTrace();
                throw new RuntimeException();
            }
        }
        return cryptosWithNormRangeForDate.entrySet().stream()
                .max(Comparator.comparingDouble(c -> c.getValue())).get().getKey();
    }
}
