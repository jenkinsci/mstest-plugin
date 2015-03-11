<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:a ="http://microsoft.com/schemas/VisualStudio/TeamTest/2006"
                xmlns:b ="http://microsoft.com/schemas/VisualStudio/TeamTest/2010" >
	<xsl:output method="xml" indent="yes" />
	<xsl:template match="/">
		<testsuites>
			<xsl:variable name="buildName" select="/a:TestRun/@name or /b:TestRun/@name"/>
			<xsl:variable name="numberOfTests" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@total | /b:TestRun/b:ResultSummary/b:Counters/@total)"/>
 			<xsl:variable name="numberOfFailures" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@failed | /b:TestRun/b:ResultSummary/b:Counters/@failed)" />
 			<xsl:variable name="numberOfErrors" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@error | /b:TestRun/b:ResultSummary/b:Counters/@error)" />
 			<xsl:variable name="numberSkipped" select="sum(/a:TestRun/a:ResultSummary/a:Counters/@notRunnable | /b:TestRun/b:ResultSummary/b:Counters/@notRunnable)" />
			<testsuite name="MSTestSuite"
				tests="{$numberOfTests}" time="0"
				failures="{$numberOfFailures}"  errors="{$numberOfErrors}"
				skipped="{$numberSkipped}">

				<xsl:for-each select="//a:UnitTestResult">
					<xsl:variable name="testName" select="@testName"/>
					<xsl:variable name="executionId" select="@executionId"/>
					<xsl:variable name="duration" select="@duration"/>
					<xsl:variable name="duration_seconds" select="substring(@duration, 7)"/>
					<xsl:variable name="duration_minutes" select="substring(@duration, 4,2 )"/>
					<xsl:variable name="duration_hours" select="substring(@duration, 1, 2)"/>
					<xsl:variable name="outcome" select="@outcome"/>
					<xsl:variable name="message" select="a:Output/a:ErrorInfo/a:Message"/>
					<xsl:variable name="stacktrace" select="a:Output/a:ErrorInfo/a:StackTrace"/>
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
							<testcase classname="{$className}"
									name="{$testName}">
									<xsl:if test="$duration">
										<xsl:attribute name="time">
											<xsl:value-of select="$duration_hours*3600 + $duration_minutes*60 + $duration_seconds"/>
										</xsl:attribute>
									</xsl:if>
									<xsl:if test="$message or $stacktrace">
<failure>
	<xsl:if test="$message">
		<xsl:attribute name="message"><xsl:value-of select="$message" /></xsl:attribute>
	</xsl:if>
	<xsl:value-of select="$stacktrace" />
</failure>
								</xsl:if>
							</testcase>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>

				<xsl:for-each select="//b:UnitTestResult">
					<xsl:variable name="testName" select="@testName"/>
					<xsl:variable name="executionId" select="@executionId"/>
					<xsl:variable name="testId" select="@testId"/>
					<xsl:variable name="duration" select="@duration"/>
					<xsl:variable name="duration_seconds" select="substring(@duration, 7)"/>
					<xsl:variable name="duration_minutes" select="substring(@duration, 4,2 )"/>
					<xsl:variable name="duration_hours" select="substring(@duration, 1, 2)"/>
					<xsl:variable name="outcome" select="@outcome"/>
					<xsl:variable name="message" select="b:Output/b:ErrorInfo/b:Message"/>
					<xsl:variable name="stacktrace" select="b:Output/b:ErrorInfo/b:StackTrace"/>
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
								<testcase classname="{$className}"
										name="{$testName}"
										>
									<xsl:if test="$duration">
										<xsl:attribute name="time">
											<xsl:value-of select="$duration_hours*3600 + $duration_minutes*60 + $duration_seconds"/>
										</xsl:attribute>
									</xsl:if>

									<xsl:if test="$message or $stacktrace">
<failure>
	<xsl:if test="$message">
		<xsl:attribute name="message"><xsl:value-of select="$message" /></xsl:attribute>
	</xsl:if>
	<xsl:value-of select="$stacktrace" />
</failure>
									</xsl:if>
								</testcase>
							</xsl:if>
					</xsl:for-each>
				</xsl:for-each>

			</testsuite>
		</testsuites>
	</xsl:template>
</xsl:stylesheet>
