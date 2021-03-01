package com.apokrovskyi.partyledger

import com.apokrovskyi.partyledger.models.DataObject
import com.apokrovskyi.partyledger.models.Ledger
import java.lang.StringBuilder

class Serializer {
    private var data = StringBuilder()

    private fun <T> addParam(param: T) {
        data.append(param.toString() + " ")
    }

    fun param(param: Int): Serializer {
        addParam(param)
        return this
    }

    fun param(param: String): Serializer {
        addParam(param.length)
        addParam(param)
        return this
    }

    inline fun <reified T> objRef(param: T): Serializer {
        param(Ledger.getRef(param))
        return this
    }

    inline fun <reified T> refList(list: List<T>): Serializer {
        param(list.size)
        for (item in list)
            objRef(item)
        return this
    }

    fun dataObj(param: DataObject): Serializer {
        param.Save(this)
        return this
    }

    fun objList(list: List<DataObject>): Serializer {
        addParam(list.size)
        for (item in list)
            dataObj(item)
        return this
    }

    override fun toString(): String {
        return data.toString()
    }
}

class Deserializer(private var data: String) {
    private fun getParam(): String {
        val i = data.indexOf(' ')

        if (i < 0) return ""

        val result = data.substring(0, i)
        data = data.substring(i + 1)
        return result
    }

    fun getInt(): Int {
        return getParam().toInt()
    }

    fun getString(): String {
        val l = getInt()
        val result = data.substring(0, l)
        data = data.substring(l + 1)
        return result
    }

    inline fun <reified T> objRef(): T {
        return Ledger.getByRef(getInt())
    }

    inline fun <reified T> refList(): MutableList<T> {
        val l = getInt()
        val result = ArrayList<T>()

        for (i in 1..l)
            result.add(objRef())
        return result
    }

    fun <T : DataObject> dataObj(getter: () -> T): T {
        val result = getter()
        result.Load(this)
        return result
    }

    fun <T : DataObject> objList(list: MutableList<T>, getter: () -> T) {
        list.clear()
        val l = getInt()
        for (i in 1..l)
            list.add(dataObj(getter))
    }

    override fun toString(): String {
        return data
    }
}