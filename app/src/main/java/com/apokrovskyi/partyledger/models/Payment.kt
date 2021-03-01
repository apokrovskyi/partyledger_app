package com.apokrovskyi.partyledger.models

class Payment(from: Member, to: Member, amount: Int) : Purchase(from, to, amount, "") {

    constructor() : this(Member(), Member(), 0)

    override fun toString(): String {
        return "${from.name} -> ${to.name} : $amount"
    }
}