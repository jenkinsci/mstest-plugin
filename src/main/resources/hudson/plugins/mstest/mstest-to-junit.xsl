<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:a ="http://microsoft.com/schemas/VisualStudio/TeamTest/2006"
                xmlns:b ="http://microsoft.com/schemas/VisualStudio/TeamTest/2010" >
	<xsl:output method="xml" indent="yes" />
	<xsl:template match="/">
		<testsuites>
			<xsl:variable name="numberOfTests" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@total | /b:TestRun/b:ResultSummary/b:Counters/@total)"/>
 			<xsl:variable name="numberOfFailures" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@failed | /b:TestRun/b:ResultSummary/b:Counters/@failed)" />
 			<xsl:variable name="numberOfErrors" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@error | /b:TestRun/b:ResultSummary/b:Counters/@error | /a:TestRun/a:ResultSummary/a:Counters/@timeout | /b:TestRun/b:ResultSummary/b:Counters/@timeout)" />
 			<xsl:variable name="skipped2006" select="/a:TestRun/a:ResultSummary/a:Counters/@total - /a:TestRun/a:ResultSummary/a:Counters/@executed"/>
            <xsl:variable name="skipped2010" select="/b:TestRun/b:ResultSummary/b:Counters/@total - /b:TestRun/b:ResultSummary/b:Counters/@executed"/>
            <xsl:variable name="numberSkipped">
                <xsl:choose>
                    <xsl:when test="$skipped2006 > 0"><xsl:value-of select="$skipped2006"/></xsl:when>
                    <xsl:when test="$skipped2010 > 0"><xsl:value-of select="$skipped2010"/></xsl:when>
                    <xsl:otherwise>0</xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
			<testsuite name="MSTestSuite"
				tests="{$numberOfTests}" time="0"
				failures="{$numberOfFailures}"  errors="{$numberOfErrors}"
				skipped="{$numberSkipped}">

				<xsl:for-each select="//a:UnitTestResult">
                    <xsl:variable name="testName">
                        <xsl:value-of select="@testName"/>
                        <xsl:if test="@dataRowInfo"> row <xsl:value-of select="@dataRowInfo"/></xsl:if>
                    </xsl:variable>
                    <xsl:variable name="executionId">
                        <xsl:choose>
                            <xsl:when test="@resultType = 'DataDrivenDataRow'">
                                <xsl:value-of select="@parentExecutionId"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="@executionId"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <xsl:variable name="duration" select="@duration"/>
					<xsl:variable name="outcome" select="@outcome"/>
					<xsl:variable name="message" select="a:Output/a:ErrorInfo/a:Message"/>
					<xsl:variable name="stacktrace" select="a:Output/a:ErrorInfo/a:StackTrace"/>
                    <xsl:variable name="textMessages">
                        <xsl:for-each select="a:Output/a:TextMessages/a:Message">
                            <xsl:value-of select="text()"/><xsl:text>&#10;</xsl:text>
                        </xsl:for-each>
                    </xsl:variable>
					<xsl:for-each select="//a:UnitTest">
						<xsl:variable name="currentExecutionId" select="a:Execution/@id"/>
						<xsl:if test="$currentExecutionId = $executionId" >
							<xsl:variable name="className">
								<xsl:choose>
									<xsl:when test="contains(a:TestMethod/@className, ',')">
										<xsl:value-of select="substring-before(a:TestMethod/@className, ',')"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="a:TestMethod/@className"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
                            <xsl:call-template name="format-test-case">
                                <xsl:with-param name="className" select="$className"/>
                                <xsl:with-param name="duration" select="$duration"/>
                                <xsl:with-param name="message" select="$message"/>
                                <xsl:with-param name="outcome" select="$outcome"/>
                                <xsl:with-param name="stacktrace" select="$stacktrace"/>
                                <xsl:with-param name="testName" select="$testName"/>
                                <xsl:with-param name="textMessages" select="$textMessages"/>
                            </xsl:call-template>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>

				<xsl:for-each select="//b:UnitTestResult">
					<xsl:variable name="testName">
                        <xsl:value-of select="@testName"/>
                        <xsl:if test="@dataRowInfo"> row <xsl:value-of select="@dataRowInfo"/></xsl:if>
                    </xsl:variable>
					<xsl:variable name="testId" select="@testId"/>
					<xsl:variable name="duration" select="@duration"/>
					<xsl:variable name="outcome" select="@outcome"/>
					<xsl:variable name="message" select="b:Output/b:ErrorInfo/b:Message"/>
					<xsl:variable name="stacktrace" select="b:Output/b:ErrorInfo/b:StackTrace"/>
                    <xsl:variable name="textMessages">
                        <xsl:for-each select="b:Output/b:TextMessages/b:Message">
                            <xsl:value-of select="text()"/><xsl:text>&#10;</xsl:text>
                        </xsl:for-each>
                    </xsl:variable>
					<xsl:for-each select="//b:UnitTest">
						<xsl:variable name="currentTestId" select="@id"/>
							<xsl:if test="$currentTestId = $testId" >
								<xsl:variable name="className">
									<xsl:choose>
										<xsl:when test="contains(b:TestMethod/@className, ',')">
											<xsl:value-of select="substring-before(b:TestMethod/@className, ',')"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="b:TestMethod/@className"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:variable>
                                <xsl:call-template name="format-test-case">
                                    <xsl:with-param name="className" select="$className"/>
                                    <xsl:with-param name="duration" select="$duration"/>
                                    <xsl:with-param name="message" select="$message"/>
                                    <xsl:with-param name="outcome" select="$outcome"/>
                                    <xsl:with-param name="stacktrace" select="$stacktrace"/>
                                    <xsl:with-param name="testName" select="$testName"/>
                                    <xsl:with-param name="textMessages" select="$textMessages"/>
                                </xsl:call-template>
							</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
			</testsuite>
		</testsuites>
	</xsl:template>

    <xsl:template name="format-test-case">
        <xsl:param name="className"/>
        <xsl:param name="testName"/>
        <xsl:param name="duration"/>
        <xsl:param name="outcome"/>
        <xsl:param name="message"/>
        <xsl:param name="stacktrace"/>
        <xsl:param name="textMessages"/>
        <xsl:variable name="duration_seconds" select="substring($duration, 7)"/>
        <xsl:variable name="duration_minutes" select="substring($duration, 4, 2 )"/>
        <xsl:variable name="duration_hours" select="substring($duration, 1, 2)"/>
        <testcase classname="{$className}" name="{$testName}">
            <xsl:if test="$duration">
                <xsl:attribute name="time">
                    <xsl:value-of select="$duration_hours*3600 + $duration_minutes*60 + $duration_seconds"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:if test="$outcome != 'Passed' or (not($outcome) and ($message or $stacktrace))">
                <xsl:variable name="tag">
                    <xsl:choose>
                        <xsl:when test="$outcome = 'Failed'">failure</xsl:when>
                        <xsl:when test="$outcome = 'NotExecuted'">skipped</xsl:when>
                        <xsl:otherwise>error</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:element name="{$tag}">
                    <xsl:if test="$message">
                        <xsl:attribute name="message"><xsl:value-of select="$message" /></xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$stacktrace">
                        <xsl:value-of select="$stacktrace" />
                    </xsl:if>
                </xsl:element>
            </xsl:if>
            <xsl:if test="$textMessages != ''">
                <system-out><xsl:value-of select="$textMessages"/></system-out>
            </xsl:if>
        </testcase>
    </xsl:template>
</xsl:stylesheet>
