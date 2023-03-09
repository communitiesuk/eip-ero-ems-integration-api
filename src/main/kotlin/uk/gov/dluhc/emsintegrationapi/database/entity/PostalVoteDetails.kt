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

@Embeddable
class PostalVoteDetails(
    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "ballot_address_id")
    @NotFound(action = NotFoundAction.IGNORE)
    var ballotAddress: Address? = null,

    var ballotAddressReason: String? = null,

    var voteForSingleDate: LocalDate? = null,

    var voteStartDate: LocalDate? = null,

    var voteEndDate: LocalDate? = null,
    @Column(name = "postal_vote_until_further_notice", columnDefinition = "BIT")
    var voteUntilFurtherNotice: Boolean? = null
)
