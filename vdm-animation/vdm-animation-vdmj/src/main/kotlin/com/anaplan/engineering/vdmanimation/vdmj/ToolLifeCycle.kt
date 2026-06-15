package com.anaplan.engineering.vdmanimation.vdmj

import com.fujitsu.vdmj.ExitStatus
import com.fujitsu.vdmj.Settings
import com.fujitsu.vdmj.config.Properties
import com.fujitsu.vdmj.lex.LexLocation
import com.fujitsu.vdmj.lex.LexTokenReader
import com.fujitsu.vdmj.lex.Token
import com.fujitsu.vdmj.mapper.ClassMapper
import com.fujitsu.vdmj.plugins.EventHub
import com.fujitsu.vdmj.plugins.Lifecycle
import com.fujitsu.vdmj.plugins.PluginConsole
import com.fujitsu.vdmj.plugins.PluginConsole.errorln
import com.fujitsu.vdmj.plugins.PluginConsole.infoln
import com.fujitsu.vdmj.plugins.PluginConsole.verboseln
import com.fujitsu.vdmj.plugins.PluginRegistry
import com.fujitsu.vdmj.plugins.analyses.INPlugin
import com.fujitsu.vdmj.plugins.events.AbstractCheckFilesEvent
import com.fujitsu.vdmj.plugins.events.CheckCompleteEvent
import com.fujitsu.vdmj.plugins.events.CheckFailedEvent
import com.fujitsu.vdmj.plugins.events.CheckPrepareEvent
import com.fujitsu.vdmj.plugins.events.CheckSyntaxEvent
import com.fujitsu.vdmj.plugins.events.CheckTypeEvent
import com.fujitsu.vdmj.runtime.Interpreter
import com.fujitsu.vdmj.runtime.ModuleInterpreter
import com.fujitsu.vdmj.syntax.ExpressionReader
import com.fujitsu.vdmj.syntax.ParserException
import com.fujitsu.vdmj.tc.TCNode
import com.fujitsu.vdmj.tc.expressions.TCExpression
import java.io.File

class ToolLifeCycle(val dialect: String, val showW: Boolean, val specFiles: List<File>) :
    Lifecycle((listOf(dialect) + specFiles.map { it.absolutePath }).toTypedArray())
{
    private var initialised: Boolean = false
    val interpreter: Interpreter? by lazy() {
        try {
            val registry: PluginRegistry = PluginRegistry.getInstance()
            val inp: INPlugin = registry.getPlugin("IN")
            val mi = inp.getInterpreter() as ModuleInterpreter
            mi
        } catch (e: java.lang.Exception) {
            PluginConsole.println(e)
            errorln("Cannot get interpreter")
            e.printStackTrace()
            null
        }
    }

    fun init(): ExitStatus {
        if (!initialised) {
            Properties.init();
            setDialect()
            loadPlugins()
            processArgs()
            warnings = showW
            initialised = true
            verboseln("Initializing VDMJ plugins")
        }
        findFiles()
        return prepare()
    }

    private fun process(event: AbstractCheckFilesEvent, whenOkay: String = "", whenFail: String = "") : ExitStatus {
        try {
            val eventHub = EventHub.getInstance()
            var messages = eventHub.publish(event)

            if (report(messages, event)) {
                if (!whenOkay.isBlank()) {
                    infoln(whenOkay)
                }
                return ExitStatus.EXIT_OK
            } else {
                if (!whenFail.isBlank()) {
                    infoln(whenFail)
                }
                messages = eventHub.publish(CheckFailedEvent(event))
                report(messages, event)
                return ExitStatus.EXIT_ERRORS
            }
        } catch (e: Exception) {
            PluginConsole.println(e)
            return ExitStatus.EXIT_ERRORS
        }
    }

    fun prepare()   = process(CheckPrepareEvent(files), "File preparation succeeded", "Preparation errors found")
    fun parse()     = process(CheckSyntaxEvent(), "Syntax checking succeeded", "Syntax errors found")
    fun typeCheck() = process(CheckTypeEvent(), "Type checking succeeded", "Type checking errors found")
    fun interpret() = process(
        CheckCompleteEvent(),
        "Loaded files initialized successfully",
        "Failed to initialize interpreter"
    )
    // make public
//    override fun startConsole(): ExitStatus = super.startConsole()
//    override fun complete(): Unit = super.complete()

    @Throws(java.lang.Exception::class)
    fun parseExpression(line: String): TCExpression {
        return parseExpression(line, interpreter!!.defaultName)
    }

    @Throws(java.lang.Exception::class)
    fun parseExpression(line: String, module: String): TCExpression {
        //protected
        //Interpreter interpreter = getInterpreter();
        //return interpreter.parseExpression(line, module);
        val ltr = LexTokenReader(line, Settings.dialect)
        val reader = ExpressionReader(ltr)
        reader.currentModule = module
        val ast = reader.readExpression()
        val end = ltr.getLast()

        if (!end.`is`(Token.EOF)) {
            throw ParserException(2330, "Tokens found after expression at $end", LexLocation.ANY, 0)
        }

        return ClassMapper.getInstance(TCNode.MAPPINGS).convertLocal<TCExpression>(ast)
    }

    fun <T : Interpreter> alternativeGetInterpreter(): T? {
        if (interpreter == null) {
            try {
                val registry: PluginRegistry = PluginRegistry.getInstance()
                val `in`: INPlugin = registry.getPlugin("IN")
                return `in`.getInterpreter()
            } catch (e: java.lang.Exception) {
                PluginConsole.println(e)
                errorln("Cannot get interpreter")
                return null
            }
        }
        @Suppress("UNCHECKED_CAST")
        return interpreter as T
    }
}
