<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">  
    

    <xsl:template name="mainmenu">
	<xsl:param name="path" select="''"/>
	<xsl:param name="rootpath" select="''"/>
	<xsl:param name="filename"  select="''"/>
 
	  
	<xsl:apply-templates select="document('_index.xml', /)/index" mode="allmenus">  
	    <xsl:with-param name="filename" select="$filename"/>
	    <xsl:with-param name="r2p" select="$path"/>
	    <xsl:with-param name="p2r" select="$rootpath"/>
	</xsl:apply-templates>
 
    </xsl:template>


    

    <xsl:template match="index" mode="allmenus">
	<xsl:param name="filename"  select="''"/>
	<xsl:param name="r2p" select="''"/>
	<xsl:param name="p2r" select="''"/>
	
	<xsl:apply-templates select="xparent" mode="allmenus">
	    <xsl:with-param name="filename" select="$filename"/>
	    <xsl:with-param name="r2p" select="$r2p"/>
	    <xsl:with-param name="p2r" select="$p2r"/>
	</xsl:apply-templates>

	  
	 <div class="menurow">   
	<xsl:apply-templates select="." mode="onemenu">
	    <xsl:with-param name="filename" select="$filename"/>
	    <xsl:with-param name="r2p" select="$r2p"/>
	    <xsl:with-param name="p2r" select="$p2r"/>
	</xsl:apply-templates>
     </div>
     
    </xsl:template>




    <xsl:template match="xparent" mode="allmenus">
	<xsl:param name="filename"  select="''"/>
	<xsl:param name="r2p" select="''"/>
	<xsl:param name="p2r" select="''"/>
	 
	<xsl:variable name="idoc"><xsl:value-of select="$p2r"/><xsl:value-of select="@path"/>_index.xml</xsl:variable>
	 
	<div class="menurow"> 
	<xsl:apply-templates select="document($idoc, /)/index" mode="onemenu">  
	    <xsl:with-param name="pathto" select="@pathto"/>
	    <xsl:with-param name="direct" select="@direct"/>
	    <xsl:with-param name="r2p" select="$r2p"/>
	    <xsl:with-param name="p2r" select="$p2r"/>
	</xsl:apply-templates>
	 </div>
	
	
    </xsl:template>


    

    <xsl:template match="index" mode="onemenu">
	<xsl:param name="filename"  select="''"/>
	<xsl:param name="pathto"  select="''"/>
	<xsl:param name="r2p" select="''"/>
	<xsl:param name="p2r" select="''"/>
	<xsl:param name="direct"/>

	<xsl:apply-templates mode="onemenu">
	    <xsl:with-param name="dir" select="@dir"/>
	    <xsl:with-param name="filename" select="$filename"/>
	    <xsl:with-param name="pathto" select="$pathto"/>
	    <xsl:with-param name="depth" select="@depth"/>
	    <xsl:with-param name="direct" select="$direct"/>
	     <xsl:with-param name="r2p" select="$r2p"/>
	    <xsl:with-param name="p2r" select="$p2r"/>
	</xsl:apply-templates>

    </xsl:template>






    <xsl:template match="index/directory" mode="onemenu">
	<xsl:param name="dir"/>
	<xsl:param name="pathto"  select="''"/>
	<xsl:param name="depth"/>
	<xsl:param name="direct"/>
	  <xsl:param name="r2p" select="''"/>
	<xsl:param name="p2r" select="''"/>
	<xsl:variable name="pgpath"><xsl:value-of select="$pathto"/><xsl:value-of select="$dir"/><xsl:value-of select="@name"/>/index.xml</xsl:variable>
	 
	 
	
	<xsl:variable name="label"><xsl:value-of select="document($pgpath, /)/file/page/@label"/></xsl:variable>
	
	
	<xsl:choose>
	    <xsl:when test="$direct = @name">
		<span class="mm{$depth}d">
		    <a class="mm{$depth}d" href="{$pathto}{@name}/index.html"><xsl:value-of select="$label"/></a>
		</span>
	    </xsl:when>
	    <xsl:otherwise>
		<span class="mm{$depth}">
		    <a class="mm{$depth}" href="{$pathto}{@name}/index.html"><xsl:value-of select="$label"/></a>
		</span>
	    </xsl:otherwise>
	</xsl:choose>
	 
    </xsl:template>





    <xsl:template match="index/xmlfile" mode="onemenu">
	<xsl:param name="filename"  select="''"/>
	<xsl:param name="dir"/>
	<xsl:param name="depth"/>
	<xsl:param name="pathto"  select="''"/>
	<xsl:param name="r2p" select="''"/>
	<xsl:param name="p2r" select="''"/>
	
	 
	<xsl:variable name="pgpath"><xsl:value-of select="$pathto"/><xsl:value-of select="@name"/>.xml</xsl:variable>
	<xsl:variable name="pgpath1"><xsl:value-of select="$p2r"/><xsl:value-of select="$dir"/><xsl:value-of select="@name"/>.xml</xsl:variable>
   
	<xsl:variable name="label"><xsl:value-of select="document($pgpath, /)/file/page/@label"/></xsl:variable>

<!--  
	<xsl:variable name="label">mising label</xsl:variable>
-->

	<xsl:choose>
	    <xsl:when test="$dir != '' and @name = 'index'">
		
	    </xsl:when>

	    <xsl:otherwise>
		<xsl:choose>
		    <xsl:when test="$filename = @name">
			<span class="mm{$depth}d"><xsl:value-of select="$label"/></span> 
			
		    </xsl:when>
		    <xsl:otherwise>
			<span class="mm{$depth}">
				<xsl:choose>
					<xsl:when test="@destination">
					<a class="mm{$depth}" href="{@destination}"><xsl:value-of select="$label"/></a>
					
					</xsl:when>
					<xsl:otherwise>
					    <a class="mm{$depth}" href="{$pathto}{@name}.html"><xsl:value-of select="$label"/></a>
					</xsl:otherwise>
				</xsl:choose>	
			
			</span>
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:otherwise>
	</xsl:choose>
	 
    </xsl:template>



</xsl:stylesheet>
