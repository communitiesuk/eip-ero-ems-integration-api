package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.ElementCollection
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Version
import jakarta.validation.Valid

@Entity
@EntityListeners(AuditingEntityListener::class)
class PostalVoteApplication(
    @Id
    val applicationId: String,

    @field:Valid
    @Embedded
    val applicationDetails: ApplicationDetails,

    @field:Valid
    @Embedded
    val applicantDetails: ApplicantDetails,

    @field:Valid
    @Embedded
    var postalVoteDetails: PostalVoteDetails? = null,

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

    var englishRejectionNotes: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "postal_vote_application_english_rejection_reasons",
        joinColumns = [JoinColumn(name = "application_id")]
    )
    var englishRejectedReasonItems: Set<RejectedReasonItem>? = mutableSetOf(),

    var welshRejectionNotes: String? = null,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "application_id")
    @NotFound(action = NotFoundAction.IGNORE)
    var primaryElectorDetails: PostalVoteApplicationPrimaryElectorDetails? = null,

    @ElementCollection
    @CollectionTable(
        name = "postal_vote_application_welsh_rejection_reasons",
        joinColumns = [JoinColumn(name = "application_id")]
    )
    var welshRejectedReasonItems: Set<RejectedReasonItem>? = mutableSetOf(),

    @Version
    var version: Long? = null,
) {
    override fun equals(other: Any?) = areEqual(this, other, PostalVoteApplication::applicationId)
    override fun hashCode() = applicationId.hashCode()
}
