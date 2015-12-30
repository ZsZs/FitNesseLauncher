package fitnesse.fit.anubhava;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit.Fixture;
import fit.Parse;
import fit.exception.FitFailureException;

/**
 * Modified or written by Anubhava Srivastava for use with FitNesse.
 * Copyright (c) 2008 Anubhava Srivastava 
 * Released under the terms of the GNU General Public License version 2
 * 
 * DSL (Domain Specific Language) Adapter is an optional add-on for 
 * Generic Fixture. DSLAdapter lets testers write their GenerixFixture
 * test tables in customer-centric language rather than in core Java. 
 * 
 * @author Anubhava Srivastava
 * @see http://anubhava.wordpress.com/
 */
public class DSLAdapter extends Fixture {
  public static final String cellDelim = "``";
  public static final char paramDelim = ',';
  public static Pattern backsp = Pattern.compile("\\\\");
  public static Pattern none = Pattern.compile("");
  public static Pattern d = Pattern.compile("\\\\?\\{(\\d+)\\}");

  public static class InvalidParamSequence extends Exception {
    private static final long serialVersionUID = 1L;
    public InvalidParamSequence(String mesg)
    {
      super(mesg);
    }
  }

  public static class CommandRow {
    protected String methodName = null;
    protected Pattern regexp = null;
    protected Matcher matcher = null;
    protected String paramSequence = null;

    public CommandRow(String expression, String methodName, String seq)
    {
      super();
      this.regexp = Pattern.compile(expression);
      this.methodName = methodName;
      this.paramSequence = seq;
    }

    @Override
    public String toString()
    {
      StringBuffer sb = new StringBuffer();
      sb.append("Method Name: " + methodName);
      List<String> params = getParameters();
      if (params != null && params.size() > 0)
        sb.append("\nMethod Parameters: " + getParameters());
      if (matcher != null)
        sb.append("\nMatcher: " + matcher.pattern());
      return sb.toString();
    }

    private String matchIt(String s)
    {
      return matchIt(s, false);
    }

    private String matchIt(String s, boolean isMethod)
    {
      //System.err.println("s is: " + s);
      Matcher m = d.matcher(s);

      StringBuffer sb = new StringBuffer();
      while (m.find()) {
        int start = m.start();
        if (s.charAt(start) == '\\')
          continue;
        else
        {
          int argNo = Integer.parseInt(m.group(1));
          if (argNo <= matcher.groupCount())
          {
            String str = matcher.group(argNo);
            //System.err.println("* group is: " + str);
            if (str.charAt(0) == '\\')
              sb.append('\\');
            else if (!isMethod) {
              Object v = GenericFixture.fetchVariable( str );
              // resolve variables only for String type
              if ( v != null && !v.getClass().isArray() 
                  && ( v.getClass().equals(String.class) || 
                    v.getClass().getGenericSuperclass().equals(String.class) )
                 )
                str = v.toString();
            }
            m.appendReplacement(sb, str);
            //System.err.println("* sb is: " + sb);
          }
        }

      }
      m.appendTail(sb);

      //strip all escape characters now
      if (s.charAt(0) != '\\')
        m = none.matcher(sb.toString());
      else
        m = backsp.matcher(sb.toString());
      String param = m.replaceAll("");
      //System.err.println("* param is: " + param);
      return  param ;
    }

    public String getMethodName() {
      //System.err.println("Finding match for methodName: " + methodName);
      String m = matchIt(methodName, true);
      return m;
    }

    public String getExpression() {
      return regexp.pattern();
    }

    public List<String> getParameters() {
      //split params on , ignoring \,
      List<String> parameters = new ArrayList<String>();
      if (paramSequence.length() > 0 && this.matcher != null)
      {
        ArrayList<String> items = new ArrayList<String>();
        int prev=0;
        // special case for bean shell
        if ( !methodName.equals("GenericFixture.bsh") )
        {
          for(int i=0; i<paramSequence.length(); i++)
          {
            if (paramSequence.charAt(i) == paramDelim &&
                (i==0 || paramSequence.charAt(i-1) != '\\'))
            {
              items.add(paramSequence.substring(prev, i).replace("\\,", ",").trim());
              prev=i+1;
            }
          }
        }
        //System.err.println("paramSequence: " + paramSequence + " - " + prev + " - " + paramSequence.substring(prev));
        items.add( paramSequence.substring(prev).replace("\\,", ",").trim() );
        //System.err.println("items: " + items);
        // now replace all $1, $2 etc
        for(String s: items)
        {
          parameters.add( matchIt(s) );
        }
      }
      return parameters;
    }

    public boolean matches(String record) {
      matcher = regexp.matcher(record);
      return matcher.matches();
    }
  }

  public void readRow(Parse row) throws InvalidParamSequence
  {
    int c=0;
    String key = null;
    List<String> values = new ArrayList<String>();
    for (Parse cells = row.parts; cells != null; c++, cells=cells.more)
    {
      String text = Parse.unescape(Parse.unformat(cells.body).trim());
      if (c==0)
      {
        key = text;
      }
      values.add(text.trim());
    }

    if (c<2)
      throw new InvalidParamSequence("Invalid DSL syntax: " + values);

    StringBuffer expression = new StringBuffer(key);
    for(c=1; c<values.size()-2; c++)
    {
      expression.append(cellDelim);
      String val = values.get(c);
      if (val.equals("%"))
        expression.append("(\\p{Print}+)");
      else
        expression.append(val);
    }
    this.add(key, expression.toString(), values.get(c), values.get(c+1));
  }

  /*
   * overriding doRows() method of Fixture class
   */
  @Override
  public void doRows(Parse rows) throws FitFailureException
  {
    try {
      for ( int r=0; rows != null; r++, rows=rows.more )
      {
        readRow(rows);
      }
      //System.out.println("Map is: " + commandMap);
    }
    catch (InvalidParamSequence ips)
    {
      throw new FitFailureException(ips.toString());
    }
  }

  private static Map<String, List<CommandRow>> commandMap = 
    new HashMap<String, List<CommandRow>>();

  public static CommandRow findMatch(String key, String... items) {
    StringBuffer record = new StringBuffer(items.length);
    record.append(key);
    for(String s: items) {
      record.append(cellDelim);
      record.append(s);
    }

    CommandRow cr = null;
    List<CommandRow> rows = commandMap.get(camel(key));
    if (rows != null)
    {
      for (CommandRow cRow: rows) {
        //System.err.println("Comparing [" + record + "] with [" +
        //cRow.getExpression() + "]");
        if (cRow.matches(record.toString()))
        {
          cr = cRow;
          //System.err.println("Matched: [" + cr + "]");
          break;
        }
      }
    }
    return cr;
  }

  public void add(String key, String value, String methodName, String seq)
  {
    key = camel(key);
    List<CommandRow> l = commandMap.get(key);
    if (l == null)
      commandMap.put(key, l=new ArrayList<CommandRow>());
    l.add(new CommandRow(value, methodName, seq));
  }

  public static void main(String[] args)
  throws InvalidParamSequence
  {
    DSLAdapter dp = new DSLAdapter();
    dp.add("user types", "user types``(\\p{Print}+)``into``(\\p{Print}+)``field",
        "type", "xpath={2},id={1},[{1}=\\{3}]");
    dp.add("user types", "user types``(\\p{Print}+)", "myMethod", "{1}");
    dp.add("user starts the app", "user starts the app", "start", "");

    CommandRow cr = DSLAdapter.findMatch("user types", "ilog_123", "into", "username", "field");
    if (cr != null)
      System.out.println("Found a matching command: " + cr.methodName + " "
          + cr.getParameters());
    else
      System.out.println("Did Not Find a matching command");

    cr = DSLAdapter.findMatch("user starts the app");
    if (cr != null)
      System.out.println("Found a matching command: " + cr.methodName + " "
          + cr.getParameters());
    else
      System.out.println("Did Not Find a matching command");
  }
}
