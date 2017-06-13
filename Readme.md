
# PatternPlus
This libary is meant to help simplify Java 1.8 Regular Expression Matching utilizing the
[Named Sequence Matching] (https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#groupname)
and inject the named groups into a provided object, either via Field, or Method, based on the named group.

see  
  [java.util.regex.Pattern]([https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html)  
  [java.util.regex.Matcher](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Matcher.html)  

## Why
Found was doing lots of regular expression parsing like below...and got tired of it, didn't find anything I liked
so wrote this...
    
    public class Obj {
        private String p1;
        public String p2;

        public void setP1(String str) { this.p1 = str; };
    }

    Pattern p = Pattern.compile("(?<p1>.*):(?<p2>.*));
    Matter m = p.match("value1:value2");
    Obj o = new Obj();
    if(m.matches()) {
      o.setP1(m.group("p1"));
      o.p2 = m.group("p2");
    }
    .....



## Examples

  Examples below show the different Matcher.InjectOption do in the Matcher.inject(Object o, InjectOption) method does.  The
  Matcher.inject(Object o, InjectOption) will return a Set<String> of all groups it did not find or failed to inject into the object on.
   
  i.e.

  Set<String> remainingGroups = m.inject(o, Matcher.InjectOption.PUBLIC_FIELD);  
  if(remainingGroups.size() == 0) {   
    System.out.println("All Groups found in regex, have been used and injected into Object o");  
  else if(remainingGroups.size() > 0) {  
   System.out.println("Some groups found in regex, were not found by the injector...");  
   //Program can still deal with this other group manually.  
  }  

Below are simple examples...See Unit test for more complex examples.

### Match.InjectOption.PUBLIC_FIELD
  inject() will just look for a field of String.class with the matching Name


    public class Obj {
       public String p1, p2;
    }

    Pattern p = Pattern.compile("(?<p1>.*):(?<p2>.*));
    Matter m = p.match("value1:value2");
    Obj o = new Obj();
    m.inject(o, Matcher.InjectOption.PUBLIC_FIELD);
    assertTrue(o.p1 == "value1");
    assertTrue(o.p2 == "value2");
     


### Matcher.InjectOption.POJO
  On this feature the inject() will prefix the group name with ''set'' and capitize the firtst character and look 
  for that method name...

    public class Obj {
       private String p1, p2;
       public void setP1(String str) { this.p1 = str; }
       public void setP2(String str) { this.p2 = str; }
       public String getP1() { return this.p1; }
       public String getP2() { return this.p2; }
    }

    Pattern p = Pattern.compile("(?<p1>.*):(?<p2>.*));
    Matter m = p.match("value1:value2");
    Obj o = new Obj();
    m.inject(o, Matcher.InjectOption.POJO);
    assertTrue(o.getP1() == "value1");
    assertTrue(o.getP2() == "value2");
 

### Matcher.InjectOption.METHOD
  This option the inject() treats every group as a method name expecting the method signature to be a single String.class,
pays not attention to the method returning object if there is one...

    public class Obj {
       private String p1, p2;
       public void method1(String str) { this.p1 = str; }
       public void method2(String str) { this.p2 = str; }
       public String getP1() { return this.p1; }
       public String getP2() { return this.p2; }
    }

    Pattern p = Pattern.compile("(?<method1>.*):(?<method2>.*));
    Matter m = p.match("value1:value2");
    Obj o = new Obj();
    m.inject(o, Matcher.InjectOption.METHOD);
    assertTrue(o.getP1() == "value1");
    assertTrue(o.getP2() == "value2");
