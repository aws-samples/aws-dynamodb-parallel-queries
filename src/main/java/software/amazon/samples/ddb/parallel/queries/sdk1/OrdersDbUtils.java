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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;

import lombok.NonNull;
import software.amazon.samples.ddb.parallel.queries.Config;
import software.amazon.samples.ddb.parallel.queries.QueryUtils;

/**
 * Utilities for read/write Orders records to and from DynamoDB table
 * 
 * @author zorani
 *
 */
public class OrdersDbUtils {
	
	private DynamoDBMapper dbMapper = null;
	
	/**
	 * C-tor
	 * 
	 * @param dbMapper
	 */
	public OrdersDbUtils(@NonNull final DynamoDBMapper dbMapper) {
		
		this.dbMapper = dbMapper;
	}
		
	/**
	 * List all orders
	 * 
	 * @param segment
	 * @return
	 */
	public List<Order> listAllOrdersUsingParallelScan(int segment) {

		return listAllOrdersUsingParallelScan(null, segment);
	}

	/**
	 * List all Orders using DynamoDB Scan operation (leveraging multi-threading via segments)
	 * 
	 * @param category
	 * @param segments
	 * @return
	 */
	public List<Order> listAllOrdersUsingParallelScan(String category, int segments) {

		DynamoDBMapperConfig mapperConfig = QueryUtils.getDefaultMapperConfig(Config.DDB_TABLE_NAME);

		DynamoDBScanExpression scanExpr = new DynamoDBScanExpression();
		scanExpr.withConsistentRead(false);

		if (StringUtils.isNotEmpty(category)) {

			Map<String,String> names = new HashMap<>();
			Map<String,AttributeValue> values = new HashMap<>();

			names.put("#cat", "category");
			values.put(":cat", new AttributeValue().withS(category.trim().toUpperCase()));

			scanExpr.setExpressionAttributeNames(names);
			scanExpr.setExpressionAttributeValues(values);
			scanExpr.setFilterExpression("#cat = :cat");
		}

		return dbMapper.parallelScan(Order.class, scanExpr, segments, mapperConfig);
	}

	/**
	 * Retrieve the Order record by id
	 * 
	 * @param orderId
	 * @return
	 */
	public Order getOrder(@NonNull String orderId) {

		if (StringUtils.isEmpty(orderId)) return null;

		DynamoDBMapperConfig mapperConfig = QueryUtils.getDefaultMapperConfig(Config.DDB_TABLE_NAME);

		return dbMapper.load(Order.class, orderId, mapperConfig);
	}
	
	/**
	 * Get the list of Orders by category and the order-date prefix
	 * 
	 * @param category
	 * @param orderDate
	 * @return
	 */
	public List<Order> listOrdersByCategoryAndOrderDate(@NonNull final String category, final String orderDate) {

		if (StringUtils.isEmpty(category)) return new ArrayList<>();

		DynamoDBMapperConfig mapperConfig = QueryUtils.getDefaultMapperConfig(Config.DDB_TABLE_NAME);

		String indexName = null;
		String keyCondExpr = null;
		Map<String,String> names = new HashMap<>();
		Map<String,AttributeValue> values = new HashMap<>();

		names.put("#cat", "category");
		values.put(":cat", new AttributeValue().withS(category.trim().toUpperCase()));
		keyCondExpr = "#cat = :cat";
		indexName = "category-order-date-index";

		if (StringUtils.isNotEmpty(orderDate)) {

			names.put("#od", "order-date");
			values.put(":od", new AttributeValue().withS(orderDate.trim()));
			keyCondExpr += " AND begins_with(#od, :od)";
		}

		DynamoDBQueryExpression<Order> queryExpr = new DynamoDBQueryExpression<Order>()
				.withKeyConditionExpression(keyCondExpr)
				.withIndexName(indexName)
				.withExpressionAttributeNames(names)
				.withExpressionAttributeValues(values)
				.withScanIndexForward(false)
				.withConsistentRead(false);

		return this.dbMapper.query(Order.class, queryExpr, mapperConfig);
	}		

	/**
	 * Get the list of Orders by category and a query-slot attribute value
	 * 
	 * @param category
	 * @param querySlot
	 * @return
	 */
	public List<Order> listOrdersByCategoryAndQuerySlot(String category, int querySlot) {

		if (StringUtils.isEmpty(category)) return new ArrayList<>();
		if (querySlot < 0 || querySlot >= 64) return new ArrayList<>();

		DynamoDBMapperConfig mapperConfig = QueryUtils.getDefaultMapperConfig(Config.DDB_TABLE_NAME);

		String indexName = null;
		String keyCondExpr = null;
		Map<String,String> names = new HashMap<>();
		Map<String,AttributeValue> values = new HashMap<>();

		names.put("#cat", "category");
		values.put(":cat", new AttributeValue().withS(category.trim().toUpperCase()));
		keyCondExpr = "#cat = :cat";
		indexName = "category-query-slot-mod64-index";

		names.put("#qs", "query-slot-mod64");
		values.put(":qs", new AttributeValue().withN(String.valueOf(querySlot)));
		keyCondExpr += " AND #qs = :qs";

		DynamoDBQueryExpression<Order> queryExpr = new DynamoDBQueryExpression<Order>()
				.withKeyConditionExpression(keyCondExpr)
				.withIndexName(indexName)
				.withExpressionAttributeNames(names)
				.withExpressionAttributeValues(values)
				.withScanIndexForward(false)
				.withConsistentRead(false);

		return this.dbMapper.query(Order.class, queryExpr, mapperConfig);
	}		
}