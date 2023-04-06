package uk.gov.dluhc.emsintegrationapi.database.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Version
import javax.validation.Valid

@Entity
@EntityListeners(AuditingEntityListener::class)
class PostalVoteApplication(
    @Id
    val applicationId: String,

    @field:Valid
    @Embedded
    val approvalDetails: ApprovalDetails,

    @field:Valid
    @Embedded
    val applicantDetails: ApplicantDetails,

    @field:Valid
    @Embedded
    var postalVoteDetails: PostalVoteDetails? = null,

    val signatureBase64: String,

    var removalDateTime: Instant? = null,

    @Enumerated(EnumType.STRING)
    var retentionStatus: RetentionStatus,

    @CreatedDate
    var dateCreated: Instant? = null,

    @LastModifiedDate
    var dateUpdated: Instant? = null,

    @Enumerated(EnumType.STRING)
    val createdBy: SourceSystem,

    @Enumerated(EnumType.STRING)
    var updatedBy: SourceSystem? = null,

    @Enumerated(EnumType.STRING)
    var status: RecordStatus,

    @Version
    var version: Long? = null,
) {
    override fun equals(other: Any?) = areEqual(this, other, PostalVoteApplication::applicationId)
    override fun hashCode() = applicationId.hashCode()
}
