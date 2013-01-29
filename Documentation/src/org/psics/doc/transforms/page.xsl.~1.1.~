<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="html"
		version="1.0"
		encoding="iso-8859-1"
		doctype-public="-//W3C//DTD HTML 4.0 Transitional//EN"
		omit-xml-declaration="yes"
		indent="yes"/>


<!--    doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" -->
<!--	doctype-system="DTD/xhtml1-strict.dtd" -->


    <xsl:include href="blocks.xsl"/>

    <xsl:include href="menus.xsl"/>

    <xsl:include href="crumbtrail.xsl"/>

    <xsl:include href="xmlverbatim.xsl"/>

    <xsl:include href="typedocs.xsl"/>

    <xsl:include href="unitsdoc.xsl"/>

    <xsl:param name="datestamp"/>
    <xsl:param name="versionname"/>
    <xsl:param name="versiondate"/>

    <xsl:param name="icingversionname"/>
	<xsl:param name="versionmodelsname"/>



    <xsl:template match="file">
	<xsl:apply-templates>
	    <xsl:with-param name="path" select="@path"/>
	     <xsl:with-param name="rootpath" select="@rootpath"/>
		<xsl:with-param name="filename" select="@name"/>
	</xsl:apply-templates>
    </xsl:template>



    <xsl:template match="page">
	<xsl:param name="path" select="''"/>
	<xsl:param name="rootpath" select="''"/>
	<xsl:param name="filename"  select="''"/>

	<html>
	    <HEAD>
	    <xsl:choose>
	    	<xsl:when test="@title">
				<TITLE><xsl:value-of select="@title"/></TITLE>
			</xsl:when>
			<xsl:otherwise>
				<TITLE>PSICS: <xsl:value-of select="@label"/></TITLE>
			</xsl:otherwise>
		</xsl:choose>

		<META name="DESCRIPTION" content="Textensor Limited"/>


		<LINK href="{$rootpath}page.css" rel="stylesheet" type="text/css"/>
		<LINK href="{$rootpath}tags.css" rel="stylesheet" type="text/css"/>
		<LINK href="{$rootpath}typedocs.css" rel="stylesheet" type="text/css"/>
		<link rel="shortcut icon" href="{$rootpath}favicon.png" type="image/x-icon"/>

		   <xsl:if test="@jsfile">
        	       <script language="javascript" src="{@jsfile}"></script>
			</xsl:if>
	    </HEAD>



	    <body>



		<div id="top">
<!--
		<div style="float:right; margin : 20px;">
			 <a href='http://a.nnotate.com/php/annotate.php?v=001' title="Take a private annotatable snapshot for your records">
				<img src="http://images.textensor.com/nnotate/annotate.png"/>
			</a>
		</div>
-->
			<table width="94%" cellspacing="0" cellpadding="0" style="margin-left : 20px; margin-top : 4px;">
				<tr>
				<td><img id="logo" src="{$rootpath}logo.png"/></td>
				<td>
					<div id="toptxt">
					<span class="fl">PSICS</span>
					- the
					<span class="fl">P</span>arallel
					<span class="fl">S</span>tochastic
					<span class="fl">I</span>on
					<span class="fl">C</span>hannel
					<span class="fl">S</span>imulator
					</div>
				</td>
				<td align="right">

<!-- Google CSE Search Box Begins  -->
<form action="http://www.psics.org/results.html" id="cse-search-box">
  <input type="hidden" name="cx" value="015685944145631860008:cgfnyrfvpke" />
  <input type="hidden" name="cof" value="FORID:11" />
  <input type="text" name="q" size="25" />
  <input type="submit" name="sa" value="Search" />
</form>
<script type="text/javascript" src="http://www.google.com/coop/cse/brand?form=cse-search-box&amp;lang=en"></script>
<!-- Google CSE Search Box Ends -->



				</td>
			</tr>
		</table>



		</div>



		<div id="menu">
		    <xsl:call-template name="mainmenu">
			<xsl:with-param name="path" select="$path"/>
			<xsl:with-param name="rootpath" select="$rootpath"/>
			<xsl:with-param name="filename" select="$filename"/>
		    </xsl:call-template>
		</div>


		<!--
		<div id="crumbtrail">
	            <xsl:call-template name="crumbtrail">
			<xsl:with-param name="path" select="$path"/>
			<xsl:with-param name="filename" select="$filename"/>
		    </xsl:call-template>
		</div>
		     -->


 		<xsl:apply-templates select=".." mode="prevnext"/>


		<div id="annotate" class="tx_nosnap">
			<b>Under development</b><br/>
			Please let us know of errors, unclear text or if you have suggestions form
			improvements:<br/>
			 <a rel="nofollow" title="Add notes to a shared copy visible to the site maintainer"
			    href="http://a.nnotate.com/php/annotate.php?a=robert@textensor.com&amp;v=002">
				<img src="http://images.textensor.com/nnotate/feedback.png"/>
			</a><br/>
		</div>


		<div id="content">

		    <xsl:apply-templates>
			<xsl:with-param name="path" select="$path"/>
			<xsl:with-param name="filename" select="$filename"/>
			<xsl:with-param name="rootpath" select="$rootpath"/>
		    </xsl:apply-templates>
		</div>


		<xsl:apply-templates select=".." mode="prevnext"/>

		<div>&#160;<br/>&#160;</div>

	    </body>
	</html>
    </xsl:template>






 <xsl:template match="googlesearchres">
<!-- Google Search Result Snippet Begins -->
<div id="cse-search-results"></div>
<script type="text/javascript">
  var googleSearchIframeName = "cse-search-results";
  var googleSearchFormName = "cse-search-box";
  var googleSearchFrameWidth = 600;
  var googleSearchDomain = "www.google.com";
  var googleSearchPath = "/cse";
</script>
<script type="text/javascript" src="http://www.google.com/afsonline/show_afs_search.js"></script>

<!-- Google Search Result Snippet Ends -->


 </xsl:template>




	<xsl:template match="page" mode="prevnext">
	  <div class="prevnext">
	  		<xsl:choose>
	  			<xsl:when test="@prev">
	  				<a href="{@prev}.html">previous</a>&#160;&#8592;&#160;
	  			</xsl:when>
	  			<xsl:otherwise>
					<span class="faint">previous&#160;&#8592;&#160;</span>
	  			</xsl:otherwise>
	  		</xsl:choose>

	  		<xsl:choose>
	  			<xsl:when test="@next">
	  				&#160;&#8594;&#160;<a href="{@next}.html">next</a>
	  			</xsl:when>
	  			<xsl:otherwise>
					<span class="faint">&#160;&#8594;&#160;next</span>
	  			</xsl:otherwise>
	  		</xsl:choose>
	  </div>
	  </xsl:template>



    <xsl:template match="p">
	<p>
	    <xsl:apply-templates/>
	</p>
    </xsl:template>

    <xsl:template match="latestinfo">
    <p style="padding : 2px 0px 2px 0px" margin="0px">
        Latest changes: <a href="about/log.html"><xsl:value-of select="$versionname"/></a>
        &#160;&#160;(<xsl:value-of select="$versiondate"/>)
    </p>
    </xsl:template>


	<xsl:template match="downloadlink">
		<a href="{$versionname}.jar"><xsl:value-of select="$versionname"/>.jar</a>
		&#160;&#160;(<xsl:value-of select="$versiondate"/>)
	</xsl:template>

	<xsl:template match="icingdownloadlink">
		<a href="{$icingversionname}.jar"><xsl:value-of select="$icingversionname"/>.jar</a>
	</xsl:template>



	<xsl:template match="modelslink">
		<a href="{$versionmodelsname}.zip"><xsl:value-of select="$versionmodelsname"/>.zip</a>
	</xsl:template>



	<xsl:template match="exfile">
		<h3 class="valfile"><xsl:value-of select="@path"/></h3>
		<div class="valfile">
			<xsl:variable name="fvp">../../../../../../PSICS/src/org/psics/samples/<xsl:value-of select="@path"/></xsl:variable>

			<xsl:apply-templates select="document($fvp)" mode="xmlverb"/>
		</div>
	</xsl:template>



	<xsl:template match="exincludeindex">
		<xsl:variable name="idxfile">../../../../../../Examples/psics-out/index.xml</xsl:variable>
		<xsl:apply-templates select="document($idxfile)"/>
	</xsl:template>

	<xsl:template match="ModelIndex">
		<ul>
			<xsl:apply-templates select="dir" mode="mi"/>
		</ul>
	</xsl:template>

	<xsl:template match="dir" mode="mi">
		<li>
		<!--  target="exswin" -->
			<a href="examples/{@name}/index.html"><xsl:value-of select="@name"/></a>
			&#160;
			<xsl:apply-templates/>
		</li>
	</xsl:template>


	<xsl:template match="summary">
		<div class="summary">
			<xsl:apply-templates/>
		</div>
	</xsl:template>


	<xsl:template match="code">
		 <div class="code">
			 <pre><xsl:apply-templates/></pre>
		 </div>
	</xsl:template>

	<xsl:template match="smallcode">
		 <div class="smallcode">
			 <pre><xsl:apply-templates/></pre>
		 </div>
	</xsl:template>


	<xsl:template match="xcode">
		<div class="xcode">
			<pre><xsl:apply-templates mode="xmlverb"/></pre>
		</div>
	</xsl:template>


	<xsl:template match="quote">
		<div class="quote">
			<xsl:apply-templates/>
		</div>
	</xsl:template>


	<xsl:template match="faqlist">
		<ol>
			<xsl:apply-templates mode="faqq"/>
		</ol>
		<p>&#160;</p>
		<xsl:apply-templates mode="faqqa"/>
	</xsl:template>

	<xsl:template match="qa" mode="faqq">
		<xsl:variable name="num" select="position()"/>
		<li><a class="faqq" href="#{$num}"><xsl:apply-templates select="question"/></a></li>
	</xsl:template>


	<xsl:template match="qa" mode="faqqa">
		<xsl:variable name="num" select="position()"/>
		<div class="question"><a name="{$num}"></a>
			<xsl:apply-templates select="question"/>

		</div>
		<div class="answer">
			<xsl:apply-templates select="answer"/>
		</div>
	</xsl:template>


	<xsl:template match="xmlembed">
		<h3>XML source file: <xsl:value-of select="@src"/></h3>
		<xsl:variable name="dsrc">../../tmpres/<xsl:value-of select="@src"/></xsl:variable>
		<div class="xmlembed">
			<xsl:apply-templates select="document($dsrc, /)" mode="xmlverb"/>
		</div>
	</xsl:template>

	<xsl:template match="psixmlembed">
		<h3>PSICS version: <xsl:value-of select="@src"/></h3>
		<xsl:variable name="dsrc">../../tmpres/<xsl:value-of select="@src"/></xsl:variable>
		<div class="psixmlembed">
			<xsl:apply-templates select="document($dsrc, /)" mode="xmlverb"/>
		</div>
	</xsl:template>



        <xsl:template match="screenshot">
                <xsl:variable name="sfnm">sml-shot<xsl:value-of select="@no"/>.png</xsl:variable>
                <xsl:variable name="ssid">ss<xsl:value-of select="@no"/></xsl:variable>
                <xsl:variable name="ctr">ctr<xsl:value-of select="@no"/></xsl:variable>
                <xsl:variable name="sty">position : relative; z-index : <xsl:value-of select="@zi"/></xsl:variable>
                <div id="{$ctr}" style="{$sty}">
                <table class="screenshot" cellspacing="0" cellpadding="0" style="z-index : 1">
                        <tr>
                                <td>
                                        <img class="screenshot" src="{$sfnm}" id="{$ssid}"/>
                                </td>
                                <td valign="top">
                                        <div style="margin-left : 20px; margin-top : -16px;">
                                                <xsl:apply-templates/>
                                        </div>
                                </td>
                        </tr>
                </table>
                </div>
        </xsl:template>





</xsl:stylesheet>



