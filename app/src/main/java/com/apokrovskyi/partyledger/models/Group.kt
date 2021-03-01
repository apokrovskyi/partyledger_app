package com.apokrovskyi.partyledger.models

import com.apokrovskyi.partyledger.Deserializer
import com.apokrovskyi.partyledger.Serializer

open class Group(var name: String, members: Iterable<Member>) : DataObject {
    open val memberList: MutableList<Member> = members.toMutableList()

    constructor() : this("", arrayListOf())

    override fun toString(): String {
        return memberList.joinToString(prefix = "$name (", postfix = ")")
    }

    override fun Load(reader: Deserializer) {
        name = reader.getString()
        val list = reader.refList<Member>()
        if (list.isNotEmpty()) {
            memberList.clear()
            memberList.addAll(list)
        }
    }

    override fun Save(writer: Serializer) {
        writer
            .param(name)
            .refList(memberList)
    }
}

open class Everybody() : Group("Everybody", arrayListOf()) {
    override val memberList: MutableList<Member> get() = Ledger.Current.members
}