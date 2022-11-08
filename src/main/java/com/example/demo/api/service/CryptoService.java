package com.example.demo.api.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
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

    public CryptoStatistics calculateCryptoStatistics(String requestedCrypto, LocalDate startDate, LocalDate endDate)
            throws CryptoNotSupportedException, NoCryptoValuesException {
        try (FileReader fileReader = new FileReader(cryptoLocation + requestedCrypto.toUpperCase() + "_values.csv")) {
            List<Crypto> cryptoValues = new CsvToBeanBuilder<Crypto>(fileReader).withType(Crypto.class).build().parse();
            // filter cryptos by selected period
            List<Crypto> filteredCryptoValues = cryptoValues.stream()
                    .filter(c -> checkIfDateIsInRange(c.getTimeStamp(), startDate, endDate))
                    .collect(Collectors.toList());
            if (filteredCryptoValues.isEmpty())
                throw new NoCryptoValuesException("No crypto values for specified period!");

            CryptoStatistics cryptoStatistics = new CryptoStatistics(
                    cryptoStatisticService.findOldestValue(filteredCryptoValues),
                    cryptoStatisticService.findNewestValue(filteredCryptoValues),
                    cryptoStatisticService.findMinValue(filteredCryptoValues),
                    cryptoStatisticService.findMaxValue(filteredCryptoValues));
            return cryptoStatistics;

        } catch (FileNotFoundException e) {
            log.info("Crypto is not supported, file: {} does not exist",
                    cryptoLocation + requestedCrypto.toUpperCase() + "_values.csv");
            e.printStackTrace();
            // if file does not exist it means crypto is not supported so we thrown exception
            throw new CryptoNotSupportedException("Not supported crypto: " + requestedCrypto);
        } catch (IOException e) {
            log.info("Error while closing file reader");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public List<String> sortCryptosByNormRange() {
        File cryptoFolder = new File(cryptoLocation);
        // we put crypto symbol and value of normalized range
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
        // we sort our map to descending order
        Stream<Map.Entry<String, Double>> sorted = cryptosWithNormRange.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        // put only keys which represent symbol to return sorted cryptos
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

    private boolean checkIfDateIsInRange(Long date, LocalDate starDate, LocalDate endDate) {
        LocalDate localDate = Instant.ofEpochMilli(date).atZone(ZoneId.systemDefault()).toLocalDate();
        return ((localDate.isEqual(starDate) || localDate.isAfter(starDate))
                && (localDate.isEqual(endDate) || localDate.isBefore(endDate)));
    }

}
