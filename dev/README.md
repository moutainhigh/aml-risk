aml-customer.git
#test2
TThis is created as skeleton for Fusion-X

## Getting Started

Change the yaml files and other qa,uat,reports,sre folder contents accordingly ...
This should generate a jar file as the product

### Prerequisites

JDK 8
Developer tools

```
Red hat Jboss Developer Studio Version: 12.9.0.GA

```

### Building and Deploying

mvn clean package

and deploy to wercker

```
provide steps
```


## Running the tests

mvn clean test

### Break down into end to end tests

Explain what these tests test and why

```
Give an example
```

### And coding style tests

Explain what these tests test and why

```
Give an example
```

## Deployment

Add additional notes about how to deploy this on environment(s)


## Built With

* Spring 
* Maven
* CircuitBreaker



## Versioning



## Authors

* **Ranjith K** - *Initial work* [LOITS](http://www.lolctech.comm)
* ** ** - *description of work* 



## License


## Acknowledgments

* 
*

## Setting ENV variables (MAC/lINUX) 

export CONF_DB_IP_PORT=130.61.46.215:3306 && 
    export CONF_DB_USER=amluser && 
    export CONF_DB_PASS=aml2841@fusionX && 
    export CONF_URL_PORT=132.145.233.0:8080 && 
    export CONF_SERVER_USR=admin && 
    export CONF_SERVER_PASS=trypassword && 
    export CONF_ENV_NAME=dev
    

## health-check endpoint
<host><port>/aml-customer/api/v1/<tenent>

## Run below command to build
mvn clean package -Denv.CONF_ENV_NAME=dev
