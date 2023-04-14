@IerClient @ClearCache
Feature: Get ERO ID by certificate serial number and Get GSS Codes by ERO ID

  Scenario: Get ERO certificate mapping by certificate serial number
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    When I send a request to get the mapping by serial number
    Then the system sent a request to get the mapping
    And I received mapping response with the ERO Id "camden-city-council"

  Scenario: Get ERO certificate mapping by certificate serial number - the system cache the certificate mapping response from IER
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    When I send a request to get the mapping by serial number
    And I received mapping response with the ERO Id "camden-city-council"
    When I send a request to get the mapping by serial number
    Then the system sent only one get mapping request
    And I received mapping response with the ERO Id "camden-city-council"

  Scenario: Get ERO certificate mapping by certificate serial number - the system clear the cache once TTL (2 seconds) is passed
    Given the certificate serial number "1234567892" mapped to the ERO Id "bristol-city-council"
    When I send a request to get the mapping by serial number
    And I received mapping response with the ERO Id "bristol-city-council"
    And I waited for 3 seconds
    When I send a request to get the mapping by serial number
    And I received mapping response with the ERO Id "bristol-city-council"
    Then the system sent 2 get mapping requests

  Scenario: Get GSS Codes By ERO ID
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
    When I send a request to get the gss codes
    Then I received the gss codes "E12345678" and "E12345679"

  Scenario: Get GSS Codes By ERO ID - the system cache get gss code response
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
    When I send a request to get the gss codes
    Then I received the gss codes "E12345678" and "E12345679"
    When I send a request to get the gss codes
    Then the system sent only one get gss codes request

  Scenario: Get GSS Codes By ERO ID - the system clear the cache once TTL (2 seconds) is passed
    Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
    And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
    When I send a request to get the gss codes
    Then I received the gss codes "E12345678" and "E12345679"
    And I waited for 3 seconds
    When I send a request to get the gss codes
    Then the system sent 2 get gss codes requests
