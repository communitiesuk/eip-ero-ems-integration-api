@GetPostalVoteApplications
Feature: Get Postal Vote Application ( Default page size is 20, max page size is 50)

  Background:
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    And the gss codes "E12345678" and "E12345679" mapped to the ERO Id

  Scenario: System returns http status 403 if certificate serial number is not attached to the request
    When I send a get postal vote applications request without a certificate serial number in the request header
    Then I received the http status 403

  Scenario: System rejects the request with status code 400 if the page size is greater than the configured page size 50
    When I send a get postal vote applications request with the page size 51 and the certificate serial number "1234567891"
    Then I received the http status 400
    And it has an error message of "The page size must be greater than or equal to 1 and less than or equal to 50"

  @ClearCache
  Scenario: System does not have any postal vote applications
    When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
    Then I received a response with 0 postal vote applications

  @DeletePostalEntity @ClearCache
  Scenario: System returns postal vote applications of a given page size
    Given there are 20 postal vote applications exist with the status "RECEIVED" and GSS Codes "E12345678","E12345679"
    When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
    Then I received a response with 10 postal vote applications

  @DeletePostalEntity @ClearCache
  Scenario: System does not have requested number of postal applications
    Given there are 2 postal vote applications exist with the status "RECEIVED" and GSS Codes "E12345678","E12345679"
    When I send a get postal vote applications request with the page size 3 and the certificate serial number "1234567891"
    Then I received a response with 2 postal vote applications

  @DeletePostalEntity @ClearCache
  Scenario: System returns default number of records if page size is not specified
    Given there are 21 postal vote applications exist with the status "RECEIVED" and GSS Codes "E12345678","E12345679"
    When I send a get postal vote request without the page size and with the certificate serial number "1234567891"
    Then I received a response with 20 postal vote applications