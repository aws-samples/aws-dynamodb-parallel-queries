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
package software.amazon.samples.ddb.parallel.queries;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import lombok.NonNull;
import software.amazon.samples.ddb.parallel.queries.sdk1.CompletableFuturesQuery;
import software.amazon.samples.ddb.parallel.queries.sdk1.Order;
import software.amazon.samples.ddb.parallel.queries.sdk1.OrdersDbUtils;
import software.amazon.samples.ddb.parallel.queries.sdk1.ParallelStreamQuery;
import software.amazon.samples.ddb.parallel.queries.sdk1.Utils;

/**
 * Run benchmarks class
 *  
 * @author zorani
 *
 */
public class RunBenchmarks {

	private OrdersDbUtils ordersDbUtils = null;
	
	private ParallelStreamQuery ps = null;
	private CompletableFuturesQuery cf = null;

	private static final Logger LOG = LoggerFactory.getLogger(RunBenchmarks.class);

	/**
	 * C-tor
	 * 
	 * @param dynamoDB
	 * @param dbMapper
	 */
	public RunBenchmarks(@NonNull final DynamoDBMapper dbMapper) {
			
		this.ordersDbUtils = new OrdersDbUtils(dbMapper);
		
		this.ps = new ParallelStreamQuery(dbMapper);
		this.cf = new CompletableFuturesQuery(dbMapper);
	}
	
	public void run() {
		
		LOG.info("Run benchmark tests ...");
		
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println("Number of cores: " + cores);
		System.out.println("Number of threads in common ForkJoinPool: " + ForkJoinPool.commonPool().getParallelism());

		System.out.printf("%n%n");
		System.out.printf("  Approach                                        Orders     Time (ms) %n");
		System.out.printf("+-----------------------------------------------+----------+----------+%n");

		System.gc();

		long start = System.currentTimeMillis();
		List<Order> orders = ordersDbUtils.listAllOrdersUsingParallelScan(16);
		System.out.printf("| Scan (parallel) entire table - 16 segments    | %8d | %8d |%n", 
				orders.size(), System.currentTimeMillis() - start);

		orders = null;
		System.gc();

		start = System.currentTimeMillis();
		orders = ordersDbUtils.listAllOrdersUsingParallelScan("SPORT", 16);
		System.out.printf("| Scan (parallel) entire table; SPORT category  | %8d | %8d |%n", 
				orders.size(), System.currentTimeMillis() - start);

		orders = null;
		System.gc();

		long[] test1 = new long[10]; long[] test2 = new long[10]; long[] test3 = new long[10];
		long[] test4 = new long[10]; long[] test5 = new long[10]; long[] test6 = new long[10];
		long[] test7 = new long[10]; long[] test8 = new long[10]; long[] test9 = new long[10];
		
		long[] totalTime = new long[10];
		
		for(int i = 0; i < 10; ++i) {

			System.gc();

			System.out.printf("%n Test: [%2d]%n", i + 1);
			System.out.printf("  Approach                                        Orders     Time (ms)  Avg (ms)  %n");
			System.out.printf("+-----------------------------------------------+----------+----------+----------+%n");

			//System.out.printf("%n%n");
			start = System.currentTimeMillis();
			orders = ordersDbUtils.listOrdersByCategoryAndOrderDate("SPORT", "2020");
			test1[i] = System.currentTimeMillis() - start;
			totalTime[1] += test1[i];
			System.out.printf("| Traditional query (single thread) approach    | %8d | %8d | %8d |%n", 
					orders.size(), test1[i], totalTime[1] / (i+1));

			orders = null;
			System.gc();

			start = System.currentTimeMillis();
			List<Order> orders2 = ps.getOrdersByCategoryAndDates12UsingParallelStream("SPORT");
			test2[i] = System.currentTimeMillis() - start;
			totalTime[2] += test2[i];
			System.out.printf("| Category/Dates  12 parallel streams           | %8d | %8d | %8d |%n", 
					orders2.size(), test2[i], totalTime[2] / (i+1));

			orders2 = null;
			System.gc();

			start = System.currentTimeMillis();
			List<Order> orders3 = ps.getOrdersByCategoryAndDates48UsingParallelStream("SPORT");
			test3[i] = System.currentTimeMillis() - start;
			totalTime[3] += test3[i];
			System.out.printf("| Category/Dates  48 parallel streams           | %8d | %8d | %8d |%n", 
					orders3.size(), test3[i], totalTime[3] / (i+1));

			orders3 = null;
			System.gc();

			start = System.currentTimeMillis();
			List<Order> orders4 = ps.getOrdersByCategoryAndDates365UsingParallelStream("SPORT");
			test4[i] = System.currentTimeMillis() - start;
			totalTime[4] += test4[i];
			System.out.printf("| Category/Dates 365 parallel streams           | %8d | %8d | %8d |%n", 
					orders4.size(), test4[i], totalTime[4] / (i+1));

			orders4 = null;
			System.gc();

			start = System.currentTimeMillis();
			List<Order> orders5 = ps.getOrdersByCategoryAndQuerySlot64UsingParallelStream("SPORT");
			test5[i] = System.currentTimeMillis() - start;
			totalTime[5] += test5[i];
			System.out.printf("| Query slost Mod 64 parallel streams           | %8d | %8d | %8d |%n", 
					orders5.size(), test5[i], totalTime[5] / (i+1));

			orders5 = null;
			System.gc();

			start = System.currentTimeMillis();
			List<Order> orders6 = cf.getOrdersUsingCategoryAndDates12QueryWithCompletableFuture("SPORT");
			test6[i] = System.currentTimeMillis() - start;
			totalTime[6] += test6[i];
			System.out.printf("| Category/Dates  12 Compleatable Futures       | %8d | %8d | %8d |%n", 
					orders6.size(), test6[i], totalTime[6] / (i+1));

			orders6 = null;
			System.gc();

			start = System.currentTimeMillis();
			List<Order> orders7 = cf.getOrdersUsingCategoryAndDates48QueryWithCompletableFuture("SPORT");
			test7[i] = System.currentTimeMillis() - start;
			totalTime[7] += test7[i];
			System.out.printf("| Category/Dates  48 Compleatable Futures       | %8d | %8d | %8d |%n", 
					orders7.size(), test7[i], totalTime[7] / (i+1));

			orders7 = null;
			System.gc();

			start = System.currentTimeMillis();
			List<Order> orders8 = cf.getOrdersUsingCategoryAndDates365QueryWithCompletableFuture("SPORT");
			test8[i] = System.currentTimeMillis() - start;
			totalTime[8] += test8[i];
			System.out.printf("| Category/Dates 365 Compleatable Futures       | %8d | %8d | %8d |%n", 
					orders8.size(), test8[i], totalTime[8] / (i+1));

			orders8 = null;
			System.gc();

			start = System.currentTimeMillis();
			List<Order> orders9 = cf.getOrdersUsingCategoryAndQuerySlots64WithCompletableFuture("SPORT");
			test9[i] = System.currentTimeMillis() - start;
			totalTime[9] += test9[i];
			System.out.printf("| Query Slots Mod 64 Compleatable Futures       | %8d | %8d | %8d |%n", 
					orders9.size(), test9[i], totalTime[9] / (i+1));

			System.out.printf("+-----------------------------------------------+----------+----------+----------+%n");
			
			System.gc();
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// averages
		
		long max1 = Arrays.stream(test1).max().getAsLong();
		long avg1 = (totalTime[1] - max1) / 9;

		long max2 = Arrays.stream(test2).max().getAsLong();
		long avg2 = (totalTime[2] - max2) / 9;

		long max3 = Arrays.stream(test3).max().getAsLong();
		long avg3 = (totalTime[3] - max3) / 9;

		long max4 = Arrays.stream(test4).max().getAsLong();
		long avg4 = (totalTime[4] - max4) / 9;

		long max5 = Arrays.stream(test5).max().getAsLong();
		long avg5 = (totalTime[5] - max5) / 9;

		long max6 = Arrays.stream(test6).max().getAsLong();
		long avg6 = (totalTime[6] - max6) / 9;

		long max7 = Arrays.stream(test7).max().getAsLong();
		long avg7 = (totalTime[7] - max7) / 9;

		long max8 = Arrays.stream(test8).max().getAsLong();
		long avg8 = (totalTime[8] - max8) / 9;

		long max9 = Arrays.stream(test9).max().getAsLong();
		long avg9 = (totalTime[9] - max9) / 9;
		
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test1[i]); System.out.printf(" .. avg: %5d%n", avg1);
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test2[i]); System.out.printf(" .. avg: %5d%n", avg2);
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test3[i]); System.out.printf(" .. avg: %5d%n", avg3);
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test4[i]); System.out.printf(" .. avg: %5d%n", avg4);
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test5[i]); System.out.printf(" .. avg: %5d%n", avg5);
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test6[i]); System.out.printf(" .. avg: %5d%n", avg6);
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test7[i]); System.out.printf(" .. avg: %5d%n", avg7);
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test8[i]); System.out.printf(" .. avg: %5d%n", avg8);
		for(int i = 0; i < 10; ++i) System.out.printf("%6d |", test9[i]); System.out.printf(" .. avg: %5d%n", avg9);		
	}
	
	/**
	 * Main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		AmazonDynamoDB dynamoDB = Utils.init();  
		DynamoDBMapper dbMapper = new DynamoDBMapper(dynamoDB);
		
		RunBenchmarks benchmarks = new RunBenchmarks(dbMapper);
		benchmarks.run();
	}
}