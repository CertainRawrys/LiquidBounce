/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2024 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package net.ccbluex.liquidbounce.render.engine

import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.Vec3i
import org.lwjgl.opengl.GL20
import java.awt.Color
import java.nio.ByteBuffer
import kotlin.math.cos
import kotlin.math.sin

data class Vec4(val x: Float, val y: Float, val z: Float, val w: Float) {
    constructor(vec: Vec3, w: Float) : this(vec.x, vec.y, vec.z, w)
}

data class Vec3(val x: Float, val y: Float, val z: Float) {
    constructor(x: Double, y: Double, z: Double) : this(x.toFloat(), y.toFloat(), z.toFloat())
    constructor(vec: Vec3d) : this(vec.x, vec.y, vec.z)
    constructor(vec: Vec4) : this(vec.x, vec.y, vec.z)
    constructor(vec: Vec3i) : this(vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat())

    fun writeToBuffer(idx: Int, buffer: ByteBuffer) {
        buffer.putFloat(idx, x)
        buffer.putFloat(idx + 4, y)
        buffer.putFloat(idx + 8, z)
    }

    fun add(other: Vec3): Vec3 {
        return Vec3(this.x + other.x, this.y + other.y, this.z + other.z)
    }

    private fun sub(other: Vec3): Vec3 {
        return Vec3(this.x - other.x, this.y - other.y, this.z - other.z)
    }

    operator fun plus(other: Vec3): Vec3 = add(other)
    operator fun minus(other: Vec3): Vec3 = sub(other)
    operator fun times(scale: Float): Vec3 = Vec3(this.x * scale, this.y * scale, this.z * scale)

    fun rotatePitch(pitch: Float): Vec3 {
        val f = cos(pitch)
        val f1 = sin(pitch)

        val d0 = this.x
        val d1 = this.y * f + this.z * f1
        val d2 = this.z * f - this.y * f1

        return Vec3(d0, d1, d2)
    }

    fun rotateYaw(yaw: Float): Vec3 {
        val f = cos(yaw)
        val f1 = sin(yaw)

        val d0 = this.x * f + this.z * f1
        val d1 = this.y
        val d2 = this.z * f - this.x * f1

        return Vec3(d0, d1, d2)
    }

    fun toVec3d() = Vec3d(this.x.toDouble(), this.y.toDouble(), this.z.toDouble())
}

/**
 * Contains a texture coordinate. The data gets normalized
 * `[0; 65535] -> [0.0f; 1.0f]`
 */
data class UV2s(val u: Short, val v: Short) {
    constructor(u: Float, v: Float) : this((u * 65535.0f).toInt().toShort(), (v * 65535.0f).toInt().toShort())

    fun writeToBuffer(idx: Int, buffer: ByteBuffer) {
        buffer.putShort(idx, u)
        buffer.putShort(idx + 2, v)
    }

    fun toFloatArray(): FloatArray {
        return floatArrayOf((u.toInt() and 0xFFFF) / 65535.0f, (v.toInt() and 0xFFFF) / 65535.0f)
    }
}

data class UV2f(val u: Float, val v: Float)

data class Color4b(val r: Int, val g: Int, val b: Int, val a: Int) {

    companion object {

        val WHITE = Color4b(255, 255, 255, 255)
        val BLACK = Color4b(0, 0, 0, 255)
        val RED = Color4b(255, 0, 0, 255)
        val GREEN = Color4b(0, 255, 0, 255)
        val BLUE = Color4b(0, 0, 255, 255)

        @Throws(IllegalArgumentException::class)
        fun fromHex(hex: String): Color4b {
            val cleanHex = hex.removePrefix("#")
            val hasAlpha = cleanHex.length == 8

            require(cleanHex.length == 6 || hasAlpha)

            return if (hasAlpha) {
                val rgba = cleanHex.toLong(16)
                Color4b(
                    (rgba shr 24).toInt() and 0xFF,
                    (rgba shr 16).toInt() and 0xFF,
                    (rgba shr 8).toInt() and 0xFF,
                    rgba.toInt() and 0xFF
                )
            } else {
                val rgb = cleanHex.toInt(16)
                Color4b(
                    (rgb shr 16) and 0xFF,
                    (rgb shr 8) and 0xFF,
                    rgb and 0xFF,
                    255
                )
            }
        }

    }

    constructor(color: Color) : this(color.red, color.green, color.blue, color.alpha)

    constructor(hex: Int, hasAlpha: Boolean = false) : this(Color(hex, hasAlpha))
    constructor(r: Int, g: Int, b: Int) : this(r, g, b, 255)

    fun writeToBuffer(idx: Int, buffer: ByteBuffer) {
        buffer.put(idx, r.toByte())
        buffer.put(idx + 1, g.toByte())
        buffer.put(idx + 2, b.toByte())
        buffer.put(idx + 3, a.toByte())
    }

    fun toHex(alpha: Boolean = false): String {
        val hex = StringBuilder("#")

        hex.append(componentToHex(r))
        hex.append(componentToHex(g))
        hex.append(componentToHex(b))
        if (alpha) hex.append((componentToHex(a)))

        return hex.toString().uppercase()
    }

    private fun componentToHex(c: Int): String {
        return Integer.toHexString(c).padStart(2, '0')
    }

    fun red(red: Int) = Color4b(red, this.g, this.b, this.a)

    fun green(green: Int) = Color4b(this.r, green, this.b, this.a)

    fun blue(blue: Int) = Color4b(this.r, this.g, blue, this.a)

    fun alpha(alpha: Int) = Color4b(this.r, this.g, this.b, alpha)

    fun toARGB() = (a shl 24) or (r shl 16) or (g shl 8) or b

    fun toABGR() = (a shl 24) or (b shl 16) or (g shl 8) or r

    fun fade(fade: Float): Color4b {
        return if (fade == 1f) {
            this
        } else {
            alpha((a * fade).toInt())
        }
    }

    fun darker() = Color4b(darkerChannel(r), darkerChannel(g), darkerChannel(b), a)

    private fun darkerChannel(value: Int) = (value * 0.7).toInt().coerceAtLeast(0)

    fun putToUniform(pointer: Int) {
        GL20.glUniform4f(pointer, r / 255f, g / 255f, b / 255f, a / 255f)
    }

}
