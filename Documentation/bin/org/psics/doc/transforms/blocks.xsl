<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">



    <xsl:template match="OL|CENTER|HR|U">
        <xsl:copy-of select="."/>
    </xsl:template>



    <xsl:template match="b|i|p|h1|h2|h3|h4|br|form|input|table|td|tr|ul|ol|li|div|hr|img|pre|a">
	<xsl:param name="path"/>
	<xsl:param name="rootpath"/>
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates>
		<xsl:with-param name="path" select="$path"/>
		<xsl:with-param name="rootpath" select="$rootpath"/>
	    </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>



	<xsl:template match="sup">
		<sup class="power"><xsl:apply-templates/></sup>
	</xsl:template>

	<xsl:template match="pow">
		<sup class="power"><xsl:apply-templates/></sup>
	</xsl:template>


 	<xsl:template match="sub">
 		<sub><xsl:apply-templates/></sub>
 	</xsl:template>

    <xsl:template match="textarea">
	<xsl:copy><xsl:copy-of select="@*"/><xsl:apply-templates/></xsl:copy>
    </xsl:template>





    <xsl:template match="incorporate">
        <xsl:variable name="src">../copy/<xsl:value-of select="@src"/></xsl:variable>
        <xsl:apply-templates select="document($src)/*">
	    <xsl:with-param name="root" select="@root"/>
	</xsl:apply-templates>
    </xsl:template>



    <xsl:template match="copyright"> &#169;</xsl:template>



    <xsl:template match="par">

	<xsl:param name="path"/>
	<xsl:param name="rootpath"/>
	<p>
	    <xsl:apply-templates>
		<xsl:with-param name="path" select="$path"/>
		<xsl:with-param name="rootpath" select="$rootpath"/>
	    </xsl:apply-templates>
        </p>
    </xsl:template>



    <xsl:template match="psmall">
	<xsl:param name="path"/>
	<xsl:param name="rootpath"/>

	<p class="small">
	    <xsl:apply-templates>
		<xsl:with-param name="path" select="$path"/>
		<xsl:with-param name="rootpath" select="$rootpath"/>
	    </xsl:apply-templates>
	</p>
    </xsl:template>





    <xsl:template match="small">
	<i class="small"><xsl:apply-templates/></i>
    </xsl:template>

    <xsl:template match="centerimg">
        <xsl:variable name="src">
            <xsl:value-of select="@src"/>
        </xsl:variable>
        <center>
            <img border="0" src="{$src}" alt="image"/>
        </center>
    </xsl:template>

    <xsl:template match="inlineimg">
        <xsl:variable name="src">
            <xsl:value-of select="@src"/>
        </xsl:variable>
	<img border="0" src="{$src}" alt="image"/>
    </xsl:template>



    <xsl:template match="docpair">
        <xsl:variable name="HREF">
            <xsl:value-of select="href"/>
        </xsl:variable>
        <xsl:variable name="PDF">
            <xsl:value-of select="pdf"/>
        </xsl:variable>

        <li><a class="body" href="{$HREF}.html"><xsl:apply-templates/></a>&#160;
            <a class="pdf" href="pdf/{$PDF}.pdf">(PDF)</a></li>
    </xsl:template>



    <xsl:template match="pdflink-working">

	<table border="0" cellpadding="4" cellspacing="8">
	    <tr>
		<td><a href="{@href}"><img src="images/PDF_icon.gif"/></a></td>
		<td><a class="body" href="{@href}"><xsl:apply-templates/></a></td>
	    </tr>
	</table>
    </xsl:template>



    <xsl:template match="text">
        <xsl:apply-templates/>
    </xsl:template>




    <xsl:template match="anew">
        <xsl:variable name="href"><xsl:value-of select="@href"/></xsl:variable>
        <a target="_blank" href="{$href}"><xsl:apply-templates/></a>
    </xsl:template>



    <xsl:template match="aint">
        <xsl:variable name="href"><xsl:value-of select="@href"/></xsl:variable>
        <a class="body" href="{$href}"><xsl:apply-templates/></a>
    </xsl:template>



    <xsl:template match="pquote">
	<p class="quote">
	    <xsl:apply-templates/>
	</p>
    </xsl:template>

 <xsl:template match="pindent">
	<p class="indent">
	    <xsl:apply-templates/>
	</p>
    </xsl:template>

<xsl:template match="pbib">
	<p class="bib">
	    <xsl:apply-templates/>
	</p>
    </xsl:template>

    <xsl:template match="parquote">
	<tr>
	    <td class="parquote">
		<p class="parquotetext">
		    <xsl:apply-templates select="text"/>
		</p>

		<p class="parquoteattribution">
		    <xsl:apply-templates select="attribution"/>
		</p>
	    </td>
	</tr>
    </xsl:template>


    <xsl:template match="parquote/text">
	<xsl:apply-templates/>
    </xsl:template>



    <xsl:template match="parquote/attribution">
	<xsl:apply-templates/>
    </xsl:template>




    <xsl:template match="email">
        <xsl:variable name="address"><xsl:value-of select="."/></xsl:variable>
        <a class="body" href="mailto:{$address}">
            <xsl:value-of select="$address"/>
        </a>
    </xsl:template>


    <xsl:template match="mailto">
        <xsl:variable name="HREF">
            <xsl:value-of select="@href"/>
        </xsl:variable>
        <a class="body" href="{$HREF}"><xsl:apply-templates/></a>
    </xsl:template>




    <xsl:template match="vskip" name ="vskip">
        <p>&#160;<br/>&#160;<br/></p>
    </xsl:template>


    <xsl:template match="bigskip">
	<p class="bigskip"></p>
    </xsl:template>

    <xsl:template match="medskip">
	<p class="medskip"></p>
    </xsl:template>

    <xsl:template match="smallskip" name="smallskip">
	<p class="smallskip"></p>
    </xsl:template>


	<xsl:template match="cmd">
		<span class="cmd"><xsl:apply-templates/></span>
	</xsl:template>


	<xsl:template match="out">
		<div class="out"><pre><xsl:apply-templates/></pre></div>
	</xsl:template>


	<xsl:template match="m">
		<span class="m"><xsl:apply-templates/></span>
	</xsl:template>

	<xsl:template match="x">
		<span class="x"><xsl:apply-templates/></span>
	</xsl:template>



	<xsl:template match="figr">
		<xsl:variable name="figsrc"><xsl:value-of select="@src"/></xsl:variable>
		<div class="frimg">
			<img src="{$figsrc}"/><br/>
			<p style="width : {@width}">
				<xsl:apply-templates/>
			</p>
		</div>
	</xsl:template>



</xsl:stylesheet>
