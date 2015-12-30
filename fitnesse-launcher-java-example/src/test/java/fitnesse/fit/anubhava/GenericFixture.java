package fitnesse.fit.anubhava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import bsh.Interpreter;
import fit.Fixture;
import fit.Parse;
import fit.TypeAdapter;

/**
 * Modified or written by Anubhava Srivastava for use with FitNesse.
 * Copyright (c) 2008 Anubhava Srivastava 
 * Released under the terms of the GNU General Public License version 2
 * 
 * This Generic class can be used to expose all the methods of a class 
 * using reflection APIs. 
 * Only condition is that all of the parameters to these methods must be
 * convertible from String using TypeAdapter.parse(String) API OR else a
 * class must provide public static Object parse(String) method to 
 * initialize itself.
 * An example of such a class is com.thoughtworks.selenium.Selenium  
 * Method names and parameter values are supplied at run time from FitNesse
 * front end (Wiki based web pages).  
 * 
 * @author Anubhava Srivastava
 * @see http://anubhava.wordpress.com/
 */
public class GenericFixture extends Fixture
{
  private static final String THIS = "this";
  private static final Pattern NULL = Pattern.compile("null(\\W|$)");
  private static final Pattern BLANK = Pattern.compile("blank(\\W|$)");
  private static final String ARRAY = "array:";
  private static final char EQ = '=';
  private static final char COMMENT = '#';
  private static boolean isDebug = false;
  private static boolean abortOnErr = false;
  private static Interpreter bshInerp = null;
  private static Map<String, Object> globals = new HashMap<String, Object>();
  private static Set<String> pageVars = new HashSet<String>();

  Object target = null;
  Class<?> targetClass = null;

  public GenericFixture()
  {
    super();
    this.target = null;
    this.targetClass = null;

    for (Iterator<Map.Entry<String, Object>> iter = globals.entrySet().iterator(); iter.hasNext();)
    {
      Map.Entry<String, Object> entry = iter.next();
      super.setSymbol(entry.getKey(), entry.getValue());
    }
  }

  public static Object getVar(String s){
    return Fixture.getSymbol(s);
  }

  public static void setVar(String symbol, String value) {
    Fixture.setSymbol(symbol, value);
  }

  public static Object bsh(String expression) throws Exception {
    getBeanShell();
    return bshInerp.eval(expression);
  }

  public static synchronized Object getBeanShell() throws Exception {
    if (bshInerp == null) 
      bshInerp = new Interpreter();

    for (Iterator<String> iterator = pageVars.iterator(); iterator.hasNext();)
    {
      String key = iterator.next();
      bshInerp.set(key, Fixture.getSymbol(key));
    }
    return bshInerp;
  }

  protected class RowEntry
  {
    List<Parse> cells = new ArrayList<Parse>();
    String methodName = null;
    String varName = null;
    String[] methodParameters = null;
    int startReturnVals = 0;
    String[] returnVals = null;
    boolean ignored = false;
    String changedSUT = null;

    public String toString()
    {
      StringBuffer sb = new StringBuffer();
      if (ignored)
        sb.append("//IGNORED");
      else {
        sb.append("Method Name: " + methodName);
        if (changedSUT != null)
          sb.append("\nChanged SUT to: " + changedSUT);
        if (varName != null)
          sb.append("\nVariable Name: " + varName);
        sb.append("\nMethod Parameters: [");
        int i;
        for (i=0; i < methodParameters.length; i++)
        {
          sb.append(methodParameters[i] != null ? methodParameters[i].toString() : "null");
          if (i < methodParameters.length-1)
            sb.append(", ");
        }
        sb.append("]\nStart Return Col: " + startReturnVals);
        sb.append("\nExpected Return Value: [");         
        for (i=0; i < returnVals.length; i++)
        {
          sb.append(returnVals[i]);
          if (i < returnVals.length-1)
            sb.append(", ");
        }
        sb.append("]");
      }
      return sb.toString();
    }

    public void changeSUT()
    {
      // check if it is var=.method expression, var=.method means 
      // calling method for the object represented by var.

      String text = new String(methodName);

      int eql = text.indexOf(EQ);
      if (eql > 0 && eql < (text.length()-1))
      {
        int eqldot = text.indexOf("=.", eql);
        if (eqldot >= 0)
        {
          this.methodName = text.substring(eqldot+2);

          if (eqldot == eql)
            this.changedSUT = camel(text.substring(0, eqldot));
          else
          {
            this.varName = camel(text.substring(0, eql));
            this.changedSUT = camel(text.substring(eql+1, eqldot));
          }
        }
        else
        {
          this.varName = camel(text.substring(0, eql));
          this.methodName = text.substring(eql+1);
        }
      }
    }

    public void exception(Throwable e)
    {
      Parse cell = cells.get(0);
      cell.addToBody("<hr/>" + label(e.toString()));
      cell.addToTag(" class=\"error\"");
    }

    public void right(int col)
    {
      GenericFixture.this.right(this.cells.get(col));
    }

    public void right()
    {
      this.right(this.startReturnVals);
    }

    public void wrong(int col, String actual)
    {
      GenericFixture.this.wrong(this.cells.get(col), actual);
    }

    public void wrong(Object o)
    {
      if (o == null)
        this.wrong("null");
      else
        this.wrong(o.toString());
    }

    public void wrong(String actual)
    {
      this.wrong(this.startReturnVals, actual);
    }

    public void addText(Object o)
    {
      if (o == null)
        this.addText("null");
      else
        this.addText(o.toString());
    }

    public void addText(String text)
    {
      if (this.startReturnVals == 0)
      {
        /*
            //add a new cell
            Parse cell = new Parse("td", text, null, null);
            this.cells.get(this.cells.size()-1).more = cell;
            this.cells.add(cell);
         */
        // user doesn't want return value
      }
      else
        this.addText(this.cells.size()-1, text);
    }

    public void addText(int cellNum, String text)
    {
      this.cells.get(cellNum).addToBody(text);
    }
  }

  public static String[] exec(String command) {
    List<String> output = new ArrayList<String>();
    try {
      Runtime runtime = Runtime.getRuntime();
      Process proc = runtime.exec(command);

      // put a BufferedReader on the shell output
      InputStream inputstream = proc.getInputStream();
      InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
      BufferedReader bufferedreader = new BufferedReader(inputstreamreader);

      // read the command output
      String line;
      while ((line = bufferedreader.readLine()) != null) {
        output.add(line);
      }
    }
    catch (IOException e) {
      System.err.println(e);
    }

    return ( (String[] ) output.toArray(new String[0]) );
  }

  public static Object arrayElement(String key, int n) throws Exception
  {
    Object val = Fixture.getSymbol(key);
    if (val == null)
      throw new Exception ("Symbol: " + key + " not found in symbol table");
    if(!val.getClass().isArray())
      throw new Exception ("Symbol: " + key + " does not contain an ARRAY value");
    return Array.get(val, n);
  }

  public static boolean arrayHasElement(Object[] arr, Object elem) throws Exception
  {
    return java.util.Arrays.binarySearch(arr, elem) > 0 ? true : false;
  }

  String[] translateSymbols(String[] strArray)
  {
    String[] retArray = new String[strArray.length];
    for (int i=0; i<strArray.length; i++)
      retArray[i] = translateSymbols(strArray[i]);
    return retArray;
  }

  String translateSymbols(String text)
  {
    text = BLANK.matcher(text).replaceAll("$1");

    if (text.equals("null"))
      text = null;
    //else if ( text.contains("null"))
      //text = NULL.matcher(text).replaceAll(null + "$1");
      
    return text;
  }

  protected RowEntry readRow(Parse row)
  {
    List<String> methodParameters = new ArrayList<String>();
    List<String> returnVals = new ArrayList<String>();

    RowEntry re = new RowEntry();

    boolean endOfMethod = false;
    int c=0;
    for (Parse cells = row.parts; cells != null; c++, cells=cells.more)
    {
      String text = Parse.unescape(Parse.unformat(cells.body).trim());

      //debug("Adding cell: " + cells);
      re.cells.add(cells);

      if (c==0)
      {
        if (text.length() == 0 || text.charAt(0) == COMMENT)
        {
          re.ignored = true;
          break;
        }

        re.methodName = text;
        re.changeSUT();
      }
      else
      {
        // check it this is blank end-of-method cell
        if (!endOfMethod && text.trim().equals(""))
        {
          endOfMethod = true;
          re.startReturnVals = c+1;
          continue;
        }

        if (!endOfMethod)
          methodParameters.add(text);
        else
        {
          // is it a static class.field?
          Object ret = getStaticField(text);
          if (ret != null)
            text = ret.toString();

          returnVals.add(text);
        }
      }
    }

    if (re.ignored == false)
    {
      DSLAdapter.CommandRow cr = 
        DSLAdapter.findMatch(re.methodName, methodParameters.toArray(new String[0]));
      if (cr != null)
      {
        re.methodName = cr.getMethodName();
        re.changeSUT();
        methodParameters = cr.getParameters();
        debug("Found matching command: " + re.methodName + " "
            + methodParameters);
      }
      else
        re.methodName = super.camel(re.methodName);

      re.methodParameters = methodParameters.toArray(new String[0]);
      re.returnVals = returnVals.toArray(new String[0]);
    }

    re.methodParameters = translateSymbols(re.methodParameters);
    re.returnVals = translateSymbols(re.returnVals);

    debug(re);
    return re;
  }

  public Object getStaticField(String fieldName)
  {
    Object ret = null;

    if (fieldName == null || fieldName.length() == 0)
      return ret;

    // is it a static class.field?
    int dot = fieldName.lastIndexOf('.');
    if (dot > 0)
    {
      try
      {
        //debug("Checking class name: " + fieldName.substring(0, dot));
        Class<?> fieldType = Class.forName(fieldName.substring(0, dot));
        if (!fieldName.substring(dot+1).equals("class"))
        {
          Field f = fieldType.getField(fieldName.substring(dot+1));
          //debug("Got static field: " + f);
          f.setAccessible(true);
          ret = f.get(null);
        }
        else
          ret = fieldType;
      }
      catch (Exception e)
      {
        // do nothing
      }
    }
    return ret;
  }

  public static Object fetchVariable(String var)
  {
    if ( var == null )
      return null;
    String text = new String( var );
    Object v = null;
    // variable name must not contain dot
    if (text != null && text.length() > 1
        && text.charAt(text.length()-1) == EQ
        && text.indexOf('.') == -1)
      v = Fixture.getSymbol(text.substring(0, text.length()-1));
    return v;
  }

  public static boolean hasEqualsMethod(Class<?> type)
  {
    boolean isDefined = false;
    try
    {
      if (type != null && !type.equals(Object.class))
      {
        Method m = type.getDeclaredMethod("equals",
            new Class[] {Object.class});
        if (m.getReturnType().equals(boolean.class))
          isDefined = true;
        else
          isDefined = false;
      }
    } catch (NoSuchMethodException e)
    {
      isDefined = false;
    }
    return isDefined;
  }

  public static boolean callEqualsMethod(Class<?> type, Object o1, Object o2)
  throws Exception
  {
    boolean result = false;

    TypeAdapter ta = TypeAdapter.adapterFor(type);
    if (ta != null &&
        !ta.getClass().equals(fit.TypeAdapter.class))
    {
      result = ta.equals(o1, ta.parse(o2.toString()));
    }
    else
    {
      Method m = type.getMethod("equals", 
          new Class[] {Object.class});
      Object ret = m.invoke(o1, new Object[] {o2});
      if (ret.getClass().equals(Boolean.class))
        result = ((Boolean) ret).booleanValue();
    }
    return result;
  }

  private Object parseParameter(String parameter, Class<?> paramType)
  throws Exception
  {
    Object paramObject = null;
    Object v = fetchVariable(parameter);
    if (v != null)
    {
      // its a variable name
      paramObject = v;
    }
    else
    {
      // is it a static class.field?
      Object ret = getStaticField(parameter);
      if (ret != null)
        return ret;

      TypeAdapter ta = TypeAdapter.adapterFor(paramType);
      //debug(paramType + " Parameter: [" + parameter + "] " + ta.getClass());
      if (ta != null &&
          !ta.getClass().equals(fit.TypeAdapter.class)
      )
      {
        //debug("Inside Parameter: [" + parameter + "] " + ta.getClass());
        try
        {
          if (paramType.isArray() && parameter.startsWith(ARRAY))
          {
            ta.init(this, paramType);
            Class<?> componentType = paramType.getComponentType();
            TypeAdapter cta = TypeAdapter.on(this, componentType);
            //debug(cta + " - componentType: " + componentType.getCanonicalName());

            String[] arrStr = parameter.substring(ARRAY.length()).split(",");
            paramObject = Array.newInstance(componentType, arrStr.length);
            for (int i=0; i < arrStr.length; i++) {
              String token = arrStr[i].trim();
              Object var = fetchVariable(token);
              if (var == null) {
                var = cta.parse(token);
                //debug(var + " - i: " + i + " - " + token);
              }
              Array.set(paramObject, i, var);
            }
          }
          else
            paramObject = ta.parse(parameter);
        }
        catch (NumberFormatException ne)
        {
          throw new NullPointerException(ne.toString());
        }
        catch (NullPointerException npe)
        {
          throw npe;
        }
      }
      else
      {
        // handle user defined objects as the method arguments                       
        // user defined objects MUST have a method defined as
        // public static Object parse(String str) { ... }
        if (super.hasParseMethod(paramType))
          paramObject = super.parse(parameter, paramType);
        else if (parameter.startsWith(ARRAY))
          paramObject = ta.parse(parameter.substring(ARRAY.length()));
        else
          paramObject = parameter;
      }
    }
    return paramObject;
  }

  /*
   * Here we try to lookup a class from a given name and
   * create an instance from given parameters.
   * Child class can use this method to set an
   * object on which reflection will invoke all methods
   */
  protected Object getTarget() throws Exception
  {
    String[] args = getArgs();
    if (targetClass == null && args != null && args.length > 0)
    {
      String className = super.camel(args[0]);
      int eql = -1;
      String varName = null;

      Object v = fetchVariable(className);
      if (v != null)
      {
        // its a variable name
        this.target = v;
        this.targetClass = this.target.getClass();
        return this.target;
      }
      else
      {
        // check if we want to store SUT in a variable
        eql = className.lastIndexOf(EQ);
        if (eql > 0)
        {
          varName = className.substring(0, eql);
          className = className.substring(eql+1);
        }
      }

      this.targetClass = Class.forName(className);
      Constructor<?>[] constructors = this.targetClass.getConstructors();
      int numParams = args.length-1;

      for (Constructor<?> constructor: constructors)
      {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        if (numParams != parameterTypes.length)
          continue;

        //debug("trying to construct " + constructor);
        Object[] methodParameters = new Object[numParams];
        boolean nextConst = false;
        for (int p=0; p < numParams; p++)
        {
          try
          {
            methodParameters[p] = 
              parseParameter(args[p+1], parameterTypes[p]);
          }
          catch (NullPointerException npe)
          {
            nextConst = true;
            break;
          }
        }

        // try to invoke the next constructor
        if (nextConst)
          continue;

        try
        {
          this.target = constructor.newInstance(methodParameters);
          debug("*** Constructed " + constructor);
          debug("--------------------------------------------");
          break;
        }
        catch (Exception e)
        {
          if (e instanceof InvocationTargetException ||
              e instanceof IllegalAccessException)
            throw e;
        }
        finally
        {
          if (eql > 0)
            storeVariable(varName, this.target);
        }
      }
    }
    return this.target;
  }

  /*
   * overriding doRows() method of Fixture class
   */
  @Override
  public void doRows(Parse rows)
  {
    //debug("Rows: " + new Table(rows).toString());
    RowEntry re = null;
    try
    {
      this.target = this.getTarget();

      if (this.target != null && this.targetClass == null)
        this.targetClass = this.target.getClass();

      //still null?
      if (this.targetClass == null)
        this.targetClass = this.getClass();
      //throw new NullPointerException("Target class is null, cannot continue");

      for ( int r=0; rows != null; r++, rows=rows.more )
      {
        re = readRow(rows);
        //debug("Rows: " + new Table(rows).toString());
        int numParams = re.methodParameters.length;

        if (re.ignored)
        {
          super.counts.ignores++;
          continue;
        }
        else if (re.methodName == null || re.methodName.length() == 0)
        {
          re.wrong(0, "Invalid Method Name");
          continue;
        }
        else if (re.varName != null && re.changedSUT != null &&
            re.methodName.equals(THIS))
        {
          storeVariable(re.varName, this.target);
          continue;
        }

        Object mTarget = null;
        Class<?> mTargetClass = null;
        int dot = 0;
        // is it a static class.method call?
        if (re.changedSUT == null)
          dot = re.methodName.lastIndexOf('.');
        if (dot > 0)
        {
          mTarget = null;
          String[] arr = re.methodName.split("\\.");
          mTargetClass = Class.forName(re.methodName.substring(0, dot));
          re.methodName = arr[arr.length-1];
        }
        else if (re.changedSUT != null)
        {
          // SUT has itself has changed
          Object o = Fixture.getSymbol(re.changedSUT);
          if (o == null)
          {
            re.exception(new Exception("Variable not found: " + re.changedSUT));
            continue;
          }
          mTarget = o;
          mTargetClass = o.getClass();
        }
        else
        {
          mTarget = this.target;
          mTargetClass = this.targetClass;
        }

        // get all the methods for a given class
        Method[] methods = mTargetClass.getMethods();

        boolean matched = false;

        // find a matching method by name
        for (Method method: methods)
        {
          if ( method.getName().equals(re.methodName) )
          {
            Class<?>[] parameterTypes = method.getParameterTypes();
            //debug(numParams + " Verifying " + method + " " + parameterTypes.length);
            if (numParams != parameterTypes.length)
              continue;

            Object[] methodParameters = new Object[numParams];
            boolean nextMethod = false;
            for (int p=0; p < numParams; p++)
            {
              try
              {
                if (re.methodParameters[p] == null)
                  methodParameters[p] = null;
                else
                  methodParameters[p] = 
                    parseParameter(re.methodParameters[p],
                        parameterTypes[p]);
              }
              catch (NullPointerException npe)
              {
                //debug("Got npe: " + npe);
                nextMethod = true;
                break;
              }
            }

            // try to get the next method
            if (nextMethod)
              continue;

            try
            {
              debug("Invoking " + method);
              method.setAccessible(true);
              Object returnVal = method.invoke(mTarget, methodParameters);
              Class<?> returnType = method.getReturnType();
              handleResult(re, returnVal, returnType);
              matched = true;
              break;
            }
            catch (Exception e)
            {
              if (e instanceof InvocationTargetException ||
                  e instanceof IllegalAccessException ||
                  e instanceof NullPointerException)
                throw e;
              else if (!(e instanceof IllegalArgumentException))
                doException(e);
            }
          }
        }

        debug("--------------------------------------------");
        if (!matched)
        {
          String err = "Method [" + mTargetClass.getCanonicalName() + "." +
          re.methodName + "()] could not be found matching given parameters";
          if (mTarget == null)
            err = "Static " + err;
          throw new NoSuchMethodException(err);
        }
      }
    }
    catch (Exception e)
    {
      RowEntry r = re;
      if (r == null)
        r = readRow(rows);
      doException(r, e);
    }
  }

  /*
   * result handler
   * child class may override this method to have their own result handling
   */
  protected void handleResult(RowEntry re, Object actual, Class<?> returnType)
  throws Exception
  {
    String returnTypeName = returnType.getName();

    debug("Actual Return Type: " + returnTypeName);
    debug("Actual Return Value: [" + actual + ']');

    if (returnTypeName.equals("void"))
      return;

    if (re.varName != null)
    {
      if (!re.varName.equals(THIS))
        storeVariable(re.varName, actual);
      else if (this.target != null && this.targetClass.equals(returnType))
        this.target = actual;
    }

    TypeAdapter ta = TypeAdapter.adapterFor(returnType);
    ta.init(this, returnType);

    if (returnType.isArray())
    {
      if (re.startReturnVals > 0 && re.returnVals.length > 0)
      {            
        if (actual == null && re.returnVals[0] == null)
          re.right();
        else
        {
          Object expected = null;

          Object v = fetchVariable(re.returnVals[0]);
          if (v != null)
          {
            // its a variable name
            expected = v;
            re.addText(re.startReturnVals, ta.toString(expected));
          }
          else
          {
            // need to parse comma delimited string into array
            expected = ta.parse(re.returnVals[0]);
          }

          if (ta.equals(expected, actual))
            re.right();
          else
            re.wrong(ta.toString(actual));
        }
      }
      else
        re.addText(ta.toString(actual));
    }
    else
    {
      if (re.startReturnVals > 0 && re.returnVals.length > 0)
      {
        Object expected = re.returnVals[0];

        if (actual == null && expected == null)
          re.right();
        else
        {
          Object v = fetchVariable(re.returnVals[0]);
          if (v != null)
          {
            // its a variable name
            expected = v;
            re.addText(re.startReturnVals, expected.toString());
          }

          boolean result = false;
          // is equals method defined for this object
          if (hasEqualsMethod(returnType))
            result = callEqualsMethod(returnType, actual, expected);
          else if (expected != null && actual != null)
          {
            // no try using string equals method 
            result = expected.toString().equals(actual.toString());
          }

          if (result)
            re.right();
          else
            re.wrong(actual);
        }
      }
      else if (re.startReturnVals > 0)
        re.addText(actual);
    }
  }

  public static void setDebug(boolean d)
  {
    isDebug = d;
  }

  public static void abortOnError(boolean a)
  {
    abortOnErr = a;
  }

  public static void setGlobalVar(String v)
  {
    globals.put(v, null);
  }

  public static void clearGlobalVars()
  {
    globals.clear();
  }

  public static void storeVariable(String v, Object o)
  {
    try {
      if (bshInerp != null)
        bshInerp.set(v, o);
    } catch (Exception e) {}

    Fixture.setSymbol(v, o);

    pageVars.add(v);

    if (globals.containsKey(v))
      globals.put(v, o);
  }

  /*
   * child class can put their code here
   * to log or print debug messages
   */
  public void debug(Object obj)
  {
    if (isDebug)
      System.err.println(obj);
  }

  /*
   * child class can put their code here
   * to do cleanup and special exception handling
   */
  public void doException(Throwable e)
  {
    System.err.println("Exception: " + e);
    if (isDebug)
      e.printStackTrace();
    if (abortOnErr)
      super.setForcedAbort(abortOnErr);
  }

  public void doException(RowEntry r, Exception e)
  {
    doException(e);
    Throwable ex = e;
    if (e instanceof InvocationTargetException)
      ex = ((InvocationTargetException ) e).getTargetException();
    if (r != null)
      r.exception(ex);
    super.counts.exceptions++;
    debug("--------------------------------------------");
  }
}
