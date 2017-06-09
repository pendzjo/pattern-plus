package com.consultwithcase.patternplus;

import java.util.Set;
import static junit.framework.TestCase.*;
import org.junit.Test;

/**
 *
 * @author johnnp
 */
public class PatternTest {

    private static String TEST_STRING = "Field(Field1:field1,Field2:field2,Field3:field3) " +
            "Pojo(Pojo1:pojo1,Pojo2:pojo2,Pojo3:pojo3)" +
            "Method((Method1:method 1,Method2:method 2,Method3:method 3)";
    
    /**
     * Checking to make sure regular expression pattern matching
     * still works...
     */
    @Test
    public void basicPatternCheck() {
        String regex = "This(?<p1>.*)A(?<p2>.*)";
        String line = "ThisIsATest";
        
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(line);
        
        assertTrue("Matcher didn't match the pattern", m.matches());
        assertEquals("Is",m.group("p1"));
        assertEquals("Test", m.group("p2"));
    
        String failLine = "ThisSuper";
        m = pattern.matcher(failLine);
        assertFalse("Matcher should have mattched this", m.matches());
        
        
    }

    public static class ExampleObject {
        public String p1;
        public String p2;
        
        public void setP1(String str) {
            this.p1 = "Method 1 " + str;
        }
        
        public void setP2(String str) {
            this.p2 = "Method 2 " + str;
        }
     }
    
    public static class ExampleExtendObject extends ExampleObject {
        public String p3;
        
        public void setP3(String str) {
            this.p3 = "EE - Method 3 " + str;
        }
        
        public void setP2(String str) {
            this.p2 = "EE - Method 2 " + str;
        }
        
    }

    @Test
    public void basicInjectTestPublicFields() {
        String line = "This [Is A Test] Of [Basic Inject] It Worked";
        String regex = "This \\[(?<p1>.*)\\] Of \\[(?<p2>.*)\\] It Worked";
        
        Pattern pattern = Pattern.compile(regex);
        Matcher m = pattern.matcher(line);
        
        assertTrue("Matcher didn't match the pattern, bad", m.matches());
        assertEquals("Is A Test",m.group("p1"));
        assertEquals("Basic Inject", m.group("p2"));
        
        
        ExampleExtendObject exampleObject = new ExampleExtendObject();
        pattern = Pattern.compile(regex);
        m = pattern.matcher(line);
        m.inject(exampleObject, Matcher.InjectOption.PUBLIC_FIELD);
        assertEquals("Is A Test", exampleObject.p1);
        assertEquals("Basic Inject", exampleObject.p2);
        
        
        exampleObject = new ExampleExtendObject();
        pattern = Pattern.compile(regex);
        m = pattern.matcher(line);
        m.inject(exampleObject, Matcher.InjectOption.POJO);
        assertEquals("Method 1 Is A Test", exampleObject.p1);
        assertEquals("EE - Method 2 Basic Inject", exampleObject.p2);
    }
    
    @Test
    public void basicMethodTest() {
        String methodRegex = ".*\\(Method1:(?<setP1>.*),Method2:(?<setP2>.*),Method3:(?<setP3>.*)\\).*";
        
        Pattern pattern = Pattern.compile(methodRegex);
        Matcher matcher = pattern.matcher(TEST_STRING);
        
        ExampleObject exampleObject = new ExampleExtendObject();
        Set<String> missedGroups = matcher.inject(exampleObject, Matcher.InjectOption.METHOD_NAME);
        assertTrue(missedGroups.isEmpty());
        assertEquals("Method 1 method 1", exampleObject.p1);
        assertEquals("EE - Method 2 method 2", exampleObject.p2);
        
        assertEquals("EE - Method 3 method 3", ((ExampleExtendObject)exampleObject).p3);
        
    }
    
    
    public static abstract class AbstractExampleObject {
        private String field1;
        
        public abstract void setter(String str);
        
        public void setField1(String str) {
            this.field1 = str;
        }

    }
    
    public static class ImplementAbstractExampleObject extends AbstractExampleObject {
        
        private String field2;
        
        @Override
        public void setter(String str) {
            this.field2 = str;
        }
        
        public String getField2() {
            return this.field2;
        }
        
    }
    
    
    @Test
    public void testAbstractAndUnusedGroups() {
        String methodRegex = ".*\\(Method1:(?<field2>.*),Method2:(?<setter>.*),Method3:(?<setP3>.*)\\).*";
    
        Pattern p = Pattern.compile(methodRegex);
        Matcher matcher = p.matcher(TEST_STRING);
        ImplementAbstractExampleObject o = new ImplementAbstractExampleObject();
        Set<String> remainingGroups = matcher.inject(o, Matcher.InjectOption.METHOD_NAME);
        
        assertTrue(remainingGroups.contains("field2") && remainingGroups.contains("setP3") && remainingGroups.size() == 2);
        assertEquals("method 2", o.getField2());
        assertEquals("method 3",matcher.group("setP3"));
        assertEquals("method 1", matcher.group("field2"));
        
    }
    
    
}
