@GetPostalVoteApplications
Feature: Get Postal Vote Application ( Default page size is 20)

  Scenario: System returns http status 403 if certificate serial number is not attached to the request
    When I send a get postal vote applications request without a certificate serial number in the request header
    Then I receive error with response status as 403

  Scenario: System does not have any postal vote applications
    When I send a get postal vote applications request with the page size 10
    Then I received a response with 0 postal vote applications

  @DeletePostalEntity
  Scenario: System returns postal vote applications of a given page size
    Given there are 20 postal vote applications exist with the status "RECEIVED"
    When I send a get postal vote applications request with the page size 10
    Then I received a response with 10 postal vote applications

  @DeletePostalEntity
  Scenario: System does not have requested number of postal applications
    Given there are 2 postal vote applications exist with the status "RECEIVED"
    When I send a get postal vote applications request with the page size 3
    Then I received a response with 2 postal vote applications

  @DeletePostalEntity
  Scenario: System returns default number of records if page size is not specified
    Given there are 21 postal vote applications exist with the status "RECEIVED"
    When I send a get postal vote request without the page size
    Then I received a response with 20 postal vote applications