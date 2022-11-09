package com.example.demo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.api.service.CryptoStatisticsService;
import com.example.demo.exceptions.NoCryptoValuesException;
import com.example.demo.model.Crypto;

@SpringBootTest
public class CryptoStatisticsServiceTests {
    
    @Autowired
	private CryptoStatisticsService cryptoStatisticsService;
    private List<Crypto> cryptoValuesTest;

    @BeforeEach
	public void initCryptos() {
		cryptoValuesTest = new ArrayList<>();
		Crypto crypto1 = new Crypto(1641009600000L, "BTC", 46813.21);
		cryptoValuesTest.add(crypto1);
		Crypto crypto2 = new Crypto(1641020400000L, "BTC", 46979.61);
		cryptoValuesTest.add(crypto2);
		Crypto crypto3 = new Crypto(1641031200000L, "BTC", 47143.98);
		cryptoValuesTest.add(crypto3);
		Crypto crypto4 = new Crypto(1641034800000L, "BTC", 46871.09);
		cryptoValuesTest.add(crypto4);
	}

    @Test
	public void findxMaxValue() {
		assertEquals(47143.98, cryptoStatisticsService.findMaxValue(cryptoValuesTest));
	}

	@Test
	public void findMinValue() {
		assertEquals(46813.21, cryptoStatisticsService.findMinValue(cryptoValuesTest));
	}

	@Test
	public void findNewestValue() {
		assertEquals(46871.09, cryptoStatisticsService.findNewestValue(cryptoValuesTest));
	}

	@Test
	public void findOldestValue() {
		assertEquals(46813.21, cryptoStatisticsService.findOldestValue(cryptoValuesTest));
	}

	@Test
	public void calculateNormalizedRange() {
		assertEquals(47142.98, cryptoStatisticsService.calculateNormalizedRange(cryptoValuesTest));
	}

	@Test
	public void calculateNormRangeForSpecificDay() throws NoCryptoValuesException {
		LocalDate date = LocalDate.of(2022, 1, 1);
		assertEquals(47142.98, cryptoStatisticsService.calculateNormRangeForSpecificDay(cryptoValuesTest, date));
	}

    @Test
	public void calculateNormRangeForSpecificDayThrowsNoCryptoValuesException() {
		LocalDate date = LocalDate.of(2021, 1, 1);
		assertThrows(NoCryptoValuesException.class,
				() -> cryptoStatisticsService.calculateNormRangeForSpecificDay(cryptoValuesTest, date));
	}
    
}
