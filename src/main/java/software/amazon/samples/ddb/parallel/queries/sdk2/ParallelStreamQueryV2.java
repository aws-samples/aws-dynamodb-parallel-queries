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
package software.amazon.samples.ddb.parallel.queries.sdk2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.reactivex.Flowable;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbAsyncTable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedAsyncClient;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PagePublisher;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.samples.ddb.parallel.queries.Config;
import software.amazon.samples.ddb.parallel.queries.QueryUtils;

/**
 * AWS Blog post: Using SDK v2 to query DynamoDB Orders table
 * 
 * @author zorani
 *
 */
public class ParallelStreamQueryV2 {

	DynamoDbAsyncClient dynamoDB = null;
	DynamoDbEnhancedAsyncClient enhancedDynamoDB = null;

	DynamoDbAsyncTable<Order> table = null;
	
	private static final Logger LOG = LoggerFactory.getLogger(ParallelStreamQueryV2.class);

	public ParallelStreamQueryV2() {

		this.dynamoDB = DynamoDbAsyncClient.builder()
                				.region(Region.US_EAST_1)
                				.credentialsProvider(ProfileCredentialsProvider.builder()
                						.profileName("default")
                						.build())
                				.build();		
		
		this.enhancedDynamoDB = DynamoDbEnhancedAsyncClient.builder().dynamoDbClient(this.dynamoDB).build();
		
        //Create a DynamoDbTable object based on Orders
        this.table = this.enhancedDynamoDB.table(Config.DDB_TABLE_NAME, TableSchema.fromBean(Order.class));
	}
	
	/**
	 * Close connection
	 */
	public void close() {
		
		this.dynamoDB.close();
	}

	/**
	 * List all orders
	 * 
	 * @return
	 */
	public List<Order> listAllOrdersUsingParallelScan(int segment) {

		return listAllOrdersUsingParallelScan(null, segment);
	}

	/**
	 * List all Orders
	 * 
	 * @param category
	 * @param segments
	 * @return
	 */
	public List<Order> listAllOrdersUsingParallelScan(String category, int segments) {

        ScanEnhancedRequest enhancedRequest = null;
		
		if (StringUtils.isNotEmpty(category)) {
			
	        AttributeValue attVal = AttributeValue.builder()
                    .s(category.trim().toUpperCase())
                    .build();

        // Get only items for a given category
        Map<String, AttributeValue> myMap = new HashMap<>();
        myMap.put(":cat", attVal);

        Map<String, String> myExMap = new HashMap<>();
        myExMap.put("#cat", "category");

        // Set the Expression so only Closed items are queried from the Work table
        Expression expression = Expression.builder()
                .expressionValues(myMap)
                .expressionNames(myExMap)
                .expression("#cat = :cat")
                .build();

        enhancedRequest = ScanEnhancedRequest.builder()
                .filterExpression(expression)
                .limit(10)
                .build();
		}
		else {
			
	        enhancedRequest = ScanEnhancedRequest.builder()
	        		.consistentRead(false)
	                .build();
		}

        // Get items in the Issues table
		PagePublisher<Order> pageOrdersPublisher = this.table.scan(enhancedRequest);
		
        // snippet-start:[dynamodb.java2.async_pagination.subscriber]
        // Use subscriber
		pageOrdersPublisher.subscribe(new Subscriber<Page<Order>>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
            	
                subscription = s;
                subscription.request(1);
            }

            //@Override
            public void onNext(Page<Order> pageOrder) {
            	
                System.out.println(pageOrder.items().size());
                subscription.request(1);
            }

            @Override
            public void onError(Throwable t) { }

            @Override
            public void onComplete() { }
            // snippet-end:[dynamodb.java2.async_pagination.subscriber]
        });

        // As the above code is non-blocking, make sure your application doesn't end immediately
        // For this example, I am using Thread.sleep to wait for all pages to get delivered
        try {
			Thread.sleep(23_000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
        // The Flowable class has many helper methods that work with any reactive streams compatible publisher implementation
//        List<Page<Order>> orders = Flowable.fromPublisher(pageOrdersPublisher)
//                                      //.flatMapIterable(ListTablesResponse::tableNames)
//                                      .toList()
//                                      .blockingGet();
//
//        System.out.println(orders.size());
        
        
        return new ArrayList<>();
	}

	/**
	 * Simple list orders operation
	 * 
	 * @param category
	 * @param orderDate
	 * @return
	 */
	public List<Order> listOrdersByCategoryAndOrderDate(final String category, final String orderDate) {

		if (StringUtils.isEmpty(category)) return null;

        //Create a DynamoDbTable object based on Orders
		DynamoDbAsyncIndex<Order> index = this.table.index("category-order-date-index");
		
		String indexName = null;
		String keyCondExpr = null;
		Map<String,String> names = new HashMap<>();
		Map<String,AttributeValue> values = new HashMap<>();

        AttributeValue catVal = AttributeValue.builder()
                .s(category.trim().toUpperCase())
                .build();
		
		names.put("#cat", "category");
		values.put(":cat", catVal);
		keyCondExpr = "#cat = :cat";

		if (StringUtils.isNotEmpty(orderDate)) {

	        AttributeValue dateVal = AttributeValue.builder()
	                .s(orderDate.trim())
	                .build();

	        names.put("#od", "order-date");
			values.put(":od", dateVal);
			keyCondExpr += " AND begins_with(#od, :od)";
		}

		QueryConditional queryConditional = QueryConditional
        					.sortBeginsWith(k -> k.partitionValue(category.trim().toUpperCase()).sortValue(orderDate.trim()).build());

        // Get items in the table and write out the ID value
        //SdkIterable<Page<Order>> iter  = index.query(queryConditional);
        //List<Order> orders = iter.stream().map(x -> x.items()).flatMap(Collection::stream).collect(Collectors.toList());
        
		final long start = System.currentTimeMillis();
		final List<Order> orders = new ArrayList<>();
		Publisher<Page<Order>> publisher = index.query(queryConditional);
		
		publisher.subscribe(new Subscriber<Page<Order>>() {
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
            	
                subscription = s;
                subscription.request(1);
            }

            //@Override
            public void onNext(Page<Order> pageOrder) {
            	
                System.out.println(pageOrder.items().size());
                orders.addAll(pageOrder.items());
                subscription.request(10_000);
            }

            @Override
            public void onError(Throwable t) { }

            @Override
            public void onComplete() { 
            	System.out.println("DONE");
                System.out.printf("Total orders for SPORT in March 2020: %d .. duration: %d msec%n", orders.size(), System.currentTimeMillis() - start);
            }
        });

        // As the above code is non-blocking, make sure your application doesn't end immediately
        // For this example, I am using Thread.sleep to wait for all pages to get delivered
        try {
			Thread.sleep(23_000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
//        customers.stream()
//        .forEach(p -> p.items().forEach(item -> System.out.println(item.getCustName())));
        
        return orders;
	}		
	
	/**
	 * Execute query for the given category and order-date prefix
	 * 
	 * @param category
	 * @param orderDate
	 * @return
	 */
	public List<Order> getOrdersListByCategoryAndOrderDate(final String category, final String orderDate) {

		if (StringUtils.isEmpty(category)) return null;

        //Create a DynamoDbTable object based on Orders
		DynamoDbAsyncIndex<Order> index = this.table.index("category-order-date-index");
		
		QueryConditional queryConditional = QueryConditional
        					.sortBeginsWith(k -> k.partitionValue(category.trim().toUpperCase()).sortValue(orderDate.trim()).build());

		Publisher<Page<Order>> publisher = index.query(queryConditional);
		
	       // The Flowable class has many helper methods that work with any reactive streams compatible publisher implementation
        List<Order> orders = Flowable.fromPublisher(publisher)
                                      .flatMapIterable(Page::items)
                                      .toList()
                                      .blockingGet();

        //System.out.printf(" >> Category: [%s], date prefix: [%s], orders list size: %d%n", category, orderDate, orders.size());
        
        return orders;
	}		

	/**
	 * Get list of orders by Category, using Java 8 parallel streams
	 * 
	 * @param category
	 * @return
	 */
	List<Order> getOrdersByCategoryAndDateUsingParallelStream(final String category) {

		List<String> yearMonthsList = QueryUtils.generate12YearMonthDatePrefixes(Order.YEAR);

		List<Order> orders = yearMonthsList.parallelStream()
				.map(x -> listOrdersByCategoryAndOrderDate(category, x))
				.flatMap(Collection::stream) // flatten results into a single stream of Orders
				.unordered() // do not insist to preserve input order (might be faster)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		return orders;
	}
    
	/**
	 * Get list of orders by Category, using Java 8 parallel streams
	 * 
	 * @param category
	 * @return
	 */
	List<Order> getOrdersByCategoryAndDateUsingReactiveStream(final String category) {

		List<String> yearMonthsList = QueryUtils.generate48YearMonthDatePrefixes(Order.YEAR);

		List<Order> orders = yearMonthsList.stream()
				.map(x -> getOrdersListByCategoryAndOrderDate(category, x))
				.flatMap(Collection::stream) // flatten results into a single stream of Orders
				.unordered() // do not insist to preserve input order (might be faster)
				.collect(Collectors.toCollection(() -> Collections.synchronizedList(new ArrayList<>())));

		return orders;
	}
		
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		ParallelStreamQueryV2 query = new ParallelStreamQueryV2();
		
		long start = System.currentTimeMillis();
		List<Order> orders = query.getOrdersByCategoryAndDateUsingReactiveStream("SPORT");
		long duration = System.currentTimeMillis() - start;
		System.out.printf("| Category/Dates  48 reactive streams           | %8d | %d |%n", orders.size(), duration);
	}
}