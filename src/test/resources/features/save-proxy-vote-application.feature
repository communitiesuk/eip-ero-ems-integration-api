@Proxy @DeleteProxyEntity @DeleteProxyMessage
Feature: System process an approved/rejected proxy vote application message

  Scenario Outline: The system process and saves a approved/rejected proxy vote application message with signature into the database
    Given a proxy vote application with the application id "<ProxyApplicationId>", electoral id "<EmsElectoralId>" and status "<ApplicationStatus>"
    When I send an sqs message to the proxy application queue
    Then the "<ApplicationStatus>" proxy vote application has been successfully saved with the application id "<ProxyApplicationId>" and signature
    Examples:
      | ProxyApplicationId       | EmsElectoralId                       | ApplicationStatus |
      | 602cf250036469154b4f85fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | APPROVED          |
      | 602cf250036469154b4f85fb | e87cbaea-0deb-4058-95c6-8240d426f5e2 | REJECTED          |

  Scenario Outline: The system process and saves a proxy vote application message with signature waiver details
    Given a proxy vote application with the application id "<ProxyApplicationId>" and signature waiver reason "<WaiverReason>"
    When I send an sqs message to the proxy application queue
    Then the proxy vote application has been successfully saved with the signature waiver reason "<WaiverReason>"
    Examples:
      | ProxyApplicationId       | WaiverReason |
      | 502cf250036469154b4f85fa | Disabled     |
      | 502cf250036469154b4f85fb | Other        |
      | 502cf250036469154b4f85fc | I have a disability that prevents me from signing or uploading my signature: Visually impaired and wants to use the full 250 character allowance. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam vehicula hendrerit eros consequat sagittis. Curabitur eget nisi ac felis tincidunt ultrices. Duis imperdiet tempus. |

  Scenario Outline: The system does not allow two different proxy applications having same id and a different ems electoral id
    Given a proxy vote application with the application id "<ProxyApplicationId>" and electoral id "<EmsElectoralId>" exists
    When I send an sqs message to the proxy application queue with an application id "<ProxyApplicationId>" and electoral id "<EmsElectoralId2>"
    Then the proxy vote application with id "<ProxyApplicationId>" and electoral id "<EmsElectoralId2>" did not save
    Examples:
      | ProxyApplicationId       | EmsElectoralId                       | EmsElectoralId2                      |
      | 602cf250036469154b4f85fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | e87cbaea-0deb-4058-95c6-8240d426f5e2 |


  Scenario Outline: The system does not allow two different proxy applications having a different application id and a same ems electoral id
    Given a proxy vote application with the application id "<ProxyApplicationId>" and electoral id "<EmsElectoralId>" exists
    When I send an sqs message to the proxy application queue with an application id "<ProxyApplicationId2>" and electoral id "<EmsElectoralId>"
    Then the proxy vote application with id "<ProxyApplicationId2>" was saved
    Examples:
      | ProxyApplicationId       | EmsElectoralId                       | ProxyApplicationId2      |
      | 602cf250036469154b4f85fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | 602cf250036469154b4f85fb |

  Scenario Outline: The system will reject a proxy vote application message with an invalid application id, application id must be 24 characters
    Given a proxy vote application with the application id "<InvalidProxyApplicationId>", electoral id "<EmsElectoralId>" and status "<ApplicationStatus>"
    When I send an sqs message to the proxy application queue
    Then the proxy vote application with id "<InvalidProxyApplicationId>" did not save
    Examples:
      | InvalidProxyApplicationId | EmsElectoralId                       | ApplicationStatus |
      | 123456                    | e87cbaea-0deb-4058-95c6-8240d426f5e4 | APPROVED          |
      | 123457                    | e87cbaea-0deb-4058-95c6-8240d426f5e6 | REJECTED          |
