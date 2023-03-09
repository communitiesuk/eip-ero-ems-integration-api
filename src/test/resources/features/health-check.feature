Feature: Service Health Check

  Scenario: The service is up and running
    Given The service is up and running
    When I send a health check request
    Then I will get the status is "UP"