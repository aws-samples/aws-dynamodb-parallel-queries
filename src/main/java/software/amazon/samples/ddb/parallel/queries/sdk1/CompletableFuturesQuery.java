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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import lombok.NonNull;
import software.amazon.samples.ddb.parallel.queries.Config;
import software.amazon.samples.ddb.parallel.queries.QueryUtils;

/**
 * AWS Blog post: Query Orders using completable futures
 * 
 * @author zorani
 *
 */
public class CompletableFuturesQuery {

	private OrdersDbUtils ordersDbUtils = null;

	/**
	 * C-tor
	 * 
	 * @param dynamoDB
	 * @param dbMapper
	 */
	public CompletableFuturesQuery(@NonNull final DynamoDBMapper dbMapper) {
			
		this.ordersDbUtils = new OrdersDbUtils(dbMapper);
	}
	
	/**
	 * List orders using CF and 12 order-date prefixes
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersUsingCategoryAndDates12QueryWithCompletableFuture(final String category) {

		if (StringUtils.isEmpty(category)) return null;
		
		// this changes depending on the sub-queries we plan to run (here: 12)
		ForkJoinPool executorService = new ForkJoinPool(12);

		// this is place where we create date prefixes, either monthly (12), 4 per month (48), 
		// or daily splits (365/366)
		List<String> yearMonthsList = QueryUtils.generate12YearMonthDatePrefixes(Config.YEAR);

		// invoke async queries and create futures
		List<CompletableFuture<List<Order>>> listFutures = yearMonthsList.stream()
				.map(x -> CompletableFuture.supplyAsync(() -> ordersDbUtils.listOrdersByCategoryAndOrderDate(category, x), executorService))
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		// wait for the completion and retrieve final list of Orders as a list
		List<Order> list = listFutures.stream()
				.map(CompletableFuture::join)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		return list;
	}

	/**
	 * List orders using CF and 48 date prefixes
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersUsingCategoryAndDates48QueryWithCompletableFuture(final String category) {

		if (StringUtils.isEmpty(category)) return null;
		
		ForkJoinPool executorService = new ForkJoinPool(48);
		List<String> yearMonthsList = QueryUtils.generate48YearMonthDatePrefixes(Config.YEAR);

		List<CompletableFuture<List<Order>>> listFutures = yearMonthsList.stream()
				.map(x -> CompletableFuture.supplyAsync(() -> ordersDbUtils.listOrdersByCategoryAndOrderDate(category, x), executorService))
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		List<Order> list = listFutures.stream()
				.map(CompletableFuture::join)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		return list;
	}

	/**
	 * List orders using CF and all date prefixes
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersUsingCategoryAndDates365QueryWithCompletableFuture(@NonNull final String category) {

		if (StringUtils.isEmpty(category)) return null;
		
		ForkJoinPool executorService = new ForkJoinPool(365);
		List<String> yearMonthsList = QueryUtils.generateDailyOrdersDatePrefixes(Config.YEAR);

		List<CompletableFuture<List<Order>>> listFutures = yearMonthsList.stream()
				.map(x -> CompletableFuture.supplyAsync(() -> ordersDbUtils.listOrdersByCategoryAndOrderDate(category, x), executorService))
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		List<Order> list = listFutures.stream()
				.map(CompletableFuture::join)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		return list;
	}

	/**
	 * List Orders using CF
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersUsingCategoryAndQuerySlots64WithCompletableFuture(@NonNull final String category) {

		if (StringUtils.isEmpty(category)) return null;
		
		ForkJoinPool executorService = new ForkJoinPool(64);
		List<Integer> querySlotValuesList = QueryUtils.create64QuerySlotsList();

		List<CompletableFuture<List<Order>>> listFutures = querySlotValuesList.stream()
				.map(x -> CompletableFuture.supplyAsync(() -> ordersDbUtils.listOrdersByCategoryAndQuerySlot(category, x), executorService))
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		List<Order> list = listFutures.stream()
				.map(CompletableFuture::join)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		return list;
	}
	
	/**
	 * List Orders using CF
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersUsingCategoryAndQuerySlots128WithCompletableFuture(@NonNull final String category) {

		if (StringUtils.isEmpty(category)) return null;
		
		ForkJoinPool executorService = new ForkJoinPool(128);
		List<Integer> querySlotValuesList = QueryUtils.create128QuerySlotsList();

		List<CompletableFuture<List<Order>>> listFutures = querySlotValuesList.stream()
				.map(x -> CompletableFuture.supplyAsync(() -> ordersDbUtils.listOrdersByCategoryAndQuerySlot(category, x), executorService))
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		List<Order> list = listFutures.stream()
				.map(CompletableFuture::join)
				.flatMap(Collection::stream)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		return list;
	}	
}