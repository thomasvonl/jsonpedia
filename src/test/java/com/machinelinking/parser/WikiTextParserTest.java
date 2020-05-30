/*
 * Copyright 2012-2015 Michele Mostarda (me@michelemostarda.it)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.machinelinking.parser;

import com.machinelinking.pagestruct.WikiTextHRDumperHandler;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Test case for {@link WikiTextParser}.
 *
 * @author Michele Mostarda (me@michelemostarda.it)
 */
//TODO: still missing support for comments within references: ex:  [[Category:Zoologists with author abbreviations<!-- -->|{{{author_abbreviation_zoo|{{{author_abbrev_zoo}}}}}}]]
public class WikiTextParserTest {

    private static final Logger logger = Logger.getLogger(WikiTextParserTest.class);

    @Test
    public void testComment1() throws IOException, WikiTextParserException {
        parse(
                "X <!-- Y --> Z",

                "Begin Document\n" +
                "Text: 'X '\n" +
                "Comment Tag:  Y \n" +
                "Text: ' Z'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testComment2() throws IOException, WikiTextParserException {
        parse(
                "X <!--- Y ---> Z",

                "Begin Document\n" +
                "Text: 'X '\n" +
                "Comment Tag: - Y -\n" +
                "Text: ' Z'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testCommentNoClose() throws IOException, WikiTextParserException {
        parse(
                "X <!-- Y ->",

                "Begin Document\n" +
                "Text: 'X '\n" +
                "Warning: Invalid comment closure, found EOF. (1, 12)\n" +
                "Comment Tag:  Y ->\n" +
                "End Document\n",

                false
        );
    }

    @Test
    public void testApostrophesParsing() throws IOException, WikiTextParserException {
        parse(
                "''A Biography of the World's Most Famous Equation''",

                "Begin Document\n" +
                "ItalicBold: 2\n" +
                "Text: 'A Biography of the World's Most Famous Equation'\n" +
                "ItalicBold: 2\n" +
                "End Document\n"
        );
    }

    @Test
    public void testUnorderedList() throws IOException, WikiTextParserException {
        parse(
                "This is a list:\n" +
                "* Unordered list are easy to do:\n" +
                "** Start every line with a star.\n" +
                "*** More stars indicate a deeper level.\n" +
                "* Previous item continues.\n" +
                "*** [http://scholar.google.com.au/citations?user=qc6CJjYAAAAJ&hl=en Einstein's Scholar Google profile]\n" +
                "** A newline\n" +
                "* in a list\n" +
                "This closes the list\n",

                "Begin Document\n" +
                "Text: 'This is a list:\n" +
                "'\n" +
                "Begin List\n" +
                "List Item: Unordered 1\n" +
                "Text: ' Unordered list are easy to do:'\n" +
                "List Item: Unordered 2\n" +
                "Text: ' Start every line with a star.'\n" +
                "List Item: Unordered 3\n" +
                "Text: ' More stars indicate a deeper level.'\n" +
                "List Item: Unordered 1\n" +
                "Text: ' Previous item continues.'\n" +
                "List Item: Unordered 3\n" +
                "Text: ' '\n" +
                "Begin Link: http://scholar.google.com.au/citations?user=qc6CJjYAAAAJ&hl=en\n" +
                "k: null\n" +
                "Text: 'Einstein's Scholar Google profile'\n" +
                "End Link: http://scholar.google.com.au/citations?user=qc6CJjYAAAAJ&hl=en\n" +
                "List Item: Unordered 2\n" +
                "Text: ' A newline'\n" +
                "List Item: Unordered 1\n" +
                "Text: ' in a list'\n" +
                "End List\n" +
                "Text: 'This closes the list\n" +
                "'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testNumberedLists() throws IOException, WikiTextParserException {
        parse(
                "This is a numbered list:\n" +
                "# ''Numbered lists'' are:\n" +
                "## Very organized\n" +
                "## Easy to follow\n" +
                "A newline marks the end of the list.\n" +
                "# New numbering starts with 1.\n",

                "Begin Document\n" +
                "Text: 'This is a numbered list:\n" +
                "'\n" +
                "Begin List\n" +
                "List Item: Numbered 1\n" +
                "Text: ' ''Numbered lists'' are:'\n" +
                "List Item: Numbered 2\n" +
                "Text: ' Very organized'\n" +
                "List Item: Numbered 2\n" +
                "Text: ' Easy to follow'\n" +
                "End List\n" +
                "Text: 'A newline marks the end of the list.\n" +
                "'\n" +
                "Begin List\n" +
                "List Item: Numbered 1\n" +
                "Text: ' New numbering starts with 1.'\n" +
                "End List\n" +
                "End Document\n"
        );
    }

    @Test
    public void testListMustNotProduceKeys() throws IOException, WikiTextParserException {
        parse(
                "\n== Examples ==\n" +
                "* The [[square root]] of [[two|2]] is algebraic over '''Q''' = \n",

                "Begin Document\n" +
                "Text: '\n" +
                "'\n" +
                "Section [0]  Examples \n" +
                "Text: '\n" +
                "'\n" +
                "Begin List\n" +
                "List Item: Unordered 1\n" +
                "Text: ' The '\n" +
                "Begin Reference: square root\n" +
                "End Reference: square root\n" +
                "Text: ' of '\n" +
                "Begin Reference: two\n" +
                "k: null\n" +
                "Text: '2'\n" +
                "End Reference: two\n" +
                "Text: ' is algebraic over '''Q''' = '\n" +
                "End List\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParagraph() throws IOException, WikiTextParserException {
        parse(
                "This is a paragraph\n" +
                "This is nothing\n" +
                "\n" +
                "This is another paragraph\n",

                "Begin Document\n" +
                "Text: 'This is a paragraph\nThis is nothing'\n" +
                "Paragraph\n" +
                "Text: 'This is another paragraph\n" +
                "'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testReadSection() throws IOException, WikiTextParserException {
        parse(
                "\n" +
                "==This is a Section==\n" +
                "Section content\n" +
                "===This is another Section===\n" +
                "Other section content\n",

                "Begin Document\n" +
                "Text: '\n" +
                "'\n" +
                "Section [0] This is a Section\n" +
                "Text: '\n" +
                "Section content\n" +
                "'\n" +
                "Section [1] This is another Section\n" +
                "Text: '\n" +
                "Other section content\n" +
                "'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseReference1() throws IOException, WikiTextParserException {
        parse(
                "this is an internal link: [[Princeton]] ending here.",

                "Begin Document\n" +
                "Text: 'this is an internal link: '\n" +
                "Begin Reference: Princeton\n" +
                "End Reference: Princeton\n" +
                "Text: ' ending here.'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseReference2() throws IOException, WikiTextParserException {
        parse(
                "this is an internal link: [[Princeton, New Jersey|Princeton]] ending here.",

                "Begin Document\n" +
                "Text: 'this is an internal link: '\n" +
                "Begin Reference: Princeton, New Jersey\n" +
                "k: null\n" +
                "Text: 'Princeton'\n" +
                "End Reference: Princeton, New Jersey\n" +
                "Text: ' ending here.'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testNestedReference() throws IOException, WikiTextParserException {
         parse(
                 "[[File:Einstein 1911 Solvay.jpg|alt=Upper body shot of man in suit, high white collar and bow tie.|thumb|upright|Einstein at the [[Solvay Conference]] in 1911]]",

                 "Begin Document\n" +
                 "Begin Reference: File:Einstein 1911 Solvay.jpg\n" +
                 "k: alt\n" +
                 "Text: 'Upper body shot of man in suit, high white collar and bow tie.'\n" +
                 "k: null\n" +
                 "Text: 'thumb'\n" +
                 "k: null\n" +
                 "Text: 'upright'\n" +
                 "k: null\n" +
                 "Text: 'Einstein at the '\n" +
                 "Begin Reference: Solvay Conference\n" +
                 "End Reference: Solvay Conference\n" +
                 "Text: ' in 1911'\n" +
                 "End Reference: File:Einstein 1911 Solvay.jpg\n" +
                 "End Document\n"
         );
    }

    @Test
    public void testReferenceWithTag() throws IOException, WikiTextParserException {
        parse(
                "[[Square brackets|<nowiki>[</nowiki>]]",

                "Begin Document\n" +
                "Begin Reference: Square brackets\n" +
                "Open Tag: nowiki attributes: []\n" +
                "Text: '['\n" +
                "Close Tag: nowiki\n" +
                "End Reference: Square brackets\n" +
                "End Document\n"
        );
    }

    @Test
    public void testReferenceWithMinus() throws IOException, WikiTextParserException {
         parse(
                 "[[Less-than sign|<]]",

                 "Begin Document\n" +
                 "Begin Reference: Less-than sign\n" +
                 // TODO: missing '<'
                 "End Reference: Less-than sign\n" +
                 "End Document\n"
         );
    }

    @Test
    public void testReferenceInvalidClose() throws IOException, WikiTextParserException {
        parse(
                "[[id:Bronkioli]\n",

                "Begin Document\n" +
                "Begin Reference: id:Bronkioli\n" +
                "Warning: Invalid closure for reference. (1, 16)\n" +
                "End Reference: id:Bronkioli\n" +
                "Text: '\n'\n" +
                "End Document\n",

                false
        );
    }

    @Test
    public void testMissingReferenceClosure() throws IOException, WikiTextParserException {
        parse(
                "land in the southeastern portion of the [[Borough (New York City)|borough of [[the Bronx]] in [[New York City]]) was named in his honor.\n",

                "Begin Document\n" +
                "Text: 'land in the southeastern portion of the '\n" +
                "Begin Reference: Borough (New York City)\n" +
                "k: null\n" +
                "Text: 'borough of '\n" +
                "Begin Reference: the Bronx\n" +
                "End Reference: the Bronx\n" +
                "Text: ' in '\n" +
                "Begin Reference: New York City\n" +
                "End Reference: New York City\n" +
                "Text: ') was named in his honor.'\n" +
                "Warning: Invalid closure for reference. (2, 0)\n" +
                "End Reference: Borough (New York City)\n" +
                "End Document\n",

                false
        );
    }

    @Test
    public void testParseLinkWithLabel() throws IOException, WikiTextParserException {
        parse(
                "[http://link Text]",

                "Begin Document\n" +
                "Begin Link: http://link\n" +
                "k: null\n" +
                "Text: 'Text'\n" +
                "End Link: http://link\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseLinkWithoutLabel() throws IOException, WikiTextParserException {
        parse(
                "[http://link]",

                "Begin Document\n" +
                "Begin Link: http://link\n" +
                "End Link: http://link\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseNoLink() throws IOException, WikiTextParserException {
        parse(
                "[thisissometext]",

                "Begin Document\n" +
                "Text: '[thisissometext'\n" +
                "Text: ']'\n" +
                "End Document\n"
        );
    }

    /**
     * There are too many pages with this issue, that can be solved only at parsing level.
     */
    @Test
    public void testLinkInMarkupNotClosed() throws IOException, WikiTextParserException {
        parse(
                "<ref>[http://www.fmv.se/WmTemplates/page.aspx?id=5295</ref>",

                "Begin Document\n" +
                "Open Tag: ref attributes: []\n" +
                "Begin Link: http://www.fmv.se/WmTemplates/page.aspx?id=5295\n" +
                "Warning: Invalid link closure. (1, 54)\n" +
                "End Link: http://www.fmv.se/WmTemplates/page.aspx?id=5295\n" +
                "Close Tag: ref\n" +
                "End Document\n",

                false
        );
    }

    @Test
    public void testParseTemplate1() throws IOException, WikiTextParserException {
        parse(
                "{{Good article}}",

                "Begin Document\n" +
                "Begin Template: Good article\n" +
                "End Template: Good article\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplate2() throws IOException, WikiTextParserException {
        parse(
                "{{Redirect|Einstein}}",

                "Begin Document\n" +
                "Begin Template: Redirect\n" +
                "k: null\n" +
                "Text: 'Einstein'\n" +
                "End Template: Redirect\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplate2_1() throws IOException, WikiTextParserException {
        parse(
                "{{Redirect|Einstein}}{{Good article}}",

                "Begin Document\n" +
                "Begin Template: Redirect\n" +
                "k: null\n" +
                "Text: 'Einstein'\n" +
                "End Template: Redirect\n" +
                "Begin Template: Good article\n" +
                "End Template: Good article\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplate3() throws IOException, WikiTextParserException {
        parse(
                "{{Redirect|Einstein}}\n" +
                "{{Good article}}\n" +
                "{{Infobox scientist\n" +
                "| name        = Albert Einstein\n" +
                "| image       = Einstein 1921 portrait2.jpg\n" +
                "| caption     = Albert Einstein in 1921\n" +
                "}}",

                "Begin Document\n" +
                "Begin Template: Redirect\n" +
                "k: null\n" +
                "Text: 'Einstein'\n" +
                "End Template: Redirect\n" +
                "Text: '\n" +
                "'\n" +
                "Begin Template: Good article\n" +
                "End Template: Good article\n" +
                "Text: '\n" +
                "'\n" +
                "Begin Template: Infobox scientist\n" +
                "\n" +
                "k: name        \n" +
                "Text: 'Albert Einstein\n" +
                "'\n" +
                "k: image       \n" +
                "Text: 'Einstein 1921 portrait2.jpg\n" +
                "'\n" +
                "k: caption     \n" +
                "Text: 'Albert Einstein in 1921\n" +
                "'\n" +
                "End Template: Infobox scientist\n" +
                "\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplate4() throws IOException, WikiTextParserException {
        parse(
                "{{Redirect|Einstein}}\n" +
                "{{Good article}}\n" +
                "{{Infobox scientist\n" +
                "| name        = Albert Einstein\n" +
                "| image       = Einstein 1921 portrait2.jpg\n" +
                "| caption     = Albert Einstein in 1921\n" +
                "| birth_date  = {{Birth date|df=yes|year=1879|month=3|14}}\n" +
               "}}",

                "Begin Document\n" +
                "Begin Template: Redirect\n" +
                "k: null\n" +
                "Text: 'Einstein'\n" +
                "End Template: Redirect\n" +
                "Text: '\n" +
                "'\n" +
                "Begin Template: Good article\n" +
                "End Template: Good article\n" +
                "Text: '\n" +
                "'\n" +
                "Begin Template: Infobox scientist\n" +
                "\n" +
                "k: name        \n" +
                "Text: 'Albert Einstein\n" +
                "'\n" +
                "k: image       \n" +
                "Text: 'Einstein 1921 portrait2.jpg\n" +
                "'\n" +
                "k: caption     \n" +
                "Text: 'Albert Einstein in 1921\n" +
                "'\n" +
                "k: birth_date  \n" +
                "Begin Template: Birth date\n" +
                "k: df\n" +
                "Text: 'yes'\n" +
                "k: year\n" +
                "Text: '1879'\n" +
                "k: month\n" +
                "Text: '3'\n" +
                "k: null\n" +
                "Text: '14'\n" +
                "End Template: Birth date\n" +
                "Text: '\n" +
                "'\n" +
                "End Template: Infobox scientist\n" +
                "\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplate5() throws IOException, WikiTextParserException {
        parse(
                "{{Redirect|Einstein}}\n" +
                "{{Good article}}\n" +
                "{{Infobox scientist\n" +
                "| name          = Albert Einstein\n" +
                "| image         = Einstein 1921 portrait2.jpg\n" +
                "| caption       = Albert Einstein in 1921\n" +
                "| birth_date    = {{Birth date|df=yes|year=1879|month=3|14}}\n" +
                "| death_place   = [[Princeton, New Jersey|Princeton]], New Jersey, United States\n" +
                "| official_site = [http://alberteinstein.org Albert Einstein Official Site]\n" +
               "}}",

               "Begin Document\n" +
               "Begin Template: Redirect\n" +
               "k: null\n" +
               "Text: 'Einstein'\n" +
               "End Template: Redirect\n" +
               "Text: '\n" +
               "'\n" +
               "Begin Template: Good article\n" +
               "End Template: Good article\n" +
               "Text: '\n" +
               "'\n" +
               "Begin Template: Infobox scientist\n" +
               "\n" +
               "k: name          \n" +
               "Text: 'Albert Einstein\n" +
               "'\n" +
               "k: image         \n" +
               "Text: 'Einstein 1921 portrait2.jpg\n" +
               "'\n" +
               "k: caption       \n" +
               "Text: 'Albert Einstein in 1921\n" +
               "'\n" +
               "k: birth_date    \n" +
               "Begin Template: Birth date\n" +
               "k: df\n" +
               "Text: 'yes'\n" +
               "k: year\n" +
               "Text: '1879'\n" +
               "k: month\n" +
               "Text: '3'\n" +
               "k: null\n" +
               "Text: '14'\n" +
               "End Template: Birth date\n" +
               "Text: '\n" +
               "'\n" +
               "k: death_place   \n" +
               "Begin Reference: Princeton, New Jersey\n" +
               "k: null\n" +
               "Text: 'Princeton'\n" +
               "End Reference: Princeton, New Jersey\n" +
               "Text: ', New Jersey, United States\n" +
               "'\n" +
               "k: official_site \n" +
               "Begin Link: http://alberteinstein.org\n" +
               "k: null\n" +
               "Text: 'Albert Einstein Official Site'\n" +
               "End Link: http://alberteinstein.org\n" +
               "Text: '\n" +
               "'\n" +
               "End Template: Infobox scientist\n" +
               "\n" +
               "End Document\n"
        );
    }

    @Test
    public void testParseTemplate6() throws IOException, WikiTextParserException {
        parse(
                "{{Redirect|Einstein}}\n" +
                "{{Good article}}\n" +
                "{{Infobox scientist\n" +
                "| name        = Albert Einstein\n" +
                "| image       = Einstein 1921 portrait2.jpg\n" +
                "| caption     = Albert Einstein in 1921\n" +
                "| birth_date  = {{Birth date|df=yes|year=1879|month=3|14}}\n" +
                "| death_place = [[Princeton, New Jersey|Princeton]], New Jersey, United States\n" +
                "| citizenship = {{Plainlist|\n" +
                "* Germany\n" +
                "* [[Kingdom of Württemberg|Württemberg/Germany]] (1879–1896)\n" +
                "* [[Statelessness|Stateless]] (1896–1901)\n" +
                "}}\n" +
                "}}\n",

                "Begin Document\n" +
                "Begin Template: Redirect\n" +
                "k: null\n" +
                "Text: 'Einstein'\n" +
                "End Template: Redirect\n" +
                "Text: '\n" +
                "'\n" +
                "Begin Template: Good article\n" +
                "End Template: Good article\n" +
                "Text: '\n" +
                "'\n" +
                "Begin Template: Infobox scientist\n" +
                "\n" +
                "k: name        \n" +
                "Text: 'Albert Einstein\n" +
                "'\n" +
                "k: image       \n" +
                "Text: 'Einstein 1921 portrait2.jpg\n" +
                "'\n" +
                "k: caption     \n" +
                "Text: 'Albert Einstein in 1921\n" +
                "'\n" +
                "k: birth_date  \n" +
                "Begin Template: Birth date\n" +
                "k: df\n" +
                "Text: 'yes'\n" +
                "k: year\n" +
                "Text: '1879'\n" +
                "k: month\n" +
                "Text: '3'\n" +
                "k: null\n" +
                "Text: '14'\n" +
                "End Template: Birth date\n" +
                "Text: '\n" +
                "'\n" +
                "k: death_place \n" +
                "Begin Reference: Princeton, New Jersey\n" +
                "k: null\n" +
                "Text: 'Princeton'\n" +
                "End Reference: Princeton, New Jersey\n" +
                "Text: ', New Jersey, United States\n" +
                "'\n" +
                "k: citizenship \n" +
                "Begin Template: Plainlist\n" +
                "Begin List\n" +
                "List Item: Unordered 1\n" +
                "Text: ' Germany'\n" +
                "List Item: Unordered 1\n" +
                "Text: ' '\n" +
                "Begin Reference: Kingdom of Württemberg\n" +
                "k: null\n" +
                "Text: 'Württemberg/Germany'\n" +
                "End Reference: Kingdom of Württemberg\n" +
                "Text: ' (1879–1896)'\n" +
                "List Item: Unordered 1\n" +
                "Text: ' '\n" +
                "Begin Reference: Statelessness\n" +
                "k: null\n" +
                "Text: 'Stateless'\n" +
                "End Reference: Statelessness\n" +
                "Text: ' (1896–1901)'\n" +
                "End List\n" +
                "End Template: Plainlist\n" +
                "Text: '\n" +
                "'\n" +
                "End Template: Infobox scientist\n" +
                "\n" +
                "Text: '\n'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplateEmptyParams() throws IOException, WikiTextParserException {
        parse(
                "{{chart | | | | ThA |y| MaP | | | | | | | | |  GH | GH=George Huxley|ThA=[[Thomas Arnold]] 1795–1842|MaP=Mary Penrose 1791–1873}}",

                "Begin Document\n" +
                "Begin Template: chart \n" +
                "k: null\n" +
                "Text: 'ThA '\n" +
                "k: null\n" +
                "Text: 'y'\n" +
                "k: null\n" +
                "Text: 'MaP '\n" +
                "k: null\n" +
                "Text: 'GH '\n" +
                "k: GH\n" +
                "Text: 'George Huxley'\n" +
                "k: ThA\n" +
                "Begin Reference: Thomas Arnold\n" +
                "End Reference: Thomas Arnold\n" +
                "Text: ' 1795–1842'\n" +
                "k: MaP\n" +
                "Text: 'Mary Penrose 1791–1873'\n" +
                "End Template: chart \n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplateQuote() throws IOException, WikiTextParserException {
        parse(
                "{{quote|This morning, as for some days past, it seems exceedingly probable that this Administration will not be re-elected. Then it will be my duty to so co-operate with the President elect, as to save the Union between the election and the inauguration; as he will have secured his election on such ground that he cannot possibly save it afterward.<ref>Basler (1953), p. 514.{{full}}</ref>}}",

                "Begin Document\n" +
                "Begin Template: quote\n" +
                "k: null\n" +
                "Text: 'This morning, as for some days past, it seems exceedingly probable that this Administration will not be re-elected. Then it will be my duty to so co-operate with the President elect, as to save the Union between the election and the inauguration; as he will have secured his election on such ground that he cannot possibly save it afterward.'\n" +
                "Open Tag: ref attributes: []\n" +
                "Text: 'Basler (1953), p. 514.'\n" +
                "Begin Template: full\n" +
                "End Template: full\n" +
                "Close Tag: ref\n" +
                "End Template: quote\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplateAnonParams() throws IOException, WikiTextParserException {
        parse(
                "{{IPAc-en|icon|ˈ|æ|l|b|ər|t|_|ˈ|aɪ|n|s|t|aɪ|n}}",

                "Begin Document\n" +
                "Begin Template: IPAc-en\n" +
                "k: null\n" +
                "Text: 'icon'\n" +
                "k: null\n" +
                "Text: 'ˈ'\n" +
                "k: null\n" +
                "Text: 'æ'\n" +
                "k: null\n" +
                "Text: 'l'\n" +
                "k: null\n" +
                "Text: 'b'\n" +
                "k: null\n" +
                "Text: 'ər'\n" +
                "k: null\n" +
                "Text: 't'\n" +
                "k: null\n" +
                "Text: '_'\n" +
                "k: null\n" +
                "Text: 'ˈ'\n" +
                "k: null\n" +
                "Text: 'aɪ'\n" +
                "k: null\n" +
                "Text: 'n'\n" +
                "k: null\n" +
                "Text: 's'\n" +
                "k: null\n" +
                "Text: 't'\n" +
                "k: null\n" +
                "Text: 'aɪ'\n" +
                "k: null\n" +
                "Text: 'n'\n" +
                "End Template: IPAc-en\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplateAnonCompositeParams() throws IOException, WikiTextParserException {
        parse(
                "{{Template|v1|v2|v3 [[link]] v4}}",

               "Begin Document\n" +
               "Begin Template: Template\n" +
               "k: null\n" +
               "Text: 'v1'\n" +
               "k: null\n" +
               "Text: 'v2'\n" +
               "k: null\n" +
               "Text: 'v3 '\n" +
               "Begin Reference: link\n" +
               "End Reference: link\n" +
               "Text: ' v4'\n" +
               "End Template: Template\n" +
               "End Document\n"
        );
    }

    @Test
    public void testParseNestedTemplate() throws IOException, WikiTextParserException {
        parse(
                "{{TemplateMapping\n" +
                "| mapToClass = Congressman\n" +
                "| mappings =\n" +
                "\t{{PropertyMapping | templateProperty = otherparty | ontologyProperty = otherParty }}\n" +
                "\t{{PropertyMapping | templateProperty = name | ontologyProperty = foaf:name }}\n" +
                "\t{{PropertyMapping | templateProperty = birth_date | ontologyProperty = birthDate }}\n" +
                "\t{{PropertyMapping | templateProperty = birth_place | ontologyProperty = birthPlace }}\n" +
                "}}\n",

                "Begin Document\n" +
                "Begin Template: TemplateMapping\n" +
                "\n" +
                "k: mapToClass \n" +
                "Text: 'Congressman\n" +
                "'\n" +
                "k: mappings \n" +
                "Begin Template: PropertyMapping \n" +
                "k: templateProperty \n" +
                "Text: 'otherparty '\n" +
                "k: ontologyProperty \n" +
                "Text: 'otherParty '\n" +
                "End Template: PropertyMapping \n" +
                "Text: '\n" +
                "\t'\n" +
                "Begin Template: PropertyMapping \n" +
                "k: templateProperty \n" +
                "Text: 'name '\n" +
                "k: ontologyProperty \n" +
                "Text: 'foaf:name '\n" +
                "End Template: PropertyMapping \n" +
                "Text: '\n" +
                "\t'\n" +
                "Begin Template: PropertyMapping \n" +
                "k: templateProperty \n" +
                "Text: 'birth_date '\n" +
                "k: ontologyProperty \n" +
                "Text: 'birthDate '\n" +
                "End Template: PropertyMapping \n" +
                "Text: '\n" +
                "\t'\n" +
                "Begin Template: PropertyMapping \n" +
                "k: templateProperty \n" +
                "Text: 'birth_place '\n" +
                "k: ontologyProperty \n" +
                "Text: 'birthPlace '\n" +
                "End Template: PropertyMapping \n" +
                "Text: '\n" +
                "'\n" +
                "End Template: TemplateMapping\n\n" +
                "Text: '\n'\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplateDefinition1() throws IOException, WikiTextParserException {
        parse(
                "{{Atemplate|prop1 = {{#directive:{{{var1|}}}|{{{var2}}}|prop2={{{var3}}}|{{var4}}}}}}",

                "Begin Document\n" +
                "Begin Template: Atemplate\n" +
                "k: prop1 \n" +
                "Begin Template: #directive:<var1:>\n" +
                "k: null\n" +
                "var: var2 [null]\n" +
                "k: prop2\n" +
                "var: var3 [null]\n" +
                "k: null\n" +
                "Begin Template: var4\n" +
                "End Template: var4\n" +
                "End Template: #directive:<var1:>\n" +
                "End Template: Atemplate\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplateDefinition2() throws IOException, WikiTextParserException {
        parse(
                "{{Atemplate|prop1={{{var1|{{{defval1|}}}}}}}}" ,

                "Begin Document\n" +
                "Begin Template: Atemplate\n" +
                "k: prop1\n" +
                "var: var1 [var: defval1 [const: []]]\n" +
                "End Template: Atemplate\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseTemplateDefinition3() throws IOException, WikiTextParserException {
        parse(
                "{{#if:{{{thesis_title|}}}|{{#if:{{{thesis_url|}}}|[{{{thesis_url}}} ''{{{thesis_title}}}'']|''{{{thesis_title}}}''}}}} {{#if:{{{thesis_year|}}}|({{{thesis_year}}})}}",

                "Begin Document\n" +
                "Begin Template: #if:<thesis_title:>\n" +
                "k: null\n" +
                "Begin Template: #if:<thesis_url:>\n" +
                "k: null\n" +
                "Begin Link: null\n" +
                "var: thesis_url [null]\n" +
                "k: null\n" +
                "Text: ' '''\n" +
                "var: thesis_title [null]\n" +
                "Text: ''''\n" +
                "End Link: null\n" +
                "k: null\n" +
                "Text: ''''\n" +
                "var: thesis_title [null]\n" +
                "Text: ''''\n" +
                "End Template: #if:<thesis_url:>\n" +
                "End Template: #if:<thesis_title:>\n" +
                "Text: ' '\n" +
                "Begin Template: #if:<thesis_year:>\n" +
                "k: null\n" +
                "Text: '('\n" +
                "var: thesis_year [null]\n" +
                "Text: ')'\n" +
                "End Template: #if:<thesis_year:>\n" +
                "End Document\n"
        );
    }

    //TODO: still missing expansion of var/templates within tag attributes.
    @Test
    public void testParseFullTemplateDefinition1() throws IOException, WikiTextParserException {
        verifyParsing("Template1");
    }

    @Test
    public void testParseTable() throws IOException, WikiTextParserException {
        parse(
                "{| class=wikitable\n" +
                "|-\n" +
                "! Title <small>(translated)</small> !! Area of focus !! Received !! Published !! Significance\n" +
                "|-\n" +
                "| ''On a Heuristic Viewpoint Concerning the Production and Transformation of Light'' || [[Photoelectric effect]] || 18 March || 9 June || Resolved an unsolved puzzle by suggesting that energy is exchanged only in discrete amounts ([[quantum|quanta]]).<ref>{{cite book |title=Lectures on quantum mechanics |first1=Ashok |last1=Das |publisher=Hindustan Book Agency |year=2003 |isbn=8-185-93141-0 |page=59 |url=http://books.google.com/books?id=KmwsAAAAYAAJ}}</ref> This idea was pivotal to the early development of quantum theory.<ref>{{cite book |title=Seven ideas that shook the universe |edition=2nd |first1=Nathan |last1=Spielberg |first2=Bryon D. |last2=Anderson |publisher=John Wiley & Sons |year=1995 |isbn=0-471-30606-1 |page=263 |url=http://books.google.com/books?id=_pbuAAAAMAAJ}}</ref>\n" +
                "|-\n" +
                "| ''On the Motion of Small Particles Suspended in a Stationary Liquid, as Required by the Molecular Kinetic Theory of Heat'' || [[Brownian motion]] || 11 May || 18 July || Explained empirical evidence for the [[atom|atomic theory]], supporting the application of [[statistical physics]].\n" +
                "|-\n" +
                "| ''On the Electrodynamics of Moving Bodies'' || [[Special relativity]] || 30 June || 26 Sept || Reconciled Maxwell's equations for electricity and magnetism with the laws of mechanics by introducing major changes to mechanics close to the speed of light, resulting from analysis based on empirical evidence that the speed of light is independent of the motion of the observer.<ref>{{cite book\n" +
                "|title=The quantum beat: principles and applications of atomic clocks |edition=2nd  |first1=Fouad G. |last1=Major |publisher=Springer  |year=2007 |isbn=0-387-69533-8 |page=142 |url=http://books.google.com/books?id=tmdr6Wx_2PYC}}</ref>  Discredited the concept of an \"[[luminiferous ether]].\"<ref>{{cite book |title=Foundations of physics |first1=Robert Bruce |last1=Lindsay |first2=Henry |last2=Margenau |publisher=Ox Bow Press |year=1981 |isbn=0-918-02417-X |page=330 |url=http://books.google.com/books?id=dwZltQAACAAJ}}</ref>\n" +
                "|-\n" +
                "| ''Does the Inertia of a Body Depend Upon Its Energy Content?'' || [[Mass–energy equivalence|Matter–energy equivalence]] || 27 Sept || 21 Nov || Equivalence of matter and energy, {{nowrap|1=''E'' = ''mc''<sup>2</sup>}} (and by implication, the ability of gravity to \"bend\" light), the existence of \"[[rest energy]]\", and the basis of nuclear energy.\n" +
                "|}",

              "Begin Document\n" +
              "Begin Table\n" +
              "Text: ' class=wikitable\n" +
              "'\n" +
              "Header Cell (1, 1)\n" +
              "Text: 'Title '\n" +
              "Open Tag: small attributes: []\n" +
              "Text: '(translated)'\n" +
              "Close Tag: small\n" +
              "Text: ' '\n" +
              "Header Cell (1, 2)\n" +
              "Text: 'Area of focus '\n" +
              "Header Cell (1, 3)\n" +
              "Text: 'Received '\n" +
              "Header Cell (1, 4)\n" +
              "Text: 'Published '\n" +
              "Header Cell (1, 5)\n" +
              "Text: 'Significance\n" +
              "'\n" +
              "Body Cell (2, 2)\n" +
              "Text: '''On a Heuristic Viewpoint Concerning the Production and Transformation of Light'' '\n" +
              "Body Cell (2, 3)\n" +
              "Begin Reference: Photoelectric effect\n" +
              "End Reference: Photoelectric effect\n" +
              "Text: ' '\n" +
              "Body Cell (2, 4)\n" +
              "Text: '18 March '\n" +
              "Body Cell (2, 5)\n" +
              "Text: '9 June '\n" +
              "Body Cell (2, 6)\n" +
              "Text: 'Resolved an unsolved puzzle by suggesting that energy is exchanged only in discrete amounts ('\n" +
              "Begin Reference: quantum\n" +
              "k: null\n" +
              "Text: 'quanta'\n" +
              "End Reference: quantum\n" +
              "Text: ').'\n" +
              "Open Tag: ref attributes: []\n" +
              "Begin Template: cite book \n" +
              "k: title\n" +
              "Text: 'Lectures on quantum mechanics '\n" +
              "k: first1\n" +
              "Text: 'Ashok '\n" +
              "k: last1\n" +
              "Text: 'Das '\n" +
              "k: publisher\n" +
              "Text: 'Hindustan Book Agency '\n" +
              "k: year\n" +
              "Text: '2003 '\n" +
              "k: isbn\n" +
              "Text: '8-185-93141-0 '\n" +
              "k: page\n" +
              "Text: '59 '\n" +
              "k: url\n" +
              "Text: 'http://books.google.com/books?id=KmwsAAAAYAAJ'\n" +
              "End Template: cite book \n" +
              "Close Tag: ref\n" +
              "Text: ' This idea was pivotal to the early development of quantum theory.'\n" +
              "Open Tag: ref attributes: []\n" +
              "Begin Template: cite book \n" +
              "k: title\n" +
              "Text: 'Seven ideas that shook the universe '\n" +
              "k: edition\n" +
              "Text: '2nd '\n" +
              "k: first1\n" +
              "Text: 'Nathan '\n" +
              "k: last1\n" +
              "Text: 'Spielberg '\n" +
              "k: first2\n" +
              "Text: 'Bryon D. '\n" +
              "k: last2\n" +
              "Text: 'Anderson '\n" +
              "k: publisher\n" +
              "Text: 'John Wiley & Sons '\n" +
              "k: year\n" +
              "Text: '1995 '\n" +
              "k: isbn\n" +
              "Text: '0-471-30606-1 '\n" +
              "k: page\n" +
              "Text: '263 '\n" +
              "k: url\n" +
              "Text: 'http://books.google.com/books?id=_pbuAAAAMAAJ'\n" +
              "End Template: cite book \n" +
              "Close Tag: ref\n" +
              "Text: '\n" +
              "'\n" +
              "Body Cell (3, 2)\n" +
              "Text: '''On the Motion of Small Particles Suspended in a Stationary Liquid, as Required by the Molecular Kinetic Theory of Heat'' '\n" +
              "Body Cell (3, 3)\n" +
              "Begin Reference: Brownian motion\n" +
              "End Reference: Brownian motion\n" +
              "Text: ' '\n" +
              "Body Cell (3, 4)\n" +
              "Text: '11 May '\n" +
              "Body Cell (3, 5)\n" +
              "Text: '18 July '\n" +
              "Body Cell (3, 6)\n" +
              "Text: 'Explained empirical evidence for the '\n" +
              "Begin Reference: atom\n" +
              "k: null\n" +
              "Text: 'atomic theory'\n" +
              "End Reference: atom\n" +
              "Text: ', supporting the application of '\n" +
              "Begin Reference: statistical physics\n" +
              "End Reference: statistical physics\n" +
              "Text: '.\n" +
              "'\n" +
              "Body Cell (4, 2)\n" +
              "Text: '''On the Electrodynamics of Moving Bodies'' '\n" +
              "Body Cell (4, 3)\n" +
              "Begin Reference: Special relativity\n" +
              "End Reference: Special relativity\n" +
              "Text: ' '\n" +
              "Body Cell (4, 4)\n" +
              "Text: '30 June '\n" +
              "Body Cell (4, 5)\n" +
              "Text: '26 Sept '\n" +
              "Body Cell (4, 6)\n" +
              "Text: 'Reconciled Maxwell's equations for electricity and magnetism with the laws of mechanics by introducing major changes to mechanics close to the speed of light, resulting from analysis based on empirical evidence that the speed of light is independent of the motion of the observer.'\n" +
              "Open Tag: ref attributes: []\n" +
              "Begin Template: cite book\n" +
              "\n" +
              "k: title\n" +
              "Text: 'The quantum beat: principles and applications of atomic clocks '\n" +
              "k: edition\n" +
              "Text: '2nd  '\n" +
              "k: first1\n" +
              "Text: 'Fouad G. '\n" +
              "k: last1\n" +
              "Text: 'Major '\n" +
              "k: publisher\n" +
              "Text: 'Springer  '\n" +
              "k: year\n" +
              "Text: '2007 '\n" +
              "k: isbn\n" +
              "Text: '0-387-69533-8 '\n" +
              "k: page\n" +
              "Text: '142 '\n" +
              "k: url\n" +
              "Text: 'http://books.google.com/books?id=tmdr6Wx_2PYC'\n" +
              "End Template: cite book\n" +
              "\n" +
              "Close Tag: ref\n" +
              "Text: '  Discredited the concept of an \"'\n" +
              "Begin Reference: luminiferous ether\n" +
              "End Reference: luminiferous ether\n" +
              "Text: '.\"'\n" +
              "Open Tag: ref attributes: []\n" +
              "Begin Template: cite book \n" +
              "k: title\n" +
              "Text: 'Foundations of physics '\n" +
              "k: first1\n" +
              "Text: 'Robert Bruce '\n" +
              "k: last1\n" +
              "Text: 'Lindsay '\n" +
              "k: first2\n" +
              "Text: 'Henry '\n" +
              "k: last2\n" +
              "Text: 'Margenau '\n" +
              "k: publisher\n" +
              "Text: 'Ox Bow Press '\n" +
              "k: year\n" +
              "Text: '1981 '\n" +
              "k: isbn\n" +
              "Text: '0-918-02417-X '\n" +
              "k: page\n" +
              "Text: '330 '\n" +
              "k: url\n" +
              "Text: 'http://books.google.com/books?id=dwZltQAACAAJ'\n" +
              "End Template: cite book \n" +
              "Close Tag: ref\n" +
              "Text: '\n" +
              "'\n" +
              "Body Cell (5, 2)\n" +
              "Text: '''Does the Inertia of a Body Depend Upon Its Energy Content?'' '\n" +
              "Body Cell (5, 3)\n" +
              "Begin Reference: Mass–energy equivalence\n" +
              "k: null\n" +
              "Text: 'Matter–energy equivalence'\n" +
              "End Reference: Mass–energy equivalence\n" +
              "Text: ' '\n" +
              "Body Cell (5, 4)\n" +
              "Text: '27 Sept '\n" +
              "Body Cell (5, 5)\n" +
              "Text: '21 Nov '\n" +
              "Body Cell (5, 6)\n" +
              "Text: 'Equivalence of matter and energy, '\n" +
              "Begin Template: nowrap\n" +
              "k: 1\n" +
              "Text: '''E'' = ''mc'''\n" +
              "Open Tag: sup attributes: []\n" +
              "Text: '2'\n" +
              "Close Tag: sup\n" +
              "End Template: nowrap\n" +
              "Text: ' (and by implication, the ability of gravity to \"bend\" light), the existence of \"'\n" +
              "Begin Reference: rest energy\n" +
              "End Reference: rest energy\n" +
              "Text: '\", and the basis of nuclear energy.\n" +
              "'\n" +
              "End Table\n" +
              "End Document\n"
        );
    }

    @Test
    public void testParseFormula() throws IOException, WikiTextParserException {
        parse(
            "<math>\\bar{ \\bar \\alpha}</math>",

            "Begin Document\n" +
            "Open Tag: math attributes: []\n" +
            "Text: '\\bar{ \\bar \\alpha}'\n" +
            "Close Tag: math\n" +
            "End Document\n"
        );
    }

    @Test
    public void testParseCite() throws IOException, WikiTextParserException {
        parse(
                "<ref>{{cite web|url=http://lcweb2.loc.gov/diglib/ihas/loc.natlib.ihas.100010615/full.html |title=Materna (O Mother Dear, Jerusalem) / Samuel Augustus Ward &#91;hymnal&#93;:Print Material Full Description: Performing Arts Encyclopedia, Library of Congress |publisher=Lcweb2.loc.gov |date=2007-10-30 |accessdate=2011-08-20}}</ref> From time to time it has been proposed as a replacement for ''[[The Star-Spangled Banner]]'' as the national anthem, including television [[sign-off]]s.{{citation needed|date=March 2012}}",

                "Begin Document\n" +
                "Open Tag: ref attributes: []\n" +
                "Begin Template: cite web\n" +
                "k: url\n" +
                "Text: 'http://lcweb2.loc.gov/diglib/ihas/loc.natlib.ihas.100010615/full.html '\n" +
                "k: title\n" +
                "Text: 'Materna (O Mother Dear, Jerusalem) / Samuel Augustus Ward &#91;hymnal&#93;:Print Material Full Description: Performing Arts Encyclopedia, Library of Congress '\n" +
                "k: publisher\n" +
                "Text: 'Lcweb2.loc.gov '\n" +
                "k: date\n" +
                "Text: '2007-10-30 '\n" +
                "k: accessdate\n" +
                "Text: '2011-08-20'\n" +
                "End Template: cite web\n" +
                "Close Tag: ref\n" +
                "Text: ' From time to time it has been proposed as a replacement for '\n" +
                "ItalicBold: 2\n" +
                "Begin Reference: The Star-Spangled Banner\n" +
                "End Reference: The Star-Spangled Banner\n" +
                "ItalicBold: 2\n" +
                "Text: ' as the national anthem, including television '\n" +
                "Begin Reference: sign-off\n" +
                "End Reference: sign-off\n" +
                "Text: 's.'\n" +
                "Begin Template: citation needed\n" +
                "k: date\n" +
                "Text: 'March 2012'\n" +
                "End Template: citation needed\n" +
                "End Document\n",

                false
        );
    }

    @Test
    public void testParseNoTemplateInLink() throws IOException, WikiTextParserException {
        parse(
                "<ref>[http://www.harrassowitz-verlag.de/pcgi/a.cgi?ausgabe=index&T=1235007315045{haupt_harrassowitz=http://www.harrassowitz-verlag.de/title_3277.ahtml?T=1235007315045}]</ref>",

                "Begin Document\n" +
                "Open Tag: ref attributes: []\n" +
                "Begin Link: http://www.harrassowitz-verlag.de/pcgi/a.cgi?ausgabe=index&T=1235007315045{upt_harrassowitz=http://www.harrassowitz-verlag.de/title_3277.ahtml?T=1235007315045}\n" +
                "End Link: http://www.harrassowitz-verlag.de/pcgi/a.cgi?ausgabe=index&T=1235007315045{upt_harrassowitz=http://www.harrassowitz-verlag.de/title_3277.ahtml?T=1235007315045}\n" +
                "Close Tag: ref\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseComplete1() throws IOException, WikiTextParserException {
        verifyParsing("Page1");
    }

    @Test
    public void testParseComplete2() throws IOException, WikiTextParserException {
        verifyParsing("Page2");
    }

    @Test
    public void testParseComplete3() throws IOException, WikiTextParserException {
        verifyParsing("Page3");
    }

    @Test
    public void testParseComplete4() throws IOException, WikiTextParserException {
        verifyParsing("Page4");
    }

    @Test
    public void testParseComplete5() throws IOException, WikiTextParserException {
        verifyParsing("Page5", false);
    }

    /**
     * Introduced so check parser looping in lists included in template params.
     *
     * @throws IOException
     * @throws WikiTextParserException
     */
    @Test
    public void testParseComplete6() throws IOException, WikiTextParserException {
        verifyParsing("Page6");
    }

    @Test
    public void testParseTable1() throws IOException, WikiTextParserException {
        verifyParsing("Table1");
    }

    @Test
    public void testParseTable2() throws IOException, WikiTextParserException {
        verifyParsing("Table2");
    }

    //TODO: TBI
    @Test(enabled = false)
    public void testParseTable3() throws IOException, WikiTextParserException {
        verifyParsing("Table3");
    }

    @Test
    public void testParseEntity() throws IOException, WikiTextParserException {
        parse(
                "&lt;[http://link]&gt;",

                "Begin Document\n" +
                "Entity: '<' (lt)\n" +
                "Begin Link: http://link\n" +
                "End Link: http://link\n" +
                "Entity: '>' (gt)\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseListInTemplateValue() throws IOException, WikiTextParserException {
        parse(
                "{{LSJ|*sminqeu/s|Σμινθεύς|shortref}}",

                "Begin Document\n" +
                "Begin Template: LSJ\n" +
                "Begin List\n" +
                "List Item: Unordered 1\n" +
                "Text: 'sminqeu/s'\n" +
                "End List\n" +
                "k: null\n" +
                "Text: 'Σμινθεύς'\n" +
                "k: null\n" +
                "Text: 'shortref'\n" +
                "End Template: LSJ\n" +
                "End Document\n"
        );
    }

    @Test
    public void testParseListAsLastTemplateValue() throws IOException, WikiTextParserException {
        parse(
                "{{lang|grc|*Ἀπέλjων}}",

                "Begin Document\n" +
                "Begin Template: lang\n" +
                "k: null\n" +
                "Text: 'grc'\n" +
                "Begin List\n" +
                "List Item: Unordered 1\n" +
                "Text: 'Ἀπέλjων'\n" +
                "End List\n" +
                "End Template: lang\n" +
                "End Document\n"
        );
    }

    private void parse(InputStreamReader reader, String expected, boolean validate)
    throws IOException, WikiTextParserException {
        final WikiTextHRDumperHandler handler = new WikiTextHRDumperHandler(validate);
        final WikiTextParser parser = new WikiTextParser(handler);
        final long begin = System.nanoTime();
        parser.parse( new URL("http://test/url"), new BufferedReader(reader) );
        final long end   = System.nanoTime();
        logger.debug(String.format("Parse time (ns): " + (end - begin) + " (ms): " + (end - begin) / (1000 * 1000)));
        Assert.assertEquals(handler.getContent(), expected);
        Assert.assertTrue(handler.isEventStackEmpty());
    }

    private void parse(String in, String expected, boolean validate) throws IOException, WikiTextParserException {
        final ByteArrayInputStream bais = new ByteArrayInputStream( in.getBytes() );
        parse( new InputStreamReader(bais), expected, validate );
    }

    private void parse(String in, String expected) throws IOException, WikiTextParserException {
        parse(in, expected, true);
    }

    private void verifyParsing(String page, boolean validate) throws IOException, WikiTextParserException {
        final InputStreamReader fullWikiPageReader = new InputStreamReader(
                this.getClass().getResourceAsStream(String.format("%s.wikitext", page))
        );
        parse(
                fullWikiPageReader,
                IOUtils.toString(this.getClass().getResourceAsStream(String.format("%s.out", page))),
                validate
        );
    }

    private void verifyParsing(String page) throws IOException, WikiTextParserException {
        verifyParsing(page, true);
    }

}
