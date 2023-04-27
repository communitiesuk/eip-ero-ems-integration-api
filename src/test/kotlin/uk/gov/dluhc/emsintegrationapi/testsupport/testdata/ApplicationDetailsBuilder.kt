package uk.gov.dluhc.emsintegrationapi.testsupport.testdata

import uk.gov.dluhc.emsintegrationapi.database.entity.ApplicationDetails
import java.time.Instant
import java.time.ZoneOffset
import uk.gov.dluhc.emsintegrationapi.messaging.models.ApplicationDetails as ApplicationDetailsMessageDto

const val GSS_CODE = "E12345678"
const val GSS_CODE2 = "E12345679"
const val SIGNATURE_WAIVER_REASON = "Other"
const val SIGNATURE_BASE64_STRING =
    "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mmuuSa+//vr0U5/6VPrpT386vfXWW9PPfe5z6e23357Onz8/vfPOO9N77rknvf/++9MHH3wwfeSRR9JHH300ffzxx7uh1xr20EMPpQ888EC3nMprujvuuCP9/Oc/n372s5/tzlfz13Kuu+667nI/8YlPdNfhqquu6r7WX72//PLLu+t3ySWXTMdHPvKR9KKLLkovuOCC6bjwwgtzcfHFF3fL6LXGn3vuuenZZ5+dfvjDH56O008/PZ03b153f8ydO7e7H7WPtK/02u9L7cMjjzwy/eAHP9iNgw8+OD3ggAPS/fffvxv77rtvus8++6R77rlnuvvuu6e77rprussuu3T/vuc97+nGbrvt1v270047pTvuuGM3tt9++3SHHXbovlZ5TbvXXnt156X5ahlano4Brc9JJ53U/dy03toebZ/2jfbRFVdc0Q3ts2uvvTa94YYbpj/D2267rfs56jNYsGBBN/R5Kvz7fsN8hJ+fQseEPtuHH344feyxx9InnnhiOp588sn0qaee6r7WcaHxOjZUVseND73X8fKFL3xhOvTehy+vY8kfT4r77rsvvffee9O77767e2zddddd3df660PrHI5T+G30wzQPH9oWDdc0Kuen8eHH+fmH89a0moc/3jW99o/++rJarvZf+F0K97lC4/Q5af/6svobhobps1D4eYWfkd77dfL7QdN95jOfSW+66ab0xhtv7H7ndJzoO6I6QN8BHVuKTqfTPdaOOuqo9AMf+EB64IEHdo/vvffeu3sM77zzztPHrI7f7bbbLt12223TrbfeOn3Xu96VbrXVVuk73/nObui1QuPe/e53d2ObbbbpTqfvgb4feq1hGrfFFlukb3vb29KNNtooXX/99dP11lsvnT17djfWXXfddJ111knf9KY3dV9rWFjGD/N//Xi9f+Mb35iuueaa6aqrrpquuOKK6bLLLtuNpZdeuvt3+eWXz4WGLbfccukyyywzPd4P89NqnELz8K99mXh+ile/+tXp6quvnq6xxhrpa17zmnSllVaanrfGKbR+Gu7npb9LLbVUN7Sc+LX+xsP9+vj1D+cVlvVlnD5kJRwlHiUdffCq8Hzi8UlDSUcHhipKJRo/XMN8xelDZf04lZ+amuoeZGeccUZ61lln5ZLOJz/5yW4iuPnmm7sHqT/4fWWlg1hfLlUAqhB8RaFKRaHXvqLQFzCsGPQFCROOlqEvgJZ59dVXd5evSlMV6Mc+9rHp8EnHJw8fWu/zzjtvOvRew8Nhqpj9OL3W9mq7TzvttG74hKN9qP2kfax9rWSvJKO//rU+l0MPPTQ95JBDuslgv/326yYHJQmfbJRU9EXSF9MnF73eY489uuX0V2WUYDRc4/UF1hdXXz4NVxl9wTVvfdnDxKP1VMVw6qmndrdF26sk7JOOT9pK4qpY/Gd5yy235BKPr7B8BeYrvXB4WJn5yjE8adC8dDzos/bHgD/58IknTDo+ofgTEk2n13GSCRORXocnMEo2PuGoUvXHlsJXtGGCCBODT0L+OFZZH37e/aaP34fzC8v49fH7Mlym3+8a56fz7/147dvwcwqTkD/58xEmID8Pvw7+e6fhmlafv084OhnR903fKX03dBydc84506/1ndB3Qce7jj0dt/4kyh+3eq8EpNCw8DhWglH4hKNEpKSixKTwCUZlNZ3e+2S15ZZbpptttlm6ySabTCceJQ+fWJQ81l577W7i8cN9+ITjE1P43ied1VZbrVup+yQSJg2fLHxS0esw8fgyYdIJk09YwYfJR6+VUJRstF5aB1/GT69EuPLKK+fWKQyfKMoSTVlCUoTr6F8vueSS09vrxzklGoUqOV/pKfEoVOn4qxuFkodCiURJRcPCs3WfgPxwlVWFpYNKlZbO9HWg6Wznox/9aC7pKOHo4A/PyvyXS19OVQJhhRGepfqKwp/1hWdbmqc/49JydFWlCjJMOFoXfSHCqxtd2Sjp+ESiL4lCVy4+kWh79D4ep/Cv/dWNrha0L7RPtH+U2LWvfcIP/yrR+GSjvwcddFD3KkSJwScRn2j0BfTvwy+jxukLqsSkaRR676dTOf8l9PPReH3h3/ve93a//DoutJ7+ikfboe3UftG+uuyyy6YTj64U9VmqggkTT3jV48NXXnFlFiYeX3FqnObhk5jG63P2V7w+8fgrHp9whiUdTeuvkuPjSWX81U54IuMreJ8UfSLxCcIP98du+NonF19B+/BJzV+taPt8Re6TkX+tcT7CZfnpFD7x+KSiYf575If58X7far/6qxqfhAYlHT9PrXOYkDVM0+j7ppM8fef0fVPC0cmc/47patl/p3Qipu+D6h5dzevEyh/j/vhV+GNXJ1Y6WfIJxScVf+Xjk47CX+1ovL8q8n91hbP55pt3Y9NNN+1e7Wy88cbpBhtskEssSiL+ykUVuIapzIYbbjhdNrzaURklqbXWWqubdHSFoasJJQFVuPGVgU80PlQmHBa+DhNOmAj0PryS8AlH6/T6178+XWWVVbqJT4lGZfwVzgorrJCbX5hAwvX0r5VA/HLCpBMnrXh8PO//DylLSqm9Bg6KAAAAAElFTkSuQmCC"

fun buildApplicationDetailsEntity(
    createdAt: Instant = getPastDateTime(),
    gssCode: String = getRandomGssCode(),
    authorisedAt: Instant = getPastDateTime(5),
    authorisingStaffId: String = getRandomEmailAddress(),
    source: String = getRandomString(10),
    applicationStatus: ApplicationDetails.ApplicationStatus = ApplicationDetails.ApplicationStatus.APPROVED,
    signatureBase64: String? = null,
    signatureWaived: Boolean? = null,
    signatureWaivedReason: String? = null,
) = ApplicationDetails(
    createdAt = createdAt,
    gssCode = gssCode,
    authorisedAt = authorisedAt,
    authorisingStaffId = authorisingStaffId,
    source = source,
    signatureBase64 = signatureBase64,
    signatureWaivedReason = signatureWaivedReason,
    signatureWaived = signatureWaived,
    applicationStatus = applicationStatus
)

fun buildApplicationDetailsMessageDto(
    applicationId: String = getIerDsApplicationId(),
    createdAt: Instant = getPastDateTime(),
    gssCode: String = getRandomGssCode(),
    authorisedAt: Instant = getPastDateTime(5),
    authorisingStaffId: String = getRandomEmailAddress(),
    source: String = getRandomString(10),
    applicationStatus: ApplicationDetailsMessageDto.ApplicationStatus = ApplicationDetailsMessageDto.ApplicationStatus.APPROVED,
    signatureBase64: String? = null,
    signatureWaived: Boolean? = null,
    signatureWaivedReason: String? = null,
) = ApplicationDetailsMessageDto(
    id = applicationId,
    createdAt = createdAt.atOffset(ZoneOffset.UTC),
    gssCode = gssCode,
    authorisedAt = authorisedAt.atOffset(ZoneOffset.UTC),
    authorisingStaffId = authorisingStaffId,
    source = source,
    applicationStatus = applicationStatus,
    signatureWaived = signatureWaived,
    signatureWaivedReason = signatureWaivedReason,
    signatureBase64 = signatureBase64
)
