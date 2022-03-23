# How to use provided source code

### Pre-requisites

You need to have a valid AWS account and the access to account. Utilities provided in this project expects that your credentials are properly stored in the credentials file (as per: https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html)

### Creating an Orders table in your DynamoDB

Please use provided CloudFormation template to create DynamoDB Orders table, mentioned in the post. The file name is CFN_TableCreation.json. You need to have permission to create DynamoDB table and indexes using CloudFormation script 

### Generate and load test tables

Project has LoadTestData.java to be used to generate and load Orders data to DynamoDB table. Load the code in your IDE of choice and choose the number of records to generate (currently is set to 100). If you only interested to test the code logic, you don't need many records in the table. If you would like to benchmark, as it is done for the Blog post, you'll need to generate 100,000 or more records to see differences between different approaches. Please note, large number of records will occur higher cost.

### Code files descriptions for package: software.amazon.samples.ddb.parallel.queries

There are several files provided with the project (package: software.amazon.samples.ddb.parallel.queries)
- Config.java: Keeps the Year that we are testing Orders with
- DatesPrefixUtils.java: Generates various order-date prefixes, like 12, 48 and 365

### Code files descriptions for package: software.amazon.samples.ddb.parallel.queries.sdk1

There are several files provided for the main part of the project:
- Order.java: model definition for POJO and DynamoDB record (using DynamoDBMapper annotations)
- Utils.java: a few handy utilities that we used throughout testing
- OrderDbUtils.java: A few common DynamoDB methods used for querying Orders data from DynamoDB
- LoadTestData.java: Used to generate and load simulated Orders data into DynamoDB table
- ParallelStreamQuery.java: All methods discussed in Blog post working with parallel streams
- CompletableFuturesQuery.java: All methods discussed in Blog post working with completable futures
- TestCases.java: Main method for testing all query methods

### Code files descriptions for package: software.amazon.samples.ddb.parallel.queries.sdk2
 
There are several files provided to illustrate use of Java AWS SDK v2:
- Order.java: model definition for POJO and DynamoDB record (using SDK v2 DynamoDb annotations)
- ParallelStreamQueryV2.java: Several reactive stream methods, as discussed in Blog post 

### Deploying to EC2 .. in case you want to benchmark with your choice of EC2 instance(s)

The Maven pom.xml file is provided. It includes both SDK v1 and SDK v2 dependencies. If you want to build the JAR with SDK v1, feel free to comment out a small section that is only for SDK v2, making your JAR smaller in size. 

SDK v2 portion in POM.xml that can be commented out for building SDK v1 JAR file:

```xml
	  <dependencyManagement>
	    <dependencies>
	      <dependency>
	        <groupId>software.amazon.awssdk</groupId>
	        <artifactId>bom</artifactId>
	        <version>2.17.140</version>
	        <type>pom</type>
	        <scope>import</scope>
	      </dependency>
	    </dependencies>
	  </dependencyManagement>
```
and

```xml
        <!-- AWS SDK v2 dependencies -->
		<dependency>
		  <groupId>software.amazon.awssdk</groupId>
		  <artifactId>dynamodb</artifactId>
		</dependency>
		<dependency>
		  <groupId>software.amazon.awssdk</groupId>
		  <artifactId>dynamodb-enhanced</artifactId>
		</dependency>
        <!-- https://mvnrepository.com/artifact/io.reactivex.rxjava2/rxjava -->
		<dependency>
	 	  <groupId>io.reactivex.rxjava2</groupId>
		  <artifactId>rxjava</artifactId>
		  <version>2.2.21</version>
		</dependency>    
```

The following steps needs to be executed:

- On your dev environment run: mvn clean package (this will create a JAR file)
- Launch EC2 instance(s) as per your wish
- Connect with your EC2 instance (for example, you can use AWS Console EC2 Connect screen)
- Install all updates on EC2
  - mkdir java
  - cd java
  - sudo amazon-linux-extras install java-openjdk11
- Create a folder for your JAR on the EC2 instance
  - cd ..
  - mkdir project
  - cd project
- Now switch to terminal on your dev machine and copy JAR file using 'scp' command (or whatever is suitable for your OS)
- Setup credentials for your EC2 (see link: https://docs.aws.amazon.com/sdk-for-java/v1/developer-guide/credentials.html)
- On the EC2 terminal, run the Java JAR, something like:  
  - java -cp order-query-parallel-0.0.1-SNAPSHOT.jar software.amazon.samples.ddb.parallel.queries.RunBenchmarks
