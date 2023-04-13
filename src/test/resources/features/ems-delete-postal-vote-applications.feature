@EMSDeletePostalVoteApplications
Feature: The EMS send a delete request on confirming a given Postal Vote Application by Id (Soft deletion)

  Scenario: System returns http status 403 if certificate serial number is not attached to the request
    When the EMS send a delete request to "/postalVotes/accepted" with an application id "502cf250036469154b4f85fb" and without the certificate serial number in the request header
    Then I received the http status 403

  Scenario: System rejects the request with status code 400 if the application id format is invalid
    When the EMS send a delete request to "/postalVotes/accepted" with an application id "123"
    Then I received the http status 400
    And it has an error message of "The application id must match the pattern ^[a-fA-F\d]{24}$"

  Scenario: System returns http status 404 if a given application does not exist
    When the EMS send a delete request to "/postalVotes/accepted" with an application id "502cf250036469154b4f85fb"
    Then I received the http status 404
    And it has an error message of "The Postal application could not be found with id `502cf250036469154b4f85fb`"

  @DeletePostalEntity @DeletePostalConfirmationMessage
  Scenario: System returns http status 204 on successful deletion
    Given a postal vote application with the application id "502cf250036469154b4f85fb" and electoral id "e87cbaea-0deb-4058-95c6-8240d426f5e1" exists
    When the EMS send a delete request to "/postalVotes/accepted" with an application id "502cf250036469154b4f85fb"
    Then the system updated the postal application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-postal-application" queue has a confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204

  @DeletePostalEntity @DeletePostalConfirmationMessage
  Scenario: System ignores the request if current status of the postal vote application is DELETED
    Given a postal vote application with the application id "502cf250036469154b4f85fb" and electoral id "e87cbaea-0deb-4058-95c6-8240d426f5e1" exists
    When the EMS send a delete request to "/postalVotes/accepted" with an application id "502cf250036469154b4f85fb"
    Then the system updated the postal application with the id "502cf250036469154b4f85fb" status as "DELETED"
    And the "deleted-postal-application" queue has a confirmation message for the application id "502cf250036469154b4f85fb"
    And I received the http status 204
    When the EMS send a delete request to "/postalVotes/accepted" with an application id "502cf250036469154b4f85fb"
    Then the system ignores request and did not update the postal application with the id "502cf250036469154b4f85fb"
    And there will be no confirmation message on the queue "deleted-postal-application"
    And I received the http status 204