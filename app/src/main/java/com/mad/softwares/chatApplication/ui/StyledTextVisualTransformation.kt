import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import com.mad.softwares.chatApplication.ui.messages.StyleRegex
import com.mad.softwares.chatApplication.ui.messages.TAGmess

class StyledTextVisualTransformation(
    private val patterns: List<StyleRegex>
) : VisualTransformation {
    val customOffset =
        object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                return 5
            }
        }
    override fun filter(text: AnnotatedString): TransformedText {
        try {
            val transformedText = buildAnnotatedString {
                var currentIndex = 0
                val inputText = text.text

                while (currentIndex < inputText.length) {
                    var matchFound = false

                    for ((regex, style, symbol) in patterns) {
                        val match = regex.find(inputText, currentIndex)
                        if (match != null && match.range.first == currentIndex) {
                            withStyle(
                                style= SpanStyle(
                                    color = Color.Gray.copy(alpha = 0.8f),

                                )
                            ){
                                append(symbol)
                            }
                            withStyle(style) {
                                append(match.groupValues[1])
                            }
                            withStyle(
                                style= SpanStyle(
                                    color = Color.Gray.copy(alpha = 0.8f),
                                )
                            ){
                                append(symbol)
                            }
                            currentIndex += match.value.length
                            matchFound = true
                            break
                        }
                    }

                    if (!matchFound) {
                        append(inputText[currentIndex])
                        currentIndex++
                    }
                }
            }
            return TransformedText(transformedText, OffsetMapping.Identity)
        }
        catch (e: Exception){
            Log.e(TAGmess,"Got error while transforming the send message : ${e.message}")
        }
        return TransformedText(buildAnnotatedString { append("Error") }, customOffset   )
    }
}
