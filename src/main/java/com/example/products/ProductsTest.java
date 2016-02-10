package com.example.products;

import static org.junit.Assert.*;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

/**
 * 
 * @author Krzysztof Urbanek
 *
 */
public class ProductsTest {

	@Test
	public void testDoWork() {
		Reader currenciesReader = new StringReader(
				"\t currency,ratio\n" //testing with additional white space characters
				+ "GBP,2.4\n"
				+ "EU,2.1\n"
				+ "PLN,1\n");
		
		Reader matchingsReader = new StringReader(
				"matching_id,top_priced_count\n"
				+ "1,2\n"
				+ "2,2\n"
				+ "3,3\n");
		
		Reader dataReader = new StringReader(
				"id,price,currency,quantity,matching_id\n"
				+ "1,1000,GBP,2,3\n"
				+ "2,1050,EU,1,1\n"
				+ "3,2000,PLN,1,1\n"
				+ "4,1750,EU,2,2\n"
				+ "5,1400,EU,4,3\n"
				+ "6,7000,PLN,3,2\n"
				+ "7,630,GBP,5,3\n"
				+ "8,4000,EU,1,3\n"
				+ "9,1400,GBP,3,1\n");
		
		String result = Products.doWork(currenciesReader, matchingsReader, dataReader);
		
		assertTrue(result.equals(
				"matching_id,total_price,avg_price,currency,ignored_products_count\n"
				+ "1,12285,6142.5,PLN,1\n"
				+ "2,28350,14175,PLN,0\n"
				+ "3,27720,9240,PLN,1\n"));
	}
	
	

}
