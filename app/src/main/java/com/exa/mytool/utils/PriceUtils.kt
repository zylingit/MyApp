package com.aam.mida.mida_yk.utils

import java.text.DecimalFormat

/**
 *  Created by Tsang on 2023/11/24
 */
object PriceUtils {
    fun currencyFormat(amount: Float): String? {
        val formatter = DecimalFormat("###,###,##0.00")
        return formatter.format(amount.toDouble())
    }
}