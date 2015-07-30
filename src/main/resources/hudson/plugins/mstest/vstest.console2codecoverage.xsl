<xsl:output method="xml" indent="yes"/>

<xsl:template match="CoverageDSPriv">
    <results>
        <modules>
            <xsl:for-each select="Module">
                <xsl:element name="module">
                    <xsl:attribute name="name">
                        <xsl:value-of select="ModuleName"/>
                    </xsl:attribute>
                    <xsl:attribute name="path">
                        <xsl:value-of select="ModuleName"/>
                    </xsl:attribute>
                    <xsl:attribute name="block_coverage">
                        <xsl:value-of select="BlocksCovered div (BlocksCovered + BlocksNotCovered) * 100"/>
                    </xsl:attribute>
                    <xsl:attribute name="line_coverage">
                        <xsl:value-of select="LinesCovered div (LinesCovered + LinesPartiallyCovered + LinesNotCovered) * 100"/>
                    </xsl:attribute>
                    <xsl:attribute name="blocks_covered">
                        <xsl:value-of select="BlocksCovered"/>
                    </xsl:attribute>
                    <xsl:attribute name="blocks_not_covered">
                        <xsl:value-of select="BlocksNotCovered"/>
                    </xsl:attribute>
                    <xsl:attribute name="lines_covered">
                        <xsl:value-of select="LinesCovered"/>
                    </xsl:attribute>
                    <xsl:attribute name="lines_partially_covered">
                        <xsl:value-of select="LinesPartiallyCovered"/>
                    </xsl:attribute>
                    <xsl:attribute name="lines_not_covered">
                        <xsl:value-of select="LinesNotCovered"/>
                    </xsl:attribute>
                    <xsl:for-each select="NamespaceTable">
                        <xsl:for-each select="Class">
                            <functions>
                                <xsl:for-each select="Method">
                                    <xsl:element name="function">
                                        <xsl:attribute name="name">
                                            <xsl:value-of select="substring-before(MethodName, '()')"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="type_name">
                                            <xsl:value-of select="../ClassName"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="block_coverage">
                                            <xsl:value-of select="BlocksCovered div (BlocksCovered + BlocksNotCovered) * 100"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="line_coverage">
                                            <xsl:value-of select="LinesCovered div (LinesCovered + LinesPartiallyCovered + LinesNotCovered) * 100"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="blocks_covered">
                                            <xsl:value-of select="BlocksCovered"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="blocks_not_covered">
                                            <xsl:value-of select="BlocksNotCovered"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="lines_covered">
                                            <xsl:value-of select="LinesCovered"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="lines_partially_covered">
                                            <xsl:value-of select="LinesPartiallyCovered"/>
                                        </xsl:attribute>
                                        <xsl:attribute name="lines_not_covered">
                                            <xsl:value-of select="LinesNotCovered"/>
                                        </xsl:attribute>
                                        <ranges>
                                            <xsl:for-each select="Lines">
                                                <xsl:element name="range">
                                                    <xsl:attribute name="source_id">
                                                        <xsl:value-of select="SourceFileID"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="covered">
                                                        <xsl:choose>
                                                            <xsl:when test="Coverage=0">yes</xsl:when>
                                                            <xsl:when test="Coverage=1">partial</xsl:when>
                                                            <xsl:when test="Coverage=2">no</xsl:when>
                                                        </xsl:choose>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="start_line">
                                                        <xsl:value-of select="LnStart"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="start_column">
                                                        <xsl:value-of select="ColStart"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="end_line">
                                                        <xsl:value-of select="LnEnd"/>
                                                    </xsl:attribute>
                                                    <xsl:attribute name="end_column">
                                                        <xsl:value-of select="ColEnd"/>
                                                    </xsl:attribute>
                                                </xsl:element>
                                            </xsl:for-each>
                                        </ranges>
                                    </xsl:element>
                                </xsl:for-each>
                            </functions>
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
