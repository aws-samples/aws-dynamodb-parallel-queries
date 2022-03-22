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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondarySortKey;

/**
 * Order POJO
 * 
 * @author zorani
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@DynamoDbBean
public class Order {
	
	@JsonIgnore
	public static final int YEAR = 2020;
	
    @JsonProperty("id")
    private String id;
	
    /**
	 * @return the id
	 */
    @DynamoDbPartitionKey
    @DynamoDbAttribute("id")
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the category
	 */
	@DynamoDbAttribute("category")
	@DynamoDbSecondaryPartitionKey(indexNames = {"category-order-date-index", "category-query-slot-mod64-index"})
	public String getCategory() {
		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the country
	 */
	@DynamoDbAttribute(value="country")
	@DynamoDbSecondaryPartitionKey(indexNames = {"country-order-date-index"})
	public String getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(String country) {
		this.country = country;
	}

	/**
	 * @return the ckCountryState
	 */
	@DynamoDbAttribute(value="ck-country-state")
	@DynamoDbSecondaryPartitionKey(indexNames = {"ck-country-state-order-date-index"})
	public String getCkCountryState() {
		return ckCountryState;
	}

	/**
	 * @param ckCountryState the ckCountryState to set
	 */
	public void setCkCountryState(String ckCountryState) {
		this.ckCountryState = ckCountryState;
	}

	/**
	 * @return the sku
	 */
	
	@DynamoDbAttribute(value="sku")
	@DynamoDbSecondaryPartitionKey(indexNames = {"sku-order-date-index"})
	public String getSku() {
		return sku;
	}

	/**
	 * @param sku the sku to set
	 */
	public void setSku(String sku) {
		this.sku = sku;
	}

	/**
	 * @return the orderDate
	 */
	@DynamoDbAttribute(value="order-date")
	@DynamoDbSecondarySortKey(indexNames = {"sku-order-date-index", "country-order-date-index", "category-order-date-index", "ck-country-state-order-date-index"})
	public String getOrderDate() {
		return orderDate;
	}

	/**
	 * @param orderDate the orderDate to set
	 */
	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	/**
	 * @return the querySlotMod64
	 */
	@DynamoDbAttribute(value="query-slot-mod64")
	@DynamoDbSecondarySortKey(indexNames = {"category-query-slot-mod64-index"})
	public int getQuerySlotMod64() {
		return querySlotMod64;
	}

	/**
	 * @param querySlotMod64 the querySlotMod64 to set
	 */
	public void setQuerySlotMod64(int querySlotMod64) {
		this.querySlotMod64 = querySlotMod64;
	}

	/**
	 * @return the qty
	 */
	@DynamoDbAttribute("qty")
	public int getQty() {
		return qty;
	}

	/**
	 * @param qty the qty to set
	 */
	public void setQty(int qty) {
		this.qty = qty;
	}

	/**
	 * @return the pricePerUnit
	 */
	@DynamoDbAttribute("unit-price")
	public double getPricePerUnit() {
		return pricePerUnit;
	}

	/**
	 * @param pricePerUnit the pricePerUnit to set
	 */
	public void setPricePerUnit(double pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

	/**
	 * @return the state
	 */
	@DynamoDbAttribute(value="state")
	public String getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(String state) {
		this.state = state;
	}

	/**
	 * @return the paymentType
	 */
	@DynamoDbAttribute(value="payment-type")
	public String getPaymentType() {
		return paymentType;
	}

	/**
	 * @param paymentType the paymentType to set
	 */
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}

	/**
	 * @return the comment
	 */
	@DynamoDbAttribute(value="comment")
	public String getComment() {
		return comment;
	}

	/**
	 * @param comment the comment to set
	 */
	public void setComment(String comment) {
		this.comment = comment;
	}

	// --- members
	
	@JsonProperty("category")
	//@DynamoDBIndexHashKey(attributeName="category", globalSecondaryIndexNames = {"category-order-date-index", "category-query-slot-mod64-index"})
	private String category;

	@JsonProperty("country")
	//@DynamoDBIndexHashKey(attributeName="country", globalSecondaryIndexName = "country-order-date-index")
	private String country;

	@JsonProperty("ck-country-state")
	//@DynamoDBIndexHashKey(attributeName="ck-country-state", globalSecondaryIndexName = "ck-country-state-order-date-index")
	private String ckCountryState;

	@JsonProperty("sku")
	//@DynamoDBIndexHashKey(attributeName="sku", globalSecondaryIndexName = "sku-order-date-index")
	private String sku;

	@JsonProperty("order-date")
	//@DynamoDBIndexRangeKey(attributeName="order-date", globalSecondaryIndexNames = {"sku-order-date-index", "country-order-date-index", "category-order-date-index", "ck-country-state-order-date-index"})
	private String orderDate;
	
    @JsonProperty("query-slot-mod64")
    //@DynamoDBIndexRangeKey(attributeName="query-slot-mod64", globalSecondaryIndexName = "category-query-slot-mod64-index")
    private int querySlotMod64;
	
	@JsonProperty("qty")
    //@DynamoDbAttribute(attributeName="qty")
	private int qty;
	
	@JsonProperty("unit-price")
	//@DynamoDbAttribute(attributeName="unit-price")
	private double pricePerUnit;

	@JsonProperty("state")
	//@DynamoDbAttribute(attributeName="state")
	private String state;
	
	@JsonProperty("payment-type")
    //@DynamoDbAttribute(attributeName="payment-type")
	private String paymentType;

	@JsonProperty("comment")
    //@DynamoDbAttribute(attributeName="comment")
	private String comment;
}