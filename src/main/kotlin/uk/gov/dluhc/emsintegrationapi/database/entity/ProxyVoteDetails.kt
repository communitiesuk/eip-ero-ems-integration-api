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
class ProxyVoteDetails(
    @field:Size(max = 35)
    val proxyFirstName: String,

    @field:Size(max = 100)
    var proxyMiddleNames: String? = null,

    @field:Size(max = 35)
    val proxySurname: String,

    @field:Size(max = 500)
    var proxyFamilyRelationship: String? = null,

    @OneToOne(
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "proxy_address_id")
    @NotFound(action = NotFoundAction.IGNORE)
    @field:Valid
    var proxyAddress: Address? = null,

    @field:Size(max = 500)
    val proxyReason: String,

    @field:Size(max = 255)
    var proxyEmail: String? = null,

    @field:Size(max = 50)
    var proxyPhone: String? = null,

    var voteForSingleDate: LocalDate? = null,

    var voteStartDate: LocalDate? = null,

    var voteEndDate: LocalDate? = null,

    @Column(name = "vote_until_further_notice", columnDefinition = "BIT")
    var voteUntilFurtherNotice: Boolean? = null
)
