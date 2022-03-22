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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;

import lombok.NonNull;
import software.amazon.samples.ddb.parallel.queries.Config;
import software.amazon.samples.ddb.parallel.queries.QueryUtils;

/**
 * AWS Blog post: Query Orders using parallel streams
 * 
 * @author zorani
 *
 */
public class ParallelStreamQuery {

	private DynamoDBMapper dbMapper = null;
	private OrdersDbUtils ordersDbUtils = null;

	/**
	 * C-tor
	 * 
	 * @param dbMapper
	 */
	public ParallelStreamQuery(@NonNull final DynamoDBMapper dbMapper) {
			
		this.dbMapper = dbMapper;
		
		this.ordersDbUtils = new OrdersDbUtils(this.dbMapper);
	}
	
	/**
	 * Get list of orders by Category, using Java 8+ parallel streams and 12 order-date monthly prefixes
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersByCategoryAndDates12UsingParallelStream(final String category) {

		if (StringUtils.isEmpty(category)) return null;

		return executeMultipleOrdersQueriesUsingParallelStream(category, 
							QueryUtils.generate12YearMonthDatePrefixes(Config.YEAR));
	}

	/**
	 * Get list of orders by Category, using Java 8+ parallel streams and 48 order-date prefixes
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersByCategoryAndDates48UsingParallelStream(final String category) {

		if (StringUtils.isEmpty(category)) return null;

		return executeMultipleOrdersQueriesUsingParallelStream(category, 
							QueryUtils.generate48YearMonthDatePrefixes(Config.YEAR));
	}

	/**
	 * Get list of orders by Category, using Java 8+ parallel streams and 365/366 order-date prefixes
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersByCategoryAndDates365UsingParallelStream(final String category) {

		if (StringUtils.isEmpty(category)) return null;

		return executeMultipleOrdersQueriesUsingParallelStream(category, 
								QueryUtils.generateDailyOrdersDatePrefixes(Config.YEAR));
	}
	
	/**
	 * Call parallel stream to execute all sub-queries (uses Common Fork Join Pool)
	 * 
	 * @param category
	 * @param orderDatePrefixList
	 * @return
	 */
	private List<Order> executeMultipleOrdersQueriesUsingParallelStream(final String category, final List<String> orderDatePrefixList) {

		if (StringUtils.isEmpty(category)) return null;
		
		List<Order> orders = orderDatePrefixList.parallelStream()
				.map(x -> ordersDbUtils.listOrdersByCategoryAndOrderDate(category, x))
				.flatMap(Collection::stream) // flatten results into a single stream of Orders
				.unordered() // do not insist to preserve input order (might be faster)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		return orders;
	}

	/**
	 * Get list of orders by Category, using Java 8 parallel streams and the Query Slot 64
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersByCategoryAndQuerySlot64UsingParallelStream(final String category) {

		if (StringUtils.isEmpty(category)) return null;

		return queriesUsingQuerySlotsAndParallelStream(category, QueryUtils.create64QuerySlotsList());
	}
	
	/**
	 * Get list of orders by Category, using Java 8 parallel streams and the Query Slot 128
	 * 
	 * @param category
	 * @return
	 */
	public List<Order> getOrdersByCategoryAndQuerySlot128UsingParallelStream(final String category) {

		if (StringUtils.isEmpty(category)) return null;

		return queriesUsingQuerySlotsAndParallelStream(category, QueryUtils.create128QuerySlotsList());
	}
	
	/**
	 * Execute query slot sub-queries using parallel stream
	 * 
	 * @param category
	 * @param querySlotValuesList
	 * @return
	 */
	private List<Order> queriesUsingQuerySlotsAndParallelStream(final String category, final List<Integer> querySlotValuesList) {
		
		List<Order> orders = querySlotValuesList.parallelStream()
				.map(x -> ordersDbUtils.listOrdersByCategoryAndQuerySlot(category, x))
				.flatMap(Collection::stream) // flatten results into a single stream of Orders
				.unordered() // do not insist to preserve input order (might be faster)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));
		
		return orders;
	}
}