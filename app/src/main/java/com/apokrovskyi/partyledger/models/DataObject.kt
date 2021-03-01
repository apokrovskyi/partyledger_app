package com.apokrovskyi.partyledger.models

import com.apokrovskyi.partyledger.Deserializer
import com.apokrovskyi.partyledger.Serializer

interface DataObject {
    fun Load(reader: Deserializer)

    fun Save(writer: Serializer)
}