package uk.gov.dluhc.emsintegrationapi.database.entity

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.EntityListeners
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.Version
import javax.validation.Valid

@Entity
@EntityListeners(AuditingEntityListener::class)
class ProxyVoteApplication(
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
    val proxyVoteDetails: ProxyVoteDetails,

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
        name = "proxy_vote_application_english_rejection_reasons",
        joinColumns = [JoinColumn(name = "application_id")]
    )
    @Column(name = "rejection_reasons", length = 50, nullable = false)
    var englishRejectionReasons: Set<String>? = mutableSetOf(),

    var welshRejectionNotes: String? = null,

    @ElementCollection
    @CollectionTable(
        name = "proxy_vote_application_welsh_rejection_reasons",
        joinColumns = [JoinColumn(name = "application_id")]
    )
    @Column(name = "rejection_reasons", length = 50, nullable = false)
    var welshRejectionReasons: Set<String>? = mutableSetOf(),

    @Version
    var version: Long? = null,
) {
    override fun equals(other: Any?) = areEqual(this, other, ProxyVoteApplication::applicationId)
    override fun hashCode() = applicationId.hashCode()
}
