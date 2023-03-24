package com.github.melin.superior.sql.parser.flink

import com.github.melin.superior.sql.parser.StatementType
import com.github.melin.superior.sql.parser.StatementType.*
import com.github.melin.superior.sql.parser.antlr4.*
import com.github.melin.superior.sql.parser.antlr4.flink.FlinkCdcSqlLexer
import com.github.melin.superior.sql.parser.antlr4.flink.FlinkCdcSqlParser
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.atn.PredictionMode
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.apache.commons.lang3.StringUtils
import com.github.melin.superior.sql.parser.model.*

/**
 *
 * Created by libinsong on 2018/1/10.
 */
object FlinkSQLHelper {

    @JvmStatic fun checkSupportedSQL(statementType: StatementType): Boolean {
        return when (statementType) {
            FLINK_CDC_BEGIN,
            FLINK_CDC_END,
            FLINK_CDC_CTAS,
            FLINK_CDC_CDAS,
            -> true
            else -> false
        }
    }

    @JvmStatic fun getStatementData(command: String) : StatementData {
        val trimCmd = StringUtils.trim(command)

        val charStream = UpperCaseCharStream(CharStreams.fromString(trimCmd))
        val lexer = FlinkCdcSqlLexer(charStream)
        lexer.removeErrorListeners()
        lexer.addErrorListener(ParseErrorListener())

        val tokenStream = CommonTokenStream(lexer)
        val parser = FlinkCdcSqlParser(tokenStream)
        parser.addParseListener(FlinkCdcSqlPostProcessor())
        parser.removeErrorListeners()
        parser.addErrorListener(ParseErrorListener())
        parser.interpreter.predictionMode = PredictionMode.SLL

        val sqlVisitor = FlinkSQLAntlr4Visitor()
        sqlVisitor.setCommand(trimCmd)

        try {
            try {
                // first, try parsing with potentially faster SLL mode
                return sqlVisitor.visit(parser.singleStatement())
            }
            catch (e: ParseCancellationException) {
                tokenStream.seek(0) // rewind input stream
                parser.reset()

                // Try Again.
                parser.interpreter.predictionMode = PredictionMode.LL
                return sqlVisitor.visit(parser.statement())
            }
        } catch (e: ParseException) {
            if(StringUtils.isNotBlank(e.command)) {
                throw e;
            } else {
                throw e.withCommand(trimCmd)
            }
        }
    }
}
