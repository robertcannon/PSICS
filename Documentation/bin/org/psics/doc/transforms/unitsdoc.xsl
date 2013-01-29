<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"> 

	 
	<xsl:template match="UnitsListRef">
		<xsl:variable name="pth">../../../work/units/<xsl:value-of select="@ref"/>.xml</xsl:variable>
		<xsl:apply-templates select="document($pth, /)"/>
	</xsl:template>



	<xsl:template match="UnitsList">
		<table class="units" cellspacing="1">
		          <tr>
							<th>Symbol</th>
							<th>Name</th>
							<th>Dimensions</th>
							<th>Factor</th>
				</tr>
	
			<xsl:apply-templates/>
	
		</table>
	</xsl:template>

 
 	<xsl:template match="Units">
 		<tr>
 		 
  		<xsl:attribute name="class"><xsl:choose><xsl:when test="position() mod 4 = 0">even</xsl:when><xsl:otherwise>odd</xsl:otherwise></xsl:choose></xsl:attribute>
 		
 		
 			<td><xsl:value-of select="@symbol"/></td>
 			<td><xsl:value-of select="@name"/></td>
 			<td>
 				<xsl:call-template name="dim">
 					<xsl:with-param name="typ" select="'M'"/>
 					<xsl:with-param name="pow" select="@M"/>
 				</xsl:call-template>
 				
 				<xsl:call-template name="dim">
 					<xsl:with-param name="typ" select="'L'"/>
 					<xsl:with-param name="pow" select="@L"/>
 				</xsl:call-template>
 				<xsl:call-template name="dim">
 					<xsl:with-param name="typ" select="'T'"/>
 					<xsl:with-param name="pow" select="@T"/>
 				</xsl:call-template>
 				<xsl:call-template name="dim">
 					<xsl:with-param name="typ" select="'A'"/>
 					<xsl:with-param name="pow" select="@A"/>
 				</xsl:call-template>
 				<xsl:call-template name="dim">
 					<xsl:with-param name="typ" select="'K'"/>
 					<xsl:with-param name="pow" select="@K"/>
 				</xsl:call-template>
 				
 			</td>
 			<td><xsl:if test="@fac != ''"><xsl:value-of select="@fac"/> * </xsl:if>
 			    <xsl:if test="@pten != '0'">10<sup><xsl:value-of select="@pten"/></sup></xsl:if></td>
 		</tr>
 	
 	</xsl:template>
 	
 
 
 	<xsl:template name="dim">
 		
 	   <xsl:param name="typ" select="X"/>
 		<xsl:param name="pow" select="0"/>
 		<xsl:choose>
 			<xsl:when test="$pow = '0'"></xsl:when>
 			<xsl:when test="$pow = '1'"><xsl:value-of select="$typ"/></xsl:when>
 			<xsl:otherwise>
 				<xsl:value-of select="$typ"/><sup><xsl:value-of select="$pow"/></sup>
 			</xsl:otherwise>
 		</xsl:choose>
 	</xsl:template>
	 
	 
	 
</xsl:stylesheet> 