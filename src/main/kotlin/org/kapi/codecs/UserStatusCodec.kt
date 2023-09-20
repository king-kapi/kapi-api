package org.kapi.codecs

import org.bson.BsonReader
import org.bson.BsonWriter
import org.bson.codecs.Codec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecRegistry
import org.kapi.enums.UserStatus

class UserStatusCodec(registry: CodecRegistry) : Codec<UserStatus> {
    init {

    }

    override fun encode(writer: BsonWriter, value: UserStatus, encoderContext: EncoderContext) {
        writer.writeInt32(value.num)
    }

    override fun decode(reader: BsonReader, decoderContext: DecoderContext): UserStatus {
        return when (reader.readInt32()) {
            0 -> UserStatus.ACTIVE
            1 -> UserStatus.IDLE
            2 -> UserStatus.DO_NOT_DISTURB
            3 -> UserStatus.OFFLINE
            else -> UserStatus.OFFLINE
        }
    }

    override fun getEncoderClass(): Class<UserStatus> = UserStatus::class.java
}