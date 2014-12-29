<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" >
<xsl:output encoding="utf-8"/>
<xsl:template match="/CoverageDSPriv">
<xsl:variable name="newline">
<xsl:text>
</xsl:text>
</xsl:variable>
<xsl:variable name="TotalClasses"><xsl:value-of select="count(//Class)"/></xsl:variable>
<xsl:variable name="CoveredClasses"><xsl:value-of select="count(//Class[child::BlocksCovered[1] != 0])"/></xsl:variable>

<xsl:variable name="TotalMethods"><xsl:value-of select="count(//Method)"/></xsl:variable>
<xsl:variable name="CoveredMethods"><xsl:value-of select="count(//Method[child::BlocksCovered[1] != 0])"/></xsl:variable>

<xsl:variable name="CoveredBlocks"><xsl:value-of select="Module/BlocksCovered"/></xsl:variable>
<xsl:variable name="TotalBlocks"><xsl:value-of select="Module/BlocksCovered + Module/BlocksNotCovered"/></xsl:variable>

<xsl:variable name="TotalLines"><xsl:value-of select="Module/LinesCovered + Module/LinesPartiallyCovered + Module/LinesNotCovered"/></xsl:variable>
<xsl:variable name="CoveredLines"><xsl:value-of select="Module/LinesCovered"/></xsl:variable>

<xsl:value-of select="$newline"/><xsl:value-of select="$newline"/>
<report>	<xsl:value-of select="$newline"/>
		<stats>	<xsl:value-of select="$newline"/>
			<packages>
				<xsl:attribute name="value">
					<xsl:value-of select="count(//NamespaceTable)"/>
				</xsl:attribute>
			</packages>	<xsl:value-of select="$newline"/>
			
			<classes>
				<xsl:attribute name="value">
					<xsl:value-of select="$TotalClasses"/>
				</xsl:attribute>
			</classes>	<xsl:value-of select="$newline"/>
			
			<methods>
				<xsl:attribute name="value">
					<xsl:value-of select="$TotalMethods"/>
				</xsl:attribute>
			</methods>	<xsl:value-of select="$newline"/>
				
			<srcfiles>
				<xsl:attribute name="value">
					<xsl:value-of select="count(//SourceFileNames)"/>
				</xsl:attribute>
			</srcfiles>	<xsl:value-of select="$newline"/>
			
			<srclines>
				<xsl:attribute name="value">
					<xsl:value-of select="$TotalLines"/>
				</xsl:attribute>
			</srclines>	<xsl:value-of select="$newline"/>
		</stats>	<xsl:value-of select="$newline"/>
		<data>	<xsl:value-of select="$newline"/>
			<all name="all classes">	<xsl:value-of select="$newline"/>
				<coverage type="class, %">
					<xsl:attribute name="value"><xsl:value-of select="$CoveredClasses div $TotalClasses * 100"/>%  (<xsl:value-of select="$CoveredClasses"/>/<xsl:value-of select="$TotalClasses"/>)</xsl:attribute>
				</coverage> <xsl:value-of select="$newline"/>
				
				<coverage type="method, %">
					<xsl:attribute name="value"><xsl:value-of select="$CoveredMethods div $TotalMethods * 100"/>%  (<xsl:value-of select="$CoveredMethods"/>/<xsl:value-of select="$TotalMethods"/>)</xsl:attribute>
				</coverage> <xsl:value-of select="$newline"/>
				
				<coverage type="block, %">
					<xsl:attribute name="value"><xsl:value-of select="$CoveredBlocks div $TotalBlocks * 100"/>%  (<xsl:value-of select="$CoveredBlocks"/>/<xsl:value-of select="$TotalBlocks"/>)</xsl:attribute>
				</coverage> <xsl:value-of select="$newline"/>
				
				<coverage type="line, %">
					<xsl:attribute name="value"><xsl:value-of select="$CoveredLines div $TotalLines * 100"/>%  (<xsl:value-of select="$CoveredLines"/>/<xsl:value-of select="$TotalLines"/>)</xsl:attribute>
				</coverage> <xsl:value-of select="$newline"/>
				
				<xsl:for-each select="//NamespaceTable"> 
				<xsl:value-of select="$newline"/>
				<package>
					<xsl:attribute name="name">
						<xsl:value-of select="NamespaceName"/>
					</xsl:attribute>
					<xsl:value-of select="$newline"/>
					<xsl:variable name="PackageClasses"><xsl:value-of select="count(Class)"/></xsl:variable>
					<xsl:variable name="PackageCoveredClasses"><xsl:value-of select="count(Class[child::BlocksCovered[1] != 0])"/></xsl:variable>

					<xsl:variable name="PackageMethods"><xsl:value-of select="count(Class/Method)"/></xsl:variable>
					<xsl:variable name="PackageCoveredMethods"><xsl:value-of select="count(Class/Method[child::BlocksCovered[1] != 0])"/></xsl:variable>

					<xsl:variable name="PackageCoveredBlocks"><xsl:value-of select="BlocksCovered"/></xsl:variable>
					<xsl:variable name="PackageBlocks"><xsl:value-of select="BlocksCovered + BlocksNotCovered"/></xsl:variable>

					<xsl:variable name="PackageLines"><xsl:value-of select="LinesCovered + LinesPartiallyCovered + LinesNotCovered"/></xsl:variable>
					<xsl:variable name="PackageCoveredLines"><xsl:value-of select="LinesCovered"/></xsl:variable>
					<coverage type="class, %">
						<xsl:attribute name="value"><xsl:value-of select="$PackageCoveredClasses div $PackageClasses * 100"/>%  (<xsl:value-of select="$PackageCoveredClasses"/>/<xsl:value-of select="$PackageClasses"/>)</xsl:attribute>
					</coverage> <xsl:value-of select="$newline"/>
					
					<coverage type="method, %">
						<xsl:attribute name="value"><xsl:value-of select="$PackageCoveredMethods div $PackageMethods * 100"/>%  (<xsl:value-of select="$PackageCoveredMethods"/>/<xsl:value-of select="$PackageMethods"/>)</xsl:attribute>
					</coverage> <xsl:value-of select="$newline"/>
					
					<coverage type="block, %">
						<xsl:attribute name="value"><xsl:value-of select="$PackageCoveredBlocks div $PackageBlocks * 100"/>%  (<xsl:value-of select="$PackageCoveredBlocks"/>/<xsl:value-of select="$PackageBlocks"/>)</xsl:attribute>
					</coverage> <xsl:value-of select="$newline"/>
					
					<coverage type="line, %">
						<xsl:attribute name="value"><xsl:value-of select="$PackageCoveredLines div $PackageLines * 100"/>%  (<xsl:value-of select="$PackageCoveredLines"/>/<xsl:value-of select="$PackageLines"/>)</xsl:attribute>
					</coverage> <xsl:value-of select="$newline"/>
					
					<xsl:variable name="Classes" select="child::Class" />
					<xsl:for-each select="//SourceFileNames">
						<xsl:variable name="SourceFileID"><xsl:value-of select="SourceFileID"/></xsl:variable>
						<xsl:if test="$Classes[child::Method/Lines[1]/SourceFileID = $SourceFileID]">
							<!-- <srcfile>
								<xsl:attribute name="name"><xsl:value-of select="ClassName"/> (<xsl:value-of select="$SourceFileID"/>)</xsl:attribute>
							</srcfile> <xsl:value-of select="$newline"/>-->
							<xsl:value-of select="$newline"/>
							<srcfile>
								<xsl:attribute name="name">
									<xsl:call-template name="RemovePath">
										<xsl:with-param name="fileName" select="SourceFileName"/>
									</xsl:call-template>
								</xsl:attribute> <xsl:value-of select="$newline"/>
								
								<xsl:variable name="ClassesInFile" select="$Classes[child::Method/Lines[1]/SourceFileID = $SourceFileID]" />
								
								<xsl:variable name="FileClasses"><xsl:value-of select="count($Classes[child::Method/Lines[1]/SourceFileID = $SourceFileID])"/></xsl:variable>
								<xsl:variable name="FileCoveredClasses"><xsl:value-of select="count($Classes[child::Method/Lines[1]/SourceFileID = $SourceFileID and child::BlocksCovered[1] != 0])"/></xsl:variable>

								<xsl:variable name="FileMethods"><xsl:value-of select="count($ClassesInFile/Method)"/></xsl:variable>
								<xsl:variable name="FileCoveredMethods"><xsl:value-of select="count($ClassesInFile/Method[child::BlocksCovered[1] != 0])"/></xsl:variable>

								<xsl:variable name="FileCoveredBlocks"><xsl:value-of select="sum($ClassesInFile/BlocksCovered)"/></xsl:variable>
								<xsl:variable name="FileBlocks"><xsl:value-of select="sum($ClassesInFile/BlocksCovered) + sum($ClassesInFile/BlocksNotCovered)"/></xsl:variable>

								<xsl:variable name="FileLines"><xsl:value-of select="sum($ClassesInFile/LinesCovered) + sum($ClassesInFile/LinesPartiallyCovered) + sum($ClassesInFile/LinesNotCovered)"/></xsl:variable>
								<xsl:variable name="FileCoveredLines"><xsl:value-of select="sum($ClassesInFile/LinesCovered)"/></xsl:variable>
								<coverage type="class, %">
									<xsl:attribute name="value"><xsl:value-of select="$FileCoveredClasses div $FileClasses * 100"/>%  (<xsl:value-of select="$FileCoveredClasses"/>/<xsl:value-of select="$FileClasses"/>)</xsl:attribute>
								</coverage> <xsl:value-of select="$newline"/>
								
								<coverage type="method, %">
									<xsl:attribute name="value"><xsl:value-of select="$FileCoveredMethods div $FileMethods * 100"/>%  (<xsl:value-of select="$FileCoveredMethods"/>/<xsl:value-of select="$FileMethods"/>)</xsl:attribute>
								</coverage> <xsl:value-of select="$newline"/>
								
								<coverage type="block, %">
									<xsl:attribute name="value"><xsl:value-of select="$FileCoveredBlocks div $FileBlocks * 100"/>%  (<xsl:value-of select="$FileCoveredBlocks"/>/<xsl:value-of select="$FileBlocks"/>)</xsl:attribute>
								</coverage> <xsl:value-of select="$newline"/>
								
								<coverage type="line, %">
									<xsl:attribute name="value"><xsl:value-of select="$FileCoveredLines div $FileLines * 100"/>%  (<xsl:value-of select="$FileCoveredLines"/>/<xsl:value-of select="$FileLines"/>)</xsl:attribute>
								</coverage> <xsl:value-of select="$newline"/>
							<!--<debug>
								<xsl:value-of select="SourceFileID"/> : <xsl:value-of select="$SourceFileID"/>;
								<xsl:value-of select="$Classes[child::Method/Lines[1]/SourceFileID = $SourceFileID]/ClassName"/>
							</debug> <xsl:value-of select="$newline"/> -->
								<xsl:for-each select="$Classes[child::Method/Lines[1]/SourceFileID = $SourceFileID]">
									<xsl:value-of select="$newline"/>
									<class>
										<xsl:attribute name="name">
											<xsl:value-of select="ClassName"/>
										</xsl:attribute> <xsl:value-of select="$newline"/>
										<xsl:variable name="ClassClasses">1</xsl:variable>
										<xsl:variable name="ClassCoveredClasses">
											<xsl:choose>
												<xsl:when test="BlocksCovered = 0">0</xsl:when>
												<xsl:when test="BlocksCovered != 0">1</xsl:when>
											</xsl:choose>
										</xsl:variable>

										<xsl:variable name="ClassMethods"><xsl:value-of select="count(child::Method)"/></xsl:variable>
										<xsl:variable name="ClassCoveredMethods"><xsl:value-of select="count(child::Method[child::BlocksCovered[1] != 0])"/></xsl:variable>

										<xsl:variable name="ClassCoveredBlocks"><xsl:value-of select="BlocksCovered"/></xsl:variable>
										<xsl:variable name="ClassBlocks"><xsl:value-of select="BlocksCovered + BlocksNotCovered"/></xsl:variable>

										<xsl:variable name="ClassLines"><xsl:value-of select="LinesCovered + LinesPartiallyCovered + LinesNotCovered"/></xsl:variable>
										<xsl:variable name="ClassCoveredLines"><xsl:value-of select="LinesCovered"/></xsl:variable>
										<coverage type="class, %">
											<xsl:attribute name="value"><xsl:value-of select="$ClassCoveredClasses div $ClassClasses * 100"/>%  (<xsl:value-of select="$ClassCoveredClasses"/>/<xsl:value-of select="$ClassClasses"/>)</xsl:attribute>
										</coverage> <xsl:value-of select="$newline"/>
										
										<coverage type="method, %">
											<xsl:attribute name="value"><xsl:value-of select="$ClassCoveredMethods div $ClassMethods * 100"/>%  (<xsl:value-of select="$ClassCoveredMethods"/>/<xsl:value-of select="$ClassMethods"/>)</xsl:attribute>
										</coverage> <xsl:value-of select="$newline"/>
										
										<coverage type="block, %">
											<xsl:attribute name="value"><xsl:value-of select="$ClassCoveredBlocks div $ClassBlocks * 100"/>%  (<xsl:value-of select="$ClassCoveredBlocks"/>/<xsl:value-of select="$ClassBlocks"/>)</xsl:attribute>
										</coverage> <xsl:value-of select="$newline"/>
										
										<coverage type="line, %">
											<xsl:attribute name="value"><xsl:value-of select="$ClassCoveredLines div $ClassLines * 100"/>%  (<xsl:value-of select="$ClassCoveredLines"/>/<xsl:value-of select="$ClassLines"/>)</xsl:attribute>
										</coverage> <xsl:value-of select="$newline"/>
										
										<xsl:for-each select="$Classes[child::Method/Lines[1]/SourceFileID = $SourceFileID]/Method">
											<method>
											<xsl:attribute name="name">
												<xsl:value-of select="MethodName"/>
											</xsl:attribute> <xsl:value-of select="$newline"/>
										
											<xsl:variable name="MethodMethods">1</xsl:variable>
											<xsl:variable name="MethodCoveredMethods">
												<xsl:choose>
													<xsl:when test="BlocksCovered = 0">0</xsl:when>
													<xsl:when test="BlocksCovered != 0">1</xsl:when>
												</xsl:choose>
											</xsl:variable>

											<xsl:variable name="MethodCoveredBlocks"><xsl:value-of select="BlocksCovered"/></xsl:variable>
											<xsl:variable name="MethodBlocks"><xsl:value-of select="BlocksCovered + BlocksNotCovered"/></xsl:variable>

											<xsl:variable name="MethodLines"><xsl:value-of select="LinesCovered + LinesPartiallyCovered + LinesNotCovered"/></xsl:variable>
											<xsl:variable name="MethodCoveredLines"><xsl:value-of select="LinesCovered"/></xsl:variable>
											
											<coverage type="method, %">
												<xsl:attribute name="value"><xsl:value-of select="$MethodCoveredMethods div $MethodMethods * 100"/>%  (<xsl:value-of select="$MethodCoveredMethods"/>/<xsl:value-of select="$MethodMethods"/>)</xsl:attribute>
											</coverage> <xsl:value-of select="$newline"/>
											
											<coverage type="block, %">
												<xsl:attribute name="value"><xsl:value-of select="$MethodCoveredBlocks div $MethodBlocks * 100"/>%  (<xsl:value-of select="$MethodCoveredBlocks"/>/<xsl:value-of select="$MethodBlocks"/>)</xsl:attribute>
											</coverage> <xsl:value-of select="$newline"/>
											
											<coverage type="line, %">
												<xsl:attribute name="value"><xsl:value-of select="$MethodCoveredLines div $MethodLines * 100"/>%  (<xsl:value-of select="$MethodCoveredLines"/>/<xsl:value-of select="$MethodLines"/>)</xsl:attribute>
											</coverage> <xsl:value-of select="$newline"/>
											
											</method> <xsl:value-of select="$newline"/>
										</xsl:for-each> 
										
									</class> <xsl:value-of select="$newline"/>
								</xsl:for-each> 
							</srcfile> <xsl:value-of select="$newline"/>
						</xsl:if>
					</xsl:for-each>
					
				</package> <xsl:value-of select="$newline"/>
				</xsl:for-each>
				
			</all> <xsl:value-of select="$newline"/>
		</data>	<xsl:value-of select="$newline"/>
	</report>
</xsl:template>	

<xsl:template name="RemovePath">
	<xsl:param name="fileName" select="emptyDefaultString"/>
	
	<xsl:choose>
		<xsl:when test="contains($fileName, '\')">
			<xsl:call-template name="RemovePath">
				<xsl:with-param name="fileName" select="substring-after($fileName, '\')" />
			</xsl:call-template>
		</xsl:when>
		
		<xsl:otherwise>
			<xsl:value-of select="$fileName"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>	
</xsl:stylesheet>