<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="2.0">
	<xsl:output method="html" version="4.0" encoding="iso-8859-1"
		doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN"
		omit-xml-declaration="yes" indent="yes" />


	<xsl:template match="*">
		<xsl:copy-of select="." />
	</xsl:template>


	<xsl:template match="TypeDoc">
		<h2><xsl:value-of select="@type"/></h2>
		<xsl:apply-templates/>
	</xsl:template>

	


</xsl:stylesheet>