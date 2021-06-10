package com.secretescapes.credits

import com.secretescapes.credits.commands.RedeemCreditCommand
import com.secretescapes.credits.commands.exceptions.InsufficientAvailableCredit
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
import spock.lang.Unroll

class CreditServiceSpec extends Specification implements ServiceUnitTest<CreditService>, DataTest {

	private static final GBP = Currency.getInstance("GBP")

	void setupSpec() {
		mockDomains(Credit, User, CreditOperation)
	}

	def setup() {
    }

    def cleanup() {
    }

	@Unroll
	void "sumAmountOfAvailableCreditsByUserAndCurrency: only non expired credits with specific status"() {
		given:
			User user = new User(credits: userCredits).save(validate: false)

		expect:
			expectedAmount == service.sumAmountOfAvailableCreditsByUserAndCurrency(user, Currency.getInstance("GBP"))

		where:
			userCredits                                                                                                                        || expectedAmount
			[new Credit(amount: 99)]                                                                                                           || 99
			[new Credit(amount: 1), new Credit(amount: 1)]                                                                                     || 2
			[new Credit(amount: 1), new Credit(amount: 1, expiresOn: new Date() - 1)]                                                          || 1
			[new Credit(amount: 1), new Credit(amount: 1, expiresOn: new Date() + 1)]                                                          || 2
			[new Credit(amount: 1, status: CreditStatus.USED), new Credit(amount: 1, status: CreditStatus.DELETED, expiresOn: new Date() + 1)] || 0
	}

    void "redeemCredit: if available credit is 0 throw InsufficientAvailableCredit"() {
        given:
			User user = new User().save(validate: false)
		when:
			service.redeemCredit(user, new RedeemCreditCommand(requestedCreditAmount: 100, currency: GBP))
		then:
			thrown(InsufficientAvailableCredit)
    }

	@Unroll
	void "redeemCredit: if available credit is less than requested throw InsufficientAvailableCredit"() {
		given:
			User user = new User(credits: userCredits).save(validate: false)
		when:
			service.redeemCredit(user, new RedeemCreditCommand(requestedCreditAmount: 100, currency: GBP))
		then:
			thrown(InsufficientAvailableCredit)
		where:
			userCredits << [
					[new Credit(amount: 99)],
					[new Credit(amount: 1), new Credit(amount: 1)],
			]
	}

	@Unroll
	void "redeemCredit: If available amount is equal or greater than requested, returns a CreditOperation and available should be the difference"() {
		given:
			User user = new User(credits: userCredits).save(validate: false)
		when:
			CreditOperation operation = service.redeemCredit(user, new RedeemCreditCommand(requestedCreditAmount: requested, currency: GBP))
		then:
			operation.credits.amount == usedCredits
		and:
			// Not sure why the dynamic finder ignores the status if I do findAllByUserAndCurrencyAndStatus
			Credit.findAllByUserAndCurrency(user, GBP).findAll{it.status == CreditStatus.AVAILABLE}.amount == remainingCreditAmounts
		where:
			userCredits                                        | requested || usedCredits | remainingCreditAmounts
			[new Credit(amount: 100)]                          | 100       || [100]       | []
			[new Credit(amount: 50), new Credit(amount: 50)]   | 100       || [50, 50]    | []
			[new Credit(amount: 200)]                          | 50        || [50]        | [150]
//			[new Credit(amount: 100), new Credit(amount: 200)] | 50        || [50]        | [100, 50]
//			[new Credit(amount: 25), new Credit(amount: 25)]   | 50        || [25, 25]    | []
	}
}
