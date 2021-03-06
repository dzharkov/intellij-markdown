package org.intellij.markdown.parser

public class LookaheadText(public val text: String) {
    private val lines: List<String> = text.split('\n')

    public val startPosition: Position? = if (text.isNotEmpty())
        Position(0, -1, -1).nextPosition()
    else
        null


    public inner class Position internal constructor(private val lineN: Int,
                                                     private val localPos: Int, // -1 if on newline before
                                                     private val globalPos: Int) {
        init {
            assert(lineN < lines.size())
            assert(localPos >= -1 && localPos < lines.get(lineN).length())
        }

        override fun toString(): String {
            return "Position: '${
                if (localPos == -1) {
                    "\\n" + currentLine
                } else {
                    currentLine.substring(localPos)
                }
            }'"
        }

        public val offset: Int
            get() = globalPos

        public val offsetInCurrentLine: Int
            get() = localPos

        public val nextLineOffset: Int?
            get() = if (lineN + 1 < lines.size()) {
                globalPos + (currentLine.length() - localPos)
            } else {
                null
            }

        public val nextLineOrEofOffset: Int
            get() = globalPos + (currentLine.length() - localPos)

        public val textFromPosition: CharSequence
            get() = text.subSequence(globalPos, text.length())

        public val currentLine: String
            get() = lines.get(lineN)

        public val currentLineFromPosition: CharSequence
            get() = currentLine.subSequence(offsetInCurrentLine, currentLine.length())

        public val nextLine: String?
            get() = if (lineN + 1 < lines.size()) {
                lines.get(lineN + 1)
            } else {
                null
            }

        public val prevLine: String?
            get() = if (lineN > 0) {
                lines.get(lineN - 1)
            } else {
                null
            }

        public val char: Char
            get() = text.charAt(globalPos)

        public fun nextPosition(delta: Int = 1): Position? {
            var remaining = delta
            var currentPosition = this

            while (true) {
                if (currentPosition.localPos + remaining < currentPosition.currentLine.length()) {
                    return Position(currentPosition.lineN,
                            currentPosition.localPos + remaining,
                            currentPosition.globalPos + remaining)
                } else {
                    val nextLine = currentPosition.nextLineOffset
                    if (nextLine == null) {
                        return null
                    } else {
                        val payload = currentPosition.currentLine.length() - currentPosition.localPos

                        currentPosition = Position(currentPosition.lineN + 1, -1, currentPosition.globalPos + payload)
                        remaining -= payload
                    }
                }
            }
        }

        public fun nextLinePosition(): Position? {
            val nextLine = nextLineOffset
                    ?: return null
            return nextPosition(nextLine - offset)
        }

        public fun charsToNonWhitespace(): Int? {
            val line = currentLine
            var offset = localPos
            while (offset < line.length()) {
                if (offset >= 0) {
                    val c = line[offset]
                    if (c != ' ' && c != '\t') {
                        return offset - localPos
                    }
                }
                offset++
            }
            return null
        }
    }

}