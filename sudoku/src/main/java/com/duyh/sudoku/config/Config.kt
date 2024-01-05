package com.duyh.sudoku.config

import com.googlecode.tesseract.android.TessBaseAPI

object Config {
    const val TESS_ENGINE = TessBaseAPI.OEM_LSTM_ONLY
    const val TESS_LANG = "eng+chi_sim"
    const val IMAGE_NAME = "sample.jpg"

    val DEFAULT_ARRAY = arrayListOf<Int>(1,2,3,4,5,6,7,8,9)
}