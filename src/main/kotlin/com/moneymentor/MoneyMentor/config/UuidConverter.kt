package com.moneymentor.MoneyMentor.config

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.stereotype.Component
import java.nio.ByteBuffer
import java.util.*

@Component
@ReadingConverter
class BinaryToUuidConverter : Converter<ByteArray, UUID> {
    override fun convert(source: ByteArray): UUID {
        val bb = ByteBuffer.wrap(source)
        val firstLong = bb.long
        val secondLong = bb.long
        return UUID(firstLong, secondLong)
    }
}

@Component
@WritingConverter
class UuidToBinaryConverter : Converter<UUID, ByteArray> {
    override fun convert(source: UUID): ByteArray {
        val bb = ByteBuffer.wrap(ByteArray(16))
        bb.putLong(source.mostSignificantBits)
        bb.putLong(source.leastSignificantBits)
        return bb.array()
    }
}
