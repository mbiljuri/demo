package com.example.demo;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.context.WebApplicationContext;

import com.example.demo.api.service.CryptoService;
import com.example.demo.api.service.CryptoStatisticsService;
import com.example.demo.exceptions.CryptoNotSupportedException;
import com.example.demo.exceptions.NoCryptoValuesException;
import com.example.demo.model.Crypto;
import com.example.demo.model.CryptoStatistics;
import com.opencsv.CSVWriter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebAppConfiguration
class DemoApplicationTests {

	@Autowired
	private CryptoStatisticsService cryptoStatisticsService;
	@Autowired
	private CryptoService cryptoService;

	private MockMvc mockMvc;
	@Autowired
	private WebApplicationContext webContext;

	private List<Crypto> cryptoValuesTest;
	private static Path resourceDirectory;

	@BeforeAll
	public static void initCsvFiles() throws IOException {
		resourceDirectory = Paths.get("src", "test", "resources");
		Files.createDirectories(resourceDirectory);
		Path csvBTC = Paths.get(resourceDirectory.toAbsolutePath() + "/BTC_values.csv");
		Files.createFile(csvBTC);
		Path csvETH = Paths.get(resourceDirectory.toAbsolutePath() + "/ETH_values.csv");
		Files.createFile(csvETH);
		Path csvXRP = Paths.get(resourceDirectory.toAbsolutePath() + "/XRP_values.csv");
		Files.createFile(csvXRP);

		FileWriter fileBTC = new FileWriter(csvBTC.toFile());
		FileWriter fileETH = new FileWriter(csvETH.toFile());
		FileWriter fileXRP = new FileWriter(csvXRP.toFile());
		// create CSVWriter object filewriter object as parameter
		CSVWriter writer1 = new CSVWriter(fileBTC);
		CSVWriter writer2 = new CSVWriter(fileETH);
		CSVWriter writer3 = new CSVWriter(fileXRP);

		// create a List which contains String array
		List<String[]> data1 = new ArrayList<String[]>();
		data1.add(new String[] { "timestamp", "symbol", "price" });
		data1.add(new String[] { "1641009600000", "BTC", "46813.21" });
		data1.add(new String[] { "1641020400000", "BTC", "46979.61" });
		data1.add(new String[] { "1641031200000", "BTC", "47143.98" });
		data1.add(new String[] { "1641034800000", "BTC", "46871.09" });
		List<String[]> data2 = new ArrayList<String[]>();
		data2.add(new String[] { "timestamp", "symbol", "price" });
		data2.add(new String[] { "1641024000000", "ETH", "3715.32" });
		data2.add(new String[] { "1641031200000", "ETH", "3718.67" });
		data2.add(new String[] { "1641049200000", "ETH", "3697.04" });
		data2.add(new String[] { "1641056400000", "ETH", "3727.61" });
		List<String[]> data3 = new ArrayList<String[]>();
		data3.add(new String[] { "timestamp", "symbol", "price" });
		data3.add(new String[] { "1640995200000", "XRP", "0.8298" });
		data3.add(new String[] { "1641016800000", "XRP", "0.842" });
		data3.add(new String[] { "1641070800000", "XRP", "0.8458" });
		data3.add(new String[] { "1641099600000", "XRP", "0.8391" });

		writer1.writeAll(data1);
		writer2.writeAll(data2);
		writer3.writeAll(data3);

		// closing writer connection
		writer1.close();
		writer2.close();
		writer3.close();
	}

	@AfterAll
	public static void clearResources() throws IOException {
		FileSystemUtils.deleteRecursively(resourceDirectory);
	}

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

	@BeforeEach
	public void initCryptoLocation() {
		ReflectionTestUtils.setField(cryptoService, "cryptoLocation",
				resourceDirectory.toAbsolutePath().toString() + "\\");
	}

	@BeforeEach
	public void setupMockMvc() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webContext).build();
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

	@Test
	public void calculateCryptoStatistics() throws CryptoNotSupportedException, NoCryptoValuesException {
		LocalDate startDate = LocalDate.of(2022, 1, 1);
		LocalDate endDate = LocalDate.of(2022, 1, 31);
		CryptoStatistics cryptoStatistics = new CryptoStatistics(46813.21, 46871.09, 46813.21, 47143.98);
		assertEquals(cryptoStatistics, cryptoService.calculateCryptoStatistics("BTC", startDate, endDate));
	}

	@Test
	public void calculateCryptoStatisticsThrowsCryptoNotSupportedException() {
		LocalDate startDate = LocalDate.of(2022, 1, 1);
		LocalDate endDate = LocalDate.of(2022, 1, 31);
		assertThrows(CryptoNotSupportedException.class,
				() -> cryptoService.calculateCryptoStatistics("BNB", startDate, endDate));
	}

	@Test
	public void calculateCryptoStatisticsThrowsNoCryptoValuesException() {
		LocalDate startDate = LocalDate.of(2021, 1, 1);
		LocalDate endDate = LocalDate.of(2021, 1, 31);
		assertThrows(NoCryptoValuesException.class,
				() -> cryptoService.calculateCryptoStatistics("BTC", startDate, endDate));
	}

	@Test
	public void sortCryptosByNormRange() {
		List<String> sortedCryptos = Arrays.asList("BTC", "ETH", "XRP");
		assertEquals(sortedCryptos, cryptoService.sortCryptosByNormRange());
	}

	@Test
	public void getCryptoWithHighestNormRangeForDate() throws NoCryptoValuesException {
		LocalDate date = LocalDate.of(2022, 1, 1);
		assertEquals("BTC", cryptoService.getCryptoWithHighestNormRangeForDate(date));
	}

	@Test
	public void getCryptoWithHighestNormRangeForDateThrowsNoCryptoValuesException() {
		LocalDate date = LocalDate.of(2021, 1, 1);
		assertThrows(NoCryptoValuesException.class, () -> cryptoService.getCryptoWithHighestNormRangeForDate(date));
	}

	@Test
	public void getCryptoStatistics() throws Exception {
		mockMvc.perform(get("/api/crypto/statistics/btc").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$.oldestValue").value(46813.21))
				.andExpect(jsonPath("$.newestValue").value(46871.09))
				.andExpect(jsonPath("$.minValue").value(46813.21))
				.andExpect(jsonPath("$.maxValue").value(47143.98));
	}

	@Test
	public void getCryptos() throws Exception {
		mockMvc.perform(get("/api/cryptos/normrange/sorted").accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(jsonPath("$[0]").value("BTC"))
				.andExpect(jsonPath("$[1]").value("ETH"))
				.andExpect(jsonPath("$[2]").value("XRP"));

	}

	@Test
	public void getCryptoWithHighestNormRangeForDateRestCall() throws Exception {
		mockMvc.perform(
				get("/api/cryptos/normrange/highest").accept(MediaType.APPLICATION_JSON).param("date", "01.01.2022"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(content().string("BTC"));
	}

}
