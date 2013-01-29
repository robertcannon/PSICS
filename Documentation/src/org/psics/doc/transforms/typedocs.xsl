<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">



	<xsl:template match="elementindex">
	    <xsl:variable name="pth">../../../work/types/Elements.xml</xsl:variable>
		<xsl:apply-templates select="document($pth, /)"/>
	</xsl:template>


	<xsl:template match="attributeindex">

	    <xsl:variable name="pth">../../../work/types/Attributes.xml</xsl:variable>
	<table cellspacing="2">

		<xsl:apply-templates select="document($pth, /)"/>
	</table>

	</xsl:template>


	<xsl:template match="TypeSetRef">
		<h2><xsl:value-of select="title"/></h2>
		<xsl:variable name="pth">../../../work/types/<xsl:value-of select="@ref"/>.xml</xsl:variable>
		<xsl:apply-templates select="document($pth, /)"/>
	</xsl:template>



	<xsl:template match="TypeSet">
		<xsl:apply-templates/>
	</xsl:template>


	<xsl:template match="TypeDoc">
		<div class="typedoc">
			<table width="100%">
				<tr>
					<td>
					<a class="nol" name="{@type}"><h3><xsl:value-of select="@type"/></h3></a>
					</td>
					<td class="tag">
				<xsl:if test="@tag">
					<p class="minimal"><xsl:value-of select="@tag"/></p>
				</xsl:if>
					</td>

					<td>
					<p class="minimal">
						<xsl:if test="@standalone">Standalone model
							<xsl:if test="UsableIn"> or </xsl:if>
						</xsl:if>
						<xsl:if test="UsableIn"> within:
							<xsl:apply-templates select="UsableIn"/>
						</xsl:if>
					</p>
				</td>
				</tr>
				</table>

				<xsl:if test="Info">
					<p>
						<xsl:apply-templates select="Info"/>
					</p>
				</xsl:if>

				<div class="typetables">
					<p>Attributes</p>
					<table cellpsacing="1" class="att">
						<tr>
							<th>Name</th>
							<th>Type</th>
							<th>Definition</th>
							<th>Units</th>
							<th>Range</th>
							<th>Required</th>
					</tr>
						<xsl:apply-templates select="Identifier"/>
						<xsl:apply-templates select="LibraryPath"/>
						<xsl:apply-templates select="ReferenceToFile"/>
						<xsl:apply-templates select="ReferenceByIdentifier"/>
						<xsl:apply-templates select="ReferenceByLabel"/>
						<xsl:apply-templates select="Expression"/>
						<xsl:apply-templates select="Choice"/>
					 	<xsl:apply-templates select="DoubleField"/>
					 	<xsl:apply-templates select="NumberField"/>
					    <xsl:apply-templates select="FlagField"/>
						 <xsl:apply-templates select="DoubleArray"/>
						 <xsl:apply-templates select="Label"/>
						 <xsl:apply-templates select="Hint"/>
						 <xsl:apply-templates select="MetadataField"/>

					</table>


			 		<xsl:choose>
			 		<xsl:when test="./Container">

			 	<p style="margin-top : 30px">Elements</p>
			 	<table cellspacing="1" class="att">
					<tr><th width="30%">Element type</th><th>Role</th></tr>
					<xsl:apply-templates select="Container"/>
					<xsl:apply-templates select="SubComponent"/>
				</table>
		</xsl:when>

		<xsl:otherwise>
			<p>Elements - No child elements are allowed</p>
		</xsl:otherwise>


		</xsl:choose>


				</div>


		<xsl:if test="ModelExample">
			<p style="padding-bottom : 0px; margin-bottom : 0px;">Example</p>
			<div class="modelexample">
				<xsl:apply-templates select="ModelExample"/>
			</div>
		</xsl:if>


		</div>

	</xsl:template>


	<xsl:template match="ModelExample">
		<xsl:apply-templates select="./*" mode="xmlverb"/>
	</xsl:template>



	<xsl:template match="Info">
		<xsl:apply-templates/>
	</xsl:template>


	<xsl:template match="Identifier">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>identifier</td>
			<td><xsl:value-of select="@tag"/></td>
			<td></td>
			<td></td>
			<td>yes</td>
		</tr>
	</xsl:template>



	<xsl:template match="DoubleField">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>Floating point value</td>
			<td width="40%"><xsl:value-of select="@tag"/></td>
			<td><xsl:value-of select="@units"/></td>
			<td><xsl:value-of select="@range"/></td>
			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>


<xsl:template match="DoubleArray">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>Array of floating point values</td>
			<td width="40%"><xsl:value-of select="@tag"/><br/><xsl:value-of select="@info"/></td>


			<td><xsl:value-of select="@units"/></td>
			<td><xsl:value-of select="@range"/></td>
			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>



	<xsl:template match="NumberField">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>Whole number</td>
			<td width="40%"><xsl:value-of select="@tag"/></td>
			<td></td>
			<td><xsl:value-of select="@range"/></td>
			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>



	<xsl:template match="FlagField">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>Flag</td>
			<td width="40%"><xsl:value-of select="@tag"/></td>
			<td></td>
			<td>"true"&#160;or&#160;"yes",  "false" or "no"</td>
			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>


	<xsl:template match="Label">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>plain text</td>
			<td width="40%"><xsl:value-of select="@tag"/></td>
			<td colspan="3">
				<xsl:value-of select="@info"/>
			</td>
		</tr>
	</xsl:template>

<xsl:template match="HintField">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>plain text</td>
			<td width="40%"><xsl:value-of select="@tag"/></td>
			<td colspan="3">
				<xsl:value-of select="@info"/>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="MetadataField">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>Metadata</td>
			<td width="40%"><xsl:value-of select="@tag"/></td>
			<td colspan="3">
				<xsl:value-of select="@info"/>
			</td>
		</tr>
	</xsl:template>



	<xsl:template match="ReferenceByIdentifier">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>identifier reference</td>
			<td width="40%"><xsl:value-of select="@tag"/>
					(<xsl:apply-templates select="TargetType"/>)
			</td>
			<td></td>
			<td></td>
			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>


	<xsl:template match="ReferenceByLabel">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>label reference</td>
			<td></td>
			<td></td>
			<td></td>
			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>





	<xsl:template match="ReferenceToFile">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>text - the path to the file or folder</td>
			<td width="40%"><xsl:value-of select="@tag"/>
			</td>
			<td></td>
			<td></td>

			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>





	<xsl:template match="Expression">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>text - the expression</td>
			<td width="40%"><xsl:value-of select="@tag"/>
			</td>
			<td></td>
			<td></td>
			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="Choice">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>one of the possible values</td>
			<td width="40%"><xsl:value-of select="@tag"/>
			</td>
			<td></td>
			<td>
				<xsl:apply-templates select="Possible"/>
			</td>

			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>


	<xsl:template match="Possible">
		<xsl:choose>
		<xsl:when test="position() = last()">
		  <xsl:value-of select="@value"/>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="@value"/>,
		</xsl:otherwise>
		</xsl:choose>
	</xsl:template>


	<xsl:template match="LibraryPath">
		<tr>
			<td><b><xsl:value-of select="@name"/></b></td>
			<td>the path to a folder to search for models</td>
			<td width="40%"><xsl:value-of select="@tag"/>
			</td>
			<td></td>
			<td></td>
			<td>
				<xsl:if test="@required">yes</xsl:if>
			</td>
		</tr>
	</xsl:template>



	<xsl:template match="UsableIn">
		  <xsl:apply-templates select="PossibleParent"/>
	</xsl:template>


	<xsl:template match="PossibleParent">
			<xsl:choose>
				<xsl:when test="position() = last()">
					<b><xsl:value-of select="@type"/></b>
				</xsl:when>
				<xsl:otherwise>
	   			    <b><xsl:value-of select="@type"/></b>,
				</xsl:otherwise>
		  </xsl:choose>

	</xsl:template>



	<xsl:template match="Container">
		 	<tr>
				<td><xsl:apply-templates select="ContentType"/></td>
				<td><xsl:value-of select="@tag"/></td>
			</tr>
	</xsl:template>



	<xsl:template match="SubComponent">
		 	<tr>
				<td><xsl:apply-templates select="ContentType"/><br/>(max 1 instance)</td>
				<td><xsl:value-of select="@tag"/></td>
			</tr>
	</xsl:template>



	<xsl:template match="ContentType">
			<xsl:choose>
				<xsl:when test="position() = last()">
					<b><xsl:apply-templates/></b>
				</xsl:when>
				<xsl:otherwise>
	   			    <b><xsl:apply-templates/></b>,
				</xsl:otherwise>
		  </xsl:choose>

	</xsl:template>


	<xsl:template match="TargetType">
		<xsl:choose>
			<xsl:when test="position() = 1"></xsl:when>
			<xsl:otherwise>, </xsl:otherwise>
		</xsl:choose>
		<i><xsl:apply-templates/></i>
	</xsl:template>





	<xsl:template match="ElementsIndex">
		<xsl:apply-templates/>
	</xsl:template>

	<xsl:template match="IndexEltItem">
		<p style="margin:0px; padding : 0px;">
			<xsl:variable name="tgt">
				<xsl:choose>
					<xsl:when test="@page='cont'">control.html</xsl:when>
					<xsl:when test="@page='envi'">environment.html</xsl:when>
					<xsl:when test="@page='stim'">stimrec.html</xsl:when>
			        <xsl:when test="@page='morp'">morphology.html</xsl:when>
			        <xsl:when test="@page='dist'">properties.html</xsl:when>
			        <xsl:when test="@page='visc'">visualization.html</xsl:when>
			        <xsl:when test="@page='chan'">channels.html</xsl:when>
				</xsl:choose>
			</xsl:variable>
			<a href="{$tgt}#{@entry}"><xsl:value-of select="@entry"/></a>
		</p>
	</xsl:template>



	<xsl:template match="IndexAttItem">
		<p style="margin : 0px; padding : 0px">
			<tr>
				<td valign="top">
					<b><xsl:value-of select="@entry"/></b>
				</td>
				<td>
					<xsl:apply-templates select="IndexRef"/>
				</td>
			</tr>
		</p>
	</xsl:template>

	<xsl:template match="IndexRef">
		<xsl:variable name="tgt">
				<xsl:choose>
					<xsl:when test="@page='cont'">control.html</xsl:when>
					<xsl:when test="@page='envi'">environment.html</xsl:when>
					<xsl:when test="@page='stim'">stimrec.html</xsl:when>
			        <xsl:when test="@page='morp'">morphology.html</xsl:when>
			        <xsl:when test="@page='dist'">properties.html</xsl:when>
			        <xsl:when test="@page='visc'">visualization.html</xsl:when>
			        <xsl:when test="@page='chan'">channels.html</xsl:when>
				</xsl:choose>
			</xsl:variable>
			<a href="{$tgt}#{@block}"><xsl:value-of select="@block"/></a>
			<xsl:choose>
				<xsl:when test="position()=last()">
				</xsl:when>
				<xsl:otherwise>,
				</xsl:otherwise>
			</xsl:choose>
	</xsl:template>

</xsl:stylesheet>