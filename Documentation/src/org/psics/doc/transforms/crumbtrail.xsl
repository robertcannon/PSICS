<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">  
    

    <xsl:template name="crumbtrail">
	<xsl:param name="path" select="''"/>
	<xsl:param name="filename"  select="''"/>


	<xsl:variable name="idoc">../tmp/<xsl:value-of select="$path"/>_index.xml</xsl:variable>


	<xsl:apply-templates select="document($idoc)/index" mode="crumbtrail">
	    <xsl:with-param name="filename" select="$filename"/>
	</xsl:apply-templates>
	
    </xsl:template>




    <xsl:template match="index" mode="crumbtrail">
	<xsl:param name="filename"/>

	<span class="crumbtrail">
	    &#160;&#160;&#160;&#160;
	    <xsl:apply-templates select="xparent" mode="crumbtrail"/>

	    <xsl:if test="$filename != 'index'">
		<xsl:call-template name="crumb">
		    <xsl:with-param name="path" select="@dir"/>
		    <xsl:with-param name="pathto" select="''"/>
		</xsl:call-template>
	    </xsl:if>

	    
	    
	    <xsl:variable name="pgpath">../tmp/<xsl:value-of select="@dir"/><xsl:value-of select="$filename"/>.xml</xsl:variable>
	    <xsl:variable name="label"><xsl:value-of select="document($pgpath)/file/page/@label"/></xsl:variable>
	    <span class="activecrumb">
		<xsl:value-of select="$label"/>
	    </span>
	</span>


    </xsl:template>




    <xsl:template match="xparent" mode="tst">
	anc file <xsl:value-of select="@direct"/>
    </xsl:template>


    <xsl:template match="xparent" mode="crumbtrail">
 
	<xsl:call-template name="crumb">
	    <xsl:with-param name="path" select="@path"/>
	    <xsl:with-param name="pathto" select="@pathto"/>
	</xsl:call-template>
    </xsl:template>




    <xsl:template name="crumb">
	<xsl:param name="path"/>
	<xsl:param name="pathto"/>

	<xsl:variable name="pgpath">../tmp/<xsl:value-of select="$path"/>index.xml</xsl:variable>
	
	<xsl:variable name="label"><xsl:value-of select="document($pgpath)/file/page/@label"/></xsl:variable>	
	
	<a class="crumbtrail" href="{$pathto}index.html"><xsl:value-of select="$label"/></a>
	&#160;&gt;&#160;

    </xsl:template>


</xsl:stylesheet>
