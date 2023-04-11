@GetProxyVoteApplications
Feature: Get Proxy Vote Application ( Default page size is 20, max page size is 50)

  Scenario: System returns http status 403 if certificate serial number is not attached to the request
    When I send a get proxy vote applications request without a certificate serial number in the request header
    Then I received the http status 403

  Scenario: System rejects the request with status code 400 if the page size is greater than the configured page size 50
    When I send a get proxy vote applications request with the page size 51
    Then I received the http status 400
    And it has an error message of "The page size must be greater than or equal to 1 and less than or equal to 50"

  Scenario: System does not have any proxy vote applications
    When I send a get proxy vote applications request with the page size 10
    Then I received a response with 0 proxy vote applications

  @DeleteProxyEntity
  Scenario: System returns proxy vote applications of a given page size
    Given there are 20 proxy vote applications exist with the status "RECEIVED"
    When I send a get proxy vote applications request with the page size 10
    Then I received a response with 10 proxy vote applications

  @DeleteProxyEntity
  Scenario: System does not have requested number of proxy applications
    Given there are 2 proxy vote applications exist with the status "RECEIVED"
    When I send a get proxy vote applications request with the page size 3
    Then I received a response with 2 proxy vote applications

  @DeleteProxyEntity
  Scenario: System returns default number of records if page size is not specified
    Given there are 21 proxy vote applications exist with the status "RECEIVED"
    When I send a get proxy vote request without the page size
    Then I received a response with 20 proxy vote applications