@IerClient
Feature: Get ERO identifier from IER by certificate serial number

  Scenario: Get ERO certificate mapping by certificate serial number
    Given the certificate serial number "1234567891" mapped to Ero Id "camden-city-council"
    When I send a request to get the mapping by serial number
    Then the system sent a request to get the mapping
    And I received mapping response with the Ero Id "camden-city-council"

  Scenario: the system cache the certificate mapping response from IER
    Given the certificate serial number "1234567891" mapped to Ero Id "camden-city-council"
    When I send a request to get the mapping by serial number
    Then the system sent a request to get the mapping
    And I received mapping response with the Ero Id "camden-city-council"
    When I send a request to get the mapping by serial number
    Then the system did not send a request to get the mapping
    And I received mapping response with the Ero Id "camden-city-council"

  Scenario: the system clear the cache once TTL (2 seconds) is passed
    Given the certificate serial number "1234567892" mapped to Ero Id "bristol-city-council"
    When I send a request to get the mapping by serial number
    Then the system sent a request to get the mapping
    And I received mapping response with the Ero Id "bristol-city-council"
    And I waited for 3 seconds
    When I send a request to get the mapping by serial number
    Then the system sent a request to get the mapping
    And I received mapping response with the Ero Id "bristol-city-council"
