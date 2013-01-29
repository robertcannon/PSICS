<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
     <xsl:output method="xml"
		 version="1.0"
		 encoding="iso-8859-1"
		 omit-xml-declaration="yes"
		 indent="yes"/>
    

<xsl:strip-space elements="index xmlfile directory htmlpage"/>

    <xsl:template match="index">
	<sitemap>

	    <xsl:apply-templates select="." mode="sub"/>
	</sitemap>
    </xsl:template>


    <xsl:template match="index" mode="sub">
	<xsl:apply-templates mode="sub">
	    <xsl:with-param name="dir" select="@dir"/>
	    <xsl:with-param name="toroot" select="@toroot"/>
	</xsl:apply-templates>
	
    </xsl:template>



    <xsl:template match="xmlfile" mode="sub">
	<xsl:param name="dir"/>
	<xsl:param name="toroot"/>
	<htmlpage dir="{$dir}" toroot="{$toroot}" filename="{@name}"/>
    </xsl:template>
    



    <xsl:template match="directory" mode="sub">
	<xsl:param name="dir"/>
	<xsl:param name="toroot"/>
	<xsl:variable name="idoc">../tmp/<xsl:value-of select="$dir"/><xsl:value-of select="@name"/>/_index.xml</xsl:variable>
	<xsl:apply-templates select="document($idoc)" mode="sub"/>
    </xsl:template>


    

</xsl:stylesheet>



