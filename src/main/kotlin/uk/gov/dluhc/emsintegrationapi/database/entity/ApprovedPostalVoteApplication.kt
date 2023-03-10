package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.Version

@Entity
@EntityListeners(AuditingEntityListener::class)
class ApprovedPostalVoteApplication(
    @Id
    val applicationId: String,

    @Embedded
    val approvalDetails: ApprovalDetails,

    @Embedded
    val applicantDetails: ApplicantDetails,

    @Embedded
    var postalVoteDetails: PostalVoteDetails? = null,

    val signatureBase64: String,

    var removalDateTime: Instant? = null,

    @Enumerated(EnumType.STRING)
    var retentionStatus: RetentionStatus,

    @CreationTimestamp
    var dateCreated: Instant? = null,

    @UpdateTimestamp
    var dateUpdated: Instant? = null,

    @Enumerated(EnumType.STRING)
    val createdBy: SourceSystem,

    @Enumerated(EnumType.STRING)
    var updatedBy: SourceSystem? = null,

    @Enumerated(EnumType.STRING)
    val status: RecordStatus,

    @Version
    var version: Long? = null,
) {
    override fun equals(other: Any?) = areEqual(this, other, ApprovedPostalVoteApplication::applicationId)
    override fun hashCode() = applicationId.hashCode()
}
