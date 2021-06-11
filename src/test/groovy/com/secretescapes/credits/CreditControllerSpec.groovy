package com.secretescapes.credits

import com.secretescapes.credits.commands.RedeemCreditCommand
import com.secretescapes.credits.commands.exceptions.InsufficientAvailableCredit
import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import org.springframework.http.HttpStatus
import spock.lang.Specification

class CreditControllerSpec extends Specification implements ControllerUnitTest<CreditController>, DataTest {

	CreditService creditService

	User user

	def setup() {
		creditService = Mock(CreditService)
		controller.creditService = creditService

		mockDomains(User, Credit)

		user = new User().save(validate: false)
		request.addHeader("X-userId", user.id)
	}

	void "index: get proper amount of credits for specific user and currency"() {
		when:
			def result = controller.index()
		then:
			1 * creditService.sumAmountOfAvailableCreditsByUserAndCurrency(_, _) >> 10
			result.amount == 10
			result.currency == Currency.getInstance("GBP")
	}

	void "redeemCredit: happy path for a single, partly used credit"() {
		given: "credit for user"
			Credit credit = new Credit(amount: 20).save(validate: false)
			user.addToCredits(credit)
		and: "prepare command"
			RedeemCreditCommand command = new RedeemCreditCommand(currency: Currency.getInstance("GBP"), requestedCreditAmount: 15)
			creditService.redeemCredit(user, command) >> new CreditOperation(operationHash: "1234")
		and:
			request.method = 'POST'

		when:
			def result = controller.redeemCredit(command)
		then:
			result.operation.operationHash == "1234"
	}

	void "redeemCredit: exception path for unsuficient amount of credits"() {
		given: "credit for user"
			Credit credit = new Credit(amount: 20).save(validate: false)
			user.addToCredits(credit)
		and: "prepare command"
			RedeemCreditCommand command = new RedeemCreditCommand(currency: Currency.getInstance("GBP"), requestedCreditAmount: 25)
			creditService.redeemCredit(user, command) >> {
				throw new InsufficientAvailableCredit("User does not have enough available credit")
			}
		and:
			request.method = 'POST'

		when:
			def result = controller.redeemCredit(command)
		then:
			!result
			response.status == HttpStatus.CONFLICT.value()
	}

	void "redeemCredit: exception path for bad parsed request payload"() {
		given: "credit for user"
			Credit credit = new Credit(amount: 20).save(validate: false)
			user.addToCredits(credit)
		and: "prepare command"
			RedeemCreditCommand command = new RedeemCreditCommand()
		and:
			request.method = 'POST'

		when:
			def result = controller.redeemCredit(command)
		then:
			!result
			response.status == HttpStatus.BAD_REQUEST.value()
	}

	void "redeemCredit: exception path for missing authentication header"() {
		given: "remove auth header"
			request.removeHeader("X-userId")
		and: "prepare command"
			RedeemCreditCommand command = new RedeemCreditCommand()
		and:
			request.method = 'POST'

		when:
			def result = controller.redeemCredit(command)
		then:
			!result
			response.status == HttpStatus.FORBIDDEN.value()
	}
}