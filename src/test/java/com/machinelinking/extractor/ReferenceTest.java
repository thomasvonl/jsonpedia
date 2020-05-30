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

package com.machinelinking.extractor;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Michele Mostarda (me@michelemostarda.it)
 */
public class ReferenceTest {

    @Test
    public void testIsImage() {
        Assert.assertTrue(Reference.isImage("en:File:test.png"));
        Assert.assertTrue(Reference.isImage("en:File:test.jpg"));
        Assert.assertTrue(Reference.isImage("en:Image:1919_eclipse_positive.jpg"));
        Assert.assertFalse(Reference.isImage("en:File:test.txt"));
        Assert.assertFalse(Reference.isImage(":United_Kingdom"));
    }

    @Test
    public void testGetURLDeclaredLang() throws MalformedURLException {
        Assert.assertEquals(Reference.getURLDeclaredLang(new URL("http://en.wikipedia.org/")), "en");
        Assert.assertEquals(Reference.getURLDeclaredLang(new URL("http://it.wikipedia.org/")), "it");
    }

    @Test
    public void testIsLangPrefix() throws MalformedURLException {
        Assert.assertEquals(Reference.isLangPrefix("en"), true);
        Assert.assertEquals(Reference.isLangPrefix("lmo"), true);
        Assert.assertEquals(Reference.isLangPrefix("fiu-vro"), true);
        Assert.assertEquals(Reference.isLangPrefix("zh-min-nan"), true);
        Assert.assertEquals(Reference.isLangPrefix("Time"), false);
        Assert.assertEquals(Reference.isLangPrefix("Zed"), false);
        Assert.assertEquals(Reference.isLangPrefix("fiuvro"), false);
        Assert.assertEquals(Reference.isLangPrefix("zhminnan"), false);
    }


    @Test
    public void testImageResourceToURL() {
        Assert.assertEquals(
                Reference.imageResourceToURL("File:Albert Einstein at the age of three (1882).jpg"),
                "http://upload.wikimedia.org/wikipedia/commons/thumb/f/fb/Albert_Einstein_at_the_age_of_three_%281882%29.jpg/110px-Albert_Einstein_at_the_age_of_three_%281882%29.jpg"
        );
        Assert.assertEquals(
                Reference.imageResourceToURL("en:Image:1919_eclipse_positive.jpg"),
                "http://upload.wikimedia.org/wikipedia/commons/thumb/b/bb/age%3A1919_eclipse_positive.jpg/110px-age%3A1919_eclipse_positive.jpg"
        );
    }

    @Test
    public void testResourceToURL() throws MalformedURLException {
        Assert.assertEquals(
                Reference.labelToURL(new URL("http://en.wikipedia.org/"), "Time 100: The Most Important People of the Century").toString(),
                "http://en.wikipedia.org/wiki/Time_100:_The_Most_Important_People_of_the_Century"
                );
        Assert.assertEquals(
                Reference.labelToURL(new URL("http://en.wikipedia.org/"), "lmo:Albert Einstein").toString(),
                "http://lmo.wikipedia.org/wiki/Albert_Einstein"
        );
        Assert.assertEquals(
                Reference.labelToURL(new URL("http://en.wikipedia.org/"), "be-x-old:Лёндан").toString(),
                "http://be-x-old.wikipedia.org/wiki/Лёндан"
        );
        Assert.assertEquals(
                Reference.labelToURL(new URL("http://en.wikipedia.org/"), ":United_Kingdom").toString(),
                "http://en.wikipedia.org/wiki/United_Kingdom"
        );
    }

    @Test
    public void testURLtoLabel() {
        Assert.assertEquals(Reference.urlToLabel("http://en.wikipedia.org/wiki/Page"), new String[]{"en", "Page"});
        Assert.assertEquals(Reference.urlToLabel("http://it.wikipedia.org/wiki/Page"), new String[]{"it", "Page"});
    }

}
