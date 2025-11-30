package com.example.fx82esclassic

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.*

class MainActivity : AppCompatActivity() {
    private lateinit var display: TextView
    private var expr: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        display = findViewById(R.id.display)

        val digits = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2", R.id.btn3 to "3",
            R.id.btn4 to "4", R.id.btn5 to "5", R.id.btn6 to "6", R.id.btn7 to "7",
            R.id.btn8 to "8", R.id.btn9 to "9", R.id.btnDot to "."
        )
        for ((id, s) in digits) findViewById<Button>(id).setOnClickListener { append(s) }

        findViewById<Button>(R.id.btnPlus).setOnClickListener { append("+") }
        findViewById<Button>(R.id.btnMinus).setOnClickListener { append("-") }
        findViewById<Button>(R.id.btnMul).setOnClickListener { append("*") }
        findViewById<Button>(R.id.btnDiv).setOnClickListener { append("/") }
        findViewById<Button>(R.id.btnOpen).setOnClickListener { append("(") }
        findViewById<Button>(R.id.btnClose).setOnClickListener { append(")") }
        findViewById<Button>(R.id.btnClear).setOnClickListener { clear() }
        findViewById<Button>(R.id.btnDel).setOnClickListener { delete() }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { equal() }

        // scientific
        findViewById<Button>(R.id.btnSin).setOnClickListener { append("sin(") }
        findViewById<Button>(R.id.btnCos).setOnClickListener { append("cos(") }
        findViewById<Button>(R.id.btnTan).setOnClickListener { append("tan(") }
        findViewById<Button>(R.id.btnSqrt).setOnClickListener { append("sqrt(") }
        findViewById<Button>(R.id.btnPow).setOnClickListener { append("^") }
    }

    private fun append(s: String) {
        expr += s
        display.text = expr
    }
    private fun clear() { expr = ""; display.text = expr }
    private fun delete() { if (expr.isNotEmpty()) expr = expr.dropLast(1); display.text = expr }
    private fun equal() {
        try {
            val res = Eval.evaluate(expr.replace("^", "**"))
            display.text = if (res % 1.0 == 0.0) res.toLong().toString() else res.toString()
        } catch (e: Exception) {
            display.text = "Error"
        }
    }
}

object Eval {
    private var pos = -1
    private var ch = 0
    private lateinit var str: String

    fun evaluate(s: String): Double {
        str = s
        pos = -1
        nextChar()
        val x = parseExpression()
        if (pos < str.length) throw RuntimeException("Unexpected: " + ch.toChar())
        return x
    }

    private fun nextChar() {
        pos++
        ch = if (pos < str.length) str[pos].code else -1
    }

    private fun eat(c: Int): Boolean {
        while (ch == ' '.code) nextChar()
        if (ch == c) { nextChar(); return true }
        return false
    }

    private fun parseExpression(): Double {
        var x = parseTerm()
        while (true) {
            if (eat('+'.code)) x += parseTerm()
            else if (eat('-'.code)) x -= parseTerm()
            else return x
        }
    }

    private fun parseTerm(): Double {
        var x = parseFactor()
        while (true) {
            if (eat('*'.code)) x *= parseFactor()
            else if (eat('/'.code)) x /= parseFactor()
            else return x
        }
    }

    private fun parseFactor(): Double {
        if (eat('+'.code)) return parseFactor()
        if (eat('-'.code)) return -parseFactor()
        var x: Double
        val start = pos
        if (eat('('.code)) {
            x = parseExpression()
            eat(')'.code)
        } else if (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) {
            while (ch >= '0'.code && ch <= '9'.code || ch == '.'.code) nextChar()
            x = str.substring(start, pos).toDouble()
        } else if (ch >= 'a'.code && ch <= 'z'.code) {
            while (ch >= 'a'.code && ch <= 'z'.code) nextChar()
            val func = str.substring(start, pos)
            x = parseFactor()
            x = when (func) {
                "sin" -> kotlin.math.sin(Math.toRadians(x))
                "cos" -> kotlin.math.cos(Math.toRadians(x))
                "tan" -> kotlin.math.tan(Math.toRadians(x))
                "sqrt" -> kotlin.math.sqrt(x)
                else -> throw RuntimeException("Unknown func: $func")
            }
        } else {
            throw RuntimeException("Unexpected: " + ch.toChar())
        }

        if (eat('^'.code) || (pos < str.length && str[pos] == '*' && pos+1 < str.length && str[pos+1] == '*')) {
            if (str[pos] == '*') { nextChar(); nextChar() } else nextChar()
            val e = parseFactor()
            x = x.pow(e)
        }
        return x
    }
}
