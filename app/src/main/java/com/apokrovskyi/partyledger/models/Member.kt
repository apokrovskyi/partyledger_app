package com.apokrovskyi.partyledger.models

import com.apokrovskyi.partyledger.Deserializer
import com.apokrovskyi.partyledger.Serializer

class Member(name: String) : Group(name, ArrayList()) {
    override val memberList = arrayListOf(this)

    constructor() : this("")

    override fun Load(reader: Deserializer) {
        name = reader.getString()
    }

    override fun Save(writer: Serializer) {
        writer.param(name)
    }

    override fun toString(): String {
        return name
    }
}