/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jaxygen.invoker.apibrowser;

import org.jaxygen.netservice.html.HTMAnchor;
import org.jaxygen.netservice.html.HTMLElement;
import org.jaxygen.netservice.html.HTMLLabel;
import org.jaxygen.netservice.html.HTMLTable;
import org.jaxygen.netservice.html.HTMLDiv;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import org.jaxygen.annotations.NetAPI;
import org.jaxygen.invoker.ClassRegistry;
import org.jaxygen.security.basic.annotations.UserProfile;
import org.jaxygen.url.UrlQuery;

/**
 *
 * @author artur
 */
public class ClassesSnippestPage extends Page {

  public static final String NAME = "ClassesSnippestPage";
  private final String browserPath;

  public ClassesSnippestPage(ServletContext context, HttpServletRequest request) throws ServletException {
    super(context);
    browserPath = request.getContextPath() + "/APIBrowser";
    append(renderClassesList());
  }

  /**
   * Render full list of classes
   *
   * @param securityProvider
   * @param classFilter
   * @param methodFilter
   * @param output
   * @return
   */
  private HTMLElement renderClassesList() {
    HTMLElement rc;
    if (registry != null) {
      HTMLTable table = new HTMLTable();
      table.getHeader().createColumns("className", "Description", "Methods");
      for (Class c : registry.getRegisteredClasses()) {
        HTMLTable.Row row = new HTMLTable.Row();
        UrlQuery showClassMethodsQuery = new UrlQuery();
        showClassMethodsQuery.add("page", ClassMethodsPage.NAME);
        showClassMethodsQuery.add("className", c.getCanonicalName());

        row.addColumn(new HTMAnchor("?" + showClassMethodsQuery, new HTMLLabel(c.getSimpleName())));
        if (c.isAnnotationPresent(NetAPI.class)) {
          NetAPI netApi = (NetAPI) c.getAnnotation(NetAPI.class);
          if (netApi != null && netApi.description() != null) {
            row.addColumn(new HTMLLabel(netApi.description()));
          }
        } else {
          row.addColumn(new HTMLLabel("DESCRIPTION MISSING"));
        }

        HTMLTable methodsTable = new HTMLTable();
        methodsTable.addRow().addColumns(renderMethodReferences(c));
        row.addColumn(methodsTable);

        table.addRow(row);
      }
      rc = table;
    } else {
      rc = new HTMLLabel("Please configure servicePath context-param in yout web.xml file. It must point to " + ClassRegistry.class.getCanonicalName() + " interface implementation");
    }

    return rc;
  }

  private HTMLElement[] renderMethodReferences(Class clazz) {
    List<HTMLElement> rows = new ArrayList<HTMLElement>();
    for (Method method : clazz.getMethods()) {
      NetAPI netApi = method.getAnnotation(NetAPI.class);
      if (netApi != null) {
        final String className = clazz.getCanonicalName();
        final String methodName = method.getName();
        boolean show = true;

        UrlQuery showMethodQuery = new UrlQuery();
        showMethodQuery.add("page", MethodInvokerPage.NAME);
        showMethodQuery.add("className", className);
        showMethodQuery.add("methodName", methodName);
        showMethodQuery.add("getForm", className + "." + methodName);
        if (show) {
          rows.add(new HTMAnchor(browserPath + "?" + showMethodQuery, new HTMLLabel(methodName)));
        }
      }
    }
    return rows.toArray(new HTMAnchor[rows.size()]);
  }
}
