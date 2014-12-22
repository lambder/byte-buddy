package net.bytebuddy.matcher;

import net.bytebuddy.instrumentation.DeclaredBy;
import net.bytebuddy.instrumentation.type.TypeDescription;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

public class DeclaringTypeMatcherTest extends AbstractElementMatcherTest<DeclaringTypeMatcher<?>> {

    @Mock
    private ElementMatcher<? super TypeDescription> typeMatcher;

    @Mock
    private DeclaredBy declaredBy;

    @Mock
    private TypeDescription typeDescription;

    @SuppressWarnings("unchecked")
    public DeclaringTypeMatcherTest() {
        super((Class<DeclaringTypeMatcher<?>>) (Object) DeclaringTypeMatcher.class, "declaredBy");
    }

    @Test
    public void testMatch() throws Exception {
        when(declaredBy.getDeclaringElement()).thenReturn(typeDescription);
        when(typeMatcher.matches(typeDescription)).thenReturn(true);
        assertThat(new DeclaringTypeMatcher<DeclaredBy>(typeMatcher).matches(declaredBy), is(true));
        verify(typeMatcher).matches(typeDescription);
        verifyNoMoreInteractions(typeMatcher);
        verify(declaredBy).getDeclaringElement();
        verifyNoMoreInteractions(declaredBy);
    }

    @Test
    public void testNoMatch() throws Exception {
        when(declaredBy.getDeclaringElement()).thenReturn(typeDescription);
        when(typeMatcher.matches(typeDescription)).thenReturn(false);
        assertThat(new DeclaringTypeMatcher<DeclaredBy>(typeMatcher).matches(declaredBy), is(false));
        verify(typeMatcher).matches(typeDescription);
        verifyNoMoreInteractions(typeMatcher);
        verify(declaredBy).getDeclaringElement();
        verifyNoMoreInteractions(declaredBy);
    }

    @Test
    public void testNoMatchWhenNull() throws Exception {
        assertThat(new DeclaringTypeMatcher<DeclaredBy>(typeMatcher).matches(declaredBy), is(false));
        verifyZeroInteractions(typeMatcher);
        verify(declaredBy).getDeclaringElement();
        verifyNoMoreInteractions(declaredBy);
    }
}
