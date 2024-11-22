@EMSPostProxyVoteApplications
Feature: The EMS send a put request on confirming a given Proxy Vote Application by Id (Soft deletion)

  Background:
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    And the gss codes "E12345678" and "E12345679" mapped to the ERO Id

  Scenario: System returns http status 403 if certificate serial number is not attached to the request
    When the EMS sends a put request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and without the certificate serial number in the request header
    Then I received the http status 403

  Scenario: System rejects the request with status code 400 if the application id format is invalid
    When the EMS sends a put request to "/proxyvotes" with an application id "123"
    Then I received the http status 400
    And it has an error message of "The application id must match the pattern ^[a-fA-F\d]{24}$"

  @ClearCache @DeleteProxyEntity
  Scenario: System returns http status 404 if a given application does not exist
    When the EMS sends a put request to "/proxyvotes" with an application id "502cf250036469154b4f85aa" and certificate serial number "1234567891" and SUCCESS status
    Then I received the http status 404
    And it has an error message of "The Proxy application could not be found with id `502cf250036469154b4f85aa`"

  @ClearCache @DeleteProxyEntity
  Scenario: System returns http status 404 if the gss codes retrieved from ERO and the application gss code are different.
    Given a proxy vote application with the application id "502cf250036469154b4f85fc", status "RECEIVED" and GSS Code "E12345699" exists
    When the EMS sends a put request to "/proxyvotes" with an application id "502cf250036469154b4f85fc" and certificate serial number "1234567891" and SUCCESS status
    Then I received the http status 404
    And it has an error message of "The Proxy application could not be found with id `502cf250036469154b4f85fc`"

  @DeleteProxyEntity @DeleteProxyConfirmationMessage @ClearCache
  Scenario: System returns http status 204 on successful deletion of a success status
    Given a proxy vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exists
    When the EMS sends a put request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and SUCCESS status
    Then the system updated the proxy application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-proxy-application" queue has a SUCCESS confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204

  @DeleteProxyEntity @DeleteProxyConfirmationMessage @ClearCache
  Scenario: System returns http status 204 on successful deletion of a failure status
    Given a proxy vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exists
    When the EMS sends a put request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and FAILURE status
    Then the system updated the proxy application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-proxy-application" queue has a FAILURE confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204

  @DeleteProxyEntity @DeleteProxyConfirmationMessage @ClearCache
  Scenario: System ignores the request if proxy vote application is already DELETED and no message will be place on queue
    Given a proxy vote application with the application id "502cf250036469154b4f85fb", status "DELETED" and GSS Code "E12345678" exists
    When the EMS sends a put request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and SUCCESS status
    Then the system ignores request and did not update the proxy application with the id "502cf250036469154b4f85fb"
    And there will be no confirmation message on the queue "deleted-proxy-application"
    And I received the http status 204