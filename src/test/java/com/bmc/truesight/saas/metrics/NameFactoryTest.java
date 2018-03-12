package com.bmc.truesight.saas.metrics;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class NameFactoryTest {

    private String prefix = "prefix";
    private String name = "foo.bar.baz.widgets";

    private MetricExtension ext = MetricExtension.Counting.COUNT;

    @Test
    public void testPrefix() throws Exception {
        NameFactory nf = new NameFactory(prefix, ImmutableList.of());
        assertThat(nf.name(name), is(prefix + "." + name));
    }

    @Test
    public void testExtension() throws Exception {
        NameFactory nf = new NameFactory(prefix, ImmutableList.of());
        assertThat(nf.name(name, ext), is(prefix + "." + name + "." + ext.getName()));
    }

    @Test
    public void testMask() {
        String name2 = "other.mask.extra.widget";
        String notMasked = "middling.other.mask.extra.widget";
        String trailing = "trailing.delimiter.should.be.masked";

        NameFactory nf = new NameFactory(prefix, ImmutableList.of("foo.bar", "other.mask", "trailing.delimiter."));

        assertThat(nf.name(name, ext), is(prefix + "." + "baz.widgets" + "." + ext.getName()));
        assertThat(nf.name(name2, ext), is(prefix + "." + "extra.widget" + "." + ext.getName()));
        assertThat(nf.name(notMasked, ext), is(prefix + "." + notMasked + "." + ext.getName()));
        assertThat(nf.name(trailing, ext), is(prefix + "." + "should.be.masked" + "." + ext.getName()));
    }

    @Test
    public void testSource() {
        NameFactory nf = new NameFactory(prefix, ImmutableList.of(), "Vulcan");
        assertThat(nf.source().getClass().getSimpleName(), is("Optional"));
        assertThat(nf.source().get(), is("Vulcan"));

    }

    @Test
    public void testNoSource() {
        NameFactory nf = new NameFactory(prefix, ImmutableList.of());
        assertThat(nf.source().getClass().getSimpleName(), is("Optional"));
        assertEquals(nf.source().orElse(null), null);

    }
}