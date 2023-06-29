package uk.gov.dluhc.emsintegrationapi.database.entity

import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.validation.Valid
import javax.validation.constraints.Size

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
