package com.secretescapes.credits.commands

import grails.validation.Validateable


class RedeemCreditCommand implements Validateable {
	BigDecimal requestedCreditAmount
	Currency currency
}
