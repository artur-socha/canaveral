<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:s3="http://s3.amazonaws.com/doc/2006-03-01/"
                version="1.0">

    <xsl:param name="prefix"/>

    <xsl:template match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="s3:Contents[not(starts-with(s3:Key, $prefix))]"/>

</xsl:stylesheet>