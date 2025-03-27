package uk.gov.dluhc.emsintegrationapi.testsupport.testhelpers

import io.awspring.cloud.sqs.operations.SqsTemplate
import mu.KLogging
import org.assertj.core.api.Assertions
import org.awaitility.kotlin.await
import org.springframework.data.repository.CrudRepository
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.dluhc.emsintegrationapi.config.ApiClient
import uk.gov.dluhc.emsintegrationapi.config.QueueConfiguration
import uk.gov.dluhc.emsintegrationapi.constants.ApplicationConstants
import uk.gov.dluhc.emsintegrationapi.database.entity.ProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.ProxyVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.Constants
import uk.gov.dluhc.emsintegrationapi.mapper.ProxyVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.ProxyVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.models.ProxyVoteApplications
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.ProxyVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_BASE64_STRING
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_WAIVER_REASON
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildProxyVoteApplicationMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerEroDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerLocalAuthorityDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.validateObjects
import java.time.Instant
import java.time.Period
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import kotlin.jvm.optionals.getOrNull

class ProxyIntegrationTestHelpers(
    private val wiremockService: WiremockService,
    private val proxyVoteApplicationRepository: ProxyVoteApplicationRepository? = null,
    private val queueMessagingTemplate: SqsTemplate,
    private val messageSenderProxy: MessageSender<ProxyVoteApplicationMessage>? = null,
) {
    val logger = KLogging().logger

    fun givenEroIdAndGssCodesMapped() {
        // Given the certificate serial number "1234567891" mapped to the ERO Id "camden-city-council"
        // And the gss codes "E12345678" and "E12345679" mapped to the ERO Id
        wiremockService.stubIerApiGetEros(
            listOf(
                buildIerEroDetails(
                    eroIdentifier = "camden-city-council",
                    name = "Camden City Council",
                    localAuthorities =
                    listOf(
                        buildIerLocalAuthorityDetails(gssCode = "E12345678"),
                        buildIerLocalAuthorityDetails(gssCode = "E12345679"),
                    ),
                    activeClientCertificateSerials = listOf("1234567891"),
                ),
            ),
        )
    }

    fun readMessage(queueName: String): EmsConfirmedReceiptMessage? {
        val message =
            queueMessagingTemplate
                .receive(
                    queueName,
                    EmsConfirmedReceiptMessage::class.java,
                ).map { it.payload }
                .getOrNull()
        return message
    }

    fun createProxyApplicationWithApplicationId(
        applicationId: String,
        gssCode: String,
        recordStatus: String? = "RECEIVED",
        daysAgo: Int? = 1,
    ) {
        val proxyVoteApplication =
            buildProxyVoteApplication(
                applicationId = applicationId,
                recordStatus = RecordStatus.valueOf(recordStatus!!),
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
            )
        // Application needs to be saved first to avoid the date created being overwritten
        proxyVoteApplicationRepository?.save(proxyVoteApplication)
        proxyVoteApplication.dateCreated = Instant.now().minus(Period.ofDays(daysAgo!!))
        proxyVoteApplicationRepository?.save(proxyVoteApplication)
    }

    fun checkQueueHasMessage(
        queueName: String,
        expectedMessage: EmsConfirmedReceiptMessage,
    ) {
        await
            .atMost(2, TimeUnit.SECONDS)
            .untilAsserted {
                val actualMessage = readMessage(queueName)
                Assertions
                    .assertThat(actualMessage)
                    .isNotNull
                    .isEqualTo(expectedMessage)
            }
    }

    fun sendGetRequestWithCertificateSerialNumberAndOptionalPageSize(
        apiClient: ApiClient,
        path: String,
        certificateSerialNumber: String,
        pageSize: Int? = null,
    ): WebTestClient.ResponseSpec {
        logger.info { "Sending get request with page size $pageSize" }
        val builder = UriComponentsBuilder.fromUriString(path)
        pageSize?.let { builder.queryParam(ApplicationConstants.PAGE_SIZE_PARAM, it) }
        return apiClient.get(
            uri = builder.build().toUriString(),
            serialNumber = certificateSerialNumber,
        )
    }

    fun buildProxyVoteApplications(
        numberOfRecords: Int,
        recordStatus: String,
        vararg gssCodes: String,
        signatureBase64: String? = SIGNATURE_BASE64_STRING,
        signatureWaived: Boolean? = false,
        signatureWaiverReason: String? = null,
    ): Map<String, ProxyVoteApplication> {
        logger.info("Creating $numberOfRecords of proxy vote applications")
        val proxyVoteApplications =
            saveRecords(
                proxyVoteApplicationRepository!!,
                numberOfRecords,
            ) {
                buildProxyVoteApplication(
                    recordStatus = RecordStatus.valueOf(recordStatus),
                    applicationDetails =
                    buildApplicationDetailsEntity(
                        gssCode = DataFaker.faker.options().option(*gssCodes),
                        signatureBase64 = signatureBase64,
                        signatureWaived = signatureWaived,
                        signatureWaivedReason = signatureWaiverReason,
                    ),
                )
            }
        // Let us create a map out of it so it will be easy for the validation
        return proxyVoteApplications.associateBy { it.applicationId }
    }

    fun validateProxyResponse(
        proxyVoteApplicationsMap: Map<String, ProxyVoteApplication>,
        hasSignature: Boolean,
        expectedPageSize: Int,
        responseSpec: WebTestClient.ResponseSpec,
    ) {
        val proxyApplications =
            ApiClient.validateStatusAndGetResponse(
                responseSpec,
                expectedHttpStatus = 200,
                ProxyVoteApplications::class.java,
            )
        Assertions.assertThat(proxyApplications).isNotNull
        Assertions.assertThat(proxyApplications!!.proxyVotes).hasSize(expectedPageSize)

        if (hasSignature) {
            validateProxyApplicationsWithSignature(proxyApplications, proxyVoteApplicationsMap)
        } else {
            validateProxyApplicationsWithoutSignature(proxyApplications, proxyVoteApplicationsMap)
        }
    }

    private fun validateProxyApplicationsWithoutSignature(
        proxyApplications: ProxyVoteApplications?,
        proxyVoteApplicationsMap: Map<String, ProxyVoteApplication>,
    ) {
        proxyApplications!!.proxyVotes!!.forEach { proxyVote ->
            ProxyVoteAssert
                .assertThat(proxyVote)
                .hasCorrectFieldsFromProxyApplication(proxyVoteApplicationsMap[proxyVote.id]!!)
                .signatureWaived()
                .hasSignatureWaiverReason(SIGNATURE_WAIVER_REASON)
                .hasNoSignature()
        }
    }

    private fun validateProxyApplicationsWithSignature(
        proxyApplications: ProxyVoteApplications?,
        proxyVoteApplicationsMap: Map<String, ProxyVoteApplication>,
    ) {
        proxyApplications!!.proxyVotes!!.forEach { proxyVote ->
            ProxyVoteAssert
                .assertThat(proxyVote)
                .hasCorrectFieldsFromProxyApplication(proxyVoteApplicationsMap[proxyVote.id]!!)
                .hasSignature(SIGNATURE_BASE64_STRING)
                .hasSignatureWaiverReason("false")
        }
    }

    fun buildProxyVoteApplicationsWithSignature(
        applicationStatus: String,
        applicationId: String,
        emsElectorId: String,
    ): ProxyVoteApplicationMessage =
        buildProxyVoteApplicationMessageDto(
            applicationDetails =
            buildApplicationDetailsMessageDto(
                applicationStatus =
                ApplicationDetails.ApplicationStatus.valueOf(
                    applicationStatus,
                ),
                applicationId = applicationId,
                signatureBase64 = SIGNATURE_BASE64_STRING,
            ),
            applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId),
        )

    infix fun sendMessage(proxyVoteApplicationMessage: ProxyVoteApplicationMessage) {
        with(proxyVoteApplicationMessage) {
            logger.info(
                "Send proxy application with id = ${applicationDetails.id} and electoral id = ${applicantDetails.emsElectorId} the queue",
            )
            messageSenderProxy?.send(proxyVoteApplicationMessage, QueueConfiguration.QueueName.PROXY_APPLICATION_QUEUE)
        }
    }

    fun buildProxyVoteApplicationWith(
        applicationId: String,
        emsElectorId: String,
    ) = buildProxyVoteApplicationMessageDto(
        applicationDetails =
        buildApplicationDetailsMessageDto(
            applicationId = applicationId,
        ),
        applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId),
    )

    fun checkProxyApplicationSuccessfullySavedWithSignatureWaiver(
        proxyVoteApplicationMessage: ProxyVoteApplicationMessage,
        waiverReason: String,
    ) {
        await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
            val optSavedEntity =
                proxyVoteApplicationRepository?.findById(proxyVoteApplicationMessage.applicationDetails.id)
            Assertions.assertThat(optSavedEntity).isNotNull
            optSavedEntity?.let {
                val savedEntity = optSavedEntity.get()
                Assertions.assertThat(savedEntity.applicationDetails.signatureWaived).isEqualTo(true)
                Assertions.assertThat(savedEntity.applicationDetails.signatureWaivedReason).isEqualTo(waiverReason)
                Assertions.assertThat(savedEntity.applicationDetails.signatureBase64).isNull()
            }
        }
    }

    fun checkProxyApplicationSuccessfullySaved(
        proxyVoteApplicationMessage: ProxyVoteApplicationMessage,
        applicationId: String,
        applicationStatus: String,
    ) {
        await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
            val optSavedEntity = proxyVoteApplicationRepository?.findById(applicationId)
            Assertions.assertThat(optSavedEntity).isNotNull
            optSavedEntity?.let {
                validateSavedEntityProxy(proxyVoteApplicationMessage, it.get(), applicationStatus)
                logger.info("Successfully validated the postal application with the id = $applicationId")
            }
        }
    }

    private fun validateSavedEntityProxy(
        proxyVoteApplicationMessage: ProxyVoteApplicationMessage,
        proxyVoteApplication: ProxyVoteApplication,
        applicationStatus: String,
    ) {
        validateObjects(
            proxyVoteApplicationMessage,
            proxyVoteApplication,
            *Constants.APPLICATION_FIELDS_TO_IGNORE,
        )
        Assertions.assertThat(proxyVoteApplication.applicationDetails.applicationStatus).isEqualTo(
            uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails.ApplicationStatus.valueOf(
                applicationStatus,
            ),
        )
        Assertions
            .assertThat(proxyVoteApplication.applicationDetails.signatureBase64)
            .isEqualTo(SIGNATURE_BASE64_STRING)
    }

    private fun <T> saveRecords(
        repository: CrudRepository<T, *>,
        numberOfRecords: Int,
        buildFunction: () -> T,
    ): MutableIterable<T> =
        repository
            .saveAll(
                IntStream
                    .rangeClosed(1, numberOfRecords)
                    .mapToObj { buildFunction() }
                    .toList(),
            )

    fun createProxyVoteApplication(
        proxyVoteApplicationMessage: ProxyVoteApplicationMessage,
        proxyVoteApplicationMessageMapper: ProxyVoteApplicationMessageMapper,
    ): () -> Unit =
        {
            proxyVoteApplicationRepository?.saveAndFlush(
                proxyVoteApplicationMessageMapper.mapToEntity(
                    proxyVoteApplicationMessage,
                ),
            )
        }

    fun confirmTheApplicationDidNotSave(applicationId: String): () -> Unit =
        {
            Assertions.assertThat(applicationId.trim()).hasSizeGreaterThan(1)
            await.during(5, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted {
                Assertions.assertThat(proxyVoteApplicationRepository?.findById(applicationId)).isEmpty
            }
        }

    fun confirmTheEntitySaved(applicationId: String): () -> Unit =
        {
            await.during(5, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted {
                Assertions.assertThat(proxyVoteApplicationRepository?.findById(applicationId)).isPresent
            }
        }
}
