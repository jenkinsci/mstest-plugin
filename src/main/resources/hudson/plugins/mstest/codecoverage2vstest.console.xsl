<xsl:output method="xml" indent="yes"/>

<xsl:template match="results">
  <CoverageDSPriv>
    <xsl:for-each select="module">
      <Module>
        <ModuleName><xsl:value-of select="@name"/></ModuleName>
        <BlocksCovered><xsl:value-of select="@blocks_covered"/></BlocksCovered>
        <BlocksNotCovered><xsl:value-of select="@blocks_not_covered"/></BlocksNotCovered>
        <LinesCovered><xsl:value-of select="@lines_covered"/></LinesCovered>
        <LinesPartiallyCovered><xsl:value-of select="@lines_partially_covered"/></LinesPartiallyCovered>
        <LinesNotCovered><xsl:value-of select="@lines_not_covered"/></LinesNotCovered>
        <xsl:apply-templates/>
      <Module>
    </xsl:foreach>
  </CoverageDSPriv>
</xsl:template>

<xsl:template match="functions">
  <NamespaceTable>
    <Class>
      <Method>
        <MethodName><xsl:value-of select="@name"/><xsl:text>()</xsl-text></MethodName>
        <BlocksCovered><xsl:value-of select="@blocks_covered"/></BlocksCovered>
        <BlocksNotCovered><xsl:value-of select="@blocks_not_covered"/></BlocksNotCovered>
        <LinesCovered><xsl:value-of select="@lines_covered"/></LinesCovered>
        <LinesPartiallyCovered><xsl:value-of select="@lines_partially_covered"/></LinesPartiallyCovered>
        <LinesNotCovered><xsl:value-of select="@lines_not_covered"/></LinesNotCovered>
        <xsl:apply-templates/>
      </Method>
    </Class>
  </NamespaceTable>
</xsl:template>

<xsl:template match="ranges">
  <Lines>
    <SourceFileID><xsl:value-of select="@source_id"/></SourceFileID>
    <Coverage>
      <xsl:value-of>
        <xsl:choose>
          <xsl:when test="@covered=yes">0</xsl:when>
          <xsl:when test="@covered=partial">1</xsl:when>
          <xsl:when test="@covered=no">2</xsl:when>
        </xsl:choose>
      </xsl:value-of>
    </Coverage>
    <LnStart><xsl:value-of select="@start_line"></LnStart>
    <LnEnd><xsl:value-of select="@end_line"></LnEnd>
    <ColStart><xsl:value-of select="@start_column"></ColStart>
    <ColEnd><xsl:value-of select="@end_column"></ColEnd>
  </Lines>
</xsl:template>

<xsl:template match="CoverageDSPriv">
    <results>
        <modules>
            <xsl:for-each select="Module">
                <xsl:element name="module">
                    <xsl:for-each select="NamespaceTable">
                        <xsl:for-each select="Class">
                            <source_files>
                                <xsl:for-each select="../../../SourceFileNames">
                                    <xsl:element name="source_file">
                                        <xsl:attribute name="id">
                                            <xsl:value-of select="SourceFileID"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="path">
                                            <xsl:value-of select="SourceFileName"/>
                                        </xsl:attribute>
                                    </xsl:element>
                                </xsl:for-each>
                            </source_files>
                        </xsl:for-each>
                    </xsl:for-each>
                </xsl:element>
            </xsl:for-each>
        </modules>
    </results>
</xsl:template>
