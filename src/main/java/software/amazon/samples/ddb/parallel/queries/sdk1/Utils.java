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
public class Utils {

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
	protected static DynamoDBMapperConfig getDefaultMapperConfig(String tableName) {
		
		return getDefaultMapperConfigBuilder(tableName).build();
	}

	/**
	 * DbMapper config builder
	 * 
	 * @param tableName
	 * @return
	 */
	protected static DynamoDBMapperConfig.Builder getDefaultMapperConfigBuilder(String tableName) {
		
		return DynamoDBMapperConfig.builder()
				.withConsistentReads(DynamoDBMapperConfig.ConsistentReads.EVENTUAL)
				.withTableNameOverride(TableNameOverride.withTableNameReplacement(tableName))
				.withPaginationLoadingStrategy(DynamoDBMapperConfig.PaginationLoadingStrategy.EAGER_LOADING);
	}
}