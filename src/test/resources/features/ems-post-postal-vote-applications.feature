@EMSPostPostalVoteApplications
Feature: The EMS send a post request on confirming a given Postal Vote Application by Id (Soft deletion)

  Background:
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    And the gss codes "E12345678" and "E12345679" mapped to the ERO Id

  Scenario: System returns http status 403 if certificate serial number is not attached to the request
    When the EMS sends a post request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and without the certificate serial number in the request header
    Then I received the http status 403

  Scenario: System rejects the request with status code 400 if the application id format is invalid
    When the EMS sends a post request to "/postalvotes" with an application id "123"
    Then I received the http status 400
    And it has an error message of "The application id must match the pattern ^[a-fA-F\d]{24}$"

  @ClearCache
  Scenario: System returns http status 404 if a given application does not exist
    When the EMS sends a post request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and SUCCESS status
    Then I received the http status 404
    And it has an error message of "The Postal application could not be found with id `502cf250036469154b4f85fb`"

  @ClearCache @DeletePostalEntity
  Scenario: System returns http status 404 if the gss codes retrieved from ERO and the application gss code are different.
    Given a postal vote application with the application id "502cf250036469154b4f85fc", status "RECEIVED" and GSS Code "E12345699" exists
    When the EMS sends a post request to "/postalvotes" with an application id "502cf250036469154b4f85fc" and certificate serial number "1234567891" and SUCCESS status
    Then I received the http status 404
    And it has an error message of "The Postal application could not be found with id `502cf250036469154b4f85fc`"

  @DeletePostalEntity @DeletePostalConfirmationMessage @ClearCache
  Scenario: System returns http status 204 on successful deletion of a success status
    Given a postal vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exists
    When the EMS sends a post request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and SUCCESS status
    Then the system updated the postal application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-postal-application" queue has a SUCCESS confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204

  @DeletePostalEntity @DeletePostalConfirmationMessage @ClearCache
  Scenario: System returns http status 204 on successful deletion of a failure status
    Given a postal vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exists
    When the EMS sends a post request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and FAILURE status
    Then the system updated the postal application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-postal-application" queue has a FAILURE confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204

  @DeletePostalEntity @DeletePostalConfirmationMessage @ClearCache
  Scenario: System ignores the request if current status of the postal vote application is DELETED
    Given a postal vote application with the application id "502cf250036469154b4f85fb", status "RECEIVED" and GSS Code "E12345678" exists
    When the EMS sends a post request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and SUCCESS status
    Then the system updated the postal application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-postal-application" queue has a SUCCESS confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204
    When the EMS sends a post request to "/postalvotes" with an application id "502cf250036469154b4f85fb" and certificate serial number "1234567891" and SUCCESS status
    Then the system ignores request and did not update the postal application with the id "502cf250036469154b4f85fb"
    And there will be no confirmation message on the queue "deleted-postal-application"
    And I received the http status 204