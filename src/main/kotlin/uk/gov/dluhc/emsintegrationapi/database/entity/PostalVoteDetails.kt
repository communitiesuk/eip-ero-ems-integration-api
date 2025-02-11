package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import java.time.LocalDate
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.validation.Valid
import jakarta.validation.constraints.Size

@Embeddable
class PostalVoteDetails(
    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "ballot_address_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @field:Valid
    var ballotAddress: Address? = null,

    @field:Size(max = 500)
    var ballotAddressReason: String? = null,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "ballot_overseas_address_id")
    @NotFound(action = NotFoundAction.IGNORE)
    var ballotOverseasAddress: OverseasAddress? = null,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "ballot_bfpo_address_id")
    @NotFound(action = NotFoundAction.IGNORE)
    var ballotBfpoAddress: BfpoAddress? = null,

    var voteForSingleDate: LocalDate? = null,

    var voteStartDate: LocalDate? = null,

    var voteEndDate: LocalDate? = null,

    @Column(name = "vote_until_further_notice", columnDefinition = "BIT")
    var voteUntilFurtherNotice: Boolean? = null
)
