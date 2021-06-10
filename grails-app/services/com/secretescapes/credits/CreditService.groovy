package com.secretescapes.credits

import grails.gorm.transactions.NotTransactional
import grails.gorm.transactions.Transactional

@Transactional
class CreditService {

	@NotTransactional
	List<Credit> findAll(User user, Currency currency, CreditStatus creditStatus) {
	    List<Credit> credits = Credit.findAllByUserAndCurrencyAndStatus(user, currency, creditStatus)
	    return  credits
    }

	@NotTransactional
	BigDecimal sumAmountOfAvailableCreditsByUserAndCurrency(User user, Currency currency){
		List<Credit> credits = findAll(user, currency, CreditStatus.AVAILABLE)
		return credits.sum { it.amount }
	}

}
