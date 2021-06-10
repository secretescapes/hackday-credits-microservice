package com.secretescapes.credits

import grails.testing.gorm.DataTest
import grails.testing.web.controllers.ControllerUnitTest
import spock.lang.Specification

class CreditControllerSpec extends Specification implements ControllerUnitTest<CreditController>, DataTest {

	CreditService creditService
    def setup() {
	    creditService = Mock(CreditService)
	    controller.creditService = creditService

	    mockDomains(User)
    }

	def cleanup() {
	}

	void "index: get proper amount of credits for specific user and currency"() {
		given:
			User user = new User().save(validate: false)
			request.addHeader("X-userId", user.id)

		when:
			def result = controller.index()

		then:
			1 * creditService.sumAmountOfAvailableCreditsByUserAndCurrency(_, _) >> 10
			result.amount == 10
			result.currency == Currency.getInstance("GBP")
	}
}