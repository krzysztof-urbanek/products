package com.example.products;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 
 * @author Krzysztof Urbanek
 *
 */
public class Products {
	private static final String CURRENCIES_CSV = "currencies.csv";
	private static final String CURRENCY_CSV_FIRST_LINE = "currency,ratio";
	private static final String CURRENCY_CSV_LINE_PATTERN = "[A-Za-z]+,\\d+.?\\d*";
	private static final String MATCHINGS_CSV = "matchings.csv";
	private static final String MATCHING_CSV_FIRST_LINE = "matching_id,top_priced_count";
	private static final String MATCHINGS_CSV_LINE_PATTERN = "\\d+,\\d+";
	private static final String DATA_CSV = "data.csv";
	private static final String DATA_CSV_FIRST_LINE = "id,price,currency,quantity,matching_id";
	private static final String DATA_CSV_LINE_PATTERN = "\\d+,\\d+.?\\d*,[A-Za-z]+,\\d+,\\d+";
	private static final String RESULT_CSV = "result.csv";
	private static final String RESULT_CSV_FIRST_LINE = "matching_id,total_price,avg_price,currency,ignored_products_count\n";
	private static final String POLISH_NOMINAL = "PLN";

	private static class Data {
		BigDecimal price;
		String currency;
		int quantity;
		int matchingId;
		
		public Data(BigDecimal price, String currency, int quantity, int matchingId) {
			this.price = price;
			this.currency = currency;
			this.quantity = quantity;
			this.matchingId = matchingId;
		}
	}
	
	private static class Result {
		int matchingId;
		BigDecimal totalPrice;
		BigDecimal avgPrice;
		String currency;
		int ignoredProductsCount;
		
		public Result(int matchingId, BigDecimal totalPrice, BigDecimal avgPrice, String currency, int ignoredProductsCount) {
			this.matchingId = matchingId;
			this.totalPrice = totalPrice;
			this.avgPrice = avgPrice;
			this.currency = currency;
			this.ignoredProductsCount = ignoredProductsCount;
		}
		
		@Override
		public String toString() {
			return String.valueOf(matchingId) + "," + totalPrice.stripTrailingZeros().toPlainString() + "," 
					+ avgPrice.stripTrailingZeros().toPlainString() + "," + currency + "," + String.valueOf(ignoredProductsCount) + "\n";
		}
	}
	
	public static void main(String[] args) throws IOException {
		Reader currenciesReader = new FileReader(CURRENCIES_CSV);
		Reader matchingsReader = new FileReader(MATCHINGS_CSV);
		Reader dataReader = new FileReader(DATA_CSV);
		
		String result = doWork(currenciesReader, matchingsReader, dataReader);
		
		File file = new File(RESULT_CSV);
		FileWriter fileWriter = new FileWriter(file);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		printWriter.print(result);
		fileWriter.flush();
		fileWriter.close();
	}
	
	public static String doWork(Reader currenciesReader, Reader matchingsReader, Reader dataReader) {
		Map<String,BigDecimal> currencies = new HashMap<>(); 
		Map<Integer,Integer> matchings = new HashMap<>();
		List<Data> dataList = new ArrayList<>();
		
		try(Scanner scanner = new Scanner(currenciesReader)) {
			//First line
			if(!scanner.hasNextLine()) throw new IllegalArgumentException();
			if(!removeWhiteCharacters(scanner.nextLine()).equals(CURRENCY_CSV_FIRST_LINE)) throw new IllegalArgumentException();

			//Other lines
			while(scanner.hasNextLine()) {
				String line = removeWhiteCharacters(scanner.nextLine());

				if(!line.matches(CURRENCY_CSV_LINE_PATTERN)) throw new IllegalArgumentException();
				String[] words = line.split(",");
				currencies.put(words[0], new BigDecimal(words[1]));
			}
		}
		
		try(Scanner scanner = new Scanner(matchingsReader)) {
			//First line
			if(!scanner.hasNextLine()) throw new IllegalArgumentException();
			if(!removeWhiteCharacters(scanner.nextLine()).equals(MATCHING_CSV_FIRST_LINE)) throw new IllegalArgumentException();
			
			//Other lines
			while(scanner.hasNextLine()) {
				String line = removeWhiteCharacters(scanner.nextLine());

				if(!line.matches(MATCHINGS_CSV_LINE_PATTERN)) throw new IllegalArgumentException();
				String[] words = line.split(",");
				matchings.put(new Integer(words[0]), new Integer(words[1]));
			}
		}
		
		try(Scanner scanner = new Scanner(dataReader)) {
			//First line
			if(!scanner.hasNextLine()) throw new IllegalArgumentException();
			if(!removeWhiteCharacters(scanner.nextLine()).equals(DATA_CSV_FIRST_LINE)) throw new IllegalArgumentException();
			
			//Other lines
			while(scanner.hasNextLine()) {
				String line = removeWhiteCharacters(scanner.nextLine());

				if(!line.matches(DATA_CSV_LINE_PATTERN)) throw new IllegalArgumentException();
				String[] words = line.split(",");
				dataList.add(new Data(new BigDecimal(words[1]), words[2], new Integer(words[3]), new Integer(words[4]))); //words[0] with id is unnecessary for the task
			}
		}

		String resultString = RESULT_CSV_FIRST_LINE;
		
		for(int i : matchings.keySet()) {
			List<Data> matchingData = new ArrayList<>();
			
			for(Data data : dataList) {
				if(data.matchingId == i) matchingData.add(data);
			}
			
			Collections.sort(matchingData, new Comparator<Data>() {
				@Override
				public int compare(Data o1, Data o2) {
					return -getValueInPLN(o1, currencies).compareTo(getValueInPLN(o2, currencies));
				}
			});
			
			BigDecimal sum = BigDecimal.ZERO;
			for(int j = 0; j < matchings.get(i); j++) {
				sum = sum.add(getValueInPLN(matchingData.get(j), currencies));
			}
			BigDecimal avg = sum.divide(new BigDecimal(matchings.get(i)));
			int ignoredCount = matchingData.size() - matchings.get(i);
			
			
			resultString = resultString + (new Result(i, sum, avg, POLISH_NOMINAL , ignoredCount)).toString();
		}
		
		return resultString;
	}
	
	private static BigDecimal getValueInPLN(Data data, Map<String,BigDecimal> currencies) {
		return data.price.multiply(new BigDecimal(data.quantity)).multiply(currencies.get(data.currency));
	}
	
	private static String removeWhiteCharacters(String s) {
		return s.replaceAll("\\s", "");
	}
}
