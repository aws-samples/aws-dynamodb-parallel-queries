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

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig.TableNameOverride;

/**
 * A few handy utilities
 * 
 * @author zorani
 *
 */
public class QueryUtils {

	/**
	 * Initialize DynamoDB client
	 */
	public static AmazonDynamoDB init() {
		
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        
        try {

        	credentialsProvider.getCredentials();
        } catch (Exception e) {
        	
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        
        // instantiate DynamoDB client
        AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.standard()
            .withCredentials(credentialsProvider)
            .withRegion("us-east-1")
            .build();

        return dynamoDB;
    }
		
	/**
	 * Get the mapper config
	 * 
	 * @param tableName
	 * @return
	 */
	public static DynamoDBMapperConfig getDefaultMapperConfig(String tableName) {
		
		return getDefaultMapperConfigBuilder(tableName).build();
	}

	/**
	 * DbMapper config builder
	 * 
	 * @param tableName
	 * @return
	 */
	public static DynamoDBMapperConfig.Builder getDefaultMapperConfigBuilder(String tableName) {
		
		return DynamoDBMapperConfig.builder()
				.withConsistentReads(DynamoDBMapperConfig.ConsistentReads.EVENTUAL)
				.withTableNameOverride(TableNameOverride.withTableNameReplacement(tableName))
				.withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING);
	}		
	
	/**
	 * Generate monthly order-date prefixes
	 * 
	 * @param year
	 * @return
	 */
	public static List<String> generate12YearMonthDatePrefixes(final int year) {

		return IntStream.rangeClosed(1, 12).mapToObj(x -> String.format("%d-%02d", year, x)).collect(Collectors.toList());
	}

	/**
	 * Generate 48 order-date monthly prefixes, such as: 2020-01-0, 2020-01-1, ... 
	 * 	  
	 * @param year
	 * @return
	 */
	public static List<String> generate48YearMonthDatePrefixes(final int year) {

		List<String> list = new ArrayList<>();
		
		for(int i = 1; i <= 12; ++i) {
			
			final int month = i;
			
			List<String> arrList = IntStream.rangeClosed(0, 3).mapToObj(x -> String.format("%d-%02d-%d", year, month, x)).collect(Collectors.toList());
			list.addAll(arrList);
		}
		
		return list;
	}

	/**
	 * Generate daily order-date prefixes for the entire year
	 * 	  
	 * @param year
	 * @return
	 */
	public static List<String> generateDailyOrdersDatePrefixes(int year) {

		LocalDate startOfTheYear = LocalDate.ofYearDay(year, 1);
		LocalDate startOfTheNextYear = LocalDate.ofYearDay(year + 1, 1);		

		Duration duration = Duration.between(startOfTheYear.atStartOfDay(), startOfTheNextYear.atStartOfDay());
		int daysInYear = (int)duration.toDays();

		return IntStream.rangeClosed(1, daysInYear)
				.mapToObj(x -> LocalDate.ofYearDay(year, x))
				.map(x -> x.toString())
				.collect(Collectors.toList());
	}	
	
	/**
	 * Generate list of 64 values, from 0 to 63 (inclusive)
	 * 
	 * @return
	 */
	public static List<Integer> create64QuerySlotsList() {
		
		List<Integer> querySlotValuesList = IntStream.range(0, 64)
				.mapToObj(x -> Integer.valueOf(x))
				.collect(Collectors.toList());

		return querySlotValuesList;
	}
	
	/**
	 * Generate list of 128 values, from 0 to 127 (inclusive)
	 * 
	 * @return
	 */
	public static List<Integer> create128QuerySlotsList() {
		
		List<Integer> querySlotValuesList = IntStream.range(0, 128)
				.mapToObj(x -> Integer.valueOf(x))
				.collect(Collectors.toList());

		return querySlotValuesList;
	}	
}