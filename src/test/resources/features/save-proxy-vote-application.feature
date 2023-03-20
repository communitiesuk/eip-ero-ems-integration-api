@Proxy @DeleteProxyEntity @DeleteProxyMessage
Feature: System process an approved/rejected proxy vote application message

  Scenario Outline: The system process and saves a proxy vote application message into the database
    Given a proxy vote application with the application id "<ProxyApplicationId>" and electoral id "<EmsElectoralId>"
    When I send an sqs message to the proxy application queue
    Then the proxy vote application has been successfully saved with the application id "<ProxyApplicationId>" and electoral id "<EmsElectoralId>"
    Examples:
      | ProxyApplicationId       | EmsElectoralId                       |
      | 602cf250036469154b4f85fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 |
      | 602cf250036469154b4f85fb | e87cbaea-0deb-4058-95c6-8240d426f5e2 |

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
    Then the proxy vote application with id "<ProxyApplicationId2>" did not save
    Examples:
      | ProxyApplicationId       | EmsElectoralId                       | ProxyApplicationId2      |
      | 602cf250036469154b4f85fa | e87cbaea-0deb-4058-95c6-8240d426f5e1 | 602cf250036469154b4f85fb |

  Scenario Outline: The system will reject a proxy vote application message with an invalid application id, application id must be 24 characters
    Given a proxy vote application with the application id "<InvalidProxyApplicationId>" and electoral id "<EmsElectoralId>"
    When I send an sqs message to the proxy application queue
    Then the proxy vote application with id "<InvalidProxyApplicationId>" did not save
    Examples:
      | InvalidProxyApplicationId | EmsElectoralId                       |
      | 123456                    | e87cbaea-0deb-4058-95c6-8240d426f5e4 |
      | 123457                    | e87cbaea-0deb-4058-95c6-8240d426f5e6 |