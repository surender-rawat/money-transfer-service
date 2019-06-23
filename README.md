# Money Transfer Service

## Preface
This project implements a simple money transfer service without using the spring framework.It uses a in memory database for storing data.

## About
A RESTful API that allows transfer money from one Bank Account to another in any currency.

It uses 2 entities:
* _transaction_ - the money transfer transaction used to initialize the transaction
* _bank account_ - the bank account which has balance in the specified currency

Right now currency conversion is implemented  with static conversion rates. 

The API is developed using Java 8 with embeded Grizzly server and H2 database.

This API guaranties the data consistency in any case. Even if it will be a huge amount concurrent users. 
This ability was achieved by setting the isolation level (Repeatable Read) using of `select ... for update` database feature which helps to lock the object until all related objects will be updated/created 
 
## Requires
* Java 8
* Maven

## Basic assumptions
* Authentication and authorization is out of scope
* Message localization is out of scope
* In Memory database is used for implementation
* No use of spring framework 
* Main focus area is money tranasfer api implementation and high consistency must be achieved in concurrent scenarios
* It uses a static money exchange service.In future it can be replaced with actual money exchange service

## How to start

Once the application is fetched from git it can be built with maven

    mvn clean install
    
This will fetch dependencies and run all tests

To run the app execute:

    mvn exec:java
or

    java -jar /target/money-transfer-service-1.0-SNAPSHOT-jar-with-dependencies.jar

The application will start on the `localhost` and will be listening to the port `8080`

## API Definition

### Bank Account
The bank account entity which has balance in the specified currency and could transfer the money
if there is enough money.

#### Account Entity Structure
    {
        "id": <number>,
        "ownerName": <string>,
        "balance": <double>,
        "blockedAmount": <double>,
        "currency": <string - one from "INR", "USD", "EUR">
    }

#### Create Bank Account

The following creates bank account and returns the created entity with `ID` specified

    POST /accounts
    {
        "ownerName": "Surender S Rawat",
        "balance": 10000.00,
        "blockedAmount": 0,
        "currency": "INR"
    }

Example response:

    HTTP 200 OK
    POST /accounts
    {
        "id": 13487343,
        "ownerName": "Surender S Rawat",
        "balance": 10000.00,
        "blockedAmount": 0,
        "currency": "INR"
    }
    
#### List all Bank Accounts

The following gets all the bank accounts that exist in the system

    GET /accounts

Example response:


    HTTP 200 OK
    [{
        "id": 13487343,
        "ownerName": "Surender S Rawat",
        "balance": 10000.00,
        "blockedAmount": 0,
        "currency": "INR"
    }]

#### Get Bank Account details

The following gets the particular account if it exists in the system

    GET /accounts/13487343

Example response:

    HTTP 200 OK
    {
        "id": 1348734346343,
        "ownerName": "Surender S Rawat",
        "balance": 10000.00,
        "blockedAmount": 0,
        "currency": "INR"
    }

#### Update Bank Account details

The following updates the details of the particular account if it exists in the system
You can not update any field except "ownerName"

    PUT /accounts/
    {
        "id": 13487343,
        "ownerName": "Surender Singh Rawat",
    }

Example response:

    HTTP 200 OK
    {
        "id": 1348734346343,
        "ownerName": "Surender Singh Rawat",
        "balance": 10000.00,
        "blockedAmount": 0,
        "currency": "INR"
    }
        
### Transaction
The money transfer transaction used to initialize the transaction. Once created
will be executed automatically. If transaction can not be created by some reason the Error(HTTP 500 Internal Error) 
will be returned with details in the body.Once a transaction resource is created it can't be modified.

#### Transaction Entity Structure
    {
        "id": <number>,
        "fromBankAccountId": <number>,
        "toBankAccountId": <number>,
        "amount": <double>,
        "currency": <string - one from "INR", "USD", "EUR">,
        "creationDate": <timestamp>,
        "updateDate": <timestamp>,
        "status": <string - one from "CREATED", "PROCESSING", "FAILED", "SUCCEED">,
        "failMessage": <string>
    }
    
#### Create a transaction

The following creates a new transaction if possible (valid Bank Accounts and parameters should be provided).
Once `id`, `creationDate`, `updateDate` or `status` provided they  will be ignored. 
You can obtain the generated values of these fields in the response of this call. 

    POST /transactions
    {
        "fromBankAccountId": 1,
        "toBankAccountId": 2,
        "amount": 16.1,
        "currency": "EUR"
    }
    
Example response:

    HTTP 200 OK
    {
        "id": 1,
        "fromBankAccountId": 1,
        "toBankAccountId": 2,
        "amount": 16.1,
        "currency": "EUR",
        "creationDate": 1537303715995,
        "updateDate": 1537303715995,
        "status": "CREATED",
        "failMessage": ""
    }

#### Get all transactions

    GET /transactions

Example response:

    HTTP 200 OK    
    [{
        "id": 1,
        "fromBankAccountId": 1,
        "toBankAccountId": 2,
        "amount": 16.1,
        "currency": "EUR",
        "creationDate": 1537303715995,
        "updateDate": 1537303715995,
        "status": "CREATED",
        "failMessage": ""
    }]
    
#### Get a specific transaction by its ID

    GET /transactions/1

Example response:

    HTTP 200 OK    
    {
        "id": 1,
        "fromBankAccountId": 1,
        "toBankAccountId": 2,
        "amount": 16.1,
        "currency": "EUR",
        "creationDate": 1537303715995,
        "updateDate": 1537303715995,
        "status": "CREATED",
        "failMessage": ""
    }
    
### Service Exception Handing
If any error will be thrown by some reason appropriate HTTP response code will be returned by service.

Sample response for Resource Not Found Error: When InValid account is passed to API

    HTTP 404 Not Found
    {
      "type": "OBJECT_IS_NOT_FOUND",
      "name": "The entity with provided ID has not been found",
      "message": "The entity with provided ID has not been found"
    }
    
Sample response for Resource Not Found Error: When Insufficient balance in bank Account 

    HTTP 500 Internal service error
    {
    "type": "OBJECT_IS_MALFORMED",
    "name": "The entity passed has been malformed",
    "message": "The entity passed has been malformed: The specified bank account could not transfer this amount of money. His balance does not have enough money"
  }
  
 ### Service WADL 
 Application wadl file can be accessed using http://localhost:8080/transfer-service/application.wadl
  
  ```
  <?xml version="1.0" encoding="UTF-8"?>
<application xmlns="http://wadl.dev.java.net/2009/02">
   <doc xmlns:jersey="http://jersey.java.net/" jersey:generatedBy="Jersey: 2.26-b07 2017-06-30 12:48:47" />
   <doc xmlns:jersey="http://jersey.java.net/" jersey:hint="This is simplified WADL with user and core resources only. To get full WADL with extended resources use the query parameter detail. Link: http://localhost:8080/transfer-service/application.wadl?detail=true" />
   <grammars />
   <resources base="http://localhost:8080/transfer-service/">
      <resource path="/transactions">
         <method id="getAllTransactions" name="GET">
            <response>
               <representation mediaType="application/json" />
            </response>
         </method>
         <method id="createTransaction" name="POST">
            <response>
               <representation mediaType="application/json" />
            </response>
         </method>
         <resource path="{id}">
            <param xmlns:xs="http://www.w3.org/2001/XMLSchema" name="id" style="template" type="xs:long" />
            <method id="getTransactionById" name="GET">
               <response>
                  <representation mediaType="application/json" />
               </response>
            </method>
         </resource>
      </resource>
      <resource path="/accounts">
         <method id="getAllBankAccounts" name="GET">
            <response>
               <representation mediaType="application/json" />
            </response>
         </method>
         <method id="updateBankAccount" name="PUT">
            <response>
               <representation mediaType="application/json" />
            </response>
         </method>
         <method id="createBankAccount" name="POST">
            <response>
               <representation mediaType="application/json" />
            </response>
         </method>
         <resource path="{id}">
            <param xmlns:xs="http://www.w3.org/2001/XMLSchema" name="id" style="template" type="xs:long" />
            <method id="getBankAccountById" name="GET">
               <response>
                  <representation mediaType="application/json" />
               </response>
            </method>
         </resource>
      </resource>
   </resources>
</application> 
