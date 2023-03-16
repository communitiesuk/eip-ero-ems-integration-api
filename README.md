> :exclamation: The EMS facing specification for the Postal and Proxy Vote API can be found at:
[src/main/resources/openapi/EMSIntegrationAPIs.yaml](src/main/resources/openapi/EMSIntegrationAPIs.yaml) :exclamation:

# ems-integration-api

Spring Boot microservice that :

- Consumes approved/rejected Postal and Proxy vote application SQS message from EROP and saved into the database
- Exposes secured REST APIs to get and delete approved Postal / Proxy vote applications
- Publishes SQS messages for each deleted application

## Developer Setup

### Kotlin API Developers

Configure your IDE with the code formatter (ktlint):

```
$ ./gradlew ktlintApplyToIdea
```

This only needs doing once to set up your IDE with the code styles.

### Integration Test Development

Integration tests use TestContainers to manage Docker containers, to reduce development time of writing IT tests it is
recommended to reuse running containers so that the only need to be set up once for the session. After development is
completed the Docker containers must be manually tidied up.

To enable TestContainer reuse ensure your home directory has a file named `.testcontainers.properties` and includes:
`testcontainers.reuse.enable=true`

#### Wiremock

The integration tests use wiremock to stub REST API integrations with external dependencies. To log the request data
that was sent to wiremock (and therefore the request data that would be sent in a real integration), set the environment
variable `logWiremockRequests` to `true`. This can either be a system environment variable, or put it
in `application-test.yml`

Sample output:

```
POST https://localhost:49761/dwp-api/citizen-information/oauth2/token
User-Agent: ReactorNetty/1.0.23
Host: localhost:49761
Accept: */*
Content-Type: application/x-www-form-urlencoded;charset=UTF-8
Content-Length: 85

grant_type=client_credentials&client_id=dwp-client-id&client_secret=dwp-client-secret
```

### Running Tests

```
$ ./gradlew check
```

This will run the tests and ktlint. (Be warned, ktlint will hurt your feelings!)

### Building docker images

```
$ ./gradlew check bootBuildImage
```

This will build a docker image for the Spring Boot application.

## Running the application

Either `./gradlew bootRun` or run the class `EmsIntegrationApiApplication`

### TODO: establish what env vars are actually needed...

### External Environment Variables

The following environment variables must be set in order to run the application:

1. `AWS_ACCESS_KEY_ID` - the AWS access key ID
2. `AWS_SECRET_ACCESS_KEY` - the AWS secret access key
3. `AWS_REGION` - the AWS region
4. `CLOUD_AWS_SQS_ENDPOINT` - optional environment variable used to override the SQS endpoint URI. Primarily


#### MYSQL Configuration

For local setup refer to src/main/resources/db/readme.

* `MYSQL_HOST`
* `MYSQL_PORT`
* `MYSQL_USER`
* `MYSQL_PASSWORD` - only used locally or when running tests

#### Infrastructure overrides

The following are overridden by the task definition in AWS:

* `SPRING_DATASOURCE_URL` - This is set to the deployed RDS' URL.
* `SPRING_DATASOURCE_DRIVERCLASSNAME` - This is overridden to use the AWS Aurora MySQL JDBC Driver.
* `SPRING_LIQUIBASE_DRIVERCLASSNAME` - This is overridden to use the AWS Aurora MySQL JDBC Driver.
* `SQS_PROXY_APPLICATION_QUEUE_NAME` - This is overridden to use the actual queue name for the approved/rejected proxy
  application.
* `SQS_POSTAL_APPLICATION_QUEUE_NAME` - This is overridden to use the actual queue name for the approved/rejected postal
  application.
* `SQS_DELETED_PROXY_APPLICATION_QUEUE_NAME` - This is overridden to use the actual queue name for the deleted proxy
  application.
* `SQS_DELETED_POSTAL_APPLICATION_QUEUE_NAME` - This is overridden to use the actual queue name for the deleted postal
  application.

#### Liquibase Configuration

* `LIQUIBASE_CONTEXT` Contexts for liquibase scripts. For local setup use ddl.

### Authentication and authorisation

Requests are authenticated by the presence of a signed cognito JWT as a bearer token in the HTTP request `authorization`
header.  
EG: `Authorization: Bearer xxxxxyyyyyyzzzzz.....`  
Requests are authorised by their membership of groups and roles carried on the JWT token.  
The UI application is expected to handle the authentication with cognito and pass the JWT token in the `authorization`
header.

### Test Liquibase Rollbacks

To test liquibase rollbacks try the following steps.

1. build a docker image based on the previous git commit.
2. remove docker containers and the docker_mysql-data docker volume
3. run `start-docker` to create the database and apply all previous changesets
4. having defined your new databaseChangeLog file, edit `db.changelog-master.xml` to comment out all references to other
   databaseChangeLog files
5. cd to your src/main/resources/db/changelog directory
6. define a `liquibase.properties` file, as shown below
7. to apply your latest DB changes execute `$LIQUIBASE_HOME/liquibase --log-level debug --contexts=ddl update`
8. verify your DB changes are as expected
9. to rollback your latest DB changes
   execute `$LIQUIBASE_HOME/liquibase --log-level debug --contexts="ddl" rollback-to-date '2022-08-23 15:17:13'` filling
   in the appropriate date from the DATABASECHANGELOG.DATEEXECUTED column
10. verify your DB changes are again as expected

A sample liquibase.properties file

```shell
changelog-file: db.changelog-master.xml
driver: com.mysql.cj.jdbc.Driver
url: jdbc:mysql://localhost:3306/ems_integration_application
username: root
password: rootPassword
classpath: /home/valtech/IdeaProjects/eip/eip-ero-ems-integration-api/src/main/resources/db/changelog/mysql-connector-java-8.0.29.jar
context=ddl
```

##### The SSL handshake works as follows:

* server presents its certificate
* client trusts the server certificate by virtue of the public CA that signed the server certificate being in its
  default trust store (JVM's default `cacerts`)
* server requests the client certificate that is signed by the CA chain that DWP hold for our service
  (DWP's SSL Context has been configured with the signing CA for our client certificate)
* client presents a certificate from it's `KeyStore` that is signed by the requested CA
* server trusts the client certificate
