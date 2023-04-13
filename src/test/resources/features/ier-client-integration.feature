@IerClient
Feature: Get ERO ID by certificate serial number and fetch GSS Code by ERO ID

  Scenario: Get ERO certificate mapping by certificate serial number
    Given the certificate serial number "1234567891" mapped to Ero Id "camden-city-council"
    When I send a request to get the mapping by serial number
    Then the system sent a request to get the mapping
    And I received mapping response with the Ero Id "camden-city-council"

  Scenario: the system cache the certificate mapping response from IER
    Given the certificate serial number "1234567891" mapped to Ero Id "camden-city-council"
    When I send a request to get the mapping by serial number
    And I received mapping response with the Ero Id "camden-city-council"
    When I send a request to get the mapping by serial number
    Then the system sent only one get mapping request
    And I received mapping response with the Ero Id "camden-city-council"

  Scenario: the system clear the cache once TTL (2 seconds) is passed
    Given the certificate serial number "1234567892" mapped to Ero Id "bristol-city-council"
    When I send a request to get the mapping by serial number
    And I received mapping response with the Ero Id "bristol-city-council"
    And I waited for 3 seconds
    When I send a request to get the mapping by serial number
    And I received mapping response with the Ero Id "bristol-city-council"
    Then the system sent 2 get mapping requests

  Scenario: Get GSS Code by ERO ID
    Given the certificate serial number "1234567891" mapped to Ero Id "camden-city-council"
    And the gss codes "E12345678" and "E12345679" mapped to ERO Id
    When I send a request to get the gss codes
    Then I received the gss codes "E12345678" and "E12345679"
