@EMSDeleteProxyVoteApplications
Feature: The EMS send a delete request on confirming a given Proxy Vote Application by Id (Soft deletion)

  Background:
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    And the gss codes "E12345678" and "E12345679" mapped to the ERO Id

  Scenario: System returns http status 403 if certificate serial number is not attached to the request
    When the EMS send a delete request to "/proxyvotes/" with an application id "502cf250036469154b4f85fb" and without the certificate serial number in the request header
    Then I received the http status 403

  Scenario: System rejects the request with status code 400 if the application id format is invalid
    When the EMS send a delete request to "/proxyvotes" with an application id "123"
    Then I received the http status 400
    And it has an error message of "The application id must match the pattern ^[a-fA-F\d]{24}$"

  @ClearCache
  Scenario: System returns http status 404 if a given application does not exist
    When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and the certificate serial number "1234567891"
    Then I received the http status 404
    And it has an error message of "The Proxy application could not be found with id `502cf250036469154b4f85fb`"

  @ClearCache @DeleteProxyEntity
  Scenario: System returns http status 404 if the gss codes retrieved from ERO and the application gss code are different.
    Given a proxy vote application with the application id "502cf250036469154b4f85fc", status "RECEIVED" and GSS Code "E12345699" exist
    When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fc" and the certificate serial number "1234567891"
    Then I received the http status 404
    And it has an error message of "The Proxy application could not be found with id `502cf250036469154b4f85fc`"

  @DeleteProxyEntity @DeleteProxyConfirmationMessage @ClearCache
  Scenario: System returns http status 204 on successful deletion
    Given a proxy vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exist
    When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and the certificate serial number "1234567891"
    Then the system updated the proxy application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-proxy-application" queue has a confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204

  @DeleteProxyEntity @DeleteProxyConfirmationMessage @ClearCache
  Scenario: System ignores the request if current status of the proxy vote application is DELETED
    Given a proxy vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exist
    When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and the certificate serial number "1234567891"
    Then the system updated the proxy application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-proxy-application" queue has a confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204
    When the EMS send a delete request to "/proxyvotes" with an application id "502cf250036469154b4f85fb" and the certificate serial number "1234567891"
    Then the system ignores request and did not update the proxy application with the id "502cf250036469154b4f85fb"
    And there will be no confirmation message on the queue "deleted-proxy-application"
    And I received the http status 204