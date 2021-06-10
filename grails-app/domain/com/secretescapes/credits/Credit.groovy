package com.secretescapes.credits

class Credit {
	Date dateCreated
	Date lastUpdated

	Currency currency = Currency.getInstance("GBP")
	BigDecimal amount = BigDecimal.ZERO
	CreditStatus status = CreditStatus.AVAILABLE
	Date expiresOn

	static belongsTo = [user: User]


	static constraints = {
		expiresOn nullable: true

	}
}
