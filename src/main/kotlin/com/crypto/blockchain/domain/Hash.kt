package com.crypto.blockchain.domain

import com.crypto.blockchain.domain.domain.Block
import com.google.gson.Gson
import java.security.MessageDigest

object Hash {
    private const val SHA_ALGORITHM = "SHA-256"
    val gson = Gson()

    fun createHash(word: String): String {
        return MessageDigest.getInstance(SHA_ALGORITHM)
            .digest(word.toByteArray())!!
            .fold("", { str, it -> str + "%02x".format(it) })
    }

    fun createHash(block: Block): String {
        return createHash(gson.toJson(block))
    }
}