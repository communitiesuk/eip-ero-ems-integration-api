This directory contains a self signed certificate that is used by the integration tests in place of the DWP client certificate.

* `client-cert.pem` - the client certificate file
* `private-key.pem` - the private key used to generate the certificate

The command used to generate the above was:
```shell
openssl req -x509 -newkey rsa:4096 -keyout private-key.pem -out client-cert.pem -sha256 -days 3650 -nodes -config openssl-csr.conf
```

The certificate was generated with a 10 year (approx - 3650 days) expiry, so expires 09/09/2032

The certificate can be read with
```shell
openssl x509 -in src/test/resources/certificates/dwp/client-cert.pem -text
```

The certificate is used as part of the test spring context (integration tests) and is manually copy and pasted in
`src/test/resources/application-test.yml` in the property `api.dwp.client-certificate`

