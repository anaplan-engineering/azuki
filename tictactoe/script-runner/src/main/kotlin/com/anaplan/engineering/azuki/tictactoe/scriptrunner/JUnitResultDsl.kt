package com.anaplan.engineering.azuki.tictactoe.scriptrunner

import com.anaplan.systemspecification.junitresultdsl.Attribute
import com.anaplan.systemspecification.junitresultdsl.Element
import com.anaplan.systemspecification.junitresultdsl.ElementList
import com.anaplan.systemspecification.junitresultdsl.Text
import com.anaplan.systemspecification.junitresultdsl.XmlTag

@DslMarker
annotation class JUnitResultMarker

@JUnitResultMarker
abstract class JUnitXmlTag(tagName: String) : XmlTag(tagName)

class FailureTag : JUnitXmlTag("failure") {
    @Attribute
    var message: String? = null

    @Attribute
    var type: String? = null

    @Text(cdata = false)
    var text: String? = null
}

class SkippedTag : JUnitXmlTag("skipped")

class ErrorTag : JUnitXmlTag("error") {
    @Attribute
    var message: String? = null

    @Attribute
    var type: String? = null

    @Text(cdata = false)
    var text: String? = null
}

class SystemOutTag : JUnitXmlTag("system-out") {
    @Text(cdata = true)
    var text: String? = null
}

class SystemErrTag : JUnitXmlTag("system-err") {
    @Text(cdata = true)
    var text: String? = null
}

class TestCaseTag : JUnitXmlTag("testcase") {
    @Attribute
    var name: String? = null

    @Attribute
    var time: String? = null

    @Attribute
    var classname: String? = null

    @Element
    var failure: FailureTag? = null

    @Element
    var skipped: SkippedTag? = null

    @Element
    var error: ErrorTag? = null

    fun setTime(time: Long) {
        this.time = formatTime(time)
    }

    fun failure(init: FailureTag.() -> Unit) {
        val failure = FailureTag()
        failure.init()
        this.failure = failure
    }

    fun skipped(init: SkippedTag.() -> Unit) {
        val skipped = SkippedTag()
        skipped.init()
        this.skipped = skipped
    }

    fun error(init: ErrorTag.() -> Unit) {
        val error = ErrorTag()
        error.init()
        this.error = error
    }
}

class TestSuiteTag : JUnitXmlTag("testsuite") {
    @Attribute
    var name: String? = null

    @Attribute
    var time: String? = null

    @Attribute
    var timestamp: String? = null

    @Attribute
    var hostname: String? = null

    @Attribute
    var tests: Int = 0

    @Attribute
    var errors: Int = 0

    @Attribute
    var skipped: Int = 0

    @Attribute
    var failures: Int = 0

    @Element
    var systemOut: SystemOutTag? = null

    @Element
    var systemErr: SystemErrTag? = null

    @ElementList
    val testCases = ArrayList<TestCaseTag>()

    fun testCase(init: TestCaseTag.() -> Unit) {
        val testCase = TestCaseTag()
        testCase.init()
        testCases.add(testCase)
    }

    fun systemOut(init: SystemOutTag.() -> Unit) {
        systemOut = SystemOutTag()
        systemOut!!.init()
    }

    fun systemErr(init: SystemErrTag.() -> Unit) {
        systemErr = SystemErrTag()
        systemErr!!.init()
    }

    fun setTime(time: Long) {
        this.time = formatTime(time)
    }

}

private fun formatTime(time: Long) = "${time / 1000}.${String.format("%03d", time % 1000)}"

fun testSuite(init: TestSuiteTag.() -> Unit): String {
    val testSuiteTag = TestSuiteTag()
    testSuiteTag.init()
    return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n$testSuiteTag"
}

