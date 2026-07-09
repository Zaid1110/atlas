package com.atlas.agent.frame

data class Frame(
    val id: Long,
    val timestampMillis: Long,
    val jpegBytes: ByteArray
) {
    val sizeBytes: Int
        get() = jpegBytes.size

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Frame) return false

        return id == other.id &&
            timestampMillis == other.timestampMillis &&
            jpegBytes.contentEquals(other.jpegBytes)
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + timestampMillis.hashCode()
        result = 31 * result + jpegBytes.contentHashCode()
        return result
    }
}
