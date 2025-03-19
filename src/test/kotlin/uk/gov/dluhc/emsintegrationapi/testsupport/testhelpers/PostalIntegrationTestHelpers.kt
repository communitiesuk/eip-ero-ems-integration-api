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
import uk.gov.dluhc.emsintegrationapi.database.entity.PostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.database.entity.RecordStatus
import uk.gov.dluhc.emsintegrationapi.database.repository.PostalVoteApplicationRepository
import uk.gov.dluhc.emsintegrationapi.mapper.Constants
import uk.gov.dluhc.emsintegrationapi.mapper.PostalVoteApplicationMessageMapper
import uk.gov.dluhc.emsintegrationapi.messaging.MessageSender
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails
import uk.gov.dluhc.emsintegrationapi.messaging.models.EmsConfirmedReceiptMessage
import uk.gov.dluhc.emsintegrationapi.messaging.models.PostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.models.PostalVoteApplications
import uk.gov.dluhc.emsintegrationapi.testsupport.WiremockService
import uk.gov.dluhc.emsintegrationapi.testsupport.assertj.assertions.PostalVoteAssert
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.DataFaker
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_BASE64_STRING
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.SIGNATURE_WAIVER_REASON
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicantDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsEntity
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildApplicationDetailsMessageDto
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplication
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.buildPostalVoteApplicationMessage
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerEroDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.testdata.models.buildIerLocalAuthorityDetails
import uk.gov.dluhc.emsintegrationapi.testsupport.validateObjects
import java.time.Instant
import java.time.Period
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream
import kotlin.jvm.optionals.getOrNull

class PostalIntegrationTestHelpers(
    private val wiremockService: WiremockService,
    private val postalVoteApplicationRepository: PostalVoteApplicationRepository? = null,
    private val queueMessagingTemplate: SqsTemplate,
    private val messageSenderPostal: MessageSender<PostalVoteApplicationMessage>? = null,
) {
    private val logger = KLogging().logger

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

    fun createPostalApplicationWithApplicationId(
        applicationId: String,
        gssCode: String,
        recordStatus: String? = "RECEIVED",
        daysAgo: Int? = 1,
    ) {
        val postalVoteApplication =
            buildPostalVoteApplication(
                applicationId = applicationId,
                recordStatus = RecordStatus.valueOf(recordStatus!!),
                applicationDetails = buildApplicationDetailsEntity(gssCode = gssCode),
            )
        // Application needs to be saved first to avoid the date created being overwritten
        postalVoteApplicationRepository?.save(postalVoteApplication)
        postalVoteApplication.dateCreated = Instant.now().minus(Period.ofDays(daysAgo!!))
        postalVoteApplicationRepository?.save(postalVoteApplication)
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

    fun sendMessage(postalVoteApplicationMessage: PostalVoteApplicationMessage) {
        with(postalVoteApplicationMessage) {
            logger.info(
                "Send postal application with id = ${applicationDetails.id} and electoral id = ${applicantDetails.emsElectorId} the queue",
            )
            messageSenderPostal?.send(
                postalVoteApplicationMessage,
                QueueConfiguration.QueueName.POSTAL_APPLICATION_QUEUE,
            )
        }
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

    fun buildPostalVoteApplications(
        numberOfRecords: Int,
        recordStatus: String,
        vararg gssCodes: String,
        signatureBase64: String? = SIGNATURE_BASE64_STRING,
        signatureWaived: Boolean? = false,
        signatureWaiverReason: String? = null,
    ): Map<String, PostalVoteApplication> {
        logger.info("Creating $numberOfRecords of postal vote applications")
        val postalVoteApplications =
            saveRecords(
                postalVoteApplicationRepository!!,
                numberOfRecords,
            ) {
                buildPostalVoteApplication(
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
        return postalVoteApplications.associateBy { it.applicationId }
    }

    fun buildPostalVoteApplicationsWithSignature(
        applicationStatus: String,
        applicationId: String,
        emsElectorId: String,
    ): PostalVoteApplicationMessage =
        buildPostalVoteApplicationMessage(
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

    fun buildPostalVoteApplicationWith(
        applicationId: String,
        emsElectorId: String,
    ) = buildPostalVoteApplicationMessage(
        applicationDetails =
            buildApplicationDetailsMessageDto(
                applicationId = applicationId,
            ),
        applicantDetails = buildApplicantDetailsMessageDto(emsElectorId = emsElectorId),
    )

    fun validatePostalResponse(
        postalVoteApplicationsMap: Map<String, PostalVoteApplication>,
        hasSignature: Boolean,
        expectedPageSize: Int,
        apiResponse: WebTestClient.ResponseSpec,
    ) {
        logger.info("Expected number of postal vote applications with signature = $expectedPageSize")
        val postalApplications =
            ApiClient.validateStatusAndGetResponse(
                apiResponse = apiResponse,
                expectedHttpStatus = 200,
                type = PostalVoteApplications::class.java,
            )
        Assertions.assertThat(postalApplications).isNotNull
        Assertions.assertThat(postalApplications!!.postalVotes).hasSize(expectedPageSize)

        if (hasSignature) {
            validatePostalApplicationsWithSignature(postalApplications, postalVoteApplicationsMap)
        } else {
            validatePostalApplicationsWithoutSignature(postalApplications, postalVoteApplicationsMap)
        }
    }

    private fun validatePostalApplicationsWithoutSignature(
        postalApplications: PostalVoteApplications?,
        postalVoteApplicationsMap: Map<String, PostalVoteApplication>,
    ) {
        postalApplications!!.postalVotes!!.forEach { postalVote ->
            PostalVoteAssert
                .assertThat(postalVote)
                .hasCorrectFieldsFromPostalApplication(postalVoteApplicationsMap[postalVote.id]!!)
                .signatureWaived()
                .hasSignatureWaiverReason(SIGNATURE_WAIVER_REASON)
                .hasNoSignature()
        }
    }

    private fun validatePostalApplicationsWithSignature(
        postalApplications: PostalVoteApplications?,
        postalVoteApplicationsMap: Map<String, PostalVoteApplication>,
    ) {
        postalApplications!!.postalVotes!!.forEach { postalVote ->
            PostalVoteAssert
                .assertThat(postalVote)
                .hasCorrectFieldsFromPostalApplication(postalVoteApplicationsMap[postalVote.id]!!)
                .hasSignature(SIGNATURE_BASE64_STRING)
                .hasSignatureWaiverReason("false")
        }
    }

    fun checkPostalApplicationSuccessfullySaved(
        postalVoteApplicationMessage: PostalVoteApplicationMessage,
        applicationId: String,
        applicationStatus: String,
    ) {
        await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
            val optSavedEntity = postalVoteApplicationRepository?.findById(applicationId)
            Assertions.assertThat(optSavedEntity).isNotNull
            optSavedEntity?.let {
                validateSavedEntityPostal(postalVoteApplicationMessage, it.get(), applicationStatus)
                logger.info("Successfully validated the postal application with the id = $applicationId")
            }
        }
    }

    fun checkPostalApplicationSuccessfullySavedWithSignatureWaiver(
        postalVoteApplicationMessage: PostalVoteApplicationMessage,
        waiverReason: String,
    ) {
        await.pollDelay(2, TimeUnit.SECONDS).atMost(5, TimeUnit.SECONDS).untilAsserted {
            val optSavedEntity =
                postalVoteApplicationRepository?.findById(postalVoteApplicationMessage.applicationDetails.id)
            Assertions.assertThat(optSavedEntity).isNotNull
            optSavedEntity?.let {
                val savedEntity = optSavedEntity.get()
                Assertions.assertThat(savedEntity.applicationDetails.signatureWaived).isEqualTo(true)
                Assertions.assertThat(savedEntity.applicationDetails.signatureWaivedReason).isEqualTo(waiverReason)
                Assertions.assertThat(savedEntity.applicationDetails.signatureBase64).isNull()
            }
        }
    }

    private fun validateSavedEntityPostal(
        postalVoteApplicationMessage: PostalVoteApplicationMessage,
        postalVoteApplication: PostalVoteApplication,
        applicationStatus: String,
    ) {
        validateObjects(
            postalVoteApplicationMessage,
            postalVoteApplication,
            *Constants.APPLICATION_FIELDS_TO_IGNORE,
        )
        Assertions.assertThat(postalVoteApplication.applicationDetails.applicationStatus).isEqualTo(
            uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails.ApplicationStatus.valueOf(
                applicationStatus,
            ),
        )
        Assertions
            .assertThat(postalVoteApplication.applicationDetails.signatureBase64)
            .isEqualTo(SIGNATURE_BASE64_STRING)
        Assertions.assertThat(postalVoteApplication.postalVoteDetails?.ballotOverseasAddress).isNotNull
        Assertions.assertThat(postalVoteApplication.postalVoteDetails?.ballotBfpoAddress).isNotNull
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

    fun createPostalVoteApplication(
        postalVoteApplicationMessage: PostalVoteApplicationMessage,
        postalVoteApplicationMessageMapper: PostalVoteApplicationMessageMapper,
    ): () -> Unit =
        {
            postalVoteApplicationRepository?.saveAndFlush(
                postalVoteApplicationMessageMapper.mapToEntity(
                    postalVoteApplicationMessage,
                ),
            )
        }

    fun confirmTheApplicationDidNotSave(applicationId: String): () -> Unit =
        {
            Assertions.assertThat(applicationId.trim()).hasSizeGreaterThan(1)
            await.during(5, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted {
                Assertions.assertThat(postalVoteApplicationRepository?.findById(applicationId)).isEmpty
            }
        }

    fun confirmTheEntitySaved(applicationId: String): () -> Unit =
        {
            await.during(5, TimeUnit.SECONDS).atMost(6, TimeUnit.SECONDS).untilAsserted {
                Assertions.assertThat(postalVoteApplicationRepository?.findById(applicationId)).isPresent
            }
        }
}
