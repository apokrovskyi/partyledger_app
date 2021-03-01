package com.apokrovskyi.partyledger.models

import com.apokrovskyi.partyledger.Deserializer
import com.apokrovskyi.partyledger.Serializer
import kotlin.math.roundToInt

open class Purchase(var from: Member, var to: Group, var amount: Int, var description: String) :
    DataObject {

    constructor() : this(Member(), Member(), 0, "")

    override fun toString(): String {
        return "${from.name} -> ${to.name} : $amount ($description)"
    }

    override fun Load(reader: Deserializer) {
        from = reader.objRef()
        to = reader.objRef()
        amount = reader.getInt()
        description = reader.getString()
    }

    override fun Save(writer: Serializer) {
        writer
            .objRef(from)
            .objRef(to)
            .param(amount)
            .param(description)
    }
}