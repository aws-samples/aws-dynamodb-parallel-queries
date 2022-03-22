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

import java.util.List;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

/**
 * Runs all query methods
 * 
 * @author zorani
 *
 */
public class TestCases {

	/**
	 * Main test cases code
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		AmazonDynamoDB dynamoDB = Utils.init();  
		DynamoDBMapper dbMapper = new DynamoDBMapper(dynamoDB);
		OrdersDbUtils ordersDbUtils = new OrdersDbUtils(dbMapper);
		
		List<Order> allOrders = ordersDbUtils.listAllOrdersUsingParallelScan(128);
		System.out.printf(">> Total orders in DynamoDB Orders table: [%d], using built in parallel scan%n", allOrders.size());
		
		ParallelStreamQuery ps = new ParallelStreamQuery(dbMapper);
		List<Order> orders = ps.getOrdersByCategoryAndDates12UsingParallelStream("SPORT");
		System.out.printf(">> Total orders: [%d], using 12 date-orders prefixes%n", orders.size());
		
		orders = ps.getOrdersByCategoryAndDates48UsingParallelStream("SPORT");
		System.out.printf(">> Total orders: [%d], using 48 date-orders prefixes%n", orders.size());

		orders = ps.getOrdersByCategoryAndDates365UsingParallelStream("SPORT");
		System.out.printf(">> Total orders: [%d], using 365 date-orders prefixes%n", orders.size());
		
		orders = ps.getOrdersByCategoryAndQuerySlot64UsingParallelStream("SPORT");
		System.out.printf(">> Total orders: [%d], using 64 query slots prefixes%n", orders.size());
		
		orders = ps.getOrdersByCategoryAndQuerySlot128UsingParallelStream("SPORT");
		System.out.printf(">> Total orders: [%d], using 128 query slots prefixes%n", orders.size());
		
		// --- CF
		
		CompletableFuturesQuery cf = new CompletableFuturesQuery(dbMapper);
		orders = cf.getOrdersUsingCategoryAndDates12QueryWithCompletableFuture("SPORT");
		System.out.printf(">> Total orders: [%d], using CF and 12 date-orders prefixes%n", orders.size());

		orders = cf.getOrdersUsingCategoryAndDates48QueryWithCompletableFuture("SPORT");
		System.out.printf(">> Total orders: [%d], using CF and 48 date-orders prefixes%n", orders.size());

		orders = cf.getOrdersUsingCategoryAndDates365QueryWithCompletableFuture("SPORT");
		System.out.printf(">> Total orders: [%d], using CF and 365 date-orders prefixes%n", orders.size());

		orders = cf.getOrdersUsingCategoryAndQuerySlots64WithCompletableFuture("SPORT");
		System.out.printf(">> Total orders: [%d], using CF and 64 query slots prefixes%n", orders.size());
		
		orders = cf.getOrdersUsingCategoryAndQuerySlots128WithCompletableFuture("SPORT");
		System.out.printf(">> Total orders: [%d], using CF and 128 query slots prefixes%n", orders.size());
	}
}