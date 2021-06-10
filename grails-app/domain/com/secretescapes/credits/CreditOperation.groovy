package com.secretescapes.credits

class CreditOperation {
	Date dateCreated

	String operationHash = UUID.randomUUID()
	OperationType type

	static hasMany = [credits: Credit]


    static constraints = {
    }
}
