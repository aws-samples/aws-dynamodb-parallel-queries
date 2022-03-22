/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
 * SPDX-License-Identifier: MIT-0
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package software.amazon.samples.ddb.parallel.queries.sdk1;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.SaveBehavior;

import lombok.NonNull;
import software.amazon.samples.ddb.parallel.queries.Config;
import software.amazon.samples.ddb.parallel.queries.QueryUtils;

/**
 * AWS Blog post: Orders data generation
 * 
 * @author zorani
 *
 */
public class LoadTestData {

	private DynamoDBMapper dbMapper = null;

	private static final Logger LOG = LoggerFactory.getLogger(LoadTestData.class);

	public LoadTestData() {}
			
	/**
	 * Generate and load test Orders data in DynamoDB table
	 */
	public void generateTestOrdersData(int totalRecordsToGenerate, int year) {

		AmazonDynamoDB dynamoDB = Utils.init();
		dbMapper = new DynamoDBMapper(dynamoDB);
		
		if (totalRecordsToGenerate < 0 || totalRecordsToGenerate > 500_000) totalRecordsToGenerate = 100;
		if (year < 2015 || year > 2030) year = 2020;

		List<String> products = Arrays.asList("A", "B", "C", "D");
		List<String> payments = Arrays.asList("CREDIT", "DEBIT", "CASH");
		List<String> states = Arrays.asList("WA", "TX", "CA", "NY", "MA", "FL", "PA", "NH", "NM");

		Random r = new Random();
		List<Order> orders = new ArrayList<>();

		long start = System.currentTimeMillis();

		for (int i = 1; i <= totalRecordsToGenerate; i++) {

			int dayOfYear = 1 + r.nextInt(365);
			LocalDate ld = LocalDate.ofYearDay(year, dayOfYear);
			if (ld.getYear() > year) ld = ld.minusDays(7);

			double price = (1 + r.nextInt(16)) + (1 + r.nextInt(99))/100.0;

			int productID = 1 + r.nextInt(99);
			String category = null;
			if (productID < 30) category = "ELECTRONICS";
			else if (productID < 60) category = "HOME";
			else if (productID < 80) category = "SPORT";
			else category = "GARDEN";

			String state = states.get(r.nextInt(states.size()));

			Order order = Order.builder()
					.sku(String.format("%s-%03d", products.get(r.nextInt(products.size())), productID))
					.orderDate(ld.toString())
					.paymentType(payments.get(r.nextInt(payments.size())))
					.qty(1 + r.nextInt(10))
					.pricePerUnit(price)
					.category(category)
					.country("USA")
					.ckCountryState("USA#" + state)
					.state(state)
					.querySlotMod64(i % 64)
					.querySlotMod128(i % 128)
					.comment("Some comment here ...")
					.build();

			orders.add(order);
		}

		// persist Orders to DynamoDB table		
		orders.parallelStream()
				.forEach(this::createOrder);

		long end = System.currentTimeMillis();

		LOG.info(" Generation of data is completed .. total Orders: {}, time: {} msec%n", 
															totalRecordsToGenerate, (end - start)/1000);
	}

	/**
	 * Saving Order object - overwrite previous values
	 * 
	 * @param order
	 */
	public Order createOrder(@NonNull Order order) {

		DynamoDBMapperConfig mapperConfig = QueryUtils.getDefaultMapperConfigBuilder(Config.DDB_TABLE_NAME)
				.withSaveBehavior(SaveBehavior.CLOBBER)
				.build();

		int attemptCount = 1;

		do {

			try {
				dbMapper.save(order, mapperConfig);
				return order;
				
			} catch (Exception e) {
				e.printStackTrace();
				LOG.warn(e.getMessage());

				try {
					Thread.sleep(100 * attemptCount);
				} catch (InterruptedException e1) {
				}
			}

		} while (++attemptCount <= 10);

		LOG.error("createOrder()::Saving DrivingSession has failed!");
		return order;
	}

	/**
	 * Loading Orders data main()
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		LoadTestData loadData = new LoadTestData();
		loadData.generateTestOrdersData(100, 2020);
	}
}