package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

object CodeSyntaxHighlighter {

    private val keywordColor = Color(0xFFC586C0) // Purplish
    private val stringColor = Color(0xFFCE9178) // Orange/Brownish
    private val numberColor = Color(0xFFB5CEA8) // Greenish
    private val propertyColor = Color(0xFF9CDCFE) // Light blue
    private val commentColor = Color(0xFF6A9955) // Dark green
    private val functionColor = Color(0xFFDCDCAA) // Yellow
    private val typeColor = Color(0xFF4EC9B0) // Teal
    
    // Simple regex matching for common tokens
    // Order matters since first matches will be applied first (though we iterate all tokens, we should apply them carefully)
    private val regexPattern = """(?<comment>//.*|/\*[\s\S]*?\*/)|(?<string>"(?:[^"\\]|\\.)*"|'(?:[^'\\]|\\.)*')|(?<number>\b\d+\.?\d*\b)|(?<keyword>\b(?:if|else|for|while|return|val|var|fun|class|interface|object|struct|type|break|continue|switch|case|default|import|from|export|def|let|const|Boolean|String|Int|Float|Double)\b)|(?<function>\b[a-zA-Z_]\w*(?=\s*\())|(?<type>\b[A-Z][a-zA-Z0-9_]*\b)""".toRegex()

    fun highlightCode(code: String, language: String): AnnotatedString {
        return buildAnnotatedString {
            append(code)
            
            // Note: In an ideal implementation like PrismJS we would parse tokens linearly to avoid overlapping styles,
            // but for a lightweight compose highlighter we can apply spans based on matches. 
            // We just iterate through matches and add span styles.
            
            try {
                regexPattern.findAll(code).forEach { matchResult ->
                    val value = matchResult.value
                    val range = matchResult.range
                    
                    if (value.startsWith("//") || value.startsWith("/*")) {
                        addStyle(SpanStyle(color = commentColor), range.first, range.last + 1)
                    } else if (value.startsWith("\"") || value.startsWith("'") || value.startsWith("`")) {
                        addStyle(SpanStyle(color = stringColor), range.first, range.last + 1)
                    } else if (value.matches(Regex("""\b\d+\.?\d*\b"""))) {
                        addStyle(SpanStyle(color = numberColor), range.first, range.last + 1)
                    } else if (value.matches(Regex("""\b[A-Z][a-zA-Z0-9_]*\b"""))) {
                        addStyle(SpanStyle(color = typeColor), range.first, range.last + 1)
                    } else if (value.matches(Regex("""\b(if|else|for|while|return|val|var|fun|class|interface|object|struct|type|break|continue|switch|case|default|import|from|export|def|let|const|Boolean|String|Int|Float|Double)\b"""))) {
                        addStyle(SpanStyle(color = keywordColor), range.first, range.last + 1)
                    } else if (value.matches(Regex("""\b[a-zA-Z_]\w*(?=\s*\()"""))) {
                        addStyle(SpanStyle(color = functionColor), range.first, range.last + 1)
                    }
                }
            } catch (e: Exception) {
                // Return unhighlighted string on regex failure
            }
        }
    }
}
