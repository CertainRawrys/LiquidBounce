/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2015 - 2025 CCBlueX
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
package net.ccbluex.liquidbounce.integration.browser.supports.tab

import net.minecraft.util.Identifier

interface ITab {

    var position: TabPosition
    var visible: Boolean
    var drawn: Boolean
    var preferOnTop: Boolean

    fun forceReload()
    fun reload()
    fun goForward()
    fun goBack()
    fun loadUrl(url: String)
    fun getUrl(): String
    fun closeTab()
    fun getTexture(): Identifier?
    fun resize(width: Int, height: Int)

    fun preferOnTop(): ITab {
        preferOnTop = true
        return this
    }


}
