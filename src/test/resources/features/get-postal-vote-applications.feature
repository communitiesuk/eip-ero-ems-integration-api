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
    Then I received a response with 0 postal vote applications with signature

  @ClearCache
  Scenario: System returns http status 404 if the attached certificate serial number does not exist
    Given the certificate serial "INVALID123" does not exist in ERO
    When I send a get postal vote applications request with the page size 10 and the certificate serial number "INVALID123"
    Then I received the http status 404
    And it has an error message of "The EROCertificateMapping for certificateSerial=[INVALID123] could not be found"

  @ClearCache
  Scenario: System returns http status 500 if ERO could not process the get mapping request
    Given the ERO could not process the get mapping request for "1234567899"
    When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567899"
    Then I received the http status 500
    And it has an error message of "Unable to retrieve EROCertificateMapping for certificate serial [1234567899] due to error: [500 Server Error: \"Error\"]"

  @ClearCache
  Scenario: System returns http status 404 if ERO Mapping Id does not exist
    Given the ERO Id "camden-city-council" does not exist in ERO
    When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
    Then I received the http status 404
    And it has an error message of "The ERO camden-city-council could not be found"

  @ClearCache
  Scenario: System returns http status 500 if ERO could not process the get gss code request
    Given the ERO could not process the get gss codes request for "camden-city-council"
    When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
    Then I received the http status 500
    And the error message contains "Unable to retrieve GSS Codes for camden-city-council due to error: [500 Internal Server Error from GET"

  @DeletePostalEntity @ClearCache
  Scenario: System returns postal vote applications of a given page size
    Given there are 20 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
    When I send a get postal vote applications request with the page size 10 and the certificate serial number "1234567891"
    Then I received a response with 10 postal vote applications with signature

  @DeletePostalEntity @ClearCache
  Scenario: System does not have requested number of postal applications
    Given there are 2 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
    When I send a get postal vote applications request with the page size 3 and the certificate serial number "1234567891"
    Then I received a response with 2 postal vote applications with signature

  @DeletePostalEntity @ClearCache
  Scenario: System returns default number of records if page size is not specified
    Given there are 21 postal vote applications exist with the signature, status "RECEIVED" and GSS Codes "E12345678","E12345679"
    When I send a get postal vote request without the page size and with the certificate serial number "1234567891"
    Then I received a response with 20 postal vote applications with signature

  @DeletePostalEntity @ClearCache
  Scenario: System returns postal vote applications with signature waiver reason
    Given there are 21 postal vote applications without signature exist with the status "RECEIVED" and GSS Codes "E12345678","E12345679"
    When I send a get postal vote request without the page size and with the certificate serial number "1234567891"
    Then I received a response with 20 postal vote applications with signature waiver

