/**************************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur. All rights reserved.                 *
 * http://aspectwerkz.codehaus.org                                                    *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the QPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package aspectwerkz.aosd.web;

import aspectwerkz.aosd.context.UserContext;
import aspectwerkz.aosd.context.Context;
import aspectwerkz.aosd.context.ContextException;
import aspectwerkz.aosd.addressbook.AddressBook;
import aspectwerkz.aosd.addressbook.Contact;
import aspectwerkz.aosd.ServiceManager;
import aspectwerkz.aosd.security.principal.PrincipalStore;
import aspectwerkz.aosd.security.principal.SimplePrincipal;
import aspectwerkz.aosd.persistence.jisp.JispPersistenceManager;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/**
 * Demo app
 *
 * Usefull url:
 *
 * http://localhost:7014/aosd/demo
 * http://localhost:7014/aosd/demo?action=LOGIN&username=alex&pwd=alex
 * http://localhost:7014/aosd/demo?action=LIST
 * http://localhost:7014/aosd/demo?action=ADD&fn=jonas&ln=boner&em=jonas_email
 * http://localhost:7014/aosd/demo?action=ADD&fn=billy&ln=boy&em=billy_email
 * http://localhost:7014/aosd/demo?action=REMOVE&ids=billy.boy&ids=jonas.boner
 *
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
 */
public class HomeServlet extends HttpServlet {

    private final static String ACTION_LOGIN = "LOGIN";
    private final static String ACTION_ADD = "ADD";
    private final static String ACTION_LIST = "LIST";
    private final static String ACTION_REMOVE = "REMOVE";
    private final static String ACTION_HOME = "HOME";
    private final static String ACTION_LOGOUT = "LOGOUT";

    private final static Map ACTION_VIEWS = new HashMap();
    static {
        ACTION_VIEWS.put(ACTION_LOGIN, "index.html");
        ACTION_VIEWS.put(ACTION_ADD, "add.jsp");
        ACTION_VIEWS.put(ACTION_LIST, "list.jsp");
        ACTION_VIEWS.put(ACTION_HOME, "home.jsp");
    }

    private final static String KEY_ACTION = "action";
    private final static String KEY_USERNAME = "username";
    private final static String KEY_PASSWORD = "pwd";
    private final static String KEY_FIRSTNAME = "fn";
    private final static String KEY_LASTNAME = "ln";
    private final static String KEY_EMAIL = "em";
    private final static String KEY_IDS = "ids";

    private final static String SESSION_USER = "user";

    private final static String VIEW_ADRESSBOOK = "adb";
    private final static String VIEW_ADRESSBOOK_COUNT = "adb_count";
    private final static String VIEW_ERROR = "error";

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        String action = httpServletRequest.getParameter(KEY_ACTION);
        try  {
            //TODO AOP JAAS
            HttpSession session = httpServletRequest.getSession();
            if (session != null) {
                UserContext ctx = (UserContext) session.getAttribute(SESSION_USER);
                if (ctx != null) {
                    PrincipalStore.setContext(ctx);
                }
            }
            System.out.println(httpServletRequest.getSession().getAttribute(SESSION_USER));

            System.out.println("processing action " + action);

            String toView = ACTION_LOGIN;
            if (action == null) {
                toView = ACTION_LOGIN;//viewLogin(httpServletRequest, httpServletResponse);
            } else if (ACTION_LOGIN.equals(action)) {
                toView = actionLogin(httpServletRequest, httpServletResponse);
            } else if (ACTION_ADD.equals(action)) {
                toView = actionAdd(httpServletRequest, httpServletResponse);
            } else if (ACTION_LIST.equals(action)) {
                toView = actionList(httpServletRequest, httpServletResponse);
            } else if (ACTION_REMOVE.equals(action)) {
                toView = actionRemove(httpServletRequest, httpServletResponse);
            } else if (ACTION_LOGOUT.equals(action)) {
                toView = actionLogout(httpServletRequest, httpServletResponse);
            } else if (ACTION_HOME.equals(action)) {
                toView = actionHome(httpServletRequest, httpServletResponse);
            }

            String view = (String) ACTION_VIEWS.get(toView);
            viewTo(view, httpServletRequest, httpServletResponse);

        } catch (Exception e) {
            httpServletRequest.setAttribute(VIEW_ERROR, e.getMessage());
            e.printStackTrace();
            try {
                actionHome(httpServletRequest, httpServletResponse);
                viewTo((String)ACTION_VIEWS.get(ACTION_HOME), httpServletRequest, httpServletResponse);
            } catch (Exception e2) {
                viewTo((String)ACTION_VIEWS.get(ACTION_LOGIN), httpServletRequest, httpServletResponse);
            }
        } finally {
            //TODO JAAS specific here !
            PrincipalStore.setContext(null);
            PrincipalStore.setSubject(null);
        }
    }

    private void viewTo(String res, HttpServletRequest req, HttpServletResponse resp)
    {
        try {
            System.out.println("forward to " + res);
            if (res == null) {
                // error case: no view specified
                getServletContext().getRequestDispatcher("/aosd/index.html").forward(req, resp);
            } else if (res.toLowerCase().startsWith("http")) {
                // http redirect
                resp.sendRedirect(res);
            } else {
                // standard view
                getServletContext().getRequestDispatcher( "/" + res).forward(req, resp);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    private void viewLogin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
//    {
//        System.out.println("LOGIN, action=HOME");
//        viewTo("index.html", httpServletRequest, httpServletResponse);
//    }

    private String actionHome(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
    {
        String userKey = getUserKey(httpServletRequest.getSession());
        AddressBook addressBook = ServiceManager.getAddressBookManager().newAddressBook(userKey);
        httpServletRequest.setAttribute(VIEW_ADRESSBOOK_COUNT, (new Integer(addressBook.getContacts().size())).toString());

        return ACTION_HOME;
    }

//    private void viewList(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
//            throws Exception {
//        System.out.println("LIST, action=REMOVE");
//        AddressBook adb = (AddressBook)httpServletRequest.getAttribute(VIEW_ADRESSBOOK);
//        for (Iterator contacts = adb.getContacts().iterator(); contacts.hasNext();) {
//            System.out.println((Contact)contacts.next());
//        }
//        viewTo("list.jsp", httpServletRequest, httpServletResponse);
//    }

    private String actionLogin(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        String user = httpServletRequest.getParameter(KEY_USERNAME);
        String pwd = httpServletRequest.getParameter(KEY_PASSWORD);

        // feed user context info in session
        UserContext ctx = new UserContext();
        ctx.put(Context.PRINCIPAL, new SimplePrincipal(user));
        ctx.put(Context.CREDENTIAL, new SimplePrincipal(pwd));
        httpServletRequest.getSession(true).setAttribute(SESSION_USER, ctx);
        System.out.println("  session created " + httpServletRequest.getSession());

        // Authentication thru AOP
        PrincipalStore.setContext(ctx);//TODO in AOP
        // TODO note: security hole if not unset !

        // forward
        return actionHome(httpServletRequest, httpServletResponse);
    }

    private String actionLogout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        System.out.println("  session dropped " + httpServletRequest.getSession());
        httpServletRequest.getSession().invalidate();

        return ACTION_LOGIN;
    }

    private String actionList(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        String userKey = getUserKey(httpServletRequest.getSession());
        AddressBook addressBook = ServiceManager.getAddressBookManager().newAddressBook(userKey);

        httpServletRequest.setAttribute(VIEW_ADRESSBOOK, addressBook);

        return ACTION_LIST;
    }

    private String actionAdd(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        String userKey = getUserKey(httpServletRequest.getSession());
        AddressBook addressBook = ServiceManager.getAddressBookManager().newAddressBook(userKey);

        String firstName = httpServletRequest.getParameter(KEY_FIRSTNAME);
        String lastName = httpServletRequest.getParameter(KEY_LASTNAME);
        String email = httpServletRequest.getParameter(KEY_EMAIL);

        // add to address book
        ServiceManager.getAddressBookManager().addContact(addressBook, firstName, lastName, email);

        // forward
        return actionList(httpServletRequest, httpServletResponse);
    }

    private String actionRemove(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws Exception {
        String userKey = getUserKey(httpServletRequest.getSession());
        AddressBook addressBook = ServiceManager.getAddressBookManager().newAddressBook(userKey);

        // adb entries are firstName.lastName
        String[] ids = httpServletRequest.getParameterValues(KEY_IDS);
        Set contacts = new HashSet();
        for (int i = 0; i < ids.length; i++) {
            System.out.println("  " + ids[i]);
            Contact c = addressBook.findContact(ids[i]);
            contacts.add(c);
        }
        ServiceManager.getAddressBookManager().removeContacts(addressBook, contacts);

        // forward
        return actionList(httpServletRequest, httpServletResponse);
    }

    private String getUserKey(HttpSession session) {
        try  {
            //TODO the http session contains JAAS specific things !
            UserContext ctx = (UserContext)session.getAttribute(SESSION_USER);
            return ((SimplePrincipal)ctx.get(Context.PRINCIPAL)).getName()
                    +"."
                    +((SimplePrincipal)ctx.get(Context.CREDENTIAL)).getName();
        } catch (ContextException ce) {
            return null;
        }
    }


}
